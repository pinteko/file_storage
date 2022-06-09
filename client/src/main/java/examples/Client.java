package examples;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class Client {

    private final int port;
    private final String host;

    public Client(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public static void main(String[] args) throws Exception {
       // Message message = new Message("type", 5, new byte[]{1, 2, 3, 4, 5});
        File file = new File("D:\\recent.txt");
        new Client(9999, "localhost").run(file);
    }

    private void run(File file) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap client = new Bootstrap();
            client.group(workerGroup);
            client.channel(NioSocketChannel.class);
            client.option(ChannelOption.SO_KEEPALIVE, true);
            client.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline entries = ch.pipeline().addLast(
                            new FileEncoder(),
                            new LineBasedFrameDecoder(80),
                            new StringDecoder(StandardCharsets.UTF_8),
                            new FileClientHandler(file)
                    );
                }
            });

            ChannelFuture future = client.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}