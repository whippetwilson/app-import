package com.company;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static Path successFilePath = Paths.get("success.txt");
    public static Path failedFilePath = Paths.get("failed.txt");

    public static void main(String[] args) {
        Path filePath = Paths.get("04Outreach.txt");
        Charset charset = StandardCharsets.UTF_8;
        initLogPaths();

        List<ClientMultiThreaded> urls = new ArrayList<>();
        List<String> urlStrings = new ArrayList<>();
        List<List<ClientMultiThreaded>> chunks = new ArrayList<>();
        CloseableHttpClient httpclient = getClient();

        try (BufferedReader bufferedReader = Files.newBufferedReader(filePath, charset)) {
            String line;
            int count = 1;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    String uri = line.split(" ")[3].replace("\"", "");
                    HttpPost req = getRequest(uri);
                    urls.add(new ClientMultiThreaded(httpclient, req, count, line));
                    urlStrings.add(uri);
                }catch (Exception e){}
                count++;
            }
        } catch (IOException ex) {
            System.out.format("I/O error: %s%n", ex);
        }
        System.out.printf("original-total: %d%n", urls.size());

        List<ClientMultiThreaded> urlToRemove = new ArrayList<>();
        try (BufferedReader bufferedReader2 = Files.newBufferedReader(successFilePath, charset)) {
            String line;
            int count = 1;
            while ((line = bufferedReader2.readLine()) != null) {
                try {
                    String uri = line.split(" ")[3].replace("\"", "");
                    if (urlStrings.contains(uri)) {
                        urlToRemove.add(urls.get(urlStrings.indexOf(uri)));
                    }
                }catch (Exception e){}
                count++;
            }
        } catch (IOException ex) {
            System.out.format("I/O error: %s%n", ex);
        }
        urls.removeAll(urlToRemove);
        System.out.printf("final-total: %d%n", urls.size());

        for (int x = 0; x < urls.size(); x += 2000){
            try {
                chunks.add(urls.subList(x, x + 2000));
            }catch (IndexOutOfBoundsException e){
                chunks.add(urls.subList(x, urls.size() - 1));
            }
        }
        for (List<ClientMultiThreaded> chunk: chunks){
            execute(chunk);
        }

    }

    public static CloseableHttpClient getClient(){
        //Creating the Client Connection Pool Manager by instantiating the PoolingHttpClientConnectionManager class.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        //Set the maximum number of connections in the pool
        connManager.setMaxTotal(500);
        //Create a ClientBuilder Object by setting the connection manager
        HttpClientBuilder clientbuilder = HttpClients.custom().setConnectionManager(connManager);
        //Build the CloseableHttpClient object using the build() method.
        return clientbuilder.build();
    }

    public static void execute(List<ClientMultiThreaded> urls){
        for (ClientMultiThreaded url: urls) {
            url.start();
        }

        for (ClientMultiThreaded url: urls) {
            try {
                url.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static HttpPost getRequest(String url){
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic SGFybXNvbjpIYXJtc29uQDEyMw==");
        return httpPost;
    }

    public static void log(Path path, String uri){
        try {
            FileWriter myWriter = new FileWriter(path.toFile(),true);
            myWriter.write(MessageFormat.format("{0}\n", uri));
            myWriter.close();
        } catch (IOException ignored) {
        }
    }

    public static void initLogPaths(){
        try {
            successFilePath.toFile().createNewFile();
            failedFilePath.toFile().createNewFile();
        } catch (IOException e) {
        }
    }
}
