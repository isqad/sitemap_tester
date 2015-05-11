package ru.cohab;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.Socket;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.regex.*;


/**
 * Реализует взаимодействие с http-сервером
 * Умеет обходить nginx-мухобойки
 */
public class HttpClient
{
    /**
     * Url ресурса
     */
    private URL url;
    private Socket socket;
    private String statusCode;
    private String path = "/";
    private String cookie;

    private int numRedirects = 0;

    public static final String SET_COOKIE_PATTERN = "Set-Cookie:\\s([^\\r\\n]+)";
    public static final String HTTP_HEADER_STATUS_PATTERN = "^HTTP/\\d.\\d\\s(\\d{3})";
    public static final String LOCATION_PATTERN = "Location:\\s([^\\r\\n]+)";
    public static final String REDIRECT_STATUS_PATTERN = "^30\\d$";

    /**
     * Создает соединение с сервером по url
     */
    public HttpClient(String url) throws MalformedURLException, UnknownHostException, IOException
    {
        this.url = new URL(url);
        // Открываем сокет
        socket = new Socket(this.url.getHost(), 80);
    }

    /**
     * Получить http код статуса
     */
    public String getStatus() throws MalformedURLException, UnknownHostException, IOException
    {
        String response = null;

        sendRequest(socket.getOutputStream());

        response = getResponse();

        extractStatusCode(response);

        checkAndFollowRedirect(response);

        return statusCode;
    }

    private void checkAndFollowRedirect(String response) throws MalformedURLException, UnknownHostException, IOException
    {
        if (numRedirects > 3)
        {
            statusCode = "508";
            return;
        }

        if (statusCode.matches(REDIRECT_STATUS_PATTERN))
        {
            numRedirects++;

            followRedirect(response);

            response = getResponse();

            extractStatusCode(response);

            checkAndFollowRedirect(response);
        } else {
            return;
        }
    }

    private String getHeader()
    {
        if (url.getPath().length() > 0)
        {
            path = url.getPath();
        }

        if (url.getQuery() != null && url.getQuery().length() > 0)
        {
            path += "?" + url.getQuery();
        }

        String header = "HEAD " + path + " HTTP/1.0\r\n";
        header += "Host: " + url.getHost() + "\r\n";
        header += "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
        header += "Accept-Language: ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3\r\n";
        header += "Connection: close\r\n";
        header += "User-Agent: Mozilla/5.0 (compatible; YandexBot/3.0)\r\n";
        if (cookie != null)
        {
            header += "Cookie: " + cookie;
        }
        header += "\r\n\r\n\r\n\r\n";

        return header;
    }

    private void sendRequest(OutputStream stream) throws IOException
    {
        stream.write(getHeader().getBytes());
    }

    private String getResponse() throws IOException
    {
        InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        StringBuffer stringBuffer = new StringBuffer();

        int character = bufferedReader.read();

        while (character != -1)
        {
            stringBuffer.append((char) character);
            character = bufferedReader.read();
        }

        socket.close();

        return stringBuffer.toString();
    }

    private void followRedirect(String response) throws MalformedURLException, UnknownHostException, IOException
    {
        extractCookie(response);
        // Поиск cookie и Location
        String location = null;

        Pattern pattern = Pattern.compile(LOCATION_PATTERN);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find())
        {
            location = matcher.group(1);

            path = "/";

            url = new URL(location);

            socket = new Socket(url.getHost(), 80);

            sendRequest(socket.getOutputStream());
        }
    }

    private void extractCookie(String response)
    {
        Pattern pattern = Pattern.compile(SET_COOKIE_PATTERN);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find())
        {
            cookie = matcher.group(1);
        }
    }

    private void extractStatusCode(String response)
    {
        Pattern pattern = Pattern.compile(HTTP_HEADER_STATUS_PATTERN);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find())
        {
            statusCode = matcher.group(1);
        }
    }
}
