import java.lang.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.net.URLDecoder;

import org.apache.commons.io.IOUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Psiral
 * Date: 2/19/13
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class httpServer {

    private static int port;
    private static String directory;
    private static OutputStream nonEncodedOut;
    private static String contentHead = "<html><head><title>Test Server</title></head><body>";
    private static String contentFoot = "</body></html>";

    public static void main(String[] args) throws Exception {
        port = Integer.parseInt(args[1]);
        directory = args[3];
        System.setProperty("user.dir", directory);
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is on port : " + port);
        System.out.println("Server is in directory: " + directory);


        while (true) {
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            nonEncodedOut = clientSocket.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(nonEncodedOut));

            handleRequest(in, out);

            clientSocket.close();
        }
    }

    public static void handleRequest(BufferedReader in, BufferedWriter out) throws Exception {
        String trimmedRequest = null;
        trimmedRequest = getFirstHeader(in, trimmedRequest);
        if(trimmedRequest == null) {
           print404(out);
        } else {
            if (trimmedRequest.length() == 0) {
                trimmedRequest = ".";
            }

            File file = new File(directory + trimmedRequest);
            boolean  fileExists = file.exists();

            if (fileExists) {
                if (file.isDirectory()) {
                    print200WithLinks(out);
                } else {
                    print200WithFile(out, file);
                }
            } else if (trimmedRequest.equals("/redirect")) {
                print307Redirect(out);
            } else if (trimmedRequest.equals("/form")) {
                print200(out);
            } else if (trimmedRequest.contains("?")){
                print200WithVariablies(out, trimmedRequest);
            } else {
                print404(out);
            }
        }

        out.close();
        in.close();
    }

    private static void print200WithVariablies(BufferedWriter out, String trimmedRequest) throws Exception {
        output200OKHeader(out);
        out.write(contentHead);
        String[] parsedRequest = parseVariable(trimmedRequest);
        for (int i = 0; i < parsedRequest.length; i++){
            out.write("<p>" + parsedRequest[i] + "</p>");
        }
        out.write(contentFoot);
    }

    private static String[] parseVariable(String trimmedRequest) throws Exception {
        String regex = "\\?";
        String[] requestArray;
        String[] parametersArray;

        requestArray = trimmedRequest.split(regex);
        parametersArray = requestArray[1].split("&");
        if (parametersArray[0].contains("%")) {
            parametersArray[0] = URLDecoder.decode(parametersArray[0], "UTF-8");
        }
        return parametersArray;
    }

    private static void output200OKHeader(BufferedWriter out) throws IOException {
        out.write("HTTP/1.1 200/OK\r\nContent-type:text/html\r\n\r\n");
    }

    private static void print200(BufferedWriter out) throws Exception {
        output200OKHeader(out);
    }

    private static String getFirstHeader(BufferedReader in, String trimmedRequest) throws IOException {
        String s = in.readLine();
            if ( s != null){
                trimmedRequest = requestHandler(s);
            }
        return trimmedRequest;
    }

    private static void print404(BufferedWriter out) throws Exception {
        out.write("HTTP/1.1 404/Object Not Found\r\n\r\n");
        out.write("404 - Resource cannot be found.");
    }

    private static void print200WithFile(BufferedWriter out, File file) throws Exception {
        String contentType = getContentType(file);
        out.write("HTTP/1.1 200/OK\r\nContent-type: " + contentType + "\r\n\r\n");
        out.write(contentHead);
        readFiles(out, file);
        out.write(contentFoot);
    }

    private static String getContentType(File file) {
        String extension = URLConnection.guessContentTypeFromName(file.getAbsolutePath());
        if (extension == null) {
            extension = "text/html";
        }
        return extension;
    }

    public static boolean stringContainsItemsFromList(String inputString) {
        String[] items = {".jpeg", ".gif", ".jpg", ".bmp", ".png"};
        for (int i = 0; i < items.length; i++) {
            if(inputString.contains(items[i])){
                return true;
            }
        }
        return false;
    }

    public static String getFileName(File file) {
        return file.getName();
    }


    private static void readFiles(BufferedWriter out, File file) throws IOException {
        BufferedReader br = null;
        String fileName = getFileName(file);

        if (stringContainsItemsFromList(fileName)) {
            outPutImage(file);
        } else {
            outPutNonImage(out, file, br);
        }
    }

    private static void outPutImage(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(is);
        nonEncodedOut.write(bytes);
    }

    private static void outPutNonImage(BufferedWriter out, File file, BufferedReader br) {
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(file));
            while ((sCurrentLine = br.readLine()) != null) {
                out.write(sCurrentLine);
           }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void print200WithLinks(BufferedWriter out) throws Exception {
        output200OKHeader(out);
        out.write(contentHead);
        outputFilesInDirectory(out);
        out.write(contentFoot);
    }

    private static void print307Redirect(BufferedWriter out) throws Exception {
        out.write("HTTP/1.1 307/Temporary Redirect\r\nLocation: http://localhost:" + port + "/\r\n\r\n");
    }

    private static void outputFilesInDirectory(BufferedWriter out) throws Exception{
        File folder = new File (directory);
        String files;
        File[] listOfFiles = folder.listFiles();


        assert listOfFiles != null;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                files = listOfFile.getName();
                out.write("<a href='/./" + files + "'>" + files + "</a><br />");
            }
        }
    }

    public static String requestHandler(String request) {
        String REGEX = "\\s";
        if (request.contains("GET") || request.contains("POST") || request.contains("PUT")) {
            String[] formattedRequest = request.split(REGEX);
            request = formattedRequest[1];
        }
        return request;
    }
}