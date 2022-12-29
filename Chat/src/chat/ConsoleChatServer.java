package chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ConsoleChatServer extends Thread {

    private final Socket socket;
    private static final ArrayList<Socket> clients = new ArrayList<Socket>(5);

    public ConsoleChatServer(Socket socket) {
        this.socket = socket;
    }

    // 접속 후 나가버린 경우 쓸 때 오류가 발생
    // ArrayList에서 클라이언트 소켓 제거
    public void remove(Socket socket) {
        for (Socket s : clients) {
            if (socket == s) {
                clients.remove(socket);
                break;
            }
        }
    }

    // thread가 할 일
    public void run(){
        InputStream fromClient = null;
        OutputStream toClient = null;

        try{
            System.out.println(socket + ": 연결됨");
            fromClient = socket.getInputStream();

            byte[] buf = new byte[1024];
            int count;
            // client가 보낸 걸 서버가 read해서 buf에 넣고 읽은 개수를 count에 넣음
            // count가 -1, 중단시키지 않으면 계속 대기
            while((count = fromClient.read(buf)) != -1){
                for (Socket s : clients) {
                    if (socket != s) { // 현재 스레드를 상대하는 socket이 아닌 socket에 글을 보냄
                        toClient = s.getOutputStream();
                        toClient.write(buf, 0, count);
                        toClient.flush();
                    }
                }
                System.out.write(buf, 0, count);
            }
        } catch (IOException e) {
            System.out.println(socket + ": 에러(" + e + ")");
        } finally{
            try{
                if (socket != null) {
                    socket.close();
                    // 접속 후 나가버린 클라이언트는 ArrayList에서 제거해야함
                    remove(socket);
                }
                fromClient = null;
                toClient = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println(serverSocket + ": 서버 소켓 생성");

        while (true) {
            try {
                // 클라이언트가 접속할 때 accept를 하면 socket이 만들엉지는데
                Socket client = serverSocket.accept();
                // 이를 ArrayList에 저장해줌으로써
                // 하나의 상대만이 아니라 모든 상대들의 socket들을 꺼내서 글을 쓴다.
                clients.add(client);

                // thread start
                ConsoleChatServer myServer = new ConsoleChatServer(client);
                myServer.start(); // run 함수 수행
            } catch (Exception e){
                break;
            }
        }
    }

}
