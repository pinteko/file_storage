package geekbrains;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        out.writeInt(msg.getType().length());
        out.writeCharSequence(msg.getType(), StandardCharsets.UTF_8);
        out.writeInt(msg.getLength());
        out.writeBytes(msg.getData());
    }
}