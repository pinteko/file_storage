package examples;


import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class FileServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object file) throws Exception {
        File file1 = (File) file;

        ChannelFuture future = ctx.writeAndFlush("OK\n");
        future.addListener(ChannelFutureListener.CLOSE);
    }
}