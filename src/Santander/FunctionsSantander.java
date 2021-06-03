package Santander;
 
import eContact.FunctionsGVP;
import eContact.OracleDBAccess;
import eContact.Parameters;
import oracle.jdbc.OracleTypes;

//import javax.servlet.jsp.PageContext;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
 
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.ssl.TrustStrategy;

//import com.vector.ae.mwlib.util.*;
 
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.CallableStatement;  
import java.sql.Connection;  
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
/*import com.vector.ae.mwlib.MWObject;
import com.vector.ae.mwlib.MWObjectException;
import com.vector.ae.mwlib.model.DataModel;
import com.vector.ae.mwlib.model.FieldDataModel;*/

import cl.econtact.soap.caller.WsActivacionCoordenadas;
import cl.econtact.soap.caller.WsCambioNumeroCelular;
import cl.econtact.soap.caller.WsCartolaOnLine;
import cl.econtact.soap.caller.WsClavePingClienteHB;
import cl.econtact.soap.caller.WsConsultaPin_SMAMP228;
import cl.econtact.soap.caller.WsConsultaSaldos_CL1MDMPV3;
import cl.econtact.soap.caller.WsConsultaSaldos_CLIMDKC01;
import cl.econtact.soap.caller.WsConsultaServiciosCliente;
import cl.econtact.soap.caller.WsCrucePreHechoIVR;
import cl.econtact.soap.caller.WsDatosPersonas;
import cl.econtact.soap.caller.WsEstadoDispositivo;
import cl.econtact.soap.caller.WsEstadoServicio;
import cl.econtact.soap.caller.WsObtieneRUTClient;
import cl.econtact.soap.caller.WsRutTitularYBeneficiario;
import cl.econtact.soap.caller.WsSaldoAhorros;
import cl.econtact.soap.caller.WsServiceInNormalAuth;
import cl.econtact.soap.caller.WsSolicitaDesafio;
import cl.econtact.soap.caller.WsSubSegmento;
import cl.econtact.soap.caller.WsTodasTarjetasRut;
import cl.econtact.soap.caller.WsUserTokenList;
import cl.econtact.soap.caller.WsUsuarioPuntoVenta;
import cl.econtact.soap.caller.WsValidaDesafio;
import cl.econtact.soap.caller.WsCONCruceDeProductos;

public class FunctionsSantander extends FunctionsGVP
{
    
   static String[] sDesafios;
   static javax.xml.rpc.Service service = null;
   static java.net.URL url = null;
   /* FIN VARIABLES PARA AUTENTICACION*/
       
   /*
    * Variables Generales para Base de Datos
    * */
   
   public Parameters parametrosBD = new Parameters();
   private OracleDBAccess conexionDB = new OracleDBAccess();
   
//    private static  Gson            gson    = new Gson();
//    private static  JsonParser      parser  = new JsonParser();
    public String  hostIvrToDB     = "127.0.0.1";
    public int     portIvrToDB     = 50081;
    public int     timeoutSocket  = 3000;
    
    
   /*
     * Variables Generales para MQ
     * */        
    public String MQhost = "127.0.0.1";
    public int MQport = 50039;
 
    /** VARIABLES PUBLICAS DE SERVICIOS MQ**/
    public String trxCod = "";
    public String trxMsj = "";
    
    public String LogAvanazadoFilePath = "";
    public String LogEncuestaFilePath = "";
    public String LogKronosFilePath = "";
//
//    public KVPairList XMLKVPairList = new KVPairList();
 
//    public Parameters Params = new Parameters();
 
    public String ExceptionMessage = "";
 
    public String InstanceID = "";
 
    public boolean DVesK = false;
 
    public FunctionsSantander()
    {
    }
    
    public FunctionsSantander(String ParametersFile)
    {
        super(ParametersFile);              
        inicializar();

        
    }
    
    public FunctionsSantander(String ParametersFile, String id)
    {        
        super(ParametersFile, id);
        inicializar();
    }
    
    /*Parametros adicionales a Leer que no lee el FGVP*/
    private void inicializar(){
        MQhost = this.Params.GetValue("IvrToMQhost", "127.0.0.1");
        MQport = Integer.valueOf(this.Params.GetValue("IvrToMQport", "50039"));
        
        timeoutSocket  = Integer.valueOf(this.Params.GetValue("SocketTimeout", "2000"));        
        LogKronosFilePath = this.Params.GetValue("LogKronosFilePath", "IVR_Santander_Banco_Peformance");
    	System.out.println("inicializando GVP....11.2.021.2 "   );

    }
    
    
    public void loggerTraza(String Message, String Level){
        //Set Logger options;                
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(this.Timezone));
        Date curDate = new Date();
        //CREA UN ARCHIVO POR DIA (Y ES BASE PARA OTROS LOGS)
        String DateToStr = format.format(curDate);
        
        log = Logger.getLogger("GVPTRAZA");
        RollingFileAppender appender = (RollingFileAppender) log.getAppender("gvplogtrazafile");         
 
        String archivo = this.DebugFilePath.replace(".log", "");        
        if (archivo.equals("")){
            archivo = "C:\\logs\\IVR\\TrazaLog";
        }        
        archivo = archivo + "-" + DateToStr + ".log";
        
        appender.setFile(archivo);
        appender.activateOptions();
        
        // DEBUG < INFO < WARN < ERROR < FATAL
        if (Level.equalsIgnoreCase("DEBUG")) {
                log.debug(Message); 
        }else if (Level.equalsIgnoreCase("INFO")) {
                log.info(Message); 
        }else if (Level.equalsIgnoreCase("WARN")) {
                log.warn(Message); 
        }else if (Level.equalsIgnoreCase("ERROR")) {
                log.error(Message); 
        }else if (Level.equalsIgnoreCase("FATAL")) {
                log.fatal(Message); 
        }else { 
                log.debug(Message); 
        }
        
    }
    
    public void loggerPeformance(String Message, String Level ){
        
        //Set Logger options;                
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(this.Timezone));        
        Date curDate = new Date();
        
        //CREA UN ARCHIVO POR DIA (Y ES BASE PARA OTROS LOGS)
        String DateToStr = format.format(curDate);
        
        SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss");
        formatHora.setTimeZone(TimeZone.getTimeZone(Timezone));
        String Hora = formatHora.format(curDate);
        
        
          log = Logger.getLogger("GVPTRAZA");
        //RollingFileAppender appender = (RollingFileAppender) log.getAppender("gvplogtrazafile");         
 
        //String archivo = LogKronosFilePath.replace(".log", "");        
        //if (archivo.equals("")){
            //archivo = "C:\\logs\\IVR\\PerformanceLog";
        //}        
        //archivo = archivo + "-" + DateToStr + ".log";
        
        //appender.setFile(archivo);
        //appender.activateOptions();
        
        
        // DEBUG < INFO < WARN < ERROR < FATAL
        if (Level.equalsIgnoreCase("DEBUG")) {
                log.debug(Message); 
        }else if (Level.equalsIgnoreCase("INFO")) {
                log.info(Message); 
        }else if (Level.equalsIgnoreCase("WARN")) {
                log.warn(Message); 
        }else if (Level.equalsIgnoreCase("ERROR")) {
                log.error(Message); 
        }else if (Level.equalsIgnoreCase("FATAL")) {
                log.fatal(Message); 
        }else { 
                log.debug(Message); 
        }
        
    }
    
    public void loggerPeformance(String nameLogger, String Message, String Level ){
        
        //Set Logger options;                
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(this.Timezone));        
        Date curDate = new Date();
        
        //CREA UN ARCHIVO POR DIA (Y ES BASE PARA OTROS LOGS)
        String DateToStr = format.format(curDate);
        
        SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss");
        formatHora.setTimeZone(TimeZone.getTimeZone(Timezone));
        String Hora = formatHora.format(curDate);
        
        
        if(!nameLogger.equals("")){
            
            log = Logger.getLogger(nameLogger);
            
           // DEBUG < INFO < WARN < ERROR < FATAL
           if (Level.equalsIgnoreCase("DEBUG")) {
                   log.debug(Message); 
           }else if (Level.equalsIgnoreCase("INFO")) {
                   log.info(Message); 
           }else if (Level.equalsIgnoreCase("WARN")) {
                   log.warn(Message); 
           }else if (Level.equalsIgnoreCase("ERROR")) {
                   log.error(Message); 
           }else if (Level.equalsIgnoreCase("FATAL")) {
                   log.fatal(Message); 
           }else { 
                   log.debug(Message); 
           }
        
        }else{
            System.out.println("Error: crear var nameLogger para este metodo, recuerde config log4j.xml");
        }
        
    }
 
    
    public void registrarLog(String Message, String tipoLog, String Level){
        //Set Logger options;                
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(this.Timezone));        
        Date curDate = new Date();
        //CREA UN ARCHIVO POR DIA (Y ES BASE PARA OTROS LOGS)
        String DateToStr = format.format(curDate);
        
        log = Logger.getLogger("GVPTRAZA");
        RollingFileAppender appender = (RollingFileAppender) log.getAppender("gvplogtrazafile");         
 
        String archivo = "";
        
        if (tipoLog.equals("KRONOS")){
            archivo = LogKronosFilePath.replace(".log", "");
        }else if (tipoLog.equals("AVANZADO")){
            archivo = LogAvanazadoFilePath.replace(".log", "");
        }else if (tipoLog.equals("ENCUESTA")){
            archivo = LogEncuestaFilePath.replace(".log", "");
        }
        
        if (archivo.equals(""))
            archivo = "C:\\logs\\IVR\\Log_Santander_Banco";
                
        archivo = archivo + "-" + DateToStr + ".log";
        
        appender.setFile(archivo);
        appender.activateOptions();
        
        // DEBUG < INFO < WARN < ERROR < FATAL
        if (Level.equalsIgnoreCase("DEBUG")) {
                log.debug(Message); 
        }else if (Level.equalsIgnoreCase("INFO")) {
                log.info(Message); 
        }else if (Level.equalsIgnoreCase("WARN")) {
                log.warn(Message); 
        }else if (Level.equalsIgnoreCase("ERROR")) {
                log.error(Message); 
        }else if (Level.equalsIgnoreCase("FATAL")) {
                log.fatal(Message); 
        }else { 
                log.debug(Message); 
        }
        
    }
    
    
    public void registrarLogAvanzado(String mensaje, String tipoLog){               
 
        if (tipoLog.equals("KRONOS")){
            Logger log = Logger.getLogger("Log_kronos");
            log.debug(mensaje);
        }
        if (tipoLog.equals("AVANZADO")){
            Logger log = Logger.getLogger("Log_avanzado");
            log.debug(mensaje);
        }
        if (tipoLog.equals("ENCUESTA")){    
            Logger log = Logger.getLogger("Log_encuesta");
            log.debug(mensaje);
        }
    
    }
 
 
    public void WriteParameters (String ParametersFile)
    {
        Debug("[FunctionsGVP - WriteParameters] Escribiendo archivo de parametros.", "Detail");
 
        Params.WriteParametersFile(ParametersFile);
    }
 
 
 
    public boolean Log (String Message, boolean IncludeDateTime)
    {
        String LogMessage = "";
 
        if( IncludeDateTime )
        {
            // Obtiene fecha  hora actual y la formatea para cabecera
            // del texto que se escribira en el archivo de Log.
            TimeZone tz = TimeZone.getTimeZone(this.Timezone);
            SimpleDateFormat DateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
            DateFormatter.setTimeZone(tz);
            String DateString = DateFormatter.format(new Date());
 
            // Compone el texto que se escribira en el archivo de Log
            LogMessage += DateString + " ";
        }
 
        LogMessage += Message + "\n";
 
        try
        {
            File fLogFile = new File(this.DebugFilePath);
 
            fLogFile.createNewFile();
 
            if( fLogFile.canWrite() )
            {
                FileOutputStream osLogFile = new FileOutputStream(fLogFile, true);
 
                osLogFile.write(LogMessage.getBytes());
                osLogFile.close();
            }
            else
            {
                return false;
            }
        }
 
        catch( Exception e )
        {
            return false;
        }
 
        return true;
    }
 
 
 
    public boolean Log (String Message)
    {
        return Log(Message, true);
    }
 
 
   
 
 

 
 
    public void RegistroControl (Map<String, String> parametros, String sTipoLog, String sPuntoControl, String sVector)
    {
        try
        {
            String sArchivoLog = (String) parametros.get("ArchivoLog");
            String sIDLlamada = (String) parametros.get("IDLlamada");
            String sRUTCliente = (String) parametros.get("RutCliente");
            String sANI = (String) parametros.get("ANI");
            String sDNIS = (String) parametros.get("DNIS");
            String sConnID = (String) parametros.get("ConnID");
 
            // La fecha y hora debe ser generada al momenmto de generar el registro
            TimeZone tz = TimeZone.getTimeZone(this.Timezone);
            SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
            df2.setTimeZone(tz);
            String sFecha = df2.format(new Date());
 
            SimpleDateFormat df3 = new SimpleDateFormat("HH:mm:ss");
            df3.setTimeZone(tz);
            String sHora = df3.format(new Date());
 
            if (sRUTCliente.equals(""))
                sRUTCliente ="0000000";
 
            StringBuffer bRegistroLog = new StringBuffer(sIDLlamada);
            bRegistroLog.append(";");
            bRegistroLog.append(sRUTCliente);
            bRegistroLog.append(";");
            bRegistroLog.append(sTipoLog);
            bRegistroLog.append(";");
            bRegistroLog.append(sPuntoControl);
            bRegistroLog.append(";");
            bRegistroLog.append(sVector);
            bRegistroLog.append(";");
            bRegistroLog.append(sFecha);
            bRegistroLog.append(";");
            bRegistroLog.append(sHora);
            bRegistroLog.append(";");
            bRegistroLog.append(sANI);
            bRegistroLog.append(";");
            bRegistroLog.append(sDNIS);
            bRegistroLog.append(";");
            bRegistroLog.append(sConnID);
 
            String sRegistroLog = bRegistroLog.toString();
 
            this.DebugFilePath = sArchivoLog;
            loggerTraza(sRegistroLog, "DEBUG");
//            Log(sRegistroLog, false);
        }
 
        catch (Exception e)
        {
            return;
        }
 
        return;
    }
 
 
    public long calcularDuracion(Date inicio, Date fin){
 
         java.util.GregorianCalendar fechaIni = new java.util.GregorianCalendar();
         fechaIni.setTime(inicio);
         
         java.util.GregorianCalendar fechaFin = new java.util.GregorianCalendar();
         fechaFin.setTime(fin);
         
         Date primer = fechaIni.getTime();
         Date ultimo = fechaFin.getTime();
 
         long resta = ultimo.getTime() - primer.getTime();
         long minutos = (resta/(1000*60));
         long horas = (resta/(1000*60*60));        
         long min = minutos - (horas*60);                     
         
         long miliSegIni = fechaIni.getTimeInMillis();
         long miliSegFin = fechaFin.getTimeInMillis();
         
//         long seg = (miliSegFin - miliSegIni)/1000;
//         
//         long totalSeg = 0;
//         if (min > 0){
//             totalSeg = seg - (min*60);
//         }else{
//             totalSeg = seg;
//         }
 
//         String duracion = ""+totalSeg;
         
         return miliSegFin - miliSegIni;
     }
    
    public void RegistroPeformance(Map<String, Object> parametros)
    {
        try
        {            
//            String sIDLlamada =  parametros.get("IDLlamada");
            String sRUT =  parametros.get("RUT").toString();
            Date fechaIni =  (Date) parametros.get("FechaInicio");
//            Date sHoraIni =  parametros.get("HoraInicio");
//            String sServicio =  parametros.get("Servicio");
//            String sTipoServicio =  parametros.get("TipoServicio");
//            String sConnID =  parametros.get("ConnID");
 
            // La fecha y hora debe ser generada al momenmto de generar el registro
            TimeZone tz = TimeZone.getTimeZone(this.Timezone);
            SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
            df2.setTimeZone(tz);
            Date fechaFin = new Date();
            String sFechaFin = df2.format(fechaFin);
 
            SimpleDateFormat df3 = new SimpleDateFormat("HH:mm:ss");
            df3.setTimeZone(tz);
            String sHoraFin = df3.format(fechaFin);
 
            long tiempoRespuesta = calcularDuracion(fechaIni, fechaFin);
            
            if (sRUT.equals(""))
                sRUT="0000000";
 
            String mensaje = "";
            mensaje += parametros.get("ConnID").toString()+";";
            mensaje += sRUT+";";
            mensaje += sFechaFin+";";
            mensaje += sHoraFin+";";
            mensaje += parametros.get("Servicio").toString()+";";
            mensaje += parametros.get("TipoServicio").toString()+";";
            mensaje += tiempoRespuesta+";";
            mensaje += parametros.get("Resultado").toString()+";";
            mensaje += parametros.get("MensajeError").toString()+";";
 
            loggerPeformance(mensaje, "DEBUG");
        }
 
        catch (Exception e){
            
        }
 
        return;
    }
    
 
 
 
    
    
    
    public Parameters leerParametrosWS(){
        Parameters parametros = new Parameters();
        String catalina = System.getProperty("catalina.base");        
        String archivo = catalina + "//lib//ConfiguracionServiciosWeb.properties";
//        String archivo = "D://Composer//workspace_Ripley//ConfiguracionServiciosWeb.properties";
        parametros.ReadParametersFile(archivo);
        return parametros;
    }
   
    public Parameters leerParametrosMQ(String nombreServicio){
        Parameters parametros = new Parameters();
        String catalina = System.getProperty("catalina.base");        
        String archivo = catalina + "//lib//ConfiguracionServiciosMQ.properties";
//        String archivo = "D://Composer//workspace_Ripley//ConfiguracionServiciosMQ.properties";
        parametros.ReadParametersFile(archivo);
        return parametros;
    }
    
    public Parameters leerMatrizCampanas(){
        Parameters parametros = new Parameters();
        String catalina = System.getProperty("catalina.base");        
        String archivo = catalina + "//lib//ConfiguracionMatrizCampanas.properties";
//        String archivo = "D://Composer//workspace_Ripley//ConfiguracionMatrizCampanas.properties";
        parametros.ReadParametersFile(archivo);
        return parametros;
    }
    
    
    public JSONObject getValidaOfertaSP(String rut) {
        JSONObject result = new JSONObject();
        Connection conn = null;  
        ResultSet rs = null;
        CallableStatement cStmt =null;
        
        try{

            if (conexionDB.OpenDataBase(this.oracleUrl, this.oracleUser, this.oraclePass, this.oracleTimeOut)){
                 Debug("[ValidaOfertaSP] Conexion Exitosa.", "INFO");                  
                 conn=conexionDB.getEcConexion();  
                 cStmt = conn.prepareCall("{call spValidaOferta (?,?)}");   
                 cStmt.setString(1, rut);    
                 cStmt.registerOutParameter(2,  OracleTypes.CURSOR);    
                 cStmt.execute();

                 ResultSet cursorResultSet = (ResultSet) cStmt.getObject(2);
                 while (cursorResultSet.next())
                 {
                	
                	if(cursorResultSet.getInt(1)==0){
                		result.put("AlertaDerivacionIVR","NO");
                	}else{
                		result.put("AlertaDerivacionIVR","SI");
                	}
                                
                 } 	            
             }else{
                Debug("[ValidaOfertaSP] Conexion Fallida.", "INFO");
                Debug("[ValidaOfertaSP] Error "+conexionDB.GetErrorMessage(), "DEBUG");
            }
        } catch (SQLException e) {
           // TODO Auto-generated catch block        
           e.printStackTrace();
        }catch(Exception ex){
            // TODO Auto-generated catch block        	
            ex.printStackTrace();
        }finally{
            if (this.conexionDB != null) conexionDB.CloseDataBase();
        }        
        System.out.println("result ValidaOfertaSP.... " + result.toString()  );
        return result;
    }
     
    
    
    
    public JSONObject getHorarioSP(String horario) {
        JSONObject result = new JSONObject();
        Connection conn = null;  
        ResultSet rs = null;
        CallableStatement cStmt =null;
        
        try{
            result.put("horario", "Habil");
            if (conexionDB.OpenDataBase(this.oracleUrl, this.oracleUser, this.oraclePass, this.oracleTimeOut)){
                Debug("[ValidaHorario] Conexion Exitosa.", "INFO");                  
                 conn=conexionDB.getEcConexion();  
                 cStmt = conn.prepareCall("{call spValidaHorario (?,?)}");   
                 cStmt.setString(1, horario);    
                 cStmt.registerOutParameter(2,  OracleTypes.CURSOR);    
                 cStmt.execute();

                 ResultSet cursorResultSet = (ResultSet) cStmt.getObject(2);
                 while (cursorResultSet.next())
                 {
                  result.put("horario", cursorResultSet.getString(1));
                  System.out.println(cursorResultSet.getString(1));                 
                 } 	            
             }else{
                Debug("[ValidaHorario] Conexion Fallida.", "INFO");
                Debug("[ValidaHorario] Error "+conexionDB.GetErrorMessage(), "DEBUG");
            }
        } catch (SQLException e) {
           // TODO Auto-generated catch block        
           e.printStackTrace();
        }catch(Exception ex){
            // TODO Auto-generated catch block        	
            ex.printStackTrace();
        }finally{
            if (this.conexionDB != null) conexionDB.CloseDataBase();
        }        
        System.out.println("result ValidaHorario.... " + result.toString()  );
        return result;
    }
  
    
    public JSONObject getObtieneParametrosSP(String identificador, String nombre) {
        JSONObject result = new JSONObject();
        Connection conn = null;  
        ResultSet rs = null;
        CallableStatement cStmt =null;
        
        try{
            result.put("parametro", "");
            result.put("existe", "NO");
            if (conexionDB.OpenDataBase(this.oracleUrl, this.oracleUser, this.oraclePass, this.oracleTimeOut)){
                Debug("[ObtieneParametrosIVR] Conexion Exitosa.", "INFO");                  
                 conn=conexionDB.getEcConexion();  
                 cStmt = conn.prepareCall("{call spObtieneParametrosIVR  (?,?)}");   
                 cStmt.setString(1, identificador);    
                 cStmt.registerOutParameter(2,  OracleTypes.CURSOR);    
                 cStmt.execute();

                 ResultSet cursorResultSet = (ResultSet) cStmt.getObject(2);
                
                 while (cursorResultSet.next())         
                 {                	              	 
                	 try{                	               		 
                		 if(!cursorResultSet.getString(1).isEmpty() && cursorResultSet.getString(1).equalsIgnoreCase(nombre)){              	      	
                			 result.put("existe", "SI");
                			 result.put("parametro", cursorResultSet.getString(2));
                			 System.out.println(cursorResultSet.getString(1));
	                   		 System.out.println(cursorResultSet.getString(2));  
	                   		 break;
                		 }  
                	 
                	 }catch(Exception e){
                		 System.out.println("error en lectura de ResultSet. Se pasa al siguiente.");
                	 }
                          
                 }
           
             }else{
                Debug("[ObtieneParametrosIVR] Conexion Fallida.", "INFO");
                Debug("[ObtieneParametrosIVR] Error "+conexionDB.GetErrorMessage(), "DEBUG");
            }
        } catch (SQLException e) {
           // TODO Auto-generated catch block        
           e.printStackTrace();
        }catch(Exception ex){
            // TODO Auto-generated catch block        	
            ex.printStackTrace();
        }finally{
            if (this.conexionDB != null) conexionDB.CloseDataBase();
        }        
        System.out.println("result ObtieneParametrosIVR.... " + result.toString()  );
        return result;
    }
     
    
    public JSONObject getListaNegraSP(String telefono) {
        JSONObject result = new JSONObject();
        Connection conn = null;  
        try{
            result.put("listaNegra", "NO");               
            if (conexionDB.OpenDataBase(this.oracleUrl, this.oracleUser, this.oraclePass, this.oracleTimeOut)){
                Debug("[ListaNegra] Conexion Exitosa.", "INFO");                  
                 conn=conexionDB.getEcConexion();  
                 CallableStatement cStmt = conn.prepareCall("{call spValidaListaNegra (?,?,?)}");   
                 cStmt.setString(1, "");    
                 cStmt.setString(2, telefono);  
                 cStmt.registerOutParameter(3, OracleTypes.CURSOR);    
                 cStmt.execute();   
                 ResultSet cursorResultSet = (ResultSet) cStmt.getObject(3);
                 while (cursorResultSet.next())
                 {             
	              	if(cursorResultSet.getInt(1)==0){
	            		result.put("listaNegra","NO");
	            	}else{
	            		result.put("listaNegra","SI");
	            	}                 
               
                 } 	         

            }else{
                Debug("[ListaNegra] Conexion Fallida.", "INFO");
                Debug("[ListaNegra] Error "+conexionDB.GetErrorMessage(), "DEBUG");
            }
        } catch (SQLException e) {
           // TODO Auto-generated catch block        
           e.printStackTrace();
        }catch(Exception ex){
            // TODO Auto-generated catch block        	
            ex.printStackTrace();
        }finally{
            if (this.conexionDB != null) conexionDB.CloseDataBase();
        }
        
        System.out.println("result listaNegra.... " + result.toString()  );
        return result;
    }
    
    public JSONObject getListaNegra(String telefono) {
        JSONObject result = new JSONObject();
        
        try{
            result.put("listaNegra", "NO");
                  
            if (conexionDB.OpenDataBase(this.oracleUrl, this.oracleUser, this.oraclePass, this.oracleTimeOut)){
                Debug("[ListaNegra] Conexion Exitosa.", "INFO");                
          
                String sqlQuery = String.format("SELECT LN_TELEFONO, LN_RUT"+
                        " FROM LISTA_NEGRA"+
                        " WHERE LN_TELEFONO='%s'"+
                        " AND LN_STATUS=1"
                         , telefono);
                
                System.out.println("Query listaNegra.... " + sqlQuery  );         
                ResultSet resultSet = conexionDB.ExecuteQuery(sqlQuery, this.oracleTimeOut);
            
                while (resultSet.next()) {
                    String resTelefono = resultSet.getString("LN_TELEFONO");
                    Debug("[ListaNegra] telefono="+resTelefono, "INFO");  
                    result.put("listaNegra", "SI");
             
                }
            }else{
                Debug("[ListaNegra] Conexion Fallida.", "INFO");
                Debug("[ListaNegra] Error "+conexionDB.GetErrorMessage(), "DEBUG");
            }
        } catch (SQLException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
        }catch(Exception ex){
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }finally{
            if (this.conexionDB != null) conexionDB.CloseDataBase();
        }
        
        System.out.println("result listaNegra.... " + result.toString()  );
        return result;
    }
    
    public JSONObject getObtieneAudioCampanaSP(String nombreCampana, String listaCampana) {
        JSONObject result = new JSONObject();
        Connection conn = null;  
        ResultSet rs = null;
        CallableStatement cStmt =null;
        
        try{

            if (conexionDB.OpenDataBase(this.oracleUrl, this.oracleUser, this.oraclePass, this.oracleTimeOut)){
                 Debug("[ObtieneAudioCampanaSP] Conexion Exitosa.", "INFO");                  
                 conn=conexionDB.getEcConexion();  
                 
                 cStmt = conn.prepareCall("{call SPOBTIENEAUDIOCAMPANHAIVR (?,?,?)}");   
                 cStmt.setString(1, nombreCampana);   
                 cStmt.setString(2, listaCampana);  
                 cStmt.registerOutParameter(3,  OracleTypes.CURSOR);    
                 cStmt.execute();

                 ResultSet cursorResultSet = (ResultSet) cStmt.getObject(3);
                 System.out.println("actualización");
           
                 System.out.println("cursorResultSet.next():  " + cursorResultSet.next());       
                                 
                 while (cursorResultSet.next())
                 {   
                	System.out.println("ingrese a rescatar el valor");  
                    result.put("audio", cursorResultSet.getString(1));
                    System.out.println(cursorResultSet);   
                  
	             } 	                
               
                     
                
             }else{
                Debug("[ObtieneAudioCampanaSP] Conexion Fallida.", "INFO");
                Debug("[ObtieneAudioCampanaSP] Error "+conexionDB.GetErrorMessage(), "DEBUG");
            }
        } catch (SQLException e) {
           // TODO Auto-generated catch block        
           e.printStackTrace();
        }catch(Exception ex){
            // TODO Auto-generated catch block        	
            ex.printStackTrace();
        }finally{
            if (this.conexionDB != null) conexionDB.CloseDataBase();
        }        
        System.out.println("result ObtieneAudioCampanaSP.... " + result.toString()  );
        return result;
    }
    
    /*
    public boolean iniciarConexionBDOracle(){
        boolean retorno = false;
        try{
            Debug("[ConexionBD] INICIO", "DEBUG");
            
            if ((this.oracleUrl!=null) && (!this.oracleUrl.equalsIgnoreCase(""))){
                Debug("[ConexionBD] Conexion URL = "+this.oracleUrl, "DEBUG");
            }else{
                Debug("[ConexionBD] Conexion a armar", "DEBUG");
 
//                String host = parametrosBD.GetValue(nombreBD+"_host");
//                String port = parametrosBD.GetValue(nombreBD+"_port");
//                String servicio = parametrosBD.GetValue(nombreBD+"_service");
//                
//                String isSID = parametrosBD.GetValue(nombreBD+"_isSID");
//            
//                connectionURL = conexionDB.getURLConexion(host, port, servicio, isSID.equalsIgnoreCase("false"));
 
            }
            
            if (conexionDB.OpenDataBase(this.oracleUrl, this.oracleUser, this.oraclePass, this.oracleTimeOut)){
                Debug("[ConexionBD] Conexion Exitosa.", "INFO");
                retorno = true;
            }else{
                Debug("[ConexionBD] Conexion Fallida.", "INFO");
                Debug("[ConexionBD] Error "+conexionDB.GetErrorMessage(), "DEBUG");
            }
        }catch(Exception ex){
            trxCod = "Timeout";
            trxMsj = ex.getMessage();
        }finally{
            
        }  
        return retorno;
    }
    */
 
    /* 
     * METODO GENERICO PARA CONEXION A IvrToMQ
     * 
     * */
    private String conexionIvrToMQ(String queueName, String mensaje){
        String respuesta = "";
        
        Socket socket = null;
        
        try{
            Debug("[Conexion_MQ] *** INICIO CONEXION A MQ ***", "DEBUG");
//            Debug("[Conexion_MQ] *** HOST : "+MQhost, "DEBUG");
//            Debug("[Conexion_MQ] *** PORT : "+MQport, "DEBUG");
            
            socket = new Socket(MQhost, MQport);    
            
            int timeout = Integer.valueOf(this.Params.GetValue("SocketTimeout", "2000"));//Integer.valueOf(ObtenerParametroProperties("WS_BeneficiariTimeout", "ConfiguracionServiciosMQ.properties")).intValue();
            socket.setSoTimeout(timeout);
           BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
 
//            BufferedReader userInputBR = new BufferedReader(new InputStreamReader(System.in));
//            String userInput = userInputBR.readLine();                
           
           //Escribiendo Mensaje al IvrToMQ
           String mensajeToMQ = "IvrToMQ:"+queueName+":"+mensaje;                        
           Debug("[Conexion_MQ] *** REQ  : "+mensaje, "DEBUG");
           out.println(mensajeToMQ);
           
           //Leyendo Respuesta del IvrToMQ
           respuesta = br.readLine();
           Debug("[Conexion_MQ] *** RESP : "+respuesta, "DEBUG");
 
       }catch (UnknownHostException e) {
           Debug("[Conexion_MQ] - UnknownHostException "+ e.getMessage(), "DEBUG");
           respuesta = "TimeOut";
           trxCod = "TimeOut";
           trxMsj = e.getMessage();
       }catch (IOException e) {
           Debug("[Conexion_MQ] - IOException "+ e.getMessage(), "DEBUG");
           respuesta = "TimeOut";
           trxCod = "TimeOut";
           trxMsj = e.getMessage();
       }catch (Exception e) {
           Debug("[Conexion_MQ] - Exception "+ e.getMessage(), "DEBUG");
           respuesta = "Error";
           trxCod = "Error";
           trxMsj = e.getMessage();
       }finally{
           if (socket != null){
               try {
                   socket.close();
               } catch (IOException e1) {
                   
                   e1.printStackTrace();
               }                
           }
           Debug("[Conexion_MQ] *** FIN CONEXION A MQ ***", "DEBUG");
       }
        
        return respuesta;
    }
 
    
    /*     * 
     * Tipo MQ
     * Nombre Servicio SFISERB850C
     * Queue Name REQ --> SFISERB850C.REQ
     * Queue Name RESP --> SFISERB850C.RESP
     * 
     * */
    public boolean TestMQ(Properties datosEntrada){
        boolean retorno = false;
        String respuesta = "";
        String requerimiento = "";
        String nombreQueue = "SFISERB850C";
        
//        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
//        String fechaHoy = formatoFecha.format(new Date());
        
        try{
            Debug("["+nombreQueue+"] *** INICIO Ejecucion de la transaccion ***", "INFO");
            Debug("["+nombreQueue+"] RUT : " + datosEntrada.getProperty("RUT"), "INFO");
            
            //Parameters parametros = leerParametrosMQ(nombreQueue);
            
            /*INICIO XML*/
            requerimiento += "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MWAS><Hdr><Servicio>"+nombreQueue+"</Servicio></Hdr><Datos><![CDATA[";
            
            /*MENSAJE*/
               //DATOS HEADER
            requerimiento += "0000000000";            // INV-COD-RETORNO                             
            requerimiento += this.Rellena(""+nombreQueue+"", " ", 50, 1);            // INV-NOMBRE-SERVICIO
            requerimiento += "000003290";            // INV-LARGO-MENSAJE
            
            //DATOS ENCABEZADO
            requerimiento += this.Rellena("15", "0", 6, 0);        // L850C-CODIGO-CANAL
            requerimiento += this.Rellena("", " ", 8, 1);        // L850C-CODIGO-USUARIO
//            requerimiento += parametros.GetValue(nombreQueue+"-COD-OFICINA", "0001");                // L850C-COD-OFICINA
//            requerimiento += parametros.GetValue(nombreQueue+"-TERM-FISICO", "0001");                // L850C-TERM-FISICO
//            requerimiento += this.Rellena(datosEntrada.getProperty("CODENT"), " ", 4, 1);        // L850C-COD-ENT 810
            requerimiento += "CL";                                                    // L850C-CODIGO-PAIS
            
            //DATOS PAGINACION
            requerimiento += "N";                                // L850C-IND-PAGINACION
            requerimiento += this.Rellena("", " ", 200, 1);        // L850C-CLAVE-INICIO
            requerimiento += this.Rellena("", " ", 200, 1);        // L850C-CLAVE-FIN
            requerimiento += "000";                                // L850C-PANTALLA-PAG
            requerimiento += "N";                                // L850C-IND-MAS-DATOS
            requerimiento += this.Rellena("", " ", 90, 1);        // L850C-MAS-DATOS
            
            //DATOS ENTRADA
            requerimiento += "    ";                            // L850C-SUCURSAL
            requerimiento += "    ";                            // L850C-DEPARTAMENTO
            requerimiento += this.Rellena(datosEntrada.getProperty("RUT"), "0", 10, 0);    // L850C-RUT
//            requerimiento += datosEntrada.getProperty("COD-FAMILIA");    // L850C-COD-FAMILIA --> 6=CREDITOS CONSUMO; 7 CREDITOS HIPOTECARIO
            requerimiento += "C";                                // L850C-TIPOIDENTIFICACION
            
            
            requerimiento += "]]></Datos></MWAS>";
            /*FIN XML*/
            
            respuesta = conexionIvrToMQ(nombreQueue, requerimiento);
 
            if (!respuesta.equals("TimeOut") && !respuesta.equals("Error")){                
               
            }
        }catch (Exception e){
            
        }
        
        
        return retorno;
    }
    
    
   /* public JSONObject MWLIBExecute(String service, Map<String, String> parametros) throws JSONException{
        JSONObject result = new JSONObject();
        
        try{
//            System.out.println("INICIO MWLIBExecute");
             Debug("INICIO MWLIBExecute", "INFO");
 
            List<DataModel> request = new ArrayList<DataModel>();
            
            Set<String> keys = parametros.keySet();
            for (String key : keys) {
                request.add(new FieldDataModel(key, parametros.get(key)));
            }
            
            MWObject mwo = new MWObject(service);            
            List<DataModel> response = mwo.execute(request);
            
             Debug("MWLIBExecute RESPONSE: "+response.size(), "INFO");
//            System.out.println("MWLIB RESPONSE: "+response.size());
                        
            for (DataModel data : response){
                Map<String, String> attributes = data.getAttributes();
 
//                System.out.println("MWLIBExecute MW DATA: "+data.getName());  
                Debug("MWLIBExecute MW DATA: "+data.getName(), "DEBUG");
                Set<String> keysAttributes = attributes.keySet();
                for (String key : keysAttributes) {
//                    System.out.println("\tKey: "+key+ " Value: "+attributes.get(key));
                    Debug("MWLIBExecute \tKey: "+key+ " Value: "+attributes.get(key), "DEBUG");
                    result.put(key, attributes.get(key));
                }            
            }
            result.put("COD", "OK");
            result.put("MSG", "");
        }catch (MWObjectException e){
//            System.out.println("EXCEPCION MWObjectException "+e.getMessage());    
            Debug("EXCEPCION MWObjectException "+e.getMessage(), "INFO");
            result.put("COD", "MWObjectException");
            result.put("MSG", e.getMessage());
//            e.printStackTrace();
        }catch (Exception e){
//             Debug("MWLIB EXCEPTION", "INFO");
            e.printStackTrace();
            Debug("EXCEPCION Exception "+e.getMessage(), "INFO");
            result.put("COD", "Exception");
            result.put("MSG", e.getMessage());
        }finally{
             Debug("FIN MWLIBExecute", "INFO");
//            System.out.println("FIN MWLIB EXECUTE");
            
            
        }
        return result;
    }
    
    */
    /**
     * Metodo utilizado para Verificar si rut de empresas puede navegar para el servicio de SAT     * 
    * 
    *
    * @param rut : Rut Empresas con DV sin Guion
    * @return retorno - boolean
     * */
    public boolean VerificarRUTParaSAT(String rut){
        boolean retorno = false;
       String iniFile = "";
       String fileName = "rutero_sat.ini";
 
//        String catalina = System.getProperty("catalina.base");
//        if(catalina != null)
//            iniFile = catalina+"\\lib\\Santander\\"+fileName;
//        else
//            iniFile = "D:\\Documentos\\Santander\\MQ\\Ini\\"+fileName;
       
       try {
           FileInputStream fis = new FileInputStream(iniFile);
           BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        
           String line = null;
           while ((line = br.readLine()) != null) 
               if (line.equalsIgnoreCase(rut))
                   retorno = true;    
           
           br.close();            
       } catch (IOException e) {
           DebugError("Error en VerificarRUTParaSAT "+e.getMessage());
           e.printStackTrace();            
       }
       return retorno;
    }

    // ------------------------------------------------------------
    // WS SOAP
    // ------------------------------------------------------------
    public JSONObject getCONPorRutTitularYBeneficiario(String canalId, String usuarioAlt, String rut) { 
    	String url = this.getParametro("urlWSCONPorRutTitularYBeneficiario");
    	return getCONPorRutTitularYBeneficiario(canalId, usuarioAlt, rut, url);
    }
    
    public JSONObject getCONPorRutTitularYBeneficiario(String canalId, String usuarioAlt, String rut, String url) {   
		JSONObject result = new JSONObject();
		try {		
			WsRutTitularYBeneficiario ws = new WsRutTitularYBeneficiario(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
		    
			result=ws.getDatos(canalId, usuarioAlt, rut);
			System.out.println("result getCONPorRutTitularYBeneficiario.... " + result.toString()  );
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK"); 
				
				//result.put("subsegmento", result.getString("subsegmento"));
			}else{
				result.put("resWS", "NOK");
			}
		} catch (Exception e) {
			DebugError("Error en getCONPorRutTitularYBeneficiario "+e.getMessage());
			e.printStackTrace();
		}
		
		return result;
    }
    
    public JSONObject getConsultaSaldos(String canalId, String usuarioAlt, String cuenta) {
    	String url = this.getParametro("urlWSConsultaSaldos");
    	return getConsultaSaldos(canalId, usuarioAlt, cuenta, url);
    }
    
    public JSONObject getConsultaSaldos(String canalId, String usuarioAlt, String cuenta, String url) {   
		JSONObject result = new JSONObject();
		try {
			WsConsultaSaldos_CL1MDMPV3 ws = new WsConsultaSaldos_CL1MDMPV3(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
						
			result=ws.getDatos(canalId, usuarioAlt, cuenta);   
			if (result.getString("status").equals("SUCCESS")){            	  
				result.put("resWS", "OK");
				
				result.put("importe8", result.getLong("importe8"));
				result.put("importe7", result.getLong("importe7"));	
				result.put("importe9", result.getLong("importe9"));		
				result.put("importe11", result.getLong("importe11"));		
				result.put("importe10", result.getLong("importe10"));		
				result.put("importe1", result.getLong("importe1"));
				result.put("importe5", result.getLong("importe5"));
			}else{
				result.put("resWS", "NOK");
				
				result.put("importe8", 0);
				result.put("importe7", 0);	
				result.put("importe9", 0);		
				result.put("importe11",0);		
				result.put("importe10",0);		
				result.put("importe1", 0);
				result.put("importe5", 0);	
			}
		} catch (Exception e) {
			DebugError("Error en getConsultaSaldos "+e.getMessage());
			e.printStackTrace();
		}
		
		return result;   
    }
    
    public JSONObject getSaldoAhorros(String canalId, String usuarioAlt, String cuenta) {
    	String url = this.getParametro("urlWSSaldoAhorros");
    	return getSaldoAhorros(canalId, usuarioAlt, cuenta, url);
    }

    public JSONObject getSaldoAhorros(String canalId, String usuarioAlt, String cuenta, String url) {   
		JSONObject result = new JSONObject();
		try {
			
			WsSaldoAhorros ws = new WsSaldoAhorros(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
						
			result=ws.getDatos(canalId, usuarioAlt, cuenta);   
			if (result.getString("status").equals("SUCCESS")){            	  
				result.put("resWS", "OK");
				
				result.put("saldototal", result.getLong("saldototal"));
				result.put("saldodisp", result.getLong("saldodisp"));				
				result.put("retencion_vencehoy", result.getLong("retencion_vencehoy"));
				result.put("retencion_no_vencehoy", result.getLong("retencion_no_vencehoy"));
				result.put("retencion_otro", result.getLong("retencion_otro"));
			}else{
				result.put("resWS", "NOK");
				
				result.put("saldototal", 0);
				result.put("saldodisp", 0);				
				result.put("retencion_vencehoy",0);
				result.put("retencion_no_vencehoy", 0);
				result.put("retencion_otro", 0);		
			}
		} catch (Exception e) {
			DebugError("Error en getSaldoAhorros "+e.getMessage());
			e.printStackTrace();
		}
		
		return result;   
    }

    public JSONObject getCartolaOnLine(String canalId, String usuarioAlt, String cuenta) {
    	String url = this.getParametro("urlWSCartolaOnLine");
    	return getCartolaOnLine(canalId, usuarioAlt, cuenta, url);
    }

    public JSONObject getCartolaOnLine(String canalId, String usuarioAlt, String cuenta, String url) {   
		JSONObject result = new JSONObject();
		try {
			WsCartolaOnLine ws = new WsCartolaOnLine(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
						
			result=ws.getDatos(canalId, usuarioAlt, cuenta);   
			if (result.getString("status").equals("SUCCESS")){            	  
				result.put("resWS", "OK");
				
				result.put("indice", result.getString("indice"));
				JSONObject param1 =result.getJSONObject("trx_datos_respuesta1");
		    	result.put("trx_datos_respuesta1", param1 );
		    	JSONObject param2=result.getJSONObject("trx_datos_respuesta2");
		    	result.put("trx_datos_respuesta2", param2 );
		    	JSONObject param3 =result.getJSONObject("trx_datos_respuesta3");
		    	result.put("trx_datos_respuesta3", param3 );
		    	JSONObject param4 =result.getJSONObject("trx_datos_respuesta4");
		    	result.put("trx_datos_respuesta4", param4 );
		    	JSONObject param5 =result.getJSONObject("trx_datos_respuesta5");
		    	result.put("trx_datos_respuesta5", param5 );
			}else{
				result.put("resWS", "NOK");
				
				result.put("trx_datos_respuesta1", "");
				result.put("trx_datos_respuesta2", "");
				result.put("trx_datos_respuesta3", "");
				result.put("trx_datos_respuesta4", "");
				result.put("trx_datos_respuesta5", "");
			}
		} catch (Exception e) {
			DebugError("Error en getConsultaSaldos "+e.getMessage());
			e.printStackTrace();
		}
		return result;   
    }
    
    public JSONObject getCLIMDKC01_ConsultaSaldos(String canalId, String usuarioAlt, String cuenta) {
    	String url = this.getParametro("urlWSCLIMDKC01ConsultaSaldos");
    	return getCLIMDKC01_ConsultaSaldos(canalId, usuarioAlt, cuenta, url);
    }
    
    public JSONObject getCLIMDKC01_ConsultaSaldos(String canalId, String usuarioAlt, String cuenta, String url) {   
		JSONObject result = new JSONObject();
		try {
			WsConsultaSaldos_CLIMDKC01 ws = new WsConsultaSaldos_CLIMDKC01(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
						
			result=ws.getDatos(canalId, usuarioAlt, cuenta);   
			if (result.getString("status").equals("SUCCESS")){            	  
				result.put("resWS", "OK");
				
				result.put("saldototal", result.getLong("saldototal"));
				result.put("saldodisp", result.getLong("saldodisp"));				
				result.put("retencion_vencehoy", result.getLong("retencion_vencehoy"));
				result.put("retencion_no_vencehoy", result.getLong("retencion_no_vencehoy"));
				result.put("retencion_otro", result.getLong("retencion_otro"));
				result.put("negativo", result.getString("negativo"));
				
				result.put("cheque1", result.getString("cheque1"));
				result.put("cheque2", result.getString("cheque2"));
				result.put("cheque3", result.getString("cheque3"));
				result.put("cheque4", result.getString("cheque4"));
				result.put("cheque5", result.getString("cheque5"));
				
				result.put("saldoau", result.getLong("saldoau"));

			}else{
				result.put("resWS", "NOK");
				
				result.put("saldototal", 0);
				result.put("saldodisp", 0);				
				result.put("retencion_vencehoy",0);
				result.put("retencion_no_vencehoy", 0);
				result.put("retencion_otro", 0);	
				result.put("negativo", "");
				result.put("cheque1", "");
				result.put("cheque2", "");
				result.put("cheque3", "");
				result.put("cheque4", "");
				result.put("cheque5", "");
				result.put("saldoau", 0);

			}
		} catch (Exception e) {
			DebugError("Error en getConsultaSaldos "+e.getMessage());
			e.printStackTrace();
		}
		
		return result;   
    }
    
    public JSONObject getCrucePreHechoIVR(String canalId, String usuarioAlt, String rut) {
    	String url = this.getParametro("urlWSCrucePreHechoIVR");
    	return getCrucePreHechoIVR(canalId, usuarioAlt, rut, url);
    }
    
    public JSONObject getCrucePreHechoIVR(String canalId, String usuarioAlt, String rut, String url) {   
		JSONObject result = new JSONObject();
		try {
			
			WsCrucePreHechoIVR ws = new WsCrucePreHechoIVR(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			ws.setCodBanco(this.getParametro("CodBanco"));
			// --
						
			result=ws.getDatos(canalId, usuarioAlt, rut);   
			
			if (result.getString("status").equals("SUCCESS")){
				if (result.getString("res").equals("OK")){
					result.put("resWS", "OK");
					
					if(!result.getString("trx_datos_respuesta_cuenta_corriente").isEmpty()){
						JSONObject cuenta_corriente =result.getJSONObject("trx_datos_respuesta_cuenta_corriente");
						result.put("trx_datos_respuesta_cuenta_corriente", cuenta_corriente);
					}
					   
					if(!result.getString("trx_datos_respuesta_linea").isEmpty()){
						JSONObject linea =result.getJSONObject("trx_datos_respuesta_linea");
						result.put("trx_datos_respuesta_linea", linea);
					}
					 
					if(!result.getString("trx_datos_respuesta_chequera").isEmpty()){
						JSONObject chequera =result.getJSONObject("trx_datos_respuesta_chequera");
						result.put("trx_datos_respuesta_chequera", chequera);                  		    
					}
					  
					if(!result.getString("trx_datos_respuesta_cuenta_vista").isEmpty()){
						JSONObject cuenta_vista =result.getJSONObject("trx_datos_respuesta_cuenta_vista");
						result.put("trx_datos_respuesta_cuenta_vista", cuenta_vista);                		   
					}
					  
					if(!result.getString("trx_datos_respuesta_tarjeta_credito").isEmpty()){
						JSONObject tarjeta_credito =result.getJSONObject("trx_datos_respuesta_tarjeta_credito");
						result.put("trx_datos_respuesta_tarjeta_credito", tarjeta_credito);    
					}
				}else{
					result.put("resWS", "NOK");
					
					result.put("CODERROR", "res NOK");
				}
			}else{
				result.put("resWS", "NOK");
			}
		} catch (Exception e) {
			DebugError("Error en getCrucePreHechoIVR "+e.getMessage());
			e.printStackTrace();
		}
		
		return result;   
    }
    
    public JSONObject getEstadoServicio(String canalId, String usuarioAlt, String rut, String servicio, String activacion) {
    	String url = this.getParametro("urlWSEstadoServicio");
    	return getEstadoServicio(canalId, usuarioAlt, rut, servicio, activacion, url);
    }
    
    public JSONObject getEstadoServicio(String canalId, String usuarioAlt, String rut, String servicio, String activacion, String url) {   
		JSONObject result = new JSONObject();
		try {
			WsEstadoServicio ws = new WsEstadoServicio(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
						
			result=ws.getDatos(canalId, usuarioAlt, rut, servicio, activacion);   
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");                        
			}else{
				result.put("resWS", "NOK");
			}
		} catch (Exception e) {
			DebugError("Error en getEstadoServicio "+e.getMessage());
			e.printStackTrace();
		}
		
		return result;   
    }
    
    public JSONObject getConsultaServiciosCliente(String canalId, String usuarioAlt, String rut) {
    	String url = this.getParametro("urlWSConsultaServiciosCliente");
    	return getConsultaServiciosCliente(canalId, usuarioAlt, rut, url);
    }
    
    public JSONObject getConsultaServiciosCliente(String canalId, String usuarioAlt, String rut, String url) {   
    	JSONObject result = new JSONObject();
		try {
			// TODO: no funciona / estructura wsdl
			WsConsultaServiciosCliente ws = new WsConsultaServiciosCliente(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
		
			// --
					
						
			result=ws.getDatos(canalId, usuarioAlt, rut);
			
			result.put("resWS", "NOK");
			result.put("COD", "NOK");
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "NOK");
				
				if (result.getString("estadoClave").equals("OK")){
					result.put("resWS", "OK");
					result.put("COD", result.getString("servicios"));              
				} else {
					result.put("CODERROR", "Estado Clave NOK");
				}
			} else {
				result.put("resWS", "NOK");
			}
		} catch (Exception e) {
			DebugError("Error en getConsultaServiciosCliente "+e.getMessage());
			e.printStackTrace();
		}
		
		return result;   
	}
    
    public JSONObject getDatosBasicosPersona(String canalId, String usuarioAlt, String rut) {
    	String url = this.getParametro("urlWSDatosBasicoPersona");
    	return getDatosBasicosPersona(canalId, usuarioAlt, rut, url);
    }

    public JSONObject getDatosBasicosPersona(String canalId, String usuarioAlt, String rut, String url) {   
		JSONObject result = new JSONObject();
		try {
			WsDatosPersonas ws = new WsDatosPersonas(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
						
			result=ws.getDatos(canalId, usuarioAlt, rut);
			System.out.println("result getDatosBasicosPersona.... " + result.toString()  );
			System.out.println("result Status.... " + result.getString("status")  );
			System.out.println("prueba de cambio" );
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");
				
				result.put("subsegmento", result.getString("subsegmento"));
				result.put("numero", result.getString("numero"));
				result.put("A_PATERNO", result.getString("A_PATERNO"));
				result.put("A_MATERNO", result.getString("A_MATERNO"));
				result.put("NOMBRE", result.getString("NOMBRE"));
				result.put("F_NACIMIENTO", result.getString("F_NACIMIENTO"));
				result.put("contrato", result.getString("contrato")); 
				
			}else{
				result.put("resWS", "NOK");
			}
		} catch (Exception e) {
			DebugError("Error en getDatosBasicosPersona "+e.getMessage());
			e.printStackTrace();
		}
		
		return result;
    }
    
    public JSONObject getValidacionPin(String usuarioAlt,String canalID,String canal,String pan,String pin, String rut) {
    	String url = this.getParametro("urlWSValidacionPin");
    	return getValidacionPin(usuarioAlt, canalID, canal, pan, pin, rut, url);
    }
    
    public JSONObject getValidacionPin(String usuarioAlt,String canalID,String canal,String pan,String pin, String rut, String url) {   
		JSONObject result = new JSONObject();
		try {
			WsConsultaPin_SMAMP228 ws = new WsConsultaPin_SMAMP228(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
						
			result=ws.getDatos( canalID, usuarioAlt, pan, pin, rut);   
			if (result.getString("status").equals("SUCCESS")){
				if (result.getString("estado").equals("00")){
					result.put("resWS", "OK");
				}else{
					result.put("resWS", "NOK");
					result.put("CODERROR", "ESTADO PIN: "+result.getString("estado"));
				}
			} else {
				result.put("resWS", "NOK");
			}
			
		} catch (Exception e) {
			DebugError("Error en getValidacionPin "+e.getMessage());
			e.printStackTrace();
		}   
		
		return result;
    }
    
    public JSONObject getCambioClavePin(String canalId, String usuarioAlt, String rut, String pin) {
    	JSONObject result = new JSONObject();
    	try {
	    	String url = this.getParametro("urlWSClavePingClienteHB");
	    	WsClavePingClienteHB ws = new WsClavePingClienteHB(url);
	    	
	    	// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
	    	
			result=ws.getDatos(canalId, usuarioAlt, rut, pin); 
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");
			}else{
				result.put("resWS", "NOK");
			}
    	} catch (Exception e) {
			DebugError("Error en getCambioClavePin "+e.getMessage());
			e.printStackTrace();
		}
    	System.out.println("result getCambioClavePin.... " + result.toString()  );
    	return result;
    }
    
    public JSONObject getSubSegmento (String canalId, String usuarioAlt, String subsegmento) {
    	JSONObject result = new JSONObject();
    	try {
    		String url = this.getParametro("urlWSSubSegmento");
    		WsSubSegmento ws = new WsSubSegmento(url);
	    	
	    	// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
	    	
			result=ws.getDatos(canalId, usuarioAlt, subsegmento); 
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");
				result.put("subsegmento",  result.getString("subsegmento"));

			}else{
				result.put("resWS", "NOK");
			}
	    } catch (Exception e) {
			DebugError("Error en getSubSegmento "+e.getMessage());
			e.printStackTrace();
		}
    	System.out.println("result getSubSegmento.... " + result.toString()  );
    	return result;
    }
    
    public JSONObject getTodasTarjetasRut (String canalId, String usuarioAlt, String rut) {
    	JSONObject result = new JSONObject();
    	try {
    		String url = this.getParametro("urlWSTodasTarjetasRut");
    		WsTodasTarjetasRut ws = new WsTodasTarjetasRut(url);
	    	
	    	// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
			
			result=ws.getDatos(canalId, usuarioAlt, rut);
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");
			}else{
				result.put("resWS", "NOK");
			}
    	} catch (Exception e) {
			DebugError("Error en getTodasTarjetasRut "+e.getMessage());
			e.printStackTrace();
		}
    	System.out.println("result getTodasTarjetasRut.... " + result.toString()  );
    	return result;
    }
    
    public JSONObject getUsuarioPuntoVenta (String canalId, String usuarioAlt, String rut, String pin, String vinculacion) {
    	JSONObject result = new JSONObject();
    	try {
    		String url = this.getParametro("urlWSUsuarioPuntoVenta");
    		WsUsuarioPuntoVenta ws = new WsUsuarioPuntoVenta(url);
	    	
	    	// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
			
			result=ws.getDatos(canalId, usuarioAlt, rut, pin, vinculacion);
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");
			}else{
				result.put("resWS", "NOK");
			}
    	} catch (Exception e) {
			DebugError("Error en getUsuarioPuntoVenta "+e.getMessage());
			e.printStackTrace();
		}
    	System.out.println("result getUsuarioPuntoVenta.... " + result.toString()  );
    	return result;
    }
    
    public JSONObject getSolicitaDesafio (String canalId, String usuarioAlt, String rut) {
    	JSONObject result = new JSONObject();
    	try {
    		String url = this.getParametro("urlWSSolicitaDesafio");
    		WsSolicitaDesafio ws = new WsSolicitaDesafio(url);
	    	
	    	// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
			
			result=ws.getDatos(canalId, usuarioAlt, rut);
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");
			}else{
				result.put("resWS", "NOK");
			}
    	} catch (Exception e) {
			DebugError("Error en getSolicitaDesafio "+e.getMessage());
			e.printStackTrace();
		}
    	System.out.println("result getSolicitaDesafio.... " + result.toString()  );
    	
    	return result;
    }
    
    public JSONObject getValidaDesafio (String canalId, String usuarioAlt, String rut, String matriz) {
    	JSONObject result = new JSONObject();
    	try {
    		String url = this.getParametro("urlWSValidaDesafio");
    		WsValidaDesafio ws = new WsValidaDesafio(url);
	    	
	    	// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
			
			result=ws.getDatos(canalId, usuarioAlt, rut, matriz);
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");
			}else{
				result.put("resWS", "NOK");
			}
    	} catch (Exception e) {
			DebugError("Error en getValidaDesafio "+e.getMessage());
			e.printStackTrace();
		}
    	System.out.println("result getValidaDesafio.... " + result.toString()  );
    	
    	return result;
    }
    
    public JSONObject getActivacionCoordenadas (String canalId, String usuarioAlt, String rut, String nrotarjeta) {
    	JSONObject result = new JSONObject();
    	try {
    		String url = this.getParametro("urlWSActivacionCoordenadas");
    		WsActivacionCoordenadas ws = new WsActivacionCoordenadas(url);
	    	
	    	// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
			
			result=ws.getDatos(canalId, usuarioAlt, rut, nrotarjeta);
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");
			}else{
				result.put("resWS", "NOK");
			}
    	} catch (Exception e) {
			DebugError("Error en getActivacionCoordenadas "+e.getMessage());
			e.printStackTrace();
		}
    	System.out.println("result getActivacionCoordenadas.... " + result.toString()  );
    	
    	return result;
    }
    
    public JSONObject getConsultaTelefono(String telefono) {
    	String url = this.getParametro("urlWSObtieneRUT");
    	return getConsultaTelefono(telefono, url);
    }

    public JSONObject getConsultaTelefono(String telefono, String url) {   
		JSONObject result = new JSONObject();
		try {
			
			WsObtieneRUTClient ws = new WsObtieneRUTClient(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
						
			System.out.println("Inicio getConsultaTelefono.... "   );
			result=ws.obtenerRUT(telefono);  
			
			if(result.length()>0){
				if (result.getString("rut")!=null && result.getString("rut")!= ""){
					result.put("resWS", "OK");
					result.put("rut", result.getString("rut"));        
				}else{
					result.put("resWS", "NOK");
					result.put("descrip",result.getString("descrip"));
				}
			}
		} catch (Exception e) {
			DebugError("Error en getConsultaTelefono"+e.getMessage());
			e.printStackTrace();
		}   
		   
		System.out.println("result getConsultaTelefono.... " + result.toString()  );
		return result;
    }
 
    public JSONObject getUserTokenList (String rut) {
    	JSONObject result = new JSONObject();
    	try {
    		String url = this.getParametro("urlWSUserTokenList");
    		WsUserTokenList ws = new WsUserTokenList(url);
	    	
	    	// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
			
			result=ws.getDatos(rut);
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");
			}else{
				result.put("resWS", "NOK");
			}
    	} catch (Exception e) {
			DebugError("Error en getUserTokenList "+e.getMessage());
			e.printStackTrace();
		}
    	System.out.println("result getUserTokenList.... " + result.toString()  );
    	
    	return result;
    }
    
    public JSONObject getCambioNumeroCelular(String canalId, String usuarioAlt, String rut) {
    	JSONObject result = new JSONObject();
    	try {
    		String url = this.getParametro("urlWSCambioNumeroCelular");
    		WsCambioNumeroCelular ws = new WsCambioNumeroCelular(url);
	    	
	    	// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
			
			result=ws.getDatos(canalId, usuarioAlt, rut);
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");
			}else{
				result.put("resWS", "NOK");
			}
    	} catch (Exception e) {
			DebugError("Error en getCambioNumeroCelular "+e.getMessage());
			e.printStackTrace();
		}
    	System.out.println("result getCambioNumeroCelular.... " + result.toString()  );
    	
    	return result;
    }
    
    public JSONObject getEstadoDispositivo(String canalId, String usuarioAlt, String rut) {
    	JSONObject result = new JSONObject();
    	try {
    		String url = this.getParametro("urlWSEstadoDispositivo");
    		WsEstadoDispositivo ws = new WsEstadoDispositivo(url);
	    	
	    	// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
			
			result=ws.getDatos(canalId, usuarioAlt, rut);
			
			if (result.getString("status").equals("SUCCESS")){
				result.put("resWS", "OK");
			}else{
				result.put("resWS", "NOK");
			}
    	} catch (Exception e) {
			DebugError("Error en getEstadoDispositivo "+e.getMessage());
			e.printStackTrace();
		}
    	System.out.println("result getEstadoDispositivo.... " + result.toString()  );
    	
    	return result;
    }
    
    public JSONObject getAdaptorAutht(String datos, String usuario, String password, String urlws) {
    	JSONObject result = new JSONObject();
    	try {
    		String url = this.getParametro("urlWSAdaptorAutht");
    		WsServiceInNormalAuth ws = new WsServiceInNormalAuth(url);
	    	
	    	// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			// --
			
			result=ws.getDatos(datos, usuario, password, urlws);
			
			
    	} catch (Exception e) {
			DebugError("Error en getAdaptorAutht "+e.getMessage());
			e.printStackTrace();
		}
    	System.out.println("result getAdaptorAutht.... " + result.toString()  );
    	
    	return result;
    }
    
    
    
    
    public JSONObject getCONCruceDeProductos(String canalId, String usuarioAlt, String rut) {
    	String url = this.getParametro("urlWSCONCruceDeProductos");
    	return getCONCruceDeProductos(canalId, usuarioAlt, rut, url);
    }
    
    public JSONObject getCONCruceDeProductos(String canalId, String usuarioAlt, String rut, String url) {   
		JSONObject result = new JSONObject();
		try {
			
			WsCONCruceDeProductos ws = new WsCONCruceDeProductos(url);
			
			// --
			ws.setTrueStorePath(this.getParametro("keyStorePath"));
			ws.setTrueStoreType(this.getParametro("Type"));
			ws.setTrueStorePassword(this.getParametro("Password"));
			
			// --
						
			result=ws.getDatos(canalId, usuarioAlt, rut);   
			
			if (result.getString("status").equals("SUCCESS")){
				if (result.getString("res").equals("OK")){
					result.put("resWS", "OK");				
				
				}else{
					result.put("resWS", "NOK");
					
					result.put("CODERROR", "res NOK");
				}
			}else{
				result.put("resWS", "NOK");
			}
		} catch (Exception e) {
			DebugError("Error en getCONCruceDeProductos "+e.getMessage());
			e.printStackTrace();
		}
		
		return result;   
    }
    
    
    
    // ------------------------------------------------------------
    
    private TrustManager[] trustAllCertificates() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
 
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {}
 
            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {}
 
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
                // or you can return null too
                return new java.security.cert.X509Certificate[0];
            }
            
        }};
        
        return trustAllCerts;
    }
    
    
    
    private SSLContext disableSSLCertificateChecking() {
       try {
           SSLContext sc = SSLContext.getInstance("SSL");          
           sc.init(null, this.trustAllCertificates(), new SecureRandom());
       
           HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
           HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
               public boolean verify(String string, SSLSession sslSession) {
                   return true;
               }
           });
           
           return sc;
       } catch (NoSuchAlgorithmException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       } catch (KeyManagementException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       
        return null;
    }
    
    public JSONObject getWSLogin(String rut, String clave) {
    	
    	System.out.println("Inicio WSLogin url");
    	JSONObject result = new JSONObject();    	
    	try {
			result.put("status", "NOK");
			result.put("codigoError", "999");
			result.put("respuesta", "ERROR");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
    	try {
    		// Body Parameter
        	JSONObject bodyParam = new JSONObject();        	
			bodyParam.put("RUTCLIENTE", rut);
			bodyParam.put("PASSWORD", clave);
	    	bodyParam.put("CANAL_LOGICO", "001");
	    	bodyParam.put("CANAL_FISICO", "IVR");    	
	    	
	    	//---
	    	 DefaultClientConfig config = new DefaultClientConfig();
		        Map<String, Object> properties = config.getProperties();
		        HTTPSProperties httpsProperties = new HTTPSProperties(new HostnameVerifier() {
		            @Override
		            public boolean verify(String s, SSLSession sslSession) {
		                return true;
		            }
		        }, disableSSLCertificateChecking());
		        properties.put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, httpsProperties);
	    	//---	  
	        
	        // Create Jersey client
	    	Client client = Client.create(config);
	    	System.out.println("crea el cliente");
	    	System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");	    	
	    
	    	WebResource resource = client.resource(this.urlWSLogin);	    	
	    	System.out.println("WSLogin resurce"+ resource.toString());
	    	System.out.println("WSLogin url prueba"+this.urlWSLogin);
	    	
	    	ClientResponse response = resource
	    			.accept(MediaType.APPLICATION_JSON)
	    			.header("content-type", MediaType.APPLICATION_JSON)
	                .type("application/json")
	                .post(ClientResponse.class, bodyParam.toString());	    	
	    	System.out.println("WSLoginresponse"+resource.toString());
	    
	    	// Parsear Resultado
	    	String output = response.getEntity(String.class);	    	
	    	JSONObject jsonResponse = new JSONObject(output);
	    	System.out.println("WSLogin jsonResponse="+jsonResponse.toString());
	    	
    		if (jsonResponse.has("DATA")) {
    			String respuesta = "";    			
    			JSONObject jsonData = jsonResponse.getJSONObject("DATA");
    			Debug("jsonData TRX_WS_LOGIN: " +jsonData.toString(), "INFO");    			
    			String codigoError = jsonData.getString("STATUS");
    			switch (codigoError) {
					case "300":
						respuesta="NOEXISTE";
						break;
					
					case "302":
						respuesta="ERRONEA";
						break;
				
					case "504":
						respuesta="BLOQUEADA";
						break;
	
					case "500":
					case "501":
					case "502":
					case "301":
					case "503":
						respuesta="DESBORDE";
						break;
					
					default:
						respuesta="ERROR";
					break;
				}    			
    			result.put("status", "NOK");
	    		result.put("respuesta", respuesta);
	    		result.put("codigoError", codigoError);
    		} else {
    			result.put("status", "OK");
		    	result.put("respuesta", "CORRECTA");
		    	result.put("codigoError", "");
    		}
    		
    		
    		
    	//} catch (JSONException | KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | KeyManagementException e) {
      	} catch (JSONException  e) {
      		
    		// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error"+e.toString());
		}
    	
    	System.out.println("WSLogin resultado="+result.toString());
    	
    	return result;
    }
  public JSONObject getTokenClaveDigital()         {
 	
 	System.out.println("Inicio getTokenClaveDigital ");
 	JSONObject result = new JSONObject();    	
 	try {
			result.put("status", "NOK");
			result.put("codigoError", "999");
			result.put("respuesta", "ERROR");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
 	try {

 		String _responseBody;
		   // 	System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");	    	
		    	System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");	    	/// Modificacion 1

/* 			        org.apache.http.impl.client.CloseableHttpClient client =  org.apache.http.impl.client.HttpClients.custom()
		                .setSSLSocketFactory(new org.apache.http.conn.ssl.SSLConnectionSocketFactory(org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, (TrustStrategy) new org.apache.http.conn.ssl.TrustSelfSignedStrategy()).build(), org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE))
		                .build();*/
 			        org.apache.http.impl.client.CloseableHttpClient client = org.apache.http.impl.client.HttpClientBuilder.create().setSSLSocketFactory(new org.apache.http.conn.ssl.SSLConnectionSocketFactory(org.apache.http.ssl.SSLContexts.custom().build(), new String[] { "TLSv1.2" }, null, org.apache.http.conn.ssl.SSLConnectionSocketFactory.getDefaultHostnameVerifier()))
 		                    .build();    /// Modificacion 2

 			        
 			        
		        System.out.println("getTokenClaveDigital Getting an access token...");
		   // 	System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");	    	

		        org.apache.http.client.methods.HttpPost tokenRequest = new org.apache.http.client.methods.HttpPost(this.urlWSEstadoClaveDigitalToken);
		        List<org.apache.http.NameValuePair> data = new ArrayList<org.apache.http.NameValuePair>();
		        data.add(new org.apache.http.message.BasicNameValuePair("grant_type", this.getParametro("grant_type")));
		        data.add(new org.apache.http.message.BasicNameValuePair("client_id", this.getParametro("client_id")));
		        data.add(new org.apache.http.message.BasicNameValuePair("client_secret", this.getParametro("client_secret")));
		        data.add(new org.apache.http.message.BasicNameValuePair("scope", "Internet_Clientes_Personas"));

		        tokenRequest.setEntity(new org.apache.http.client.entity.UrlEncodedFormEntity(data));
		        org.apache.http.client.methods.CloseableHttpResponse tokenResponse = client.execute(tokenRequest);
		        
		        /*
		         * inicio
		         */
		        
		        System.out.printf("getWSEstadoClaveDigital _ProtocolVersion:", tokenRequest.getProtocolVersion());  ///Modificacion 3
		        
		        org.apache.http.StatusLine statusLine = tokenResponse.getStatusLine();
		        if (statusLine == null){
			        System.out.printf("getTokenClaveDigital statusLine is Null");
	        		throw new IOException("statusLine is Null");

		        }
		        
		        System.out.printf("getTokenClaveDigital _statusCode: %d%n",statusLine.getStatusCode());
		        System.out.printf("getTokenClaveDigital _reasonPhrase: %s%n", statusLine.getReasonPhrase());

		  

		        org.apache.http.HttpEntity entity = tokenResponse.getEntity();
		        if (entity == null){
			        System.out.printf("getTokenClaveDigital entity is Null");
	        		throw new IOException("entity is Null");

		        }
		        try (InputStream is = entity.getContent();) {
		            _responseBody = org.apache.http.util.EntityUtils.toString(entity);
		        } catch (Throwable ex) {
		        	
	        		throw new IOException("entity content is Null");

		        }

		            System.out.printf("getTokenClaveDigital tokenResponse: %s%n", _responseBody);
			        JSONObject tokenObject = new JSONObject(_responseBody);
			        String accessToken = tokenObject.getString("access_token");
			        Integer expiresIn = tokenObject.getInt("expires_in");
			        Integer consented_on = tokenObject.getInt("consented_on");
			        String token_type = tokenObject.getString("token_type");

			        System.out.printf("getTokenClaveDigital Access token: %s%n", accessToken);
			        System.out.printf("getTokenClaveDigital Expires in: %d%n", expiresIn);
			        System.out.printf("getTokenClaveDigital Consented on: %d%n", consented_on);
			        System.out.printf("getTokenClaveDigital token type: %s%n", token_type);
		    			if (!accessToken.equals("")){
		    				result.put("status", "OK");
		    				result.put("respuesta", accessToken);
		    				result.put("codigoError", "");
		    			} else {
		    				result.put("status", "NOK");
		    				result.put("respuesta", "RESPUESTA VACIA");
		    				result.put("codigoError", "555");
		    			}
		
//   	} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | UnsupportedOperationException  | IOException  | JSONException  e) {
   	} catch (KeyManagementException | NoSuchAlgorithmException | UnsupportedOperationException  | IOException  | JSONException  e) {
   		
 		// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error"+e.toString());
			try {
				result.put("status", "NOK");
				result.put("codigoError", "222");
				result.put("respuesta", "ERROR LLAMADA SERVICIO");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
 	
 	System.out.println("getTokenClaveDigital resultado="+result.toString());
 	
 	return result;
 }

 
 public JSONObject getWSEstadoClaveDigital(String rut, String token)         {
 	
 	System.out.println("Inicio getWSEstadoClaveDigital ");
 	System.out.println("getWSEstadoClaveDigital Token "+token);

 	JSONObject result = new JSONObject();    	
 	try {
			result.put("status", "NOK");
			result.put("codigoError", "999");
			result.put("respuesta", "ERROR");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
 	try {

 		String _responseBodyService;
// 	 	System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");	    	
    	System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");	///" Modificacion 1    	

 		
		    /*    org.apache.http.impl.client.CloseableHttpClient client =  org.apache.http.impl.client.HttpClients.custom()
		                .setSSLSocketFactory(new org.apache.http.conn.ssl.SSLConnectionSocketFactory(org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, (TrustStrategy) new org.apache.http.conn.ssl.TrustSelfSignedStrategy()).build(), org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE))
		                .build();*/
		        
		        org.apache.http.impl.client.CloseableHttpClient client = org.apache.http.impl.client.HttpClientBuilder.create().setSSLSocketFactory(new org.apache.http.conn.ssl.SSLConnectionSocketFactory(org.apache.http.ssl.SSLContexts.custom().build(), new String[] { "TLSv1.2" }, null, org.apache.http.conn.ssl.SSLConnectionSocketFactory.getDefaultHostnameVerifier()))
	                    .build(); /// Modificacion 2
		        
			        System.out.println("Calling oauth secured service...");
			        String varUrlEstadoClave;
			        varUrlEstadoClave=this.urlWSEstadoClaveDigital.replace(":persona_id", this.Rellena(rut, "0", 11, 11));
			        System.out.printf("getWSEstadoClaveDigital rut: %s%n", rut);
			        System.out.printf("getWSEstadoClaveDigital varUrlEstadoClave: %s%n", varUrlEstadoClave);


			        org.apache.http.client.methods.HttpGet serviceRequest = new org.apache.http.client.methods.HttpGet(varUrlEstadoClave);
			        serviceRequest.addHeader("Authorization", "Bearer " + token);
			        
			        org.apache.http.client.methods.CloseableHttpResponse serviceResponse = client.execute(serviceRequest);
			        
			        /**
			         *   inicio
			         */
			        org.apache.http.StatusLine statusLineService = serviceResponse.getStatusLine();
			        if (statusLineService == null){
				        System.out.printf("getWSEstadoClaveDigital statusLineService is Null");
		        		throw new IOException("statusLineService is Null");

			        }
			        
			        System.out.printf("getWSEstadoClaveDigital _ProtocolVersion:", serviceRequest.getProtocolVersion()); /// Modificacion 3

			        
			        System.out.printf("getWSEstadoClaveDigital _statusCode: %d%n",statusLineService.getStatusCode());
			        System.out.printf("getWSEstadoClaveDigital _reasonPhrase: %s%n", statusLineService.getReasonPhrase());

			  

			        org.apache.http.HttpEntity entityService = serviceResponse.getEntity();
			        if (entityService == null){
				        System.out.printf("getWSEstadoClaveDigital entityService is Null");
		        		throw new IOException("entityService is Null");

			        }
			        try (InputStream is = entityService.getContent();) {
			            _responseBodyService = org.apache.http.util.EntityUtils.toString(entityService);
			        } catch (Throwable ex) {
			        	
		        		throw new IOException("entityService content is Null");

			        }
			        
			        /**
			         *   fin
			         */
			        JSONObject serviceObject = new JSONObject(_responseBodyService);

			        System.out.printf("getWSEstadoClaveDigital jsonResponse: %s%n", serviceObject.toString());
			        
			    	if (serviceObject.has("codPrincipalLdap")) {
			    		String codLdap=(serviceObject.has("codErrExtLdap"))?""+serviceObject.getInt("codErrExtLdap"):"0";
				        System.out.printf("getWSEstadoClaveDigital codErrExtLdap: %s\n",codLdap);

		    			if (serviceObject.getInt("codPrincipalLdap") == 000){
		    				result.put("trx_CodErrExtLdap",""+ codLdap);
		    				result.put("status", "OK");
		    				result.put("respuesta", serviceObject.getString("estadoCta"));
		    				result.put("codigoError", "");
		    			} else {
		    				result.put("trx_CodErrExtLdap",codLdap);
		    				result.put("status", "OK");
		    				result.put("respuesta", "NO EXISTE");
		    				result.put("codigoError", "");
		    			}
		    		}
		
		        
		    //---
    
		        // curl -ik -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ3THpCZ20tcUZTdmlOUFA3V0RwYlF0N0tpaHhvM2t3dmxiTTNXODRuTVFnIn0.eyJqdGkiOiI5YTg0NzI5MS05MDkzLTRmNzMtOGIyMC0yZjIyY2NjN2FiMmQiLCJleHAiOjE0NzUzMDg5NzQsIm5iZiI6MCwiaWF0IjoxNDc1MzA4Njc0LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwODAvYXV0aC9yZWFsbXMvc3ByaW5nYm9vdGpheHJzIiwiYXVkIjoiY2xpZW50Iiwic3ViIjoiYzE4ZWExOGMtMDVlYS00MjE5LTg4YjEtMDIyNjU0ZDUzMWYxIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiNDI4NDZlYjgtN2MxZS00NzM1LWFhMTYtMDdjYjY1Zjg4NWM4IiwiYWNyIjoiMSIsImNsaWVudF9zZXNzaW9uIjoiYjlhYzcwZDQtMThlOS00Mjk1LThmMWYtNDQ0ZGVhNGUwNTJiIiwiYWxsb3dlZC1vcmlnaW5zIjpbXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImFwaSJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImNsaWVudCI6eyJyb2xlcyI6WyJhcGkiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJ2aWV3LXByb2ZpbGUiXX19LCJjbGllbnRIb3N0IjoiMTcyLjE3LjAuMSIsImNsaWVudElkIjoiY2xpZW50IiwibmFtZSI6IiIsInByZWZlcnJlZF91c2VybmFtZSI6InNlcnZpY2UtYWNjb3VudC1hcGkiLCJjbGllbnRBZGRyZXNzIjoiMTcyLjE3LjAuMSIsImVtYWlsIjoic2VydmljZS1hY2NvdW50LWFwaUBwbGFjZWhvbGRlci5vcmcifQ.flw8cW1isddPqDGZVDQOrbtx6K1r-4Fzz5M39JmRsJkEyVyrNOWC5V1i5ks0zfEIzO9OED0vMFgQF7D2aAMgdCMsulGdGoeg0yi4ErI5-FNzAYYxRidKixmYOWo_fBSUSCoxEmzSkr-NJT6zHBwvx71bLZmeLCXHkuj0FegTVY09rh76isj4xmnPhVj2AgazGvbAIajX7YxMtPMevBs-SrxjoYZ_8w40VI1wV49lOAHCPhHANFqUJKdUytfONOq61PsEnzn_N7fXHNsJlHZ5Otduh--AjsGN0S5K4WmcIJlSgCJsABwp4FjU11sDzOpoAI3_Ktqs0uT_ka8wndhKKQ" https://localhost:8443/api/message?message=Hola
		 
 		
 //   	} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | UnsupportedOperationException  | IOException  | JSONException  e) {
   	} catch (KeyManagementException | NoSuchAlgorithmException  | UnsupportedOperationException  | IOException  | JSONException  e) {
   		
 		// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error"+e.toString());
			try {
				result.put("status", "NOK");
				result.put("codigoError", "222");
				result.put("respuesta", "ERROR LLAMADA SERVICIO");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
 	
 	System.out.println("getWSEstadoClaveDigital resultado="+result.toString());
 	
 	return result;
 }

 
}