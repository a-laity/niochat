package MultiThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xujunchen
 * @date 2023/7/19 22:12
 * @describe todo
 */
public class MultiThreadReactor {
    private Selector selector;
    private ServerSocketChannel serverSocket;
    private ExecutorService executorService;
    private SubReactor[] subReactors;

    public MultiThreadReactor(int port, int n) throws IOException {
        // 创建 Selector 和 ServerSocketChannel
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);

        // 注册 Accept 事件
        SelectionKey key = serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        // 创建线程池和从 Reactor
        executorService = Executors.newFixedThreadPool(n);
        subReactors = new SubReactor[n];
        for (int i = 0; i < n; i++) {
            subReactors[i] = new SubReactor();
            executorService.submit(subReactors[i]);
        }
    }

    public void start() throws IOException {
        while (!Thread.interrupted()) {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (key.isAcceptable()) {
                    // 处理连接请求，将连接分配给从 Reactor 处理
                    SocketChannel channel = serverSocket.accept();
                    channel.configureBlocking(false);
                    subReactors[channel.hashCode() % subReactors.length].registerChannel(channel);
                } else {
                    // 处理读写请求，将请求分配给从 Reactor 处理
                    SubReactor subReactor = (SubReactor) key.attachment();
                    subReactor.addTask(new Task(key));
                }
            }
        }
    }

    private class SubReactor implements Runnable {
        private Selector selector;
        private TaskQueue taskQueue;

        public SubReactor() throws IOException {
            selector = Selector.open();
            taskQueue = new TaskQueue();
        }

        public void registerChannel(SocketChannel channel) throws IOException {
            // 注册读写事件
            channel.register(selector, SelectionKey.OP_READ, this);
        }

        public void addTask(Task task) {
            taskQueue.addTask(task);
            selector.wakeup();
        }

        public void run() {
            while (!Thread.interrupted()) {
                try {
                    selector.select();
                    taskQueue.executeTasks();
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();

                        if (key.isReadable()) {
                            // 处理读事件
                            // ...
                        } else if (key.isWritable()) {
                            // 处理写事件
                            // ...
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Task {
        private SelectionKey key;

        public Task(SelectionKey key) {
            this.key = key;
        }

        public void execute() {
            // 处理读写请求
            // ...
        }
    }

    private class TaskQueue {
        private ConcurrentLinkedQueue<Task> queue = new ConcurrentLinkedQueue<>();

        public void addTask(Task task) {
            queue.offer(task);
        }

        public void executeTasks() {
            Task task;
            while ((task = queue.poll()) != null) {
                task.execute();
            }
        }
    }
}
