package post;

import connect.*;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.text.SimpleDateFormat;

import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.http.client.ClientProtocolException;

import org.json.JSONArray;
import org.json.JSONObject;

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

    static TemporalAccessor parseDate(String dateAsString) {
        return FORMATTER.parseBest(dateAsString, LocalDate::from, YearMonth::from, Year::from);
    }

    public static boolean isValidDate(String dateAsString) {
        try {
            parseDate(dateAsString);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }


    public static  void main(String[] args) throws ClientProtocolException, IOException, ClassNotFoundException, SQLException {
        JSONArray array = new JSONArray();
        /**Запрос JSON*/
        //try {
        array = Getpostsite.postFromSiteToJSON("https://liwest.ru/partners-app/all_partners_to_xml.php?get_partner=no_transfer&pass=GjKeXbNm;");
        System.out.println("Начинаем работать date:"+LocalDateTime.now().toString());
        if (array.toString() != "[]"){
             String DB_DRIVER="org.firebirdsql.jdbc.FBDriver";
             String DB_URL="jdbc:firebirdsql:LAO:D:\\LiWest\\Bases\\LiWest.fdb";
             //String DB_URL="jdbc:firebirdsql:localhost:E:\\runliwest\\base\\works\\LIWEST.FDB";
             Connection connFire = null;
        // Connection connFire1 = null;
        //if (new Connect_bd().getConnection()) ;
              try {
                  Class.forName(DB_DRIVER);
                     } catch (ClassNotFoundException e)
                {
                    System.out.println("connectToDB: Firebird JCA-JDBC драйвер не найден");
                }
               try {
                 Properties paramConnection = new Properties();
                 paramConnection.setProperty("user", "SYSDBA");
                 paramConnection.setProperty("password", "ktybdsq,fnjy");
                 paramConnection.setProperty("encoding", "WIN1251");
                 connFire = DriverManager.getConnection(DB_URL, paramConnection);
            }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
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
             /*sqlInDistributors.append(" and birthdate=").append("\'").append(object.getString("birthday").toString()).append("\'");*/
             //System.out.println(sqlInDistributors.toString());

            Statement stm= connFire.createStatement();
            ResultSet res=stm.executeQuery(sqlInDistributors.toString());

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
            Statement getIdSponsor  = connFire.createStatement();
            ResultSet idSponsor = getIdSponsor.executeQuery(sqlIdSponsor.toString());
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
                 /**Если записи есть то пометим их в bitrix*/
                    Getpostsite.postToSite(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(code).append("&pass=PfUhE;Ty;").append("&sponsoremail=").append(sponsoremail).toString());
                    System.out.printf(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(code).append("&pass=PfUhE;Ty;").append("&sponsoremail=").append(sponsoremail).toString());
                  /****/
                 System.out.println(new StringBuilder("site_id=").append(code).append(" уже есть в базе ").append(LocalDateTime.now().toString()));
             } else {
                 if (connFire != null) {

                     /**Код договора*/
                     count= -1;
                     String newCode= " ";
                     String prexixDogovora ="555%";

                     Long date= System.currentTimeMillis();
                     /**
                      * Готовим код договора если в базе есть договор с 555* то добавляем к нему 1 если нет договора то номером будет 555000001
                       */
                     StringBuilder sqlMaxCode = new StringBuilder("select max(code) code from distributors where code like ").append("\'").append(prexixDogovora).append("\'");
                     Statement stm555 = connFire.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                     ResultSet resMaxCode = stm555.executeQuery(sqlMaxCode.toString());
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
                     Statement genKeyIdDistribution  = connFire.createStatement();
                     ResultSet keyValue = genKeyIdDistribution.executeQuery("select gen_id(DISTRIBUTOR_gen, 1) from  RDB$DATABASE");
                     Integer keyIdDistributor = 0;
                     while (keyValue.next()) {
                            keyIdDistributor = keyValue.getInt("GEN_ID");
                            count++;
                     }


                     StringBuilder objectsInsert = new StringBuilder("INSERT INTO OBJECTS (BRANCH, ID, OBJ_TYPE, NAME) VALUES ");
                     objectsInsert.append("(").append("999").append(",").append(keyIdDistributor.toString()).append(",100").append(",\' \'").append(")");
                     Statement stmObjectsInsert = connFire.createStatement();
                     /**Вставка в object*/
                      boolean resstmObjectsInsert = stmObjectsInsert.execute(objectsInsert.toString());
                      /**
                      */

                     /**Готовим вставку в DISTRIBUTORS id берем из предыдущей генерации для связи реляций*/

                     SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                     Date datenow = new Date(System.currentTimeMillis());

                     StringBuilder distributorsInsert = new StringBuilder("INSERT INTO DISTRIBUTORS (id, lastname, firstname, middlename, code, phone1, phone2,");
                     distributorsInsert.append("BIRTHDATE, ZIPCODE, COUNTRY, REGION, CITY, ADDRESS, PASSPORT,CREATE_DATE, CREATE_BY, REGDATE, SOURCESPONSOR, EDIT_DATE,");
                     distributorsInsert.append("DELETEDATE, BRANCH, ID_BRANCH, DECLAREDSTATUS, PHONE3, EMAIL) VALUES (");
                     distributorsInsert.append(keyIdDistributor).append(",");;//id
                     distributorsInsert.append("\'").append(lastFirstMiddle[0].toString()).append("\',");//lastname
                     distributorsInsert.append("\'").append(lastFirstMiddle[1].toString()).append("\',");//firstname
                     distributorsInsert.append("\'").append(lastFirstMiddle[2].toString()).append("\',");//middlename
                     distributorsInsert.append("\'").append(newCode).append("\',");//code
                     distributorsInsert.append("\'").append("\',");//phone1
                     distributorsInsert.append("\'").append("\',");//pfone2
                     distributorsInsert.append("\'").append(object.getString("birthday")).append("\',");
                     distributorsInsert.append("\'").append("\',");//ZIPCODE
                     distributorsInsert.append("\'").append("\',");//COUNTRY
                     distributorsInsert.append("\'").append("\',");//REGION
                     distributorsInsert.append("\'").append("\',");//CITY
                     distributorsInsert.append("\'").append("\',");//ADDRESS
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
                     distributorsInsert.append("\'").append("\',");//PHONE3
                     distributorsInsert.append("\'").append(object.getString("email").toString()).append("\')");//email

                     Statement stmDistributorsInsert = connFire.createStatement();
                     /**Вставка в Distributor*/
                     boolean resDistributorsInsert = stmDistributorsInsert.execute(distributorsInsert.toString());
                      /***/

                     /**Обновляем инфу в object
                      *  */
                     StringBuilder objectsUpdate = new StringBuilder("UPDATE objects SET name=");
                     objectsUpdate.append("\'").append(newCode).append(" ").append(lastFirstMiddle[0].toString()).append(" ").append(lastFirstMiddle[1].substring(1, 2).toUpperCase()).append(".").append(".\'");
                     objectsUpdate.append(" where BRANCH = 999 and ID =").append(keyIdDistributor).append(" and OBJ_TYPE=100");
                     Statement stmobjectsUpdate = connFire.createStatement();


                      boolean resobjectsUpdate = stmDistributorsInsert.execute(objectsUpdate.toString());

                     /**Пометили в bitrix*/
                     System.out.println(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(newCode).append("&pass=PfUhE;Ty;").append("&sponsoremail=").append(sponsoremail).toString());
                     System.out.println("Добавили а базу и отправили в bitrix c кодом договора "+newCode.toString()+" date:"+LocalDateTime.now().toString());
                     Getpostsite.postToSite(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(newCode).append("&pass=PfUhE;Ty;").append("&sponsoremail=").append(sponsoremail).toString());
                     //Getpostsite.postToSite(new StringBuilder("https://liwest.ru/partners-app/check_partner_xml.php?check_partner=").append(object.getInt("site_id")).append("&code=").append(newCode).append("&pass=PfUhE;Ty;").append("&sponsoremail=").append("support@liwest.ru").toString());
                     /***/
                 } else System.out.println("Нет связи с БД");
             }
        }
        /**} catch (Exception ex) {
           ex.printStackTrace();
        }*/
    }
}
}








