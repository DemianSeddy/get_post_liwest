package post;

import bd.Connect_bd;
import loadsettingfromxml.ParserXPATH;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.text.SimpleDateFormat;

import java.util.StringTokenizer;

import org.apache.http.client.ClientProtocolException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;


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
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy[-MM[-dd]]");
    /*private static final StringBuilder urlDomen = StringBuilder();*/

    static TemporalAccessor parseDate(String dateAsString) {
        return FORMATTER.parseBest(dateAsString, LocalDate::from, YearMonth::from, Year::from);
    }



    public static void main(String[] args) throws ClientProtocolException, IOException, ClassNotFoundException, SQLException, XPathExpressionException, ParserConfigurationException, SAXException {

        //Через файл XML connect
        String fileXml= "src/main/basesetting.xml";
        String reg = new ParserXPATH("/setting/property_base)",fileXml).getParametr();

        Statement statement = null;

        try {
             statement = new Connect_bd().getStatementForFBConnect();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        JSONArray array = new JSONArray();
        boolean test = false;
        //if (statement) logger.info("");

        /**Запрос JSON*/
        //try {
        // "[{"birthday":"24.06.1995","sponsor":null,"address":"Ростов, ул.Ленина, 67-76","flag":"N","sponsor_code":"85858555","phone":"+7-454-455-64-78","isc_address":"Тюмень, ИСЦ Пети","site_id":"15519","name":"Тестовик Тестик Тест","email":"goro@mail.ru"}]";
        //array = Getpostsite.postFromSiteToJSON("https://liwest.ru/partners-app/all_partners_to_xml.php?get_partner=no_transfer&pass=GjKeXbNm;");

        try {
              File file = new File("d:\\jsontest.txt");
              FileReader fileReader = new FileReader(file); // поток, который подключается к текстовому файлу
              BufferedReader bufferedReader = new BufferedReader(fileReader); // соединяем FileReader с BufferedReader
              String line;
              while((line = bufferedReader.readLine()) != null) {
                array = new JSONArray(line); // выводим содержимое файла на экран построчно
            }
            bufferedReader.close(); // закрываем поток
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(array);





       System.out.println("Начинаем работать date:"+LocalDateTime.now().toString());

        if (array.toString() != "[]"){
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                /**Разбивка name на фамилию имя отчество
                 * и заливли в массив*/
                String[] lastFirstMiddle= new String[3];
                int number=0;
                StringTokenizer st = new StringTokenizer(object.getString("name").toString(), " ");
                while(st.hasMoreTokens()){
                      lastFirstMiddle[number]= st.nextToken();
                      number++;
                }

            if (lastFirstMiddle[2]==null)
                   {lastFirstMiddle[2]=" ";};

            /**окончание заливки*/
            /**Формирование запроса
             * */
             StringBuilder sqlInDistributors= new StringBuilder();
             sqlInDistributors.append("select id,code from distributors where deletedate is null ");
             sqlInDistributors.append(" and upper(lastname)=upper(trim(").append("\'").append(lastFirstMiddle[0].toString().toUpperCase()).append("\'").append("))");
             sqlInDistributors.append(" and upper(firstname)=upper(trim(").append("\'").append(lastFirstMiddle[1].toString().toUpperCase()).append("\'").append("))");
             //sqlInDistributors.append(" and upper(middlename)=upper(trim(").append("\'").append(lastFirstMiddle[2].toString().toUpperCase()).append("\'").append("))");
             sqlInDistributors.append(" and sourcesponsor in (select id from distributors where code=").append("\'").append(object.getString("sponsor_code").toString()).append("\')");
             sqlInDistributors.append(" and birthdate=").append("\'").append(object.getString("birthday").toString()).append("\'");
             //System.out.println(sqlInDistributors.toString());

             //Statement stm = Connect_bd.Connect(); //connFire.createStatement();

            ResultSet res = statement.executeQuery(sqlInDistributors.toString());

            String code = "";
            int count=-1;
            int id=0;

            while (res.next()) {
                id = res.getInt("id");
                code = res.getString("code");
            }
            /**
             * Забрали код спонсора*/
            count = -1;
            Integer sponsor = 0;
            String sponsoremail = "";

            StringBuilder sqlIdSponsor = new StringBuilder("select id sponsor,email sponsoremail from distributors where code=").append("\'").append(object.getString("sponsor_code").toString()).append("\'");
            //Statement getIdSponsor  = Connect_bd.Connect();
            ResultSet idSponsor = statement.executeQuery(sqlIdSponsor.toString());
            while (idSponsor.next()) {
                sponsor = idSponsor.getInt("sponsor");
                if (sponsoremail!=null) {
                    sponsoremail = idSponsor.getString("sponsoremail");
                };
                count++;
            }

             if (sponsor==0) {
                 System.out.println("Не заполнен спонсор");
             }

             if (id>0) {
                 /**Если записи есть то пометим их в bitrix
                    Getpostsite.postToSite(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(code).append("&pass=PfUhE;Ty;").append("&sponsoremail=").append(sponsoremail).toString());
                    System.out.printf(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(code).append("&pass=PfUhE;Ty;").append("&sponsoremail=").append(sponsoremail).toString());
                    System.out.println(new StringBuilder("site_id=").append(code).append(" уже есть в базе ").append(LocalDateTime.now().toString()));
                  */
             } else {
                 if (statement != null) {

                     /**Код договора*/
                     count= -1;
                     String newCode= " ";
                     String prexixDogovora ="555%";

                     Long date= System.currentTimeMillis();
                     /**
                      * Готовим код договора если в базе есть договор с 555* то добавляем к нему 1 если нет договора то номером будет 555000001
                       */
                     StringBuilder sqlMaxCode = new StringBuilder("select max(code) code from distributors where code like ").append("\'").append(prexixDogovora).append("\'");
                     //Statement stm555 = Connect_bd.Connect();/*createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);*/
                     ResultSet resMaxCode = statement.executeQuery(sqlMaxCode.toString());
                     //System.out.println(resMaxCode == null);
                     //System.out.println(resMaxCode.getMetaData().getColumnCount());
                     while (resMaxCode.next()) {
                         count++;
                         newCode = resMaxCode.getString("code");
                     }
                     if (newCode == null) {
                         newCode = "555000001";
                     } else {
                         newCode = BigInteger.valueOf(Long.parseLong(newCode)).add(BigInteger.ONE).toString();
                     }
                     /**
                      * Готовим вставку в базу дистрибьютера генерим id и добавляем в object
                      * */
                     //Statement genKeyIdDistribution  = Connect_bd.Connect();
                     ResultSet keyValue = statement.executeQuery("select gen_id(DISTRIBUTOR_gen, 1) from  RDB$DATABASE");
                     Integer keyIdDistributor = 0;
                     while (keyValue.next()) {
                            keyIdDistributor = keyValue.getInt("GEN_ID");
                            count++;
                     }


                     StringBuilder objectsInsert = new StringBuilder("INSERT INTO OBJECTS (BRANCH, ID, OBJ_TYPE, NAME) VALUES ");
                     objectsInsert.append("(").append("999").append(",").append(keyIdDistributor.toString()).append(",100").append(",\' \'").append(")");
                     //Statement stmObjectsInsert = Connect_bd.Connect();
                     /**Вставка в object  убирать коменты */

                     boolean resstmObjectsInsert = statement.execute(objectsInsert.toString());


                     /**Готовим вставку в DISTRIBUTORS id берем из предыдущей генерации для связи реляций*/

                     SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                     Date datenow = new Date(System.currentTimeMillis());

                     StringBuilder distributorsInsert = new StringBuilder("INSERT INTO DISTRIBUTORS (id, lastname, firstname, middlename, code, phone1, phone2,");
                     distributorsInsert.append("BIRTHDATE, ZIPCODE, COUNTRY, REGION, CITY, ADDRESS, PASSPORT,CREATE_DATE, CREATE_BY, REGDATE, SOURCESPONSOR, EDIT_DATE,");
                     distributorsInsert.append("DELETEDATE, BRANCH, ID_BRANCH, DECLAREDSTATUS, PHONE3, EMAIL) VALUES (");
                     distributorsInsert.append(keyIdDistributor).append(",");;//id
                     distributorsInsert.append("\'").append(lastFirstMiddle[0].toString().toUpperCase()).append("\',");//lastname
                     distributorsInsert.append("\'").append(lastFirstMiddle[1].toString().toUpperCase()).append("\',");//firstname
                     distributorsInsert.append("\'").append(lastFirstMiddle[2].toString().toUpperCase()).append("\',");//middlename
                     distributorsInsert.append("\'").append(newCode).append("\',");//code
                     distributorsInsert.append("\'").append("\',");//phone1
                     distributorsInsert.append("\'").append("\',");//phone2
                     distributorsInsert.append("\'").append(object.getString("birthday")).append("\',");
                     distributorsInsert.append("\'").append("\',");//ZIPCODE
                     distributorsInsert.append("\'").append("\',");//COUNTRY
                     distributorsInsert.append("\'").append("\',");//REGION
                     distributorsInsert.append("\'").append("\',");//CITY
                     distributorsInsert.append("\'").append(object.getString("address").toString()).append("\',");;//ADDRESS
                     distributorsInsert.append("\'").append("\',");//PASSPORT
                     distributorsInsert.append("\'").append(datenow.toString()).append("\',");//CREATE_DATE
                     distributorsInsert.append("\'").append("1").append("\',");//CREATE_BY
                     distributorsInsert.append("\'").append(datenow.toString()).append("\',");//REGDATE
                     distributorsInsert.append(sponsor.toString()).append(",");//SOURCESPONSOR
                     distributorsInsert.append("NULL").append(",");//EDIT_DATE
                     distributorsInsert.append("NULL").append(",");//DELETEDATE
                     distributorsInsert.append("\'").append("1").append("\',");//BRANCH
                     distributorsInsert.append("\'").append("999").append("\',");//ID_BRANCH
                     distributorsInsert.append("NULL").append(",");//DECLAREDSTATUS
                     distributorsInsert.append("\'").append(object.getString("phone")).append("\',");//pfone3
                     distributorsInsert.append("\'").append(object.getString("email").toString()).append("\')");//email

                     //Statement stmDistributorsInsert = Connect_bd.Connect();

                     System.out.println(distributorsInsert.toString());
                     /**Вставка в Distributor убирать коменты*/
                     boolean resDistributorsInsert = statement.execute(distributorsInsert.toString());
                     StringBuilder objectsUpdate = new StringBuilder("UPDATE objects SET name=");
                     objectsUpdate.append("\'").append(newCode).append(" ").append(lastFirstMiddle[0].toString()).append(" ").append(lastFirstMiddle[1].substring(1, 2).toUpperCase()).append(".").append(".\'");
                     objectsUpdate.append(" where BRANCH = 999 and ID =").append(keyIdDistributor).append(" and OBJ_TYPE=100");
                     //Statement stmobjectsUpdate = Connect_bd.Connect();

                     /**Обновляем инфу в object убирать коменты*/
                      boolean resobjectsUpdate = statement.execute(objectsUpdate.toString());

                     /**Пометили в bitrix убирать коменты*/
                     System.out.println(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(newCode).append("&pass=PfUhE;Ty;").append("&sponsoremail=").append(sponsoremail).toString());
                     System.out.println("Добавили а базу и отправили в bitrix c кодом договора "+newCode.toString()+" date:"+LocalDateTime.now().toString());
                     //Getpostsite.postToSite(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(newCode).append("&pass=PfUhE;Ty;").append("&sponsoremail=").append(sponsoremail).toString());
                     //Getpostsite.postToSite(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(newCode).append("&pass=PfUhE;Ty;").append("&sponsoremail=").append("support@liwest.ru").toString());

                 } else System.out.println("Нет связи с БД");
             }
        }
        /**} catch (Exception ex) {
           ex.printStackTrace();
        }*/
    }
        //cbd.closeAccess();

        }
}









