package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xujunchen
 * @date 2022/11/19 21:47
 * @describe todot
 */
public class ChatServer {
    public final int DEFAULT_PORT = 8080;
    public final String QUIT = "quit";
    private ServerSocket serverSocket = null;
    private Map<Integer, Writer> connectClient = new HashMap<Integer, Writer>();
    ExecutorService executorService;

    public ChatServer() {
         executorService = Executors.newFixedThreadPool(10);
    }

    public synchronized void addClient(Socket socket) throws IOException {
        if (socket != null) {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            if (!connectClient.containsKey(socket.getPort())) {
                connectClient.put(socket.getPort(), bufferedWriter);
                System.out.println("客户端[" + socket.getPort() + "]已连接到服务器");
            }
        }
    }

    public synchronized void removeClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            if (connectClient.containsKey(port)) {
                connectClient.get(port).close();
                connectClient.remove(port);
                System.out.println("客户端[" + port + "]已断开连接");
            }
        }
    }

    private void startwork() {
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT + "...");
            while (true) {
                Socket socket = serverSocket.accept();
                //new Thread(new ChatHandler(this, socket)).start();
                executorService.execute(new ChatHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }

    public synchronized void sendMsg(Socket socket, String msg) throws IOException {
        int port = socket.getPort();
        for (Integer id : connectClient.keySet()) {
            if (!id.equals(port)) {
                connectClient.get(id).write(msg);
                connectClient.get(id).flush();
            }
        }

    }

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public synchronized void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                System.out.println("关闭serverSocket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.startwork();
    }
}
