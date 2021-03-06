package com.bencini.itismeucci;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class App implements Runnable {
    static final File WEB_ROOT = new File("./src/main/resources");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "./pages/404.html";
    static final String METHOD_NOT_SUPPORTED = "./pages/not_supported.html";
    String XMLclasse = "src/main/resources/classe.xml";
    String JSONclasse = "src/main/resources/puntiVendita.json";
    File fileXML = new File(XMLclasse);
    File fileJSON = new File(JSONclasse);
    XmlMapper xmlMapper = new XmlMapper();
    ObjectMapper objectMapper = new ObjectMapper();

    static final int PORT = 8080;

    static final boolean verbose = true;

    private Socket connect;

    public App(Socket c) {
        connect = c;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            while (true) {
                App myServer = new App(serverConnect.accept());

                if (verbose) {
                    System.out.println("Connecton opened. (" + new Date() + ")");
                }

                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }

    }

    public root xmlDeserializer() throws IOException {
        root value = xmlMapper.readValue(fileXML, root.class);

        for (int i = 0; i < value.getStudenti().size(); i++) {
            System.out.println("-" + value.getStudenti().get(i).getCognome());
        }
        return value;
    }

    public String jsonSerializer() throws IOException {
        root value = xmlDeserializer();
        return objectMapper.writeValueAsString(value);
    }

    public root jsonDeserializer() throws IOException {
        root value = objectMapper.readValue(fileJSON, root.class);
        return value;
    }

    public String xmlSerializer() throws IOException {
        root value = jsonDeserializer();
        return objectMapper.writeValueAsString(value);
    }

    @Override
    public void run() {

        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {

            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));

            out = new PrintWriter(connect.getOutputStream());

            dataOut = new BufferedOutputStream(connect.getOutputStream());

            String input = in.readLine();

            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase();

            fileRequested = parse.nextToken().toLowerCase();

            if (!method.equals("GET") && !method.equals("HEAD")) {
                if (verbose) {
                    System.out.println("501 Not Implemented : " + method + " method.");
                }

                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";

                byte[] fileData = readFileData(file, fileLength);

                out.println("HTTP/1.1 501 Not Implemented");
                out.println("Server: Java HTTP Server from SSaurel : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + contentMimeType);
                out.println("Content-length: " + fileLength);
                out.println();
                out.flush();

                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();

            } else {
                boolean transfer = false;
                if (fileRequested.endsWith("/")) {
                    fileRequested += DEFAULT_FILE;
                }

                if (method.equals("GET")) {

                    if (transfer) {
                        out.println("HTTP/1.1 301 Resource Transfered");
                        out.println("Location: http://localhost:" + PORT);
                    }


                    byte[] fileData = null;
                    Integer fileLength = null;

                    String content = getContentType(fileRequested);

                    if (fileRequested.endsWith("classe.json")) {
                        String s = jsonSerializer();
                        fileData = s.getBytes();
                        fileLength = s.length();
                    } else if(fileRequested.endsWith("puntivendita.xml")){
                        String s = xmlSerializer();
                        fileData = s.getBytes();
                        fileLength = s.length();
                    } else {

                        // leggo file da file system
                        File file = new File(WEB_ROOT, fileRequested);
                        fileLength = (int) file.length();

                        fileData = readFileData(file, fileLength);

                        // fileNotFound(out, dataOut, fileRequested);
                    }



                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Java HTTP Server from SSaurel : 1.0");
                    out.println("Date: " + new Date());
                    out.println("Content-type: " + content);
                    out.println("Content-length: " + fileLength);
                    out.println();
                    out.flush();
                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();

                }

                if (verbose) {
                    String content = getContentType(fileRequested);

                    System.out.println("input:" + input + "\n");
                    System.out.println("File " + fileRequested + " of type " + content + " returned");
                }

            }

        } catch (FileNotFoundException fnfe) {

            try {
                fileNotFound(out, dataOut, fileRequested);
            } catch (IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close();
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }

    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        String[] divisa = fileRequested.split("\\.");
        String fine = divisa[divisa.length - 1];

        switch (fine) {
            case "htm":
                return "text/html";

            case "html":
                return "text/html";

            case "png":
                return "image/png";

            case "jpg":
                return "image/jpg";

            case "jpeg":
                return "image/jpeg";

            case "gif":
                return "image/gif";

            case "css":
                return "text/css";

            case "js":
                return "text/javascript";

            case "xml":
                return "application/xml";

            case "json":
                return "application/json";

            default:
                return "text/plain";
        }
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server from SSaurel : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println();
        out.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (verbose) {
            System.out.println("File " + fileRequested + " not found");
        }
    }

}