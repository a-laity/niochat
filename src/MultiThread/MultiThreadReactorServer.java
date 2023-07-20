package MultiThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadReactorServer implements Runnable {
    private final Selector selector;
    private final ServerSocketChannel serverSocket;
    private final ExecutorService executor;

    public MultiThreadReactorServer(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        sk.attach(new Acceptor());
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey sk = it.next();
                    it.remove();
                    dispatch(sk);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void dispatch(SelectionKey sk) {
        Runnable r = (Runnable) sk.attachment();
        if (r != null) {
            executor.submit(r);
        }
    }

    private class Acceptor implements Runnable {
        public void run() {
            try {
                SocketChannel socketChannel = serverSocket.accept();
                if (socketChannel != null) {
                    new Handler(selector, socketChannel);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class Handler implements Runnable {
        private final SocketChannel socketChannel;
        private final SelectionKey sk;
        private ByteBuffer input = ByteBuffer.allocate(1024);
        private ByteBuffer output = ByteBuffer.allocate(1024);

        public Handler(Selector selector, SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            socketChannel.configureBlocking(false);
            sk = socketChannel.register(selector, 0);
            sk.attach(this);
            sk.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        }

        public void run() {
            try {
                if (sk.isReadable()) {
                    read();
                } else if (sk.isWritable()) {
                    write();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void read() throws IOException {
            input.clear();
            socketChannel.read(input);
            input.flip();
            String message = new String(input.array(), 0, input.limit()).trim();
            System.out.println("Received message: " + message);
            sk.interestOps(SelectionKey.OP_WRITE);
        }

        private void write() throws IOException {
            output.clear();
            output.put("Hello from server".getBytes());
            output.flip();
            socketChannel.write(output);
            sk.interestOps(SelectionKey.OP_READ);
        }
    }

    public static void main(String[] args) throws IOException {
        MultiThreadReactorServer server = new MultiThreadReactorServer(8080);
        new Thread(server).start();
    }
}