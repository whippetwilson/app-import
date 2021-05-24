package com.company;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class ClientMultiThreaded extends Thread {
    CloseableHttpClient httpClient;
    HttpPost httpPost;
    int id;

    public ClientMultiThreaded(CloseableHttpClient httpClient, HttpPost httpPost, int id) {
        this.httpClient = httpClient;
        this.httpPost = httpPost;
        this.id = id;
    }

    @Override
    public void run() {
        try{
            //Executing the request
            CloseableHttpResponse httpresponse = httpClient.execute(httpPost);
            //Displaying the status of the request.
            System.out.println("status of thread "+id+":"+httpresponse.getStatusLine());
            //Retrieving the HttpEntity and displaying the no.of bytes read
            HttpEntity entity = httpresponse.getEntity();
            if (entity != null) {
                System.out.printf("Bytes read by thread thread %d:%d%n", id, EntityUtils.toByteArray(entity).length);
                System.out.println(httpresponse.getStatusLine());
            }
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}