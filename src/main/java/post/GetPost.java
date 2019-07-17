package post;

import bd.Connect_bd;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class GetPost {

    private static HttpClient httpClient = new DefaultHttpClient();

    /**
     * @param args
     */

    public static void main(String[] args) {

        GetPost fixture = new GetPost();

        try {
            fixture.doGet();
            /*fixture.doPost();*/
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }





    private void doGet() throws ClientProtocolException, IOException, SQLException {

        /* create the HTTP client and GET request */
        HttpGet httpGet = new HttpGet("https://liwest.ru/partners-app/all_partners_to_xml.php?get_partner=no_transfer&pass=GjKeXbNm;");

        httpGet.addHeader("accept", "application/json");
        httpGet.addHeader("Host", "api.acme.com");
        httpGet.addHeader("X-Api-Version", "1.0");
        httpGet.addHeader("Authorization", "Basic ...");;
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();

        String json = IOUtils.toString(httpResponse.getEntity().getContent());
        JSONArray array = new JSONArray(json);
        System.out.println(array);

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            System.out.println(object.getInt("site_id"));
            System.out.println(object.getString("name"));
            //System.out.println(object.getBoolean("sponsor"));
        }
        /* execute request */
        Connection connbd = +
                new Connection();
        connbd.nativeSQL("select * from statment");
        /* process response */
        if (httpResponse.getStatusLine().getStatusCode() == 200) {


        } else {
            System.err.println("Invalid HTTP response: "
                    + httpResponse.getStatusLine().getStatusCode());
        }

    }



}
