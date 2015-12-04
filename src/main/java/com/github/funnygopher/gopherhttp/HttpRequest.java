package com.github.funnygopher.gopherhttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class HttpRequest {

    public static final int GET = 0;
    public static final int POST = 1;

    private int requestType;
    private String address;
    private URL url;
    private HttpURLConnection conn;
    private String parameters;
    private OutputStream out;

    public HttpRequest(int requestType, String address) throws IOException {
        this.requestType = requestType;
        this.address = address;
    }

    public HttpRequest withParameters(Map<String, String> parameterMap) throws IOException {
        StringBuffer params = new StringBuffer();

        boolean first = true;
        for (String key : parameterMap.keySet()) {
            if(!first)
                params.append("&");
            params.append(key + "=" + URLEncoder.encode(parameterMap.get(key), "UTF-8"));

            if(first)
                first = false;
        }

        parameters = params.toString();
        return this;
    }

    public HttpRequest withString(String string) throws IOException {
        parameters = string;
        return this;
    }

    public String send() throws IOException {
        if(requestType == GET) {
            prepareGet();
        }

        if(requestType == POST) {
            preparePost();
            conn.setFixedLengthStreamingMode(parameters.getBytes("UTF-8").length);
            out = conn.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(parameters);
            writer.flush();
            writer.close();
        }

        String response = getResponse();
        conn.disconnect();
        return response;

    }

    private void prepareGet() throws IOException {
        url = new URL(address + "?" + parameters);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        prepare();
    }

    private void preparePost() throws IOException {
        try {
            url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepare() {
        conn.setDoInput(true);
        conn.setReadTimeout(1000);
        conn.setConnectTimeout(1000);
    }

    private String getResponse() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String inputLine;
        StringBuffer response = new StringBuffer();
        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}
