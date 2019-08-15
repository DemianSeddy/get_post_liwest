package post;

import bd.Connect_bd;
import connect.*;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.http.client.ClientProtocolException;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetPost {

    /**
     * По запросу
     * "https://liwest.ru/partners-app/all_partners_to_xml.php?get_partner=no_transfer&pass=GjKeXbNm;"
     * получить JSON и проверить в BD (Фамилию(LASTNAME),
     * Имя(FIRSTNAME),
     * Отчество(MIDDLENAME),
     * дату рождения(BIRTHDATE),
     * Спонсора(SOURCESPONSOR),
     * email(EMAIL)
     * и Дату удаления(DELETEDATE)
     * Если Дата удаления не NULL и данные есть,
     * то ID_BITRIX отправить на сайт,
     * иначе породить записи в TABLE DISTRIBUTORS и в OBJECTS (BRANCH, ID)
     * отправить ID_BITRIX отправить на сайт)
     */


    public static  void main(String[] args) throws ClientProtocolException, IOException, ClassNotFoundException, SQLException {

        /**Запрос JSON*/
        JSONArray array = Getpostsite.postFromSiteToJSON("https://liwest.ru/partners-app/all_partners_to_xml.php?get_partner=no_transfer&pass=GjKeXbNm;");
        /**Получили массив данных для обработки [{"site_id":"14588","name":"Бельская Ирина Васильевна","sponsor":"РАШИДОВА ЕЛЕНА ЮЛДАШЕВНА","sponsor_code":"888028072","birthday":"25.05.1978","address":"Сургут Ленина, 38-51","email":"irinavi@yandex.ru","phone":"+79227772136","isc_address":"СУРГУТ г. Сургут, ул.Быстринская, д.8, оф.7, Торгово-офисный центр &quot;Быстринский&quot;, второй этаж","flag":"N"},
         * {"site_id":"14676","name":"Фаткиева Лидия  Павловна","sponsor":null,"sponsor_code":"777088956","birthday":"27.09.1983","address":"Город Уссурийский  Некрасова ","email":"Lapsina_l@mail.ru","phone":"+79147251132","isc_address":"ВЛАДИВОСТОК ул. Светланская, д.9, 2 этаж (вход со двора)","flag":"N"}]*/

        String DB_DRIVER="org.firebirdsql.jdbc.FBDriver";
        String DB_URL="jdbc:firebirdsql:localhost:E:\\runliwest\\base\\30062019\\LIWEST.FDB";
        Connection connFire = null;
        //connFire= (Connection) new Connect_bd();


        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("connectToDB: Firebird JCA-JDBC драйвер не найден");
        }

        try {
            Properties paramConnection = new Properties();
            paramConnection.setProperty("user", "SYSDBA");
            paramConnection.setProperty("password", "masterkey");
            paramConnection.setProperty("encoding", "WIN1251");
            connFire = DriverManager.getConnection(DB_URL, paramConnection);
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            System.out.println(object.getInt("site_id"));
            System.out.println(object.getString("birthday"));
            System.out.println(object.getString("sponsor_code"));
            //System.out.println(object.getString(""));
            /**Разбивка name на фамилию имя отчество
             * и заливли в массив*/
            String[] lastFirstMiddle= new String[3];
            int number=0;
            StringTokenizer st = new StringTokenizer(object.getString("name").toString(), " ");
            while(st.hasMoreTokens()){
                  lastFirstMiddle[number]= st.nextToken();
                  number++;
            }
            //System.out.println( Arrays.toString(lastFirstMiddle));
            /**окончание заливки*/
            /**Формирование запроса
             * */
             StringBuilder sqlInDistributors= new StringBuilder();
             sqlInDistributors.append("select id,code from distributors where deletedate is null ");
             sqlInDistributors.append(" and upper(lastname)=upper(trim(").append("\'").append(lastFirstMiddle[0].toString()).append("\'").append("))");
             sqlInDistributors.append(" and upper(firstname)=upper(trim(").append("\'").append(lastFirstMiddle[1].toString()).append("\'").append("))");
             sqlInDistributors.append(" and upper(middlename)=upper(trim(").append("\'").append(lastFirstMiddle[2].toString()).append("\'").append("))");
             sqlInDistributors.append(" and sourcesponsor in (select id from distributors where code=").append("\'").append(object.getString("sponsor_code").toString()).append("\')");
             sqlInDistributors.append(" and birthdate=").append("\'").append(object.getString("birthday").toString()).append("\'");
             //System.out.println(sqlInDistributors.toString());

            String code="";
             Statement stm= connFire.createStatement();
             ResultSet res=stm.executeQuery(sqlInDistributors.toString());
             int count=0;
             int id=0;
             while (res.next()) {
                   count++;
                   id=res.getInt("id");
                   code=res.getString("code");
             }

             if (id>0) {
                 /**Если записи есть то пометим их в bitrix*/
                 /**Getpostsite.postToSite(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(code).append("&pass=PfUhE;Ty;").toString());*/
             } else {
                 /*Добавить в базу и отправить на сайт object.getInt("site_id");*/
                 System.out.println("Добавлять");

                 /**Код спонсора*/
                 count = 0;
                 int codeId = 0;

                 StringBuilder sqlIdCode = new StringBuilder("select id from distributors where code = ").append("\'").append(object.getString("sponsor_code").toString()).append("\'");
                 Statement stmId = connFire.createStatement();
                 ResultSet resId = stmId.executeQuery(sqlIdCode.toString());
                 //System.out.println(sqlIdCode);
                 count = 0;
                 while (resId.next()) {
                     count++;
                     codeId = resId.getInt("id");
                 }

                 //System.out.println(sqlMaxCode.toString());
                 if (connFire != null) {

                     /**Код договора*/
                     count= 0;
                     String newCode= " ";
                     String prexixDogovora ="555%";

                     Long date= System.currentTimeMillis();

                     StringBuilder sqlMaxCode = new StringBuilder("select max(code) code from distributors where code like ").append("\'").append(prexixDogovora).append("\'");
                     Statement stm555 = connFire.createStatement();
                     ResultSet resMaxCode = stm555.executeQuery(sqlMaxCode.toString());
                     BigInteger bigCode = BigInteger.valueOf(0);
                     //System.out.println(sqlMaxCode.toString());
                     while (resMaxCode.next()) {
                         count++;
                         newCode = resMaxCode.getString("code");
                         bigCode = (BigInteger) BigInteger.valueOf(Long.parseLong(newCode));
                     }
                     /*System.out.println("555000001");*/
                     if (count == 0) newCode = "555000001";
                     //System.out.println(bigCode.add(BigInteger.ONE));
                     newCode = bigCode.add(BigInteger.ONE).toString();

                     /**gen_id(DISTRIBUTOR_gen, 1), 100*/
                     StringBuilder objectsInsert = new StringBuilder("INSERT INTO OBJECTS (BRANCH, ID, OBJ_TYPE, NAME) VALUES ");
                     objectsInsert.append("(").append("999").append(",").append("gen_id(DISTRIBUTOR_gen, 1)").append(",100").append(",\' \'").append(")");

                     Statement stmObjectsInsert = connFire.createStatement();
                     boolean resstmObjectsInsert = stmObjectsInsert.execute(objectsInsert.toString());

                     System.out.println(objectsInsert);
                     /*gen_id(DISTRIBUTOR_gen, 0),'Фаткиева','Лидия','Павловна','555000001','','','27.09.1983','','','','','','','13.08.2019',1,null,777088956,null,null,1,999,null,'','Lapsina_l@mail.ru'*/
                     StringBuilder distributorsInsert = new StringBuilder("INSERT INTO DISTRIBUTORS (id, lastname, firstname, middlename, code, phone1, phone2,");
                     distributorsInsert.append("BIRTHDATE, ZIPCODE, COUNTRY, REGION, CITY, ADDRESS, PASSPORT,CREATE_DATE, CREATE_BY, REGDATE, SOURCESPONSOR, EDIT_DATE,");
                     distributorsInsert.append("DELETEDATE, BRANCH, ID_BRANCH, DECLAREDSTATUS, PHONE3, EMAIL) VALUES (");
                     distributorsInsert.append("gen_id(DISTRIBUTOR_gen, 0),");//id
                     distributorsInsert.append("\'").append(lastFirstMiddle[0].toString()).append("\',");//lastname
                     distributorsInsert.append("\'").append(lastFirstMiddle[1].toString()).append("\',");//firstname
                     distributorsInsert.append("\'").append(lastFirstMiddle[2].toString()).append("\',");//middlename
                     distributorsInsert.append("\'").append(newCode).append("\',");//code
                     distributorsInsert.append("\'").append("\',");//phone1
                     distributorsInsert.append("\'").append("\',");//pfone2
                     distributorsInsert.append("\'").append(object.getString("birthday")).append("\',");//birthdate
                     distributorsInsert.append("\'").append("\',");//ZIPCODE
                     distributorsInsert.append("\'").append("\',");//COUNTRY
                     distributorsInsert.append("\'").append("\',");//REGION
                     distributorsInsert.append("\'").append("\',");//CITY
                     distributorsInsert.append("\'").append("\',");//ADDRESS
                     distributorsInsert.append("\'").append("\',");//PASSPORT
                     distributorsInsert.append("\'").append(date.toString()).append("\',");//CREATE_DATE
                     distributorsInsert.append("\'").append("1").append("\',");//CREATE_BY
                     distributorsInsert.append("NULL").append(",");//REGDATE
                     distributorsInsert.append(codeId).append(",");//SOURCESPONSOR
                     distributorsInsert.append("NULL").append(",");//EDIT_DATE
                     distributorsInsert.append("NULL").append(",");//DELETEDATE
                     distributorsInsert.append("\'").append("1").append("\',");//BRANCH
                     distributorsInsert.append("\'").append("999").append("\',");//ID_BRANCH
                     distributorsInsert.append("NULL").append(",");//DECLAREDSTATUS
                     distributorsInsert.append("\'").append("\',");//PHONE3
                     distributorsInsert.append("\'").append(object.getString("email").toString()).append("\')");//email

                     Statement stmDistributorsInsert = connFire.createStatement();
                     boolean resDistributorsInsert = stmDistributorsInsert.execute(distributorsInsert.toString());
                     System.out.println(resDistributorsInsert);

                     StringBuilder objectsUpdate = new StringBuilder("UPDATE objects SET name=");
                     objectsUpdate.append("\'").append(newCode).append(" ").append(lastFirstMiddle[0].toString()).append(" ").append(lastFirstMiddle[1].substring(1, 2).toUpperCase()).append(".").append(lastFirstMiddle[2].substring(1, 2).toUpperCase()).append(".\'");
                     objectsUpdate.append(" where BRANCH = 999 and ID =gen_id(DISTRIBUTOR_gen, 0) and OBJ_TYPE=100");
                     Statement stmobjectsUpdate = connFire.createStatement();
                     boolean resobjectsUpdate = stmDistributorsInsert.execute(objectsUpdate.toString());
                     /**Пометили в bitrix*/
                     Getpostsite.postToSite(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(newCode).append("&pass=PfUhE;Ty;").toString());
                     /***/
                 } else System.out.println("Нет связи с БД");
                 }
        }
    }



}








