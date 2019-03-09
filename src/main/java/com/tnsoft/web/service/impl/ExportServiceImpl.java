package com.tnsoft.web.service.impl;

import com.expertise.common.util.StringUtils;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.tnsoft.hibernate.model.Express;
import com.tnsoft.hibernate.model.Tag;
import com.tnsoft.hibernate.model.TagExpress;
import com.tnsoft.hibernate.model.TempExpress;
import com.tnsoft.web.dao.ExpressDAO;
import com.tnsoft.web.dao.TagDAO;
import com.tnsoft.web.dao.TagExpressDAO;
import com.tnsoft.web.dao.TempExpressDAO;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.service.ExportService;
import com.tnsoft.web.util.ChartUtil;
import com.tnsoft.web.util.Utils;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

@Service("exportService")
public class ExportServiceImpl implements ExportService {

    @Resource(name = "tempExpressDAO")
    private TempExpressDAO tempExpressDAO;
    @Resource(name = "expressDAO")
    private ExpressDAO expressDAO;
    @Resource(name = "tagExpressDAO")
    private TagExpressDAO tagExpressDAO;
    @Resource(name = "tagDAO")
    private TagDAO tagDAO;

    @Override
    public void exportToPDF(int expressId, Date start, Date end, int flag, HttpServletRequest request,
                            HttpServletResponse response) {

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.reset();
            response.setContentType("application/pdf");

            List<TempExpress> tempExpressList = tempExpressDAO.getByExpressIdWithTimeLimit(expressId,
                    start, end);
            Express express = expressDAO.getById(expressId);
            TagExpress tagExpress = tagExpressDAO.getLastTagExpressByEId(expressId);
            Tag tag = tagDAO.getById(tagExpress.getTagNo());

            String fileName = express.getExpressNo();
            if (StringUtils.isEmpty(fileName)) {
                fileName = expressId + ".pdf";
            } else {
                fileName += ".pdf";
            }
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            //Initialize PDF writer
            PdfWriter writer = new PdfWriter(outputStream);
            //Initialize PDF document
            PdfDocument pdf = new PdfDocument(writer);
            // Initialize document
            Document document = new Document(pdf);

            PdfFont helvetica = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H", false);

            //不同版本名称
            String expressNo, statusActive, statusFinish;
            if (flag == Constants.Version.EXPRESS) {
                //物流版
                expressNo = "订单号";
                statusActive = "配送中";
                statusFinish = "已签收";
            } else {
                //医药/标准版
                expressNo = "监测点名称";
                statusActive = "监测中";
                statusFinish = "已结束";
            }

            Float maxAlertTemperature, minAlertTemperature;

            if (express.getTemperatureMax() != null) {
                maxAlertTemperature = express.getTemperatureMax();
            } else {
                if (tag.getTemperatureMax() != null) {
                    maxAlertTemperature = tag.getTemperatureMax();
                } else {
                    maxAlertTemperature = null;
                }
            }

            if (express.getTemperatureMin() != null) {
                minAlertTemperature = express.getTemperatureMin();
            } else {
                if (tag.getTemperatureMin() != null) {
                    minAlertTemperature = tag.getTemperatureMin();
                } else {
                    minAlertTemperature = null;
                }
            }

            String maxAlertTemperatureStr, minAlertTemperatureStr;
            if (null != maxAlertTemperature) {
                maxAlertTemperatureStr = maxAlertTemperature + "";
            } else {
                maxAlertTemperatureStr = "无";
            }
            if (null != minAlertTemperature) {
                minAlertTemperatureStr = minAlertTemperature + "";
            } else {
                minAlertTemperatureStr = "无";
            }


            float[] briefTableWidths = {90, 90, 90, 90, 90, 90};
            Table briefTable = new Table(briefTableWidths);
            briefTable.addCell(new Paragraph(expressNo).setFont(helvetica)).addCell(new Cell(1, 3).add(new
                    Paragraph(express.getExpressNo()).setFont(helvetica)));
            if (express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
                briefTable.addCell(new Paragraph("状态").setFont(helvetica)).addCell(new Paragraph(statusFinish)
                        .setFont(helvetica));
            } else {
                briefTable.addCell(new Paragraph("状态").setFont(helvetica)).addCell(new Paragraph(statusActive)
                        .setFont(helvetica));
            }
            String creationTimeStr = "";
            String finishedTimeStr = "";
            if (null != express.getCreationTime()) {
                creationTimeStr = Utils.SF.format(express.getCreationTime());
            }
            if (null != express.getCheckOutTime()) {
                finishedTimeStr = Utils.SF.format(express.getCheckOutTime());
            }
            briefTable.addCell(new Paragraph("开始时间").setFont(helvetica)).addCell(new Cell(1, 2).add(new Paragraph
                    (creationTimeStr).setFont(helvetica)));
            briefTable.addCell(new Paragraph("结束时间").setFont(helvetica)).addCell(new Cell(1, 2).add(new Paragraph
                    (finishedTimeStr).setFont(helvetica)));
            briefTable.addCell(new Paragraph("低温阈值").setFont(helvetica)).addCell(new Paragraph
                    (minAlertTemperatureStr).setFont(helvetica));
            briefTable.addCell(new Paragraph("高温阈值").setFont(helvetica)).addCell(new Paragraph
                    (maxAlertTemperatureStr).setFont(helvetica));

            //温湿度曲线获得
            BufferedImage bufferedImage = ChartUtil.getChart(tempExpressList, maxAlertTemperature,
                    minAlertTemperature, flag);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(byteArrayOutputStream);
            encoder.encode(bufferedImage);
            ImageData imageData = ImageDataFactory.create(byteArrayOutputStream.toByteArray());
            Image image = new Image(imageData);


            float[] widths = {200, 150, 150};
            Table originalDataTable = new Table(widths);
            originalDataTable.addCell(new Paragraph("时间").setFont(helvetica)).addCell(new Paragraph("温度（℃）")
                    .setFont(helvetica)).addCell(new Paragraph("湿度（%）").setFont(helvetica));
            int alertCount = 0;
            Float averageTemp = null, minTemp = null, maxTemp = null, averageHumidity = null, minHumidity = null,
                    maxHumidity = null;
            if (tempExpressList.size() > 0) {
                minTemp = tempExpressList.get(0).getTemperature();
                maxTemp = tempExpressList.get(0).getTemperature();
                averageTemp = 0F;
                minHumidity = tempExpressList.get(0).getHumidity();
                maxHumidity = tempExpressList.get(0).getHumidity();
                averageHumidity = 0F;
                int size = tempExpressList.size();
                for (TempExpress tempExpress : tempExpressList) {
                    if (tempExpress.getHumidity() == 0) {
                        originalDataTable.addCell(Utils.SF.format(tempExpress.getCreationTime())).addCell(tempExpress
                                .getTemperature() + "").addCell("-");
                    } else {
                        originalDataTable.addCell(Utils.SF.format(tempExpress.getCreationTime())).addCell(tempExpress
                                .getTemperature() + "").addCell(tempExpress.getHumidity() + "");
                    }
                    if (null != maxAlertTemperature && tempExpress.getTemperature() >= maxAlertTemperature) {
                        alertCount++;
                    }
                    if (null != minAlertTemperature && tempExpress.getTemperature() <= minAlertTemperature) {
                        alertCount++;
                    }
                    if (minTemp > tempExpress.getTemperature()) {
                        minTemp = tempExpress.getTemperature();
                    }
                    if (maxTemp < tempExpress.getTemperature()) {
                        maxTemp = tempExpress.getTemperature();
                    }
                    if (minHumidity > tempExpress.getHumidity()) {
                        minHumidity = tempExpress.getHumidity();
                    }
                    if (maxHumidity < tempExpress.getHumidity()) {
                        maxHumidity = tempExpress.getHumidity();
                    }
                    averageTemp += tempExpress.getTemperature() / size;
                    averageHumidity += tempExpress.getHumidity() / size;
                }
            }

            //将统计数据添加到报表
            briefTable.addCell(new Paragraph("报警次数").setFont(helvetica)).addCell(new Paragraph
                    (alertCount + "").setFont(helvetica));
            if (null != averageTemp) {
                briefTable.addCell(new Paragraph("平均温度").setFont(helvetica)).addCell(new Paragraph
                        (averageTemp + "").setFont(helvetica));
            } else {
                briefTable.addCell(new Paragraph("平均温度").setFont(helvetica)).addCell(new Paragraph
                        ("无").setFont(helvetica));
            }
            if (null != maxTemp) {
                briefTable.addCell(new Paragraph("最高温度").setFont(helvetica)).addCell(new Paragraph
                        (maxTemp + "").setFont(helvetica));
            } else {
                briefTable.addCell(new Paragraph("最高温度").setFont(helvetica)).addCell(new Paragraph
                        ("无").setFont(helvetica));
            }
            if (null != minTemp) {
                briefTable.addCell(new Paragraph("最低温度").setFont(helvetica)).addCell(new Paragraph
                        (minTemp + "").setFont(helvetica));
            } else {
                briefTable.addCell(new Paragraph("最低温度").setFont(helvetica)).addCell(new Paragraph
                        ("无").setFont(helvetica));
            }

            if (null != averageHumidity && averageHumidity > 0) {
                briefTable.addCell(new Paragraph("平均湿度").setFont(helvetica)).addCell(new Paragraph
                        (averageHumidity + "").setFont(helvetica));
            } else {
//                briefTable.addCell(new Paragraph("平均湿度").setFont(helvetica)).addCell(new Paragraph
//                        ("无").setFont(helvetica));
            }
            if (null != maxHumidity && maxHumidity > 0) {
                briefTable.addCell(new Paragraph("最高湿度").setFont(helvetica)).addCell(new Paragraph
                        (maxHumidity + "").setFont(helvetica));
            } else {
//                briefTable.addCell(new Paragraph("最高湿度").setFont(helvetica)).addCell(new Paragraph
//                        ("无").setFont(helvetica));
            }
            if (null != minHumidity && minHumidity > 0) {
                briefTable.addCell(new Paragraph("最低湿度").setFont(helvetica)).addCell(new Paragraph
                        (minHumidity + "").setFont(helvetica));
            } else {
//                briefTable.addCell(new Paragraph("最低湿度").setFont(helvetica)).addCell(new Paragraph
//                        ("无").setFont(helvetica));
            }

            briefTable.addCell(new Paragraph("设备识别号").setFont(helvetica)).addCell(new Cell(1, 5).add(new Paragraph
                    (tag.getTagNo()).setFont(helvetica)));

            //添加到pdf
            document.add(new Paragraph("数据曲线").setFont(helvetica).setFontSize(24));
            document.add(image);
            document.add(new Paragraph("数据报表").setFont(helvetica).setFontSize(24));
            document.add(briefTable);
            document.add(new Paragraph("原始数据").setFont(helvetica).setFontSize(24));
            document.add(originalDataTable);

            //Close document
            document.close();
            pdf.close();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void exportToXLS(int expressId, Date start, Date end, int flag, HttpServletRequest request,
                            HttpServletResponse response) {


        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.reset();//清空输出流
            response.setContentType("application/msexcel");


            List<TempExpress> tempExpressList = tempExpressDAO.getByExpressIdWithTimeLimit(expressId,
                    start, end);
            Express express = expressDAO.getById(expressId);
//            TagExpress tagExpress = tagExpressDAO.getLastTagExpressByEId(expressId);
//            Tag tag = tagDAO.getById(tagExpress.getTagNo());

            String fileName = express.getExpressNo();
            if (StringUtils.isEmpty(fileName)) {
                fileName = expressId + ".xls";
            } else {
                fileName += ".xls";
            }

            //不同版本名称
            String expressNo;
            if (flag == Constants.Version.EXPRESS) {
                //物流版
                expressNo = "订单号";

            } else {
                //医药/标准版
                expressNo = "监测点名称";
            }

            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            WritableWorkbook wwb = Workbook.createWorkbook(outputStream);
            WritableSheet ws = wwb.createSheet("sheet1", 0);
            ws.addCell(new Label(0, 0, expressNo)); //将生成的单元格添加到工作表中
            ws.addCell(new Label(1, 0, express.getExpressNo()));
            ws.addCell(new Label(0, 1, "时间"));
            ws.addCell(new Label(1, 1, "温度（℃）"));

            int row = 2;
            if (tempExpressList.size() > 0 && tempExpressList.get(0).getHumidity() > 0) {
                for (TempExpress tempExpress : tempExpressList) {
                    ws.addCell(new Label(2, 1, "湿度（%）"));
                    ws.addCell(new Label(0, row, Utils.SF.format(tempExpress.getCreationTime())));
                    ws.addCell(new Label(1, row, tempExpress.getTemperature() + ""));
                    ws.addCell(new Label(2, row, tempExpress.getHumidity() + ""));
                    row++;
                }
            } else {
                for (TempExpress tempExpress : tempExpressList) {
                    ws.addCell(new Label(0, row, Utils.SF.format(tempExpress.getCreationTime())));
                    ws.addCell(new Label(1, row, tempExpress.getTemperature() + ""));
                    row++;
                }
            }

            wwb.write();
            wwb.close();


        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void exportCalibrationToPDF(String tagNo, HttpServletRequest request, HttpServletResponse response) {
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.reset();
            response.setContentType("application/pdf");

            Tag tag = tagDAO.getById(tagNo);
            if (null == tag) {
                return;
            }

            String fileName = tag.getTagNo() + ".pdf";

            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            //Initialize PDF writer
            PdfWriter writer = new PdfWriter(outputStream);
            //Initialize PDF document
            PdfDocument pdf = new PdfDocument(writer);
            // Initialize document
            Document document = new Document(pdf);

            PdfFont helvetica = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H", false);


            String rangeCN, rangeEN, rangeHCN, rangHEN, model;
            if (tag.getCalibrationType() == Constants.Calibrate.TM20) {
                rangeCN = "测温范围：-20 ~ 50℃";
                rangeEN = "Temp Range:-20 ~ 50℃";
                rangeHCN = "";
                rangHEN = "";
                model = "TM20";
            } else if (tag.getCalibrationType() == Constants.Calibrate.TM20E) {
                rangeCN = "测温范围：-100 ~ 50℃";
                rangeEN = "Temp Range:-100 ~ 50℃";
                rangeHCN = "";
                rangHEN = "";
                model = "TM20E";
            } else if (tag.getCalibrationType() == Constants.Calibrate.THM20) {
                rangeCN = "测温范围：-20 ~ 60℃";
                rangeEN = "Temp Range:-20 ~ 60℃";
                rangeHCN = "测湿范围：0-100%RH";
                rangHEN = "Humidity Range:0-100%RH";
                model = "THM20";
            } else if (tag.getCalibrationType() == Constants.Calibrate.THM20E) {
                rangeCN = "测温范围：-40 ~ 85℃";
                rangeEN = "Temp Range:-40 ~ 85℃";
                rangeHCN = "测湿范围：0-100%RH";
                rangHEN = "Humidity Range:0-100%RH";
                model = "THM20E";
            } else {
                rangeCN = "";
                rangeEN = "";
                rangeHCN = "";
                rangHEN = "";
                model = "";
            }

            //设置默认字体
            document.setFont(helvetica);
            document.setFontSize(10.5f);
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));

            float[] widths = {170, 180, 180};
            Table infoTable = new Table(widths);

            infoTable.addCell(new Paragraph("设备名称：智能温湿度监测仪")).addCell(new Paragraph(rangeCN)).addCell(new Paragraph
                    (rangeHCN));
            infoTable.addCell(new Paragraph("Name:Intelligent temperature and humidity monitor")).addCell(new
                    Paragraph(rangeEN)).addCell(new
                    Paragraph(rangHEN));
            infoTable.addCell(new Cell(1, 3).add(new Paragraph("")));
            infoTable.addCell(new Paragraph("型号规格：" + model)).addCell(new Cell(1, 2).add(new Paragraph("设备识别码：" + tag
                    .getTagNo())));
            infoTable.addCell(new Paragraph("Model:" + model)).addCell(new Cell(1, 2).add(new Paragraph("Instrument " +
                    "ID:" + tag.getTagNo())));
            infoTable.addCell(new Cell(1, 3).add(new Paragraph("")));
            infoTable.addCell(new Cell(1, 3).add(new Paragraph("校准日期：" + Utils.SF.format(tag.getCalibrationTime()))));
            infoTable.addCell(new Cell(1, 3).add(new Paragraph("Calibration Date:" + Utils.SF.format(tag
                    .getCalibrationTime()))));

            infoTable.getCell(0, 0).setBorder(Border.NO_BORDER);
            infoTable.getCell(0, 1).setBorder(Border.NO_BORDER);
            infoTable.getCell(0, 2).setBorder(Border.NO_BORDER);
            infoTable.getCell(1, 0).setBorder(Border.NO_BORDER);
            infoTable.getCell(1, 1).setBorder(Border.NO_BORDER);
            infoTable.getCell(1, 2).setBorder(Border.NO_BORDER);
            infoTable.getCell(2, 0).setBorder(Border.NO_BORDER);
            infoTable.getCell(3, 0).setBorder(Border.NO_BORDER);
            infoTable.getCell(3, 1).setBorder(Border.NO_BORDER);
            infoTable.getCell(4, 0).setBorder(Border.NO_BORDER);
            infoTable.getCell(4, 1).setBorder(Border.NO_BORDER);
            infoTable.getCell(5, 0).setBorder(Border.NO_BORDER);
            infoTable.getCell(6, 0).setBorder(Border.NO_BORDER);
            infoTable.getCell(7, 0).setBorder(Border.NO_BORDER);

            document.add(infoTable);

            DecimalFormat decimalFormat = new DecimalFormat("##0.00");
            float[] widths1 = {145, 145, 145, 145};

            document.add(new Paragraph(""));
            document.add(new Paragraph("一、温度示值校准 Temperature：（℃）"));
            Table tempTable = new Table(widths1);
            tempTable.addCell(new Paragraph("标准温度值（℃）\n Reference Value"))
                    .addCell(new Paragraph("被校仪器示值（℃）\n Measured Value"))
                    .addCell(new Paragraph("示值误差（℃）\n Indication Error"))
                    .addCell(new Paragraph("允许误差（℃）\n Permissible Error"));


            if (tag.getCalibrationType() == Constants.Calibrate.TM20 || tag.getCalibrationType() == Constants
                    .Calibrate.TM20E) {

                tempTable.addCell(new Paragraph(tag.getStandardLowTemp() + ""))
                        .addCell(new Paragraph(tag.getCalibrationLowTemp() + ""))
                        .addCell(new Paragraph(decimalFormat.format(tag.getCalibrationLowTemp() - tag
                                .getStandardLowTemp())))
                        .addCell(new Paragraph(Constants.Calibrate.PERMISSIBLE_ERROR_TEMP + ""));
                tempTable.addCell(new Paragraph(tag.getStandardMediumTemp() + ""))
                        .addCell(new Paragraph(tag.getCalibrationMediumTemp() + ""))
                        .addCell(new Paragraph(decimalFormat.format(tag.getCalibrationMediumTemp() - tag
                                .getStandardMediumTemp())))
                        .addCell(new Paragraph(Constants.Calibrate.PERMISSIBLE_ERROR_TEMP + ""));
                tempTable.addCell(new Paragraph(tag.getStandardHighTemp() + ""))
                        .addCell(new Paragraph(tag.getCalibrationHighTemp() + ""))
                        .addCell(new Paragraph(decimalFormat.format(tag.getCalibrationHighTemp() - tag
                                .getStandardHighTemp())))
                        .addCell(new Paragraph(Constants.Calibrate.PERMISSIBLE_ERROR_TEMP + ""));
                document.add(tempTable);

                document.add(new Paragraph(""));
                document.add(new Paragraph(""));
                document.add(new Paragraph(""));
                document.add(new Paragraph(""));
            } else {

                if (tag.getStandardLowTemp() > 0) {
                    tempTable.addCell(new Paragraph(tag.getStandardLowTemp() + ""))
                            .addCell(new Paragraph(tag.getCalibrationLowTemp() + ""))
                            .addCell(new Paragraph(decimalFormat.format(tag.getCalibrationLowTemp() - tag
                                    .getStandardLowTemp())))
                            .addCell(new Paragraph(Constants.Calibrate.PERMISSIBLE_ERROR_TEMP + ""));
                } else {
                    tempTable.addCell(new Paragraph(tag.getStandardLowTemp() + ""))
                            .addCell(new Paragraph(tag.getCalibrationLowTemp() + ""))
                            .addCell(new Paragraph(decimalFormat.format(tag.getCalibrationLowTemp() - tag
                                    .getStandardLowTemp())))
                            .addCell(new Paragraph("1"));
                }

                if (tag.getStandardMediumTemp() > 0) {
                    tempTable.addCell(new Paragraph(tag.getStandardMediumTemp() + ""))
                            .addCell(new Paragraph(tag.getCalibrationMediumTemp() + ""))
                            .addCell(new Paragraph(decimalFormat.format(tag.getCalibrationMediumTemp() - tag
                                    .getStandardMediumTemp())))
                            .addCell(new Paragraph(Constants.Calibrate.PERMISSIBLE_ERROR_TEMP + ""));
                } else {
                    tempTable.addCell(new Paragraph(tag.getStandardMediumTemp() + ""))
                            .addCell(new Paragraph(tag.getCalibrationMediumTemp() + ""))
                            .addCell(new Paragraph(decimalFormat.format(tag.getCalibrationMediumTemp() - tag
                                    .getStandardMediumTemp())))
                            .addCell(new Paragraph("1"));
                }

                if (tag.getStandardHighTemp() > 0) {
                    tempTable.addCell(new Paragraph(tag.getStandardHighTemp() + ""))
                            .addCell(new Paragraph(tag.getCalibrationHighTemp() + ""))
                            .addCell(new Paragraph(decimalFormat.format(tag.getCalibrationHighTemp() - tag
                                    .getStandardHighTemp())))
                            .addCell(new Paragraph(Constants.Calibrate.PERMISSIBLE_ERROR_TEMP + ""));
                    document.add(tempTable);
                } else {
                    tempTable.addCell(new Paragraph(tag.getStandardHighTemp() + ""))
                            .addCell(new Paragraph(tag.getCalibrationHighTemp() + ""))
                            .addCell(new Paragraph(decimalFormat.format(tag.getCalibrationHighTemp() - tag
                                    .getStandardHighTemp())))
                            .addCell(new Paragraph("1"));
                    document.add(tempTable);
                }


                document.add(new Paragraph(""));
                document.add(new Paragraph("二、相对湿度示值校准 Relative Humidity：（%RH）"));
                Table humidityTable = new Table(widths1);
                humidityTable.addCell(new Paragraph("标准湿度值（%RH）\n Reference Value"))
                        .addCell(new Paragraph("被校仪器示值（%RH）\n Measured Value"))
                        .addCell(new Paragraph("示值误差（%RH）\n Indication Error"))
                        .addCell(new Paragraph("允许误差（%RH）\n Permissible Error"));
                humidityTable.addCell(new Paragraph(tag.getStandardHumidity() + ""))
                        .addCell(new Paragraph(tag.getCalibrationHumidity() + ""))
                        .addCell(new Paragraph(decimalFormat.format(tag.getCalibrationHumidity() - tag
                                .getStandardHumidity())))
                        .addCell(new Paragraph(Constants.Calibrate.PERMISSIBLE_ERROR_HUMIDITY + ""));
                document.add(humidityTable);
            }

            document.add(new Paragraph(""));
            document.add(new Paragraph("Note:"));
            document.add(new Paragraph("1.本次校准依据的技术规范：JJG205-2005 机械式温湿度计检定规程\nReference documents for the " +
                    "calibration: JJG205-2005 Mechanical " +
                    "Thermo-hygrometers\n2.本次校准地点及其环境：物华通信及环境试验实验室，25℃，75%RH\nEnvironment Used in Calibration ： WUVA " +
                    "Communications and Environmental test laboratory,25℃," +
                    "75%RH\n3.本证书仅对所检的器具有效，该证书未经本公司许可不得翻录，本设备校准周期为12 个月\nThis certificate only for the inspection " +
                    "instruments effectively, the certificate shall not be copied without permission of the company, " +
                    "the equipment calibration cycle is 12 months\n"));

            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph("校 准 员                                          核 验 员                         " +
                    "                 批 准 人\nOperator                                         Inspector              " +
                    "                           Approver").setTextAlignment(TextAlignment.CENTER));

            //Close document
            document.close();
            pdf.close();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
