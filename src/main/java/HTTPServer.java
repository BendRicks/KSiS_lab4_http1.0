import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OptionalDataException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

public class HTTPServer {

    public static final String FILES_DIRECTORY = "C:\\WebData";
    public static final String GET_REGEXP = "(GET).+";
    public static final String POST_REGEXP = "(POST).+";

    public static void main(String[] args) {
        try {
            ServerSocket httpSocket = new ServerSocket(80);
            while (true) {
                Socket userSocket = httpSocket.accept();
                new Thread(() -> {
                    try {
                        BufferedReader bis = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
                        while (!userSocket.isClosed()) {
                            ArrayList<String> strings = new ArrayList<>();
                            while (bis.ready()){
                                strings.add(bis.readLine());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
