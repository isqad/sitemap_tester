package ru.cohab;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.Socket;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;


/**
 * Реализует взаимодействие с http-сервером
 */
public class HttpClient
{
    /**
     * Url ресурса
     */
    private URL url;

    /**
     * Создает соединение с сервером по url
     */
    public HttpClient(String url) throws MalformedURLException
    {
        this.url = new URL(url);
    }

    /**
     * Получить http код статуса
     */
    public String getStatus() throws UnknownHostException, IOException
    {
        String response = null;

        Socket socket = new Socket(this.url.getHost(), 80);
        OutputStream stream = socket.getOutputStream();
        System.out.write(getHeader().getBytes());
        stream.write(getHeader().getBytes());

        InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        StringBuffer stringBuffer = new StringBuffer();

        String lineResp = null;
        while ((lineResp = bufferedReader.readLine()) != null)
        {
            stringBuffer.append(lineResp);
        }
        response = stringBuffer.toString();

        socket.close();

        return response;
    }

    private String getHeader()
    {
        String path = "/";
        if (this.url.getPath().length() > 0)
        {
            path = this.url.getPath();
        }
        String header = "GET " + path + " HTTP/1.1\r\n";
        header += "Host: " + this.url.getHost() + "\r\n";
        header += "Accept: text/html\r\n";
        header += "Connection: close\r\n";
        header += "User-Agent: Mozilla/5.0 (Windows NT 6.1; rv:37.0) Gecko/20100101 Firefox/37.0\r\n";
        header += "\r\n\r\n\r\n\r\n";

        return header;
    }
}