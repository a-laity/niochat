package MultiThread;

/**
 * @author xujunchen
 * @date 2023/7/21 0:40
 * @describe todo
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MultiThreadReactorClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8080));
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("Hello from client".getBytes());
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();
        String message = new String(buffer.array(), 0, buffer.limit()).trim();
        System.out.println("Received message: " + message);
        socketChannel.close();
    }
}