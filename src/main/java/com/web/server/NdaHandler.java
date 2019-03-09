package com.web.server;

import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tnsoft.web.model.AuthResponse;
import com.tnsoft.web.model.OfflineDataPoint;
import com.tnsoft.web.model.RequestEntity;
import com.tnsoft.web.model.UploadResponse;
import com.tnsoft.web.service.HandlerService;
import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.netty.channel.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 数据处理，处理NdaDecoder解析出的数据
 *
 * @author z
 */
@Component("ndaHandler")
public class NdaHandler extends SimpleChannelUpstreamHandler {

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting()
            .create();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Resource
    private HandlerService handlerService;

    public NdaHandler() {
    }


    /**
     * 对转成json的报文格式进行调整
     *
     * @param result 调整前
     * @return 调整后
     */
    private static String formatData(String result) {
        if (StringUtils.isEmpty(result)) {
            return "";
        }
        result = result.replaceAll("\n", "");
        result = result.replaceAll(" ", "");
        result = result.replace("$", " ");
        result = result.replace("activate_status\":1", "activate_status\": 1");
        result = result.replace("status\":200", "status\": 200");
        result = result.replace("nonce\":", "nonce\": ");
        return result;
    }

    /**
     * 温度数据为负值时，会有两个负号（如-2.-34），需处理下变为-2.34
     *
     * @param temp 硬件上传的温度数据
     * @return 转化格式后的温度数据
     */
    private static float getTempValueFromString(String temp) {
        if (StringUtils.isEmpty(temp)) {
            return 0;
        }
        int position = temp.indexOf("-");
        float y;
        if (position < 0) {
            y = Float.parseFloat(temp);
        } else {
            int pp = temp.indexOf(".");
            StringBuilder sb = new StringBuilder();
            sb.append(temp, 0, pp + 1);
            sb.append(temp.substring(pp + 2));
            y = Float.parseFloat(sb.toString());
        }
        return y;
    }

    /**
     * 报文处理
     *
     * @param ctx   ChannelHandlerContext
     * @param event MessageEvent
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {

        // 获取待处理对象
        Nda nda = (Nda) event.getMessage();

        // 获得将nda转为byte再转为utf-8,
        String tmp = StringUtils.toStringQuietly(nda.getData());

        // 将返回的字符串数据转换为RequestEntity对象，方便处理
        RequestEntity requestEntity = null;
        try {
            // 程序运行至此,就已经将报文消息变为对象,可以开始逻辑判断
            requestEntity = GSON.fromJson(tmp, RequestEntity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (requestEntity == null) {
            return;
        }

        // 获取上传的token值
        String tk = requestEntity.getMeta().getAuthorization();
        int pos = tk.indexOf("token");
        String tagNo = tk.substring(pos + 6).trim();

        //第二版在线数据
        if ("/v2/online".equalsIgnoreCase(requestEntity.getPath())) {
            //回复处理放在前面
            UploadResponse up = handlerService.handleTagOnlineData(tagNo,
                    requestEntity.getBody().getDatapoint().getX(), requestEntity.getBody().getDatapoint().getY(), requestEntity.getFlashErr());

            if (null == up) {
                return;
            }
            String result = GSON.toJson(up);
            if (StringUtils.isEmpty(result)) {
                return;
            }
            result = formatData(result);
            result = StringEscapeUtils.unescapeJava(result);

            Channel channel = event.getChannel();
            if (channel != null) {
                Logger.error("在线回复:" + result);
                channel.write(new Nda(StringUtils.toBytesQuietly(result)));
            }

            handlerService.handleData(tagNo, getTempValueFromString(requestEntity.getBody().getDatapoint().getX()),
                    requestEntity.getBody().getDatapoint().getY(), null, requestEntity.getVdd(), requestEntity
                            .getWIFI(), requestEntity.getFlashErr());

            return;
        }


        //离线数据压缩版
        if ("offline".equalsIgnoreCase(requestEntity.getPath())) {
            if (requestEntity.getBody().getOfflineData() != null) {

                //先回复离线数据再进行处理
                String result = "{\"offline_message GET\",\"SleepTime\":0.0}";
                result = StringEscapeUtils.unescapeJava(result);
                Channel channel = event.getChannel();
                if (channel != null) {
                    Logger.error("离线回复:" + result);
                    channel.write(new Nda(StringUtils.toBytesQuietly(result)));
                }

                // 离线数据处理
                List<OfflineDataPoint> dataList = new ArrayList<>();
                String[] offlineDataElems = requestEntity.getBody().getOfflineData().split(";");
                for (int i = 0; i < offlineDataElems.length; i++) {
                    String[] elems = offlineDataElems[i].split(",");
                    if (elems.length != 4) {
                        continue;
                    }

                    //温度数据为负值时，会有两个负号（如-2.-34），需处理下变为-2.34
                    float elemTemperature = getTempValueFromString(elems[0]);

                    float eleHumidity = Float.parseFloat(elems[1]);
                    Date elemDate = SIMPLE_DATE_FORMAT.parse(elems[2]);
                    long elemAddSecond = Long.parseLong(elems[3]) * 60 * 1000;
                    Date realElemDate = new Date(elemDate.getTime() + elemAddSecond);
                    dataList.add(new OfflineDataPoint(elemTemperature, eleHumidity, realElemDate));
                }

                // 对数据List进行处理
                for (OfflineDataPoint myDataStruct : dataList) {
                    //超出当前时间的数据剔除
                    if (myDataStruct.getTime().after(new Date())) {
                        continue;
                    }
                    handlerService.handleData(tagNo, myDataStruct.getTemperature(), myDataStruct.getHumidity(), myDataStruct
                            .getTime(), null, null, null);
                }
                return;
            }
        }

        // 设置反馈数据处理
        if ("feedback".equalsIgnoreCase(requestEntity.getPath())) {

            //上传数据需要除以100得到真实值
            Float tPrecision = requestEntity.getTemPrecision() / 100;
            Float hPrecision = requestEntity.getHumPrecision() / 100;
            Float tmax = requestEntity.getTmax() / 100;
            Float tmin = requestEntity.getTmin() / 100;
            Float hmax = requestEntity.getHmax() / 100;
            Float hmin = requestEntity.getHmin() / 100;
            if (tmax > 99) {
                tmax = null;
            }
            if (tmin < -99) {
                tmin = null;
            }
            if (hmax > 99) {
                hmax = null;
            }
            if (hmin <= 0) {
                hmin = null;
            }

            handlerService.handleFeedbackData(tagNo, requestEntity.getSsid(), requestEntity.getPassword(),
                    requestEntity.getBuzzer(), tPrecision, hPrecision, tmax, tmin, hmax, hmin);
            return;
        }

        if (requestEntity.getPath().contains("activate")) {

            AuthResponse au = handlerService.handleActiveData(tagNo, requestEntity.getNonce());

            if (null == au) {
                return;
            }
            String result = GSON.toJson(au);

            if (result != null) {
                result = formatData(result);

                Logger.error("激活回复:" + result);
                result = StringEscapeUtils.unescapeJava(result);
                Channel channel = event.getChannel();
                if (channel != null) {
                    channel.write(new Nda(StringUtils.toBytesQuietly(result)));
                }
                return;
            }

        }

        //第一版上传数据，在线离线根据Time字段判断。
        if ("/v1/datastreams/tem_hum/datapoint/".equalsIgnoreCase(requestEntity.getPath())) {

            Date dataTime = new Date();
            if (requestEntity.getTime() != null) {
                dataTime = requestEntity.getTime();
            }

            handlerService.handleData(tagNo, getTempValueFromString(requestEntity.getBody().getDatapoint().getX()),
                    requestEntity.getBody().getDatapoint().getY(), dataTime, null, null, null);

            String result;
            if (requestEntity.getTime() == null) {

                UploadResponse up = handlerService.handleTagOnlineData(tagNo,
                        requestEntity.getBody().getDatapoint().getX(), requestEntity.getBody().getDatapoint().getY(), null);

                result = GSON.toJson(up);
                result = formatData(result);

            } else {
                result = "{\"offline_message GET\",\"SleepTime\":0.0}";
            }

            result = StringEscapeUtils.unescapeJava(result);
            Channel channel = event.getChannel();
            if (channel != null) {
                Logger.error("第一版回复:" + result);
                channel.write(new Nda(StringUtils.toBytesQuietly(result)));
            }
            return;
        }

        //其他情况
        Logger.error("无数据可回复");
    }

    /**
     * 异常处理
     *
     * @param ctx   ChannelHandlerContext对象
     * @param event 异常事件
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent event) {
        Throwable cause = event.getCause();
        if (!(cause instanceof IOException)) {
            Logger.error("异常原因：" + cause);
        }
        ctx.getChannel().close();
    }

}

