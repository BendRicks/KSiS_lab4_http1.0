import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class HTTPClientHandlerThread implements Runnable {

    public static final String FILES_DIRECTORY = "C:\\WebData";
    private final Socket userSocket;
    private BufferedReader bis;
    private BufferedWriter bos;
    private final SimpleDateFormat dateFormat;
    private final Calendar calendar;

    public HTTPClientHandlerThread(Socket socket) throws IOException {
        userSocket = socket;
        bis = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
        bos = new BufferedWriter(new OutputStreamWriter(userSocket.getOutputStream()));
        dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar = Calendar.getInstance();
    }

    @Override
    public void run() {
//        try {
//            userSocket.setSoTimeout(1000);
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
        try {
            userSocket.setKeepAlive(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (!userSocket.isClosed()) {
            final ArrayList<String> strings = new ArrayList<>();
            try {
                boolean isPost = false;
                while (bis.ready()) {
                    if (strings.size() == 1){
                        String[] commandParts = strings.get(0).split(" ");
                        if (commandParts[0].equals("POST")){
                            isPost = true;
                        }
                    }
                    String str = bis.readLine();
                    strings.add(str);
                    if (isPost && str.equals("")){
                        StringBuilder stringBuilder = new StringBuilder();
                        while (bis.ready()){
                            char[] buff = new char[1024];
                            int read = bis.read(buff);
                            stringBuilder.append(Arrays.copyOfRange(buff, 0, read));
                        }
                        strings.add(stringBuilder.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!strings.isEmpty() && strings.get(0) != null) {
                String[] commandParts = strings.get(0).split(" ");
                if (commandParts[0].equals("GET")) {
                    try {
                        File requestedFile = new File(FILES_DIRECTORY + commandParts[1]);
                        var fis = new BufferedReader(new FileReader(requestedFile));
                        StringBuilder httpRespond = new StringBuilder("HTTP/1.1 200 OK\r\n");
                        httpRespond.append("Date: ").append(dateFormat.format(calendar.getTime())).append("\r\n");
                        httpRespond.append("Server: Kartoha/1.0\r\n");
                        if (strings.get(2).equals("Connection: keep-alive")) {
                            httpRespond.append("Connection: keep-alive\r\n");
                        }
                        String str1 = getExtensionByStringHandling(requestedFile.getName()).get();
                        if (str1.equals("ttf")){
                            httpRespond.append("Content-type: ").append("font/ttf").append("\r\n");
                        } else {
                            httpRespond.append("Content-type: ").append(Files.probeContentType(requestedFile.toPath())).append("\r\n");
                        }
                        httpRespond.append("Content-length: ").append(requestedFile.length()).append("\r\n");
                        httpRespond.append("Expires: Sat, 01 Jan 2023 00:59:59 GMT\r\n");
                        httpRespond.append("Last-modified: ").append(dateFormat.format(requestedFile.lastModified())).append("\r\n\r\n");
                        String str = httpRespond.toString();
                        bos.write(str);
                        char[] buffer = new char[8192];
                        int size = 0;
                        int bytesAmount;
                        while ((bytesAmount = fis.read(buffer)) != -1) {
                            StringBuilder responceContent = new StringBuilder();
                            char[] gotBytes = Arrays.copyOfRange(buffer, 0, bytesAmount);
                            for (int i = 0; i < bytesAmount; i++) {
                                responceContent.append(gotBytes[i]);
                            }
                            bos.write(responceContent.toString());
                        }
                        bos.flush();
                        fis.close();
                        if (strings.get(2).equals("Connection: close")) {
                            bos.close();
                            bis.close();
                            userSocket.close();
                        }
                    } catch (FileNotFoundException ex) {
                        try {
                            StringBuilder httpResponce = new StringBuilder("HTTP/1.1 404 Resource not found\r\n");
                            httpResponce.append("Date: ").append(dateFormat.format(calendar.getTime())).append("\r\n");
                            httpResponce.append("Server: Kartoshka/1.0\r\n");
                            httpResponce.append("\r\n");
                            bos.write(httpResponce.toString());
                            bos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (commandParts[0].equals("HEAD")) {
                    try {
                        File requestedFile = new File(FILES_DIRECTORY + commandParts[1]);
                        StringBuilder httpRespond = new StringBuilder("HTTP/1.1 200 OK\r\n");
                        httpRespond.append("Date: ").append(dateFormat.format(calendar.getTime())).append("\r\n");
                        httpRespond.append("Server: Kartoha/1.0\r\n");
                        if (strings.get(2).equals("Connection: keep-alive")) {
                            httpRespond.append("Connection: keep-alive\r\n");
                        }
                        String str1 = getExtensionByStringHandling(requestedFile.getName()).get();
                        if (str1.equals("ttf")){
                            httpRespond.append("Content-type: ").append("font/ttf").append("\r\n");
                        } else {
                            httpRespond.append("Content-type: ").append(Files.probeContentType(requestedFile.toPath())).append("\r\n");
                        }
                        httpRespond.append("Content-length: ").append(requestedFile.length()).append("\r\n");
                        httpRespond.append("Expires: Sat, 01 Jan 2023 00:59:59 GMT\r\n");
                        httpRespond.append("Last-modified: ").append(dateFormat.format(requestedFile.lastModified())).append("\r\n\r\n");
                        String str = httpRespond.toString();
                        bos.write(str);
                        bos.flush();
                        if (strings.get(2).equals("Connection: close")) {
                            bos.close();
                            bis.close();
                            userSocket.close();
                        }
                    } catch (FileNotFoundException ex) {
                        try {
                            StringBuilder httpResponce = new StringBuilder("HTTP/1.1 404 Resource not found\r\n");
                            httpResponce.append("Date: ").append(dateFormat.format(calendar.getTime())).append("\r\n");
                            httpResponce.append("Server: Kartoshka/1.0\r\n");
                            bos.write(httpResponce.toString());
                            bos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (commandParts[0].equals("POST")) {
                    int counter = 0;
                    while (!strings.get(counter).equals("")){
                        counter++;
                    }
                    ArrayList<String> postData = new ArrayList<>();
                    while (counter < strings.size()){
                        postData.add(strings.get(counter));
                        counter++;
                    }
                    try {
                        bos.write("HTTP/1.1 200 OK\r\n");
                        bos.write("Content-Length: 14\r\n");
                        bos.write("Content-Type: text/html\r\n");
                        bos.write("\r\n");
                        bos.write("<p>Success</p>");
                        bos.close();
                        if (strings.get(2).equals("Connection: close")) {
                            bos.close();
                            bis.close();
                            userSocket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                //Код ошибки
            }
        }

    }

    public Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
}