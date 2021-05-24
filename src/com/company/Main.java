package com.company;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {


    public static void main(String[] args) {
        Path filePath = Paths.get("data.txt");
        Charset charset = StandardCharsets.UTF_8;

        List<ClientMultiThreaded> urls = new ArrayList<>();
        List<List<ClientMultiThreaded>> chunks = new ArrayList<>();
        CloseableHttpClient httpclient = getClient();

        try (BufferedReader bufferedReader = Files.newBufferedReader(filePath, charset)) {
            String line;
            int count = 1;
            while ((line = bufferedReader.readLine()) != null) {
                HttpPost req = getRequest(line.split(" ")[3].replace("\"", ""));
                urls.add(new ClientMultiThreaded(httpclient, req, count));
                count++;
            }
        } catch (IOException ex) {
            System.out.format("I/O error: %s%n", ex);
        }
        System.out.printf("total: %d%n", urls.size());
        urls = urls.subList(1326, urls.size() - 1);
        for (int x = 0; x < urls.size(); x += 2000){
            try {
                chunks.add(urls.subList(x, x + 2000));
            }catch (IndexOutOfBoundsException e){
                chunks.add(urls.subList(x, urls.size() - 1));
            }
        }
        for (List<ClientMultiThreaded> chunk: chunks){
            System.out.println(chunk.size());
            execute(chunk);
        }

    }

    public static CloseableHttpClient getClient(){
        //Creating the Client Connection Pool Manager by instantiating the PoolingHttpClientConnectionManager class.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        //Set the maximum number of connections in the pool
        connManager.setMaxTotal(100);
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
}
