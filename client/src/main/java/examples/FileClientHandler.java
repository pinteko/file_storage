package examples;


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


