package examples;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileDecoder extends ReplayingDecoder<File> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        int lengthName = in.readInt();
       String name = in.readCharSequence(lengthName, StandardCharsets.UTF_8).toString();
       int lengthByte = in.readInt();
        byte[] bytes = new byte[lengthByte];
        in.readBytes(bytes, 0, lengthByte);

        File file = new File("C:\\Users\\Kirill\\OneDrive\\Рабочий стол\\place\\" + "copy" + name);

        try {
            if (!file.exists()) {
                file.createNewFile();
                Files.write(file.toPath(), bytes);
            } else {
                Files.write(file.toPath(), bytes);
            }
            out.add(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    }

