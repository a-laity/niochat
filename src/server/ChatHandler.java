package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author xujunchen
 * @date 2022/11/19 22:12
 * @describe todo
 */
public class ChatHandler implements Runnable {
    private Socket socket;
    private ChatServer chatServer;

    public ChatHandler(ChatServer chatServer, Socket socket) {
        this.socket = socket;
        this.chatServer = chatServer;
    }

    @Override
    public void run() {
        try {
            chatServer.addClient(socket);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg = null;
            while ((msg = bufferedReader.readLine()) != null) {
                String fwdMsg = "客户端[" + socket.getPort() + "]: " + msg + "\n";
                System.out.print(fwdMsg);
                chatServer.sendMsg(socket, fwdMsg);
                if (chatServer.readyToQuit(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                chatServer.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
