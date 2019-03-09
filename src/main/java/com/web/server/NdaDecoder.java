package com.web.server;

import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * 解析硬件上传的数据包
 */
public class NdaDecoder extends FrameDecoder {

    private static final int MAX_PACKET_SIZE = 4096;
    //每个数据包以\n字符作为结束符
    private static final byte[] FOOTER = {'\n'};

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) {

        //判断\n字符位置        
        int pos = indexOf(buffer, FOOTER);

        //如果\n字符存在，说明一个数据包上传结束，数据为起始位置到该字符位置为止
        if (pos > 0) {
            //保存数据
            byte[] rawData = new byte[pos];

            buffer.readBytes(rawData);

            buffer.skipBytes(FOOTER.length);

            buffer.markReaderIndex();

            Logger.error(StringUtils.toStringQuietly(rawData));

            //返回Nda对象
            return new Nda(rawData);
        }

        return null;
    }

    //判断字符位置
    private int indexOf(ChannelBuffer buffer, byte[] value) {
        int start = 0;
        int len = buffer.readableBytes();
        BEGIN:
        while (start < len) {
            int pos = buffer.indexOf(start, len, value[0]);
            if (pos < 0) {
                return -1;
            }
            for (int i = 1; i < value.length; ++i) {
                int posi = buffer.indexOf(pos, len, value[i]);
                if (posi != pos + i) {
                    ++start;
                    continue BEGIN;
                }
            }
            return pos;
        }
        return -1;
    }
}
