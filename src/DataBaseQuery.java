public class DataBaseQuery {
    public volatile String serialNumber = "";
    public String selectAllQuery = "SELECT *\n" +
            "FROM [Billingi].[dbo].[t_simcard]\n" +
            "WHERE [Billingi].[dbo].[t_simcard].[serial_number] = '"+serialNumber + "';";

    String selectTopQuery = "SELECT TOP (10) [simcard_idx]\n" +
            "               ,[msisdn]\n" +
            "               ,[serial_number]\n" +
            "               ,[apn]\n" +
            "               ,[ip_addr]\n" +
            "               ,[pin1]\n" +
            "               ,[pin2]\n" +
            "               ,[puk1]\n" +
            "               ,[puk2]\n" +
            "               ,[voice_number]\n" +
            "               ,[person_idx]\n" +
            "               ,[description]\n" +
            "               ,[added_date]\n" +
            "               ,[simcard_status_idx]\n" +
            "               ,[umowa_client_idx]\n" +
            "               ,[price_plan_idx]\n" +
            "               ,[umowa_operator_idx]\n" +
            "               ,[abonament_idx]\n" +
            "               ,[mpk]\n" +
            "               ,[voice_card]\n" +
            "               ,[localization]\n" +
            "               ,[info_check_time]\n" +
            "               ,[formal_ok]\n" +
            "               ,[fakturowanie_on]\n" +
            "               ,[simcard_protocol_idx]\n" +
            "               ,[status_change_date]\n" +
            "               ,[pobierajacy]\n" +
            "               ,[apn_priv_service_kb_limit]\n" +
            "               ,[apn_public_service_kb_limit]\n" +
            "               ,[sms_service_limit]\n" +
            "               ,[invoicing_period]\n" +
            "FROM [Billingi].[dbo].[t_simcard]";


    volatile String PINQuery = "SELECT [Billingi].[dbo].[t_simcard].pin1\n" +
            "FROM [Billingi].[dbo].[t_simcard]\n" +
            "WHERE [Billingi].[dbo].[t_simcard].[serial_number] = '" + serialNumber + "';";

    volatile String PIN2Query =  "SELECT [Billingi].[dbo].[t_simcard].pin2\n" +
            "FROM [Billingi].[dbo].[t_simcard]\n" +
            "WHERE [Billingi].[dbo].[t_simcard].[serial_number] = '" + serialNumber + "';";

    volatile String PUKQuery = "SELECT [Billingi].[dbo].[t_simcard].puk1\n" +
            "FROM [Billingi].[dbo].[t_simcard]\n" +
            "WHERE [Billingi].[dbo].[t_simcard].[serial_number] = '" + serialNumber + "';";

    volatile String PUK2Query = "SELECT [Billingi].[dbo].[t_simcard].puk2\n" +
            "FROM [Billingi].[dbo].[t_simcard]\n" +
            "WHERE [Billingi].[dbo].[t_simcard].[serial_number] = '" + serialNumber + "';";

    volatile String MSISDNQuery = "SELECT [Billingi].[dbo].[t_simcard].msisdn\n" +
            "FROM [Billingi].[dbo].[t_simcard]\n" +
            "WHERE [Billingi].[dbo].[t_simcard].[serial_number] = '" + serialNumber + "';";

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String tmpSerialNumber) {
        this.serialNumber = tmpSerialNumber;
    }

    public String getMSISDNQuery(){
        return this.MSISDNQuery;
    }

    public String getPINQuery(){
        return this.PINQuery;
    }

    public String getPIN2Query(){
        return this.PIN2Query;
    }

    public String getPUKQuery(){
        return this.PUKQuery;
    }

    public String getPUK2Query(){
        return this.PUK2Query;
    }

    public DataBaseQuery(String tmpSerialNumber){
        this.serialNumber = tmpSerialNumber;
    }
    public String getSelectAllQuery() {
        return selectAllQuery;
    }

    public String getSelectTopQuery() {
        return selectTopQuery;
    }
}
