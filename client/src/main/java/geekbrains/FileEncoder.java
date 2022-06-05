package geekbrains;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileEncoder extends MessageToByteEncoder<File> {
    private int readLength = 8;
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, File file, ByteBuf out) throws Exception {
        out.writeInt(file.getName().length());
        out.writeCharSequence(file.getName(), StandardCharsets.UTF_8);
        out.writeInt(Files.readAllBytes(file.toPath()).length);
        out.writeBytes(Files.readAllBytes(file.toPath()));
    }
}
