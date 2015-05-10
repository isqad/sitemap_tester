package ru.cohab;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.io.IOException;

/**
 * Реализует приложение
 *
 */
public class App
{
    public static void main(String[] args) throws MalformedURLException, UnknownHostException, IOException
    {
        HttpClient client = new HttpClient("http://www.habrahabr.ru");

        System.out.println("Http host: " + client.getStatus());
    }
}
