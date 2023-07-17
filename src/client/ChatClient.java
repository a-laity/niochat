package client;

import java.io.*;
import java.net.Socket;

/**
 * @author xujunchen
 * @date 2022/11/19 22:46
 * @describe todo
 */
public class ChatClient {
    public final String LOCALHOST = "127.0.0.1";
    public final int PORT = 8080;
    public final String QUIT = "quit";
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;
    Socket socket = null;

    public void writeMsg(String msg) throws IOException {
        if (!socket.isInputShutdown()) {
            bufferedWriter.write(msg + "\n");
            bufferedWriter.flush();
        }
    }

    public String receive() throws IOException {
        String input = null;
        if (!socket.isInputShutdown()) {
            input = bufferedReader.readLine();
        }
        return input;
    }

    public void startWork() {
        try {
            socket = new Socket(LOCALHOST, PORT);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            new Thread(new ClientHandler(this)).start();
            String receive =null;
            while ((receive = receive())!= null) {
                System.out.println(receive);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public void close() {
        if (bufferedWriter != null) {
            try {
                System.out.println("关闭socket");
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.startWork();
    }
}
