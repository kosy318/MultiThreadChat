package chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

// 서버에서 보내오는 값을 받기 위한 Thread
class ServerHandler extends Thread{
    Socket socket = null;

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    public void run(){
        InputStream fromServer = null;

        try {
            fromServer = socket.getInputStream();

            byte[] buf = new byte[1024];
            int count;

            // 종료하지 않으면 서버로부터 읽어들인것을 적음
            while ((count = fromServer.read(buf)) != -1) {
                System.out.write(buf, 0, count);
            }
        } catch (IOException e) {
            System.out.println("연결 종료 (" + e + ")");
        } finally{
            try {
                if (fromServer != null) {
                    fromServer.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ignored) {
            }
        }
    }
}

public class ConsoleChatClient {

    public static void main(String[] args) {

        try (Socket socket = new Socket("localhost", 9999)) {
            // 서버와 연결
            System.out.println(socket + ": 연결됨");

            OutputStream toServer = socket.getOutputStream(); // 클라이언트가 서버에 글을 쓰기 위함

            // 서버에서 보내오는 값을 받기위한 쓰레드가 따로 필요함
            // 이렇게 하지 않으면 글을 쓰고 있을 때 상대방이 던지는 메시지를 리얼타임으로 받지 못하고 글을 다 쓴 후 받게 됨
            ServerHandler handler = new ServerHandler(socket);
            handler.start();

            byte[] buf = new byte[1024];
            int count;

            // 클라이언트로부터 키보드로 입력 들어옴
            // 키보드 입력을 바이트 단위로 읽어 buf 라는 바이트배열에 기록 후 읽은 바이트수를 리턴
            // 읽을것이 없는 경우 read 메소드는 대기하고 Ctrl + C등 눌러 종료하면 -1이 리턴한다.
            // 스트림의 끝인 경우 -1을 리턴한다.
            while ((count = System.in.read(buf)) != -1) {
                // 콘솔에서 읽은 값을 서버에 보냄
                toServer.write(buf, 0, count);
                toServer.flush();
            }
        } catch (IOException e) {
            System.out.println("연결 종료 (" + e + ")");
        }
    }

}
