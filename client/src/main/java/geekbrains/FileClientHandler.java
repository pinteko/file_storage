package geekbrains;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileClientHandler extends ChannelInboundHandlerAdapter {

    private File file;

    public FileClientHandler(File file) {
        this.file = file;
    }

    // Количество байтов, обрабатываемых за раз


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(file);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String response = (String) msg;
        System.out.println("response = " + response);
    }
}

//    private void sendFile(Channel channel) throws IOException {
//        File file = new File("D:\\recent.txt");
//        FileInputStream fis = new FileInputStream(file);
//        int count = 0;
//        for (;;) {
//            BufferedInputStream bis = new BufferedInputStream(fis);
//            byte[] bytes = new byte[readLength];
//            int readNum = bis.read(bytes, 0, readLength);
//            if (readNum == -1) {
//                return;
//            }
//            sendToServer(bytes, channel, readNum);
//            System.out.println("Send count: " + ++count);
//        }
//    }
//
//    private void sendToServer(byte[] bytes, Channel channel, int length) throws IOException {
//        ChannelBuffer buffer = ChannelBuffers.copiedBuffer(bytes, 0, length);
//        channel.write(buffer);
//    }
//}
