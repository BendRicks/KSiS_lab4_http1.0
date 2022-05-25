import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPServer {

    public static final String FILES_DIRECTORY = "C:\\WebData";

    public static void main(String[] args) {
        try (ServerSocket httpSocket = new ServerSocket(2517)) {
            while (!httpSocket.isClosed()) {
                Socket userSocket = httpSocket.accept();
                new Thread(new HTTPClientHandlerThread(userSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
