package connect;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Getpostsite {

    private static HttpURLConnection connection=null;
    private static JSONArray  array_Json = new JSONArray("[{}]");

    public static JSONArray postFromSiteToJSON(String urlString) {

        StringBuilder sb = new StringBuilder();
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setConnectTimeout(250);
            connection.setReadTimeout(250);
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode())
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null)
                {
                    sb.append(line);
                }
            }
            else {
                System.out.println("Нет соедениения с сайтом https://liwest.ru/");
            }
        } catch (Throwable cause) {
            cause.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        try {
            System.out.println(sb);
            array_Json= new JSONArray(sb.toString());

        } catch (JSONException e) {
            System.out.println("JSON Parser Error parsing data "+e.toString() );
            array_Json= new JSONArray("[{},{}]");
       }
          return array_Json;
    }

    public static void postToSite(String urlString) {

        connection = null;
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setConnectTimeout(250);
            connection.setReadTimeout(250);
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) System.out.println(urlString);
        } catch (Throwable cause) {
            cause.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
