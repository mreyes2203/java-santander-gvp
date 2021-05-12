package eContact;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javax.servlet.http.HttpServlet;

 
public class FunctionsGVP extends HttpServlet{
 
   protected String InstanceID = "";
   
   /*ARCHIVO PARAMETROS*/
   public Parameters Params = new Parameters();
   private String propFileGeneral = "FunctionsGVP.IVR_General.properties";
    
   /*INICIO Variables Log*/
   protected Logger log;
   private String loggerName;
   private String loggerNameError;
   
   protected String DebugLevel = "";
   protected String DebugFilePath = "";
   protected String ErrorFilePath = "";
   protected String DebugFileMaxSize = "1097152000";
   protected String Timezone = "America/Buenos_Aires";
   protected String DebugFilePathTrazaKRONOS = "";
   protected String DebugFilePathTrazaAVANZADO= "";
   protected String DebugFilePathTrazaAVANZADO2= "";
   protected String DebugFilePathTrazaNOMBRE= "";
   protected String DebugFilePathTrazaNOMBRE2= "";
   protected String DebugFilePathTrazaENCUESTA= "";    
   protected String DebugFilePathTrazaENCUESTA2= "";
   protected String UsuarioAlt = "";
   protected String CodigoCanal = "";
   protected String CodTipoCruce = "";
   protected String VDN = "";
   protected String SIPSERVER = "";
   
   /* Variables WebServices */
   public String urlWSObtieneRUT="";
   public String urlWSLogin="";
   public String urlWSEstadoClaveDigitalToken="";
   public String urlWSEstadoClaveDigital="";
   
   /*FIN Variables Log*/
   
   /*DB Conectors*/
   public KVPairList            RoutingKVPairs            = new KVPairList();
   public KVPairList             RecordSetKVPs            = new KVPairList();
   
   /* Variables Oracle */
//    private DBAccess            RoutingDAP                = new DBAccess();
   private OracleDBAccess      DataAccessPointOracle   = new OracleDBAccess();
    protected String oracleUrl="";
    protected String oracleUser="";
    protected String oraclePass="";
    protected int oracleTimeOut;
    
   /*Sockets Conectors*/
   private Sockets                SocketRequest            = new Sockets();
//    private SSTConector            SSTSockets                = new SSTConector();
   public boolean                SocketIsOK                = false;
   
   private URL urlFile = null;
       
   private boolean DVesK = false;
   
   public Date fechaDate = null;
   public String fechaString = "";
   
   private static String OS = System.getProperty("os.name").toLowerCase();
   
   
   public FunctionsGVP(String ParametersFile) {
//        String atalina = System.getProperty("catalina.base");
//        if(catalina != null)
//            if (ParametersFile.equals("")){            
//                ParametersFile = catalina + "//lib//FunctionsGVP.properties";                        
//            }else{
//                ParametersFile = catalina + "//lib//"+ParametersFile;
//            }
        
       Inicializar(ParametersFile);
//        this.InstanceID = id;               
    }
   
   public FunctionsGVP(String ParametersFile, String id) {
//        String catalina = System.getProperty("catalina.base");
//        if(catalina != null)
//            if (ParametersFile.equals("")){            
//                ParametersFile = catalina + "//lib//FunctionsGVP.properties";                        
//            }else{
//                ParametersFile = catalina + "//lib//"+ParametersFile;
//            }        
 
       Inicializar(ParametersFile);
        this.InstanceID = id;               
    }
   
   
    /**
     * Inicializa datos para el registro de log
     * @param ParametersFile Archivo de parámetros.
     */
    private void Inicializar(String ParametersFile) {
    	int[] arraySelect = {650,630,620,610,601,516,515,510};
    	
    	int tam= arraySelect.length;
        String DirectoryFile = "";
        
        if (isWindows()) {
           DirectoryFile = "C:\\Program Files (x86)\\GCTI\\Composer 8.1\\tomcat\\lib\\";
           //DirectoryFile = "C:\\GCTI\\Composer 8.1\\tomcat\\lib\\";
       } else if (isMac()) {
           DirectoryFile = "";
       } else if (isUnix()) {
           DirectoryFile = "/software/was85/IBM/WebSphere/AppServer/DriversTFC/IVR/lib/";
       } else if (isSolaris()) {
           DirectoryFile = "/software/was85/IBM/WebSphere/AppServer/DriversTFC/IVR/";
       } else {
           System.out.println("Your OS is not support!!");
       }
        
        InstanceID = (new Long((new Random()).nextLong()).toString());
        
        if( InstanceID.substring(0, 1).compareTo("-") == 0 )
            InstanceID = InstanceID.substring(1);
        
        InstanceID = "00000000000000000000" + InstanceID;
        InstanceID = InstanceID.substring(InstanceID.length() - 20);
        
        // Parametros Generales
        String ParametersGeneral = DirectoryFile+this.propFileGeneral;
        ReadParameters(ParametersGeneral);
        
        UsuarioAlt = Params.GetValue("UsuarioAlt");
        urlWSObtieneRUT = Params.GetValue("urlWSObtieneRUT");
        urlWSLogin = Params.GetValue("urlWSLogin");
        urlWSEstadoClaveDigitalToken = Params.GetValue("urlWSEstadoClaveDigitalToken");
        urlWSEstadoClaveDigital = Params.GetValue("urlWSEstadoClaveDigital");

        
        String nombreBD = "SANTANDER";
        this.oracleUrl = Params.GetValue(nombreBD+"_url");
        this.oracleUser = Params.GetValue(nombreBD+"_user");
        this.oraclePass = Params.GetValue(nombreBD+"_pass");
        this.oracleTimeOut = Integer.valueOf(Params.GetValue(nombreBD+"_timeout"));
        
        // Parametros Por IVR
        ParametersFile = DirectoryFile+ParametersFile;
        ReadParameters(ParametersFile);
 
        Timezone = Params.GetValue("Timezone");
        
        VDN = Params.GetValue("VDN");
        SIPSERVER = Params.GetValue("SIPSERVER");
        
        CodigoCanal = Params.GetValue("CodigoCanal");
        CodTipoCruce = Params.GetValue("CodTipoCruce");
        
        loggerName = Params.GetValue("LoggerName", "GVP");
        loggerNameError = Params.GetValue("LoggerNameError", "GVP_ERROR");
        DebugLevel = Params.GetValue("DebugLevel", "None");
        DebugFilePath = Params.GetValue("DebugFilePath");
        DebugFilePathTrazaKRONOS = Params.GetValue("DebugFilePathTrazaKRONOS");
        DebugFilePathTrazaAVANZADO = Params.GetValue("DebugFilePathTrazaAVANZADO");       
        DebugFilePathTrazaAVANZADO2 = Params.GetValue("DebugFilePathTrazaAVANZADO2");
        DebugFilePathTrazaENCUESTA = Params.GetValue("DebugFilePathTrazaENCUESTA");
        DebugFilePathTrazaENCUESTA2 = Params.GetValue("DebugFilePathTrazaENCUESTA2");
        DebugFilePathTrazaNOMBRE = Params.GetValue("DebugFilePathTrazaNOMBRE");       
        DebugFilePathTrazaNOMBRE2 = Params.GetValue("DebugFilePathTrazaNOMBRE2");
        DebugFileMaxSize = Params.GetValue("DebugFileMaxSize", "1097152000");    

        ErrorFilePath = Params.GetValue("ErrorFilePath");
        
        InicializarLogger();
//        Debug("FunctionsGVP.Initialize - " + ParametersFile, "Detail");
        
    }
 
    
   public static boolean isWindows() {
 
       return (OS.indexOf("win") >= 0);
 
   }
 
   public static boolean isMac() {
 
       return (OS.indexOf("mac") >= 0);
 
   }
 
   public static boolean isUnix() {
 
       return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
 
   }
 
   public static boolean isSolaris() {
 
       return (OS.indexOf("sunos") >= 0);
 
   }
    
   public String getPropFileGeneral() {
       return propFileGeneral;
   }
 
   public void setPropFileGeneral(String propFileGeneral) {
       this.propFileGeneral = propFileGeneral;
   }
   
    public void InicializarLogger(){
//        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
//        Date curDate = new Date();
        //CREA UN ARCHIVO POR DIA (Y ES BASE PARA OTROS LOGS)
//        String DateToStr = format.format(curDate);
        
        //CREA UN ARCHIVO POR CADA 15 MINUTOS
//        format = new SimpleDateFormat("mm");        
//        DateToStr = DateToStr + "-" + String.format("%02d",(Integer.parseInt(format.format(curDate))/15) * 15);
 
//        String archivo = DebugFilePath.replace(".log", "");
//        if (archivo.equals("") || ( (archivo.equals("null")) || (archivo == null)) ){
//            archivo = "C:\\logs\\IVR\\IVRLog";
//        }        
//
//        
//        archivo = archivo + "-" + DateToStr + ".log";
        
        //Set Logger options;
        log = Logger.getLogger(loggerName);
//        RollingFileAppender appender = (RollingFileAppender) log.getAppender("gvplogfile");  
//        appender.setFile(archivo);
//        appender.activateOptions();
    }
    
    public void logger(String Message, String Level){ 
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(Timezone));
        Date curDate = new Date();
        //CREA UN ARCHIVO POR DIA (Y ES BASE PARA OTROS LOGS)
        String DateToStr = format.format(curDate);
        
        SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss");
        formatHora.setTimeZone(TimeZone.getTimeZone(Timezone));
        String Hora = formatHora.format(curDate);
 
//        log = Logger.getLogger(loggerName);
//        RollingFileAppender appender = (RollingFileAppender) log.getAppender("gvplogfile");         
//
//        String archivo = DebugFilePath.replace(".log", "");
//        if (archivo.equals("")){
//            archivo = "C:\\logs\\IVR\\IVRLog";
//        }        
//        
//        archivo = archivo + "-" + DateToStr + ".log";
//        
//        appender.setFile(archivo);
//        appender.activateOptions();
                        
        Message = DateToStr+ " "+Hora+" "+Rellena(Level, " ", 10, 1)+" ["+this.InstanceID + "] " + Message;        
 
        System.out.println(Message);
        if (DebugLevel.equalsIgnoreCase("Detail") || DebugLevel.equalsIgnoreCase("Trace") || DebugLevel.equalsIgnoreCase("DEBUG")){
            log.debug(Message); 
        }else{
            if (Level.equalsIgnoreCase("Standard") || Level.equalsIgnoreCase("INFO")){
                log.debug(Message); 
            }
        }
//        // DEBUG < INFO < WARN < ERROR < FATAL
//        if (Level.equalsIgnoreCase("DEBUG")) {
//                log.debug(Message); 
//        }else if (Level.equalsIgnoreCase("INFO")) {
//                log.info(Message); 
//        }else if (Level.equalsIgnoreCase("WARN")) {
//                log.warn(Message); 
//        }else if (Level.equalsIgnoreCase("ERROR")) {
//                log.error(Message); 
//        }else if (Level.equalsIgnoreCase("FATAL")) {
//                log.fatal(Message); 
//        }else { 
//                log.debug(Message); 
//        }
    }
   
    
    public void loggerError(String Message){                
 
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(Timezone));
        Date curDate = new Date();
        //CREA UN ARCHIVO POR DIA (Y ES BASE PARA OTROS LOGS)
        String DateToStr = format.format(curDate);
        
        SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss");
        formatHora.setTimeZone(TimeZone.getTimeZone(Timezone));
        String Hora = formatHora.format(curDate);
        
        log = Logger.getLogger(loggerNameError);
//        RollingFileAppender appender = (RollingFileAppender) log.getAppender("gvplogfile");         
//
//        String archivo = ErrorFilePath.replace(".log", "");
//        if (archivo.equals("")){
//            archivo = "C:\\logs\\IVR\\IVRLog";
//        }        
//        
//        archivo = archivo + "-" + DateToStr + ".log";
//        
//        appender.setFile(archivo);
//        appender.activateOptions();
                        
        Message = DateToStr+ " "+Hora+" "+Rellena("ERROR", " ", 10, 1)+" ["+this.InstanceID + "] " + Message;        
 
        log.error(Message); 
        
    }
    
    /**
     * Permite registrar mensajes en un archivo de log.
     * Los parámetros para generar el archivo de log están definidos en el constructor utilizado.
     * @param Message Mensaje a registrar en archivo de log.
     * @param Level Nivel de log (None, Standard, Trace, Detail)
     * @return True: Éxito<br>
     * False: Error
   
     *
     */
    //descomentar para probar los logs DE LA NUEVA FORMA SIN LOGGER
  public boolean Debug(String Message) {
        return Debug(Message, "Standard");
    }
   
  public boolean DebugT(String Message,String Level) {
       Debug("DebugT: "+Message+Level , "Detail");
        
      return DebugTraza(Message, Level);
  }
    /**
     * Permite registrar mensajes en un archivo de log.
     * Los parámetros para generar el archivo de log están definidos en el constructor utilizado.
     * @param Message Mensaje a registrar en archivo de log.
     * @param Level Nivel de log (None, Standard, Trace, Detail)
     * @return True: Éxito<br>
     * False: Error
     */public FunctionsGVP() {
       // TODO Auto-generated constructor stub
   }
     
   //Comentar para probar los logs DE LA NUEVA FORMA SIN LOGGER
/* public boolean Debug(String Message, String Level) {
       logger(Message, Level);
       return true;
   }*/
   
public boolean DebugTraza(String Message, String Level) {         
         Debug("DebugTraza: "+Message+Level , "Detail");
         String DebugMessage = "" ;        
         String DebugFilePathAsig = "";
         String DebugFilePathAsig1 = "";
         String DebugFilePathAsig2 = "";
         String DebugFilePathAsig3 = "";
   
         
         boolean AVANZADO2= false;
         boolean ENCUESTA2= false;    
         boolean NOMBRE= false;
         boolean NOMBRE2= false;
         
         if(Level.equals("KRONOS")){
             DebugFilePathAsig= DebugFilePathTrazaKRONOS;
         }else if(Level.equals("AVANZADO")){
                DebugFilePathAsig= DebugFilePathTrazaAVANZADO;
             Debug("DebugT AVANZADO: ", "Detail");
             AVANZADO2=true;
             NOMBRE=true;
             NOMBRE2=true;
         }if(Level.equals("ENCUESTA")){
                DebugFilePathAsig= DebugFilePathTrazaENCUESTA;
                ENCUESTA2=true;
         }
             
         if(AVANZADO2){
             DebugFilePathAsig1= DebugFilePathTrazaAVANZADO2;
         }
         if(NOMBRE){
             DebugFilePathAsig2= DebugFilePathTrazaNOMBRE;
         }
         if(NOMBRE2){
             DebugFilePathAsig3= DebugFilePathTrazaNOMBRE2;
         }
         if(ENCUESTA2){
             DebugFilePathAsig1= DebugFilePathTrazaENCUESTA2;
         }
         
         DebugMessage += Message + "\n";       
        
        this.Write(DebugFilePathAsig,  DebugMessage );
        this.Write(DebugFilePathAsig1, DebugMessage );
        this.Write(DebugFilePathAsig2, DebugMessage );
        this.Write(DebugFilePathAsig3, DebugMessage );
         
         return true;
     } 
 
public boolean Write(String DebugFilePathAsig,String DebugMessage ){
  try {
        TimeZone tz = TimeZone.getTimeZone("Chile/Continental");              
         SimpleDateFormat df = new SimpleDateFormat("-ddMMyyyy.");
         df.setTimeZone(tz);
         String ds = df.format(new Date());
     
        File fDebugFile = new File(DebugFilePathAsig.replace(".", ds));                 
             
         /*if( fDebugFile.length() > Integer.parseInt(DebugFileMaxSize) ) {            
             TimeZone tz = TimeZone.getTimeZone("Chile/Continental");              
             SimpleDateFormat df = new SimpleDateFormat("-ddMMyyyy.");
             df.setTimeZone(tz);
             String ds = df.format(new Date());             
                         
             File fOldDebugFile = new File(DebugFilePathAsig.replace(".", ds));
             Debug("fOldDebugFile " + fOldDebugFile);             
         
                 fDebugFile.renameTo(fOldDebugFile);
                 fDebugFile = new File(DebugFilePathAsig);
                                 
        }*/     
         
         
        // fDebugFile.createNewFile();   
         if (fDebugFile.createNewFile()){
        	 File directory = new File(fDebugFile.getParent());
           	 System.out.println("Directorio de archivos: "+fDebugFile.getParent());
           	 
        	 if(directory.exists()){

           	    // long purgeTime = System.currentTimeMillis() - (61 * 24 * 60 * 60 * 1000);
           	     Calendar calendar = Calendar.getInstance();  
           	     calendar.add(Calendar.DATE, -61);  
               	 System.out.println("Fecha de corte Millis: "+ calendar.getTimeInMillis());
               	 System.out.println("creando Archivo: "+ fDebugFile.getName());
               	 int ini = fDebugFile.getName().indexOf("-");
       	         int fin = fDebugFile.getName().indexOf(".");
       	         String dia = new SimpleDateFormat("ddMMyyyy").format(calendar.getTime());
       	    

       	         String nuevaCadena = "-" + dia + ".";
       	         String subcadena = "-" + fDebugFile.getName().substring(ini+1, fin) + ".";
       	         String deleteFile = fDebugFile.getParent() + "/" + fDebugFile.getName().replace(subcadena,nuevaCadena);	
       	 	     System.out.println("eliminando Archivo: "+ deleteFile);
       	 	     File f = new File(deleteFile);
       	 	     if(f.exists() && !f.isDirectory()) { 
       	 	    	 if(!f.delete()) {
       	 	    		 System.out.println("Error al eliminar archivo: "+f.getName());
       	 	    		 System.err.println("Unable to delete file: " + f);
       	 	    	 }else{
       	 	    		 System.out.println("Se ha eliminado el archivo: "+f.getName());
       	 	    	 }
       	 	     }    
       	 	     // eliminaciones en duro
       	 	    
       	 	     String deleteFile1 = fDebugFile.getParent() + "/" + "IVR_CobranzaFECHAlog".replace("FECHA",nuevaCadena);	
    	 	     System.out.println("eliminando Archivo: "+ deleteFile1);
    	 	     File f1 = new File(deleteFile1);
    	 	     if(f1.exists() && !f1.isDirectory()) { 
    	 	    	 if(!f1.delete()) {
    	 	    		 System.out.println("Error al eliminar archivo: "+f1.getName());
    	 	    		 System.err.println("Unable to delete file: " + f1);
    	 	    	 }else{
    	 	    		 System.out.println("Se ha eliminado el archivo: "+f1.getName());
    	 	    	 }
    	 	     }    
       	 	 
    	 	     String deleteFile2 = fDebugFile.getParent() + "/" + "IVR_Derivacion_Cero_RingFECHAlog".replace("FECHA",nuevaCadena);	
    	 	     System.out.println("eliminando Archivo: "+ deleteFile2);
    	 	     File f2 = new File(deleteFile2);
    	 	     if(f2.exists() && !f2.isDirectory()) { 
    	 	    	 if(!f2.delete()) {
    	 	    		 System.out.println("Error al eliminar archivo: "+f2.getName());
    	 	    		 System.err.println("Unable to delete file: " + f2);
    	 	    	 }else{
    	 	    		 System.out.println("Se ha eliminado el archivo: "+f2.getName());
    	 	    	 }
    	 	     }    

    	 	     String deleteFile3 = fDebugFile.getParent() + "/" + "IVR_Persona_BancoFECHAlog".replace("FECHA",nuevaCadena);	
    	 	     System.out.println("eliminando Archivo: "+ deleteFile3);
    	 	     File f3 = new File(deleteFile3);
    	 	     if(f3.exists() && !f3.isDirectory()) { 
    	 	    	 if(!f3.delete()) {
    	 	    		 System.out.println("Error al eliminar archivo: "+f3.getName());
    	 	    		 System.err.println("Unable to delete file: " + f3);
    	 	    	 }else{
    	 	    		 System.out.println("Se ha eliminado el archivo: "+f3.getName());
    	 	    	 }
    	 	     }    

    	 	    String deleteFile4 = fDebugFile.getParent() + "/" + "IVR_SATFECHAlog".replace("FECHA",nuevaCadena);	
    	 	    System.out.println("eliminando Archivo: "+ deleteFile4);
    	 	    File f4 = new File(deleteFile4);
    	 	    if(f4.exists() && !f4.isDirectory()) { 
    	 	    	if(!f4.delete()) {
    	 	    		System.out.println("Error al eliminar archivo: "+f4.getName());
    	 	    		System.err.println("Unable to delete file: " + f4);
    	 	    	}else{
    	 	    		System.out.println("Se ha eliminado el archivo: "+f4.getName());
    	 	    	}
    	 	    }    

    	 	    String deleteFile5 = fDebugFile.getParent() + "/" + "IVR_Santander_PassFECHAlog".replace("FECHA",nuevaCadena);	
    	 	    System.out.println("eliminando Archivo: "+ deleteFile5);
    	 	    File f5 = new File(deleteFile5);
    	 	    if(f5.exists() && !f5.isDirectory()) { 
    	 	    	if(!f5.delete()) {
    	 	    		System.out.println("Error al eliminar archivo: "+f5.getName());
    	 	    		System.err.println("Unable to delete file: " + f5);
    	 	    	}else{
    	 	    		System.out.println("Se ha eliminado el archivo: "+f5.getName());
    	 	    	}
    	 	    }    

    	 	    String deleteFile6 = fDebugFile.getParent() + "/" + "IVR_SmartbackFECHAlog".replace("FECHA",nuevaCadena);	
    	 	    System.out.println("eliminando Archivo: "+ deleteFile6);
    	 	    File f6 = new File(deleteFile6);
    	 	    if(f6.exists() && !f6.isDirectory()) { 
    	 	    	if(!f6.delete()) {
    	 	    		System.out.println("Error al eliminar archivo: "+f6.getName());
    	 	    		System.err.println("Unable to delete file: " + f6);
    	 	    	}else{
    	 	    		System.out.println("Se ha eliminado el archivo: "+f6.getName());
    	 	    	}
    	 	    }    
    	 	    
    	 	    String deleteFile7 = fDebugFile.getParent() + "/" + "IVR_ValevistaFECHAlog".replace("FECHA",nuevaCadena);	
    	 	    System.out.println("eliminando Archivo: "+ deleteFile7);
    	 	    File f7 = new File(deleteFile7);
    	 	    if(f7.exists() && !f7.isDirectory()) { 
    	 	    	if(!f7.delete()) {
    	 	    		System.out.println("Error al eliminar archivo: "+f7.getName());
    	 	    		System.err.println("Unable to delete file: " + f7);
    	 	    	}else{
    	 	    		System.out.println("Se ha eliminado el archivo: "+f7.getName());
    	 	    	}
    	 	    }    

    	 	    String deleteFile8 = fDebugFile.getParent() + "/" + "IVR_Validacion_PinPassFECHAlog".replace("FECHA",nuevaCadena);	
    	 	    System.out.println("eliminando Archivo: "+ deleteFile8);
    	 	    File f8 = new File(deleteFile8);
    	 	    if(f8.exists() && !f8.isDirectory()) { 
    	 	    	if(!f8.delete()) {
    	 	    		System.out.println("Error al eliminar archivo: "+f8.getName());
    	 	    		System.err.println("Unable to delete file: " + f8);
    	 	    	}else{
    	 	    		System.out.println("Se ha eliminado el archivo: "+f8.getName());
    	 	    	}
    	 	    }    

    	 	    String deleteFile9 = fDebugFile.getParent() + "/" + "IVR_Validacion_Superclave_BancoFECHAlog".replace("FECHA",nuevaCadena);	
    	 	    System.out.println("eliminando Archivo: "+ deleteFile9);
    	 	    File f9 = new File(deleteFile9);
    	 	    if(f9.exists() && !f9.isDirectory()) { 
    	 	    	if(!f9.delete()) {
    	 	    		System.out.println("Error al eliminar archivo: "+f9.getName());
    	 	    		System.err.println("Unable to delete file: " + f9);
    	 	    	}else{
    	 	    		System.out.println("Se ha eliminado el archivo: "+f9.getName());
    	 	    	}
    	 	    }    
    	 	    
    	 	    String deleteFile10 = fDebugFile.getParent() + "/" + "IVR_Clave_Consultiva_BancoFECHAlog".replace("FECHA",nuevaCadena);	
    	 	    System.out.println("eliminando Archivo: "+ deleteFile10);
    	 	    File f10 = new File(deleteFile10);
    	 	    if(f10.exists() && !f10.isDirectory()) { 
    	 	    	if(!f10.delete()) {
    	 	    		System.out.println("Error al eliminar archivo: "+f10.getName());
    	 	    		System.err.println("Unable to delete file: " + f10);
    	 	    	}else{
    	 	    		System.out.println("Se ha eliminado el archivo: "+f10.getName());
    	 	    	}
    	 	    }    
    	 	    
    	 	    String deleteFile11 = fDebugFile.getParent() + "/" + "IVR_Clave_Consultiva_AsistidaFECHAlog".replace("FECHA",nuevaCadena);	
    	 	    System.out.println("eliminando Archivo: "+ deleteFile11);
    	 	    File f11 = new File(deleteFile11);
    	 	    if(f11.exists() && !f11.isDirectory()) { 
    	 	    	if(!f11.delete()) {
    	 	    		System.out.println("Error al eliminar archivo: "+f11.getName());
    	 	    		System.err.println("Unable to delete file: " + f11);
    	 	    	}else{
    	 	    		System.out.println("Se ha eliminado el archivo: "+f11.getName());
    	 	    	}
    	 	    }    
    	 	        	 	    

    	 	    
    }
           	 }                  
         
         if( fDebugFile.canWrite() ) {
             FileOutputStream osDebugFile = new FileOutputStream(fDebugFile, true);             
             osDebugFile.write(DebugMessage.getBytes());
             osDebugFile.close();
         } else {
             return false;
         }   
                 
     }
     
     catch( Exception e ) {
         e.printStackTrace();
         return false;
     }
  
  return true;
}
     
//LA NUEVA FORMA SIN LOGGER
 public boolean Debug(String Message, String Level) {
        String DebugMessage = "";
        
        if( DebugLevel.compareTo("None") == 0 )
            return true;
        
        if( DebugFilePath.compareTo("") == 0 )
            return true;
        
        if( (DebugLevel.compareTo("Standard") == 0) && (Level.compareTo("Standard") != 0) )
            return true;
        
        if( (DebugLevel.compareTo("Trace") == 0) && (Level.compareTo("Detail") == 0) )
            return true;
 
        TimeZone tz = TimeZone.getTimeZone("Chile/Continental");             
        SimpleDateFormat DateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
        DateFormatter.setTimeZone(tz);
        String DateString = DateFormatter.format(new Date());
        
        // Compone el texto que se escribir� en el archivo de Log
        DebugMessage += "[" + DateString + " " + InstanceID + "] " + (new String(Level + "          ")).substring(0, 10);
        
        DebugMessage += Message + "\n";
        
        try {
            File fDebugFile = new File(DebugFilePath);
            
            if( fDebugFile.length() > Integer.parseInt(DebugFileMaxSize) ) {
                SimpleDateFormat df = new SimpleDateFormat("-ddMMyyyy.");
                df.setTimeZone(tz);
                String ds = df.format(new Date());
                
                File fOldDebugFile = new File(DebugFilePath.replace(".", ds));
                
               // if( fDebugFile.renameTo(fOldDebugFile) )
               //     fDebugFile = new File(DebugFilePath);
                
                fDebugFile.renameTo(fOldDebugFile);
              //  fDebugFile = new File(DebugFilePath);
            }
            
            //fDebugFile.createNewFile();
           /* if (fDebugFile.createNewFile()){
            	File directory = new File(fDebugFile.getParent());
            	System.out.println("Directorio de archivos: "+fDebugFile.getParent());

            	if(directory.exists()){

            		File[] listFiles = directory.listFiles();           
            		// long purgeTime = System.currentTimeMillis() - (61 * 24 * 60 * 60 * 1000);
            		Calendar calendar = Calendar.getInstance();  
            		calendar.add(Calendar.DATE, -61);  
            		long purgeTime = calendar.getTimeInMillis();
            		System.out.println("Fecha de corte Millis: "+ calendar.getTimeInMillis());

            		for(File listFile : listFiles) {
            			int starpos = listFile.getName().indexOf("-");
            			int endpos = listFile.getName().indexOf(".");
            			SimpleDateFormat dfa = new SimpleDateFormat("ddMMyyyy");

            			Date d = dfa.parse(listFile.getName().substring(starpos+1, endpos));
            			System.out.println("Comparando archivo con fecha: "+listFile.getName().substring(starpos+1, endpos));

            			if(d.getTime() < purgeTime) {
            				System.out.println("Eliminando archivo: "+listFile.getName());
            				if(!listFile.delete()) {
            					System.out.println("Error al eliminar archivo: "+listFile.getName());

            					System.err.println("Unable to delete file: " + listFile);
            				}else{
            					System.out.println("Se ha eliminado el archivo: "+listFile.getName());
            				}
            			}
            		}
            	}
            }                  
*/
            if (fDebugFile.createNewFile()){
           	 File directory = new File(fDebugFile.getParent());
              	 System.out.println("Directorio de archivos: "+fDebugFile.getParent());
              	 
           	 if(directory.exists()){

              	    // long purgeTime = System.currentTimeMillis() - (61 * 24 * 60 * 60 * 1000);
              	     Calendar calendar = Calendar.getInstance();  
              	     calendar.add(Calendar.DATE, -61);  
                  	 System.out.println("Fecha de corte Millis: "+ calendar.getTimeInMillis());
                  	 System.out.println("creando Archivo: "+ fDebugFile.getName());
                  	 int ini = fDebugFile.getName().indexOf("-");
          	         int fin = fDebugFile.getName().indexOf(".");
          	         String dia = new SimpleDateFormat("ddMMyyyy").format(calendar.getTime());
          	    

          	         String nuevaCadena = "-" + dia + ".";
          	         String subcadena = "-" + fDebugFile.getName().substring(ini+1, fin) + ".";
          	         String deleteFile = fDebugFile.getParent() + "/" + fDebugFile.getName().replace(subcadena,nuevaCadena);	
          	 	     System.out.println("eliminando Archivo: "+ deleteFile);
          	 	     File f = new File(deleteFile);
          	 	     if(f.exists() && !f.isDirectory()) { 
          	 	    	 if(!f.delete()) {
          	 	    		 System.out.println("Error al eliminar archivo: "+f.getName());
          	 	    		 System.err.println("Unable to delete file: " + f);
          	 	    	 }else{
          	 	    		 System.out.println("Se ha eliminado el archivo: "+f.getName());
          	 	    	 }
          	 	     }   
          	 	  // eliminaciones en duro
            	 	    
           	 	     String deleteFile1 = fDebugFile.getParent() + "/" + "IVR_CobranzaFECHAlog".replace("FECHA",nuevaCadena);	
        	 	     System.out.println("eliminando Archivo: "+ deleteFile1);
        	 	     File f1 = new File(deleteFile1);
        	 	     if(f1.exists() && !f1.isDirectory()) { 
        	 	    	 if(!f1.delete()) {
        	 	    		 System.out.println("Error al eliminar archivo: "+f1.getName());
        	 	    		 System.err.println("Unable to delete file: " + f1);
        	 	    	 }else{
        	 	    		 System.out.println("Se ha eliminado el archivo: "+f1.getName());
        	 	    	 }
        	 	     }    
           	 	 
        	 	     String deleteFile2 = fDebugFile.getParent() + "/" + "IVR_Derivacion_Cero_RingFECHAlog".replace("FECHA",nuevaCadena);	
        	 	     System.out.println("eliminando Archivo: "+ deleteFile2);
        	 	     File f2 = new File(deleteFile2);
        	 	     if(f2.exists() && !f2.isDirectory()) { 
        	 	    	 if(!f2.delete()) {
        	 	    		 System.out.println("Error al eliminar archivo: "+f2.getName());
        	 	    		 System.err.println("Unable to delete file: " + f2);
        	 	    	 }else{
        	 	    		 System.out.println("Se ha eliminado el archivo: "+f2.getName());
        	 	    	 }
        	 	     }    

        	 	     String deleteFile3 = fDebugFile.getParent() + "/" + "IVR_Persona_BancoFECHAlog".replace("FECHA",nuevaCadena);	
        	 	     System.out.println("eliminando Archivo: "+ deleteFile3);
        	 	     File f3 = new File(deleteFile3);
        	 	     if(f3.exists() && !f3.isDirectory()) { 
        	 	    	 if(!f3.delete()) {
        	 	    		 System.out.println("Error al eliminar archivo: "+f3.getName());
        	 	    		 System.err.println("Unable to delete file: " + f3);
        	 	    	 }else{
        	 	    		 System.out.println("Se ha eliminado el archivo: "+f3.getName());
        	 	    	 }
        	 	     }    

        	 	    String deleteFile4 = fDebugFile.getParent() + "/" + "IVR_SATFECHAlog".replace("FECHA",nuevaCadena);	
        	 	    System.out.println("eliminando Archivo: "+ deleteFile4);
        	 	    File f4 = new File(deleteFile4);
        	 	    if(f4.exists() && !f4.isDirectory()) { 
        	 	    	if(!f4.delete()) {
        	 	    		System.out.println("Error al eliminar archivo: "+f4.getName());
        	 	    		System.err.println("Unable to delete file: " + f4);
        	 	    	}else{
        	 	    		System.out.println("Se ha eliminado el archivo: "+f4.getName());
        	 	    	}
        	 	    }    

        	 	    String deleteFile5 = fDebugFile.getParent() + "/" + "IVR_Santander_PassFECHAlog".replace("FECHA",nuevaCadena);	
        	 	    System.out.println("eliminando Archivo: "+ deleteFile5);
        	 	    File f5 = new File(deleteFile5);
        	 	    if(f5.exists() && !f5.isDirectory()) { 
        	 	    	if(!f5.delete()) {
        	 	    		System.out.println("Error al eliminar archivo: "+f5.getName());
        	 	    		System.err.println("Unable to delete file: " + f5);
        	 	    	}else{
        	 	    		System.out.println("Se ha eliminado el archivo: "+f5.getName());
        	 	    	}
        	 	    }    

        	 	    String deleteFile6 = fDebugFile.getParent() + "/" + "IVR_SmartbackFECHAlog".replace("FECHA",nuevaCadena);	
        	 	    System.out.println("eliminando Archivo: "+ deleteFile6);
        	 	    File f6 = new File(deleteFile6);
        	 	    if(f6.exists() && !f6.isDirectory()) { 
        	 	    	if(!f6.delete()) {
        	 	    		System.out.println("Error al eliminar archivo: "+f6.getName());
        	 	    		System.err.println("Unable to delete file: " + f6);
        	 	    	}else{
        	 	    		System.out.println("Se ha eliminado el archivo: "+f6.getName());
        	 	    	}
        	 	    }    
        	 	    
        	 	    String deleteFile7 = fDebugFile.getParent() + "/" + "IVR_ValevistaFECHAlog".replace("FECHA",nuevaCadena);	
        	 	    System.out.println("eliminando Archivo: "+ deleteFile7);
        	 	    File f7 = new File(deleteFile7);
        	 	    if(f7.exists() && !f7.isDirectory()) { 
        	 	    	if(!f7.delete()) {
        	 	    		System.out.println("Error al eliminar archivo: "+f7.getName());
        	 	    		System.err.println("Unable to delete file: " + f7);
        	 	    	}else{
        	 	    		System.out.println("Se ha eliminado el archivo: "+f7.getName());
        	 	    	}
        	 	    }    

        	 	    String deleteFile8 = fDebugFile.getParent() + "/" + "IVR_Validacion_PinPassFECHAlog".replace("FECHA",nuevaCadena);	
        	 	    System.out.println("eliminando Archivo: "+ deleteFile8);
        	 	    File f8 = new File(deleteFile8);
        	 	    if(f8.exists() && !f8.isDirectory()) { 
        	 	    	if(!f8.delete()) {
        	 	    		System.out.println("Error al eliminar archivo: "+f8.getName());
        	 	    		System.err.println("Unable to delete file: " + f8);
        	 	    	}else{
        	 	    		System.out.println("Se ha eliminado el archivo: "+f8.getName());
        	 	    	}
        	 	    }    

        	 	    String deleteFile9 = fDebugFile.getParent() + "/" + "IVR_Validacion_Superclave_BancoFECHAlog".replace("FECHA",nuevaCadena);	
        	 	    System.out.println("eliminando Archivo: "+ deleteFile9);
        	 	    File f9 = new File(deleteFile9);
        	 	    if(f9.exists() && !f9.isDirectory()) { 
        	 	    	if(!f9.delete()) {
        	 	    		System.out.println("Error al eliminar archivo: "+f9.getName());
        	 	    		System.err.println("Unable to delete file: " + f9);
        	 	    	}else{
        	 	    		System.out.println("Se ha eliminado el archivo: "+f9.getName());
        	 	    	}
        	 	    }    
        	 	    
        	 	    String deleteFile10 = fDebugFile.getParent() + "/" + "IVR_Clave_Consultiva_BancoFECHAlog".replace("FECHA",nuevaCadena);	
        	 	    System.out.println("eliminando Archivo: "+ deleteFile10);
        	 	    File f10 = new File(deleteFile10);
        	 	    if(f10.exists() && !f10.isDirectory()) { 
        	 	    	if(!f10.delete()) {
        	 	    		System.out.println("Error al eliminar archivo: "+f10.getName());
        	 	    		System.err.println("Unable to delete file: " + f10);
        	 	    	}else{
        	 	    		System.out.println("Se ha eliminado el archivo: "+f10.getName());
        	 	    	}
        	 	    }
        	 	    
        	 	    String deleteFile11 = fDebugFile.getParent() + "/" + "IVR_Clave_Consultiva_AsistidaFECHAlog".replace("FECHA",nuevaCadena);	
        	 	    System.out.println("eliminando Archivo: "+ deleteFile11);
        	 	    File f11 = new File(deleteFile11);
        	 	    if(f11.exists() && !f11.isDirectory()) { 
        	 	    	if(!f11.delete()) {
        	 	    		System.out.println("Error al eliminar archivo: "+f11.getName());
        	 	    		System.err.println("Unable to delete file: " + f11);
        	 	    	}else{
        	 	    		System.out.println("Se ha eliminado el archivo: "+f11.getName());
        	 	    	}
        	 	    }   

          	 	    }
              	 }               
            
            if( fDebugFile.canWrite() ) {
                FileOutputStream osDebugFile = new FileOutputStream(fDebugFile, true);
                
                osDebugFile.write(DebugMessage.getBytes());
                osDebugFile.close();
            } else {
                return false;
            }
        }
        
        catch( Exception e ) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
        
    /**
  
    
    
    
    /**
     * Permite registrar mensajes de error en un archivo de log dedicado para esta tarea.
     * El archivo de error se generará según lo definido en el parámetro <b>ErrorFilePath</b> del constructor utilizado..
     * @param Message Mensaje de error a registrar en archivo de log.
     */
    public void DebugError(String Message) {
//        String ErrorMessage = "";
//        
//        if( ErrorFilePath.compareTo("") == 0 )
//            return;
//        
//        // Obtiene fecha  hora actual y la formatea para cabecera
//        // del texto que se escribirá en el archivo de Log.
//        
//        String zonaHoraria = Params.GetValue("Timezone");//ObtenerParametroProperties("Timezone", "ConfiguracionServiciosWeb.properties");
//        
//        TimeZone tz = TimeZone.getTimeZone(zonaHoraria);
////        TimeZone tz = TimeZone.getTimeZone("America/Buenos_Aires");
////        TimeZone tz = TimeZone.getTimeZone("Chile/Continental");
//        SimpleDateFormat DateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
//        DateFormatter.setTimeZone(tz);
//        String DateString = DateFormatter.format(new Date());
//        
//        // Compone el texto que se escribirá en el archivo de Log
//        ErrorMessage += "[" + DateString + " " + InstanceID + "]   ";
//        
//        ErrorMessage += Message + "\n";
//        
//        try {
//            File fErrorFile = new File(ErrorFilePath);
//            
//            if( fErrorFile.length() > Integer.parseInt(DebugFileMaxSize) ) {
//                SimpleDateFormat df = new SimpleDateFormat("_yyyyMMdd_HHmmss.");
//                df.setTimeZone(tz);
//                String ds = df.format(new Date());
//                
//                File fOldDebugFile = new File(ErrorFilePath.replaceFirst("\\.", ds));
//                
//                if( fErrorFile.renameTo(fOldDebugFile) )
//                    fErrorFile = new File(ErrorFilePath);
//            }
//            
//            fErrorFile.createNewFile();
//            
//            if( fErrorFile.canWrite() ) {
//                FileOutputStream osErrorFile = new FileOutputStream(fErrorFile, true);
//                
//                osErrorFile.write(ErrorMessage.getBytes());
//                osErrorFile.close();
//            } else {
//                return;
//            }
//        }
//        
//        catch( Exception e ) {
//            e.printStackTrace();
//            return;
//        }
        
        loggerError(Message);
        
        return;
    }
    
   
 
   public void VerInfoMap(String mensaje, Map<String, String> mapa) {
 
        Set<String> keys = mapa.keySet();
        for (String key : keys) {
            logger(mensaje+" DATOS "+key+"="+mapa.get(key), "DEBUG");
       }
    }
    
    public void VerInfoProperties(String mensaje, Properties properties) {
 
        Set<Object> keys = properties.keySet();
        for (Object key : keys) {
            logger(mensaje+" DATOS "+key.toString()+"="+properties.get(key.toString()).toString(), "DEBUG");
       }
    }
    
    
    /**
     * Lee parámetros desde un archivo de parámetros.<br>
     * Utilizar junto a método <b>GetValue</b> de la clase Parameters.
     * @param ParametersFile Archivo de parámetros.
     */
    public void ReadParameters(String ParametersFile) {
//        System.out.println("FunctionsGVP.Leyendo archivo de parámetros [" + ParametersFile +"]");
        if ( Params.ReadParametersFile(ParametersFile)){            
//            System.out.println("FunctionsGVP.Se pudo leer archivo [" + ParametersFile + "]");
        }else{
//            System.out.println("FunctionsGVP.No se pudo leer archivo [" + ParametersFile + "]");
        }
            
    }
    
    /**
     * Método que busca un archivo dentro su entorno ClassLoader si lo encuentra devuelve true y  almacena el path en urlFile
     * @param fileName Nombre Archivo.
     * @return true o false.<br>
     * <b>urlFile</b> Path del Archivo (variable public).
     */
   private boolean getFile(String fileName){  
        this.urlFile = null;
        try
        {
            this.urlFile = this.getClass().getClassLoader().getResource(fileName);  //Archivo buscado, ej. Fonobank.FunctionsGVP.properties
            return true;
        }
        catch(Exception e)
        {
            DebugError("FunctionsGVP.getFile.IOException [" + e.getMessage() + "]");
            return false;
        }
    }
    
    /**
    * Método utilizado en Fonobank Personas y empresas y crediChile
    *  el cual devuelve la URL del servicio
    * ejemplo:
    *
    * @param Codigo, nombre del archivo
    * @return URL
    */
    public String ObtenerParametroProperties(String Nombre, String Archivo){ 
     String url = "";     
    // Archivo = "/usr/local/tomcat/shared/lib/ConfiguracionServiciosWeb.properties";
     //Archivo = "ConfiguracionServiciosWeb.properties";
     String r = "";
     try{               
        
        Properties pMov = new Properties();
        if (getFile(Archivo))
        {
           pMov.load(this.urlFile.openStream());
           try
           {                              
               url = pMov.getProperty(Nombre);
               if ( !url.equals("") ){                                   
                   r = url;                      
               }                             
           }
           catch(Exception e){                 
               DebugError("FunctionsGVP.ObtenerParametroProperties.Exception [" + e.getMessage() + "]");              
           }                      
        }        
      }catch(IOException e){
          DebugError("FunctionsGVP.ObtenerParametroProperties.IOException [" + e.getMessage() + "]");          
      }finally{
          url = r;          
      }
         
     return url;
    }
    
    
    
    /**
     * Método para validar digito verificador.
     * @param RUT  Rut
     * @param DV  Digito verifricador
     * @param EquivalenciaK equivalencia "K"
     * @return True o False
     */    
    public boolean ValidaRUT(String RUT, String DV, String EquivalenciaK) {
        try{
            int auxI;
            int auxJ = 2;
            int auxSuma = 0;
            int auxDV;
            boolean auxB = false;
            DVesK = false;
            
            Debug("FunctionsGVP.Validando RUT: " + RUT + "-" + DV + " (EquivalenciaK=" + EquivalenciaK + ").", "Detail");
            
            for( auxI = RUT.length() - 1 ; auxI >= 0 ; auxI-- ) {
                auxSuma += Integer.parseInt(RUT.substring(auxI, auxI + 1)) * auxJ;
                
                if( ++auxJ > 7 )
                    auxJ = 2;
            }
            
            auxDV = 11 - (auxSuma % 11);
            
            if( auxDV < 10 )            // Corresponde a digito calculado
            {
                if( Integer.parseInt(DV) == auxDV )
                    auxB = true;
            } else if( auxDV == 10 )        // Corresponde a letra K
            {
                // if( Integer.parseInt(DV) == Integer.parseInt(EquivalenciaK) ){
                if(DV.equals(EquivalenciaK)){
                    auxB = true;
                    DVesK = true;
                }
            } else                        // Corresponde a digito cero
            {
                if( Integer.parseInt(DV) == 0 )
                    auxB = true;
            }
            
            Debug("FunctionsGVP.    RUT: " + RUT + "-" + DV + " " + (auxB ? "valido" : "invalido") + ".", "Trace");
            
            return auxB;
        }catch(Exception e) { 
            DebugError("FunctionsGVP.ValidaRUT.Exception [" + e.getMessage() + "]");
            e.printStackTrace();
            return false;
        }
    }
 
    
    /* INICIO Retorno de Variables del FunctionsGVP*/
 
    public String getInstanceID() {
       return InstanceID;
   }
 
 
   public boolean isDVesK() {
       return DVesK;
   }    
    /*
     *  FIN Retorno de Variables del FunctionsGVP
     *  */
   
   
   /*
    *    INICIO Funciones Genericas para Fechas y Validacion de Horario       
    * */    
   
   public String obtenerHostname(){
       String hostname = "";
       try 
       {
           InetAddress address = InetAddress.getLocalHost();
           hostname = address.getHostName();
       }
       catch (UnknownHostException e) 
       {        
           DebugError("USUARIO : ERROR " + e.getMessage());
           e.printStackTrace();
       }
       return hostname;
   }
   
   public String obtenerHostaddress(){
       String hostaddress = "";
       try 
       {
           InetAddress address = InetAddress.getLocalHost();
           hostaddress = address.getHostAddress();
       }
       catch (UnknownHostException e) 
       {        
           DebugError("USUARIO : ERROR " + e.getMessage());
           e.printStackTrace();
       }
       return hostaddress;
   }
   
   public String obtenerDirectorioTomcatWindows(){
        return System.getProperty("catalina.base");
    }
   
   public int obtenerTimestampEpoch(){
       return (int) (System.currentTimeMillis() / 1000L);
   }
   
   
   /*
    * Metodo que lee el Status de los Speech Server
    * Verifica si el Text To Speech debe estar ACTIVO o INACTIVO
    * Flujos de IVR cambian sus Input acorde a este valor
    * */
   public boolean verificarTTS(){
        String status = ObtenerParametroProperties("TTS_Estatus", "ConfiguracionServiciosWeb.properties");
        
        if (status.equals("ACTIVO")){
            return true;
        }else{
            return false;
        }
    }
   
   /*
    * Metodo que lee el Status de los Speech Server
    * Verifica si el Automatic Speech Recoginition debe estar ACTIVO o INACTIVO
    * Flujos de IVR cambian sus Input acorde a este valor
    * */
   public boolean verificarASR(){
        String status = ObtenerParametroProperties("ASR_Estatus", "ConfiguracionServiciosWeb.properties");
        
        if (status.equals("ACTIVO")){
            return true;
        }else{
            return false;
        }
    }
   
   
   /** Metodo para rellenar cadenas
     * Orden = 1:derecha   2:izquierda
     * @params valor String
     * @params caracter String
     * @params largo Int
     * @params orden Int
     * @return valor
     */
 public String Rellena(String valor, String caracter, int largo, int orden)
 {
     int largoV = valor.length();
 
     if(orden == 1)
     {
         for(int i = largoV; i<largo; i++)
         {
             valor = valor + caracter;
         }
     }
     else
     {
         for(int i = largoV; i<largo; i++)
         {
             valor = caracter + valor;
         }
     }
 
     return valor;
 }
 
 public static String tiraceros ( String Numero)
 {
  /*
  Elimina los ceros a la izquierda de una cantidad conservando el signo
  */
     boolean bTieneSigno = false;
     String sCero = "0";
     String sSigno = "-";
     String sCantidad = Numero;
 
     while ( sCantidad.length() > 0 && (sCantidad.substring(0,1).equals(sCero) || sCantidad.substring(0,1).equals(sSigno) )) {
 
         if (sCantidad.substring(0,1).equals(sCero))
             sCantidad = sCantidad.substring(1,sCantidad.length());
         else if (sCantidad.substring(0,1).equals(sSigno)) {
             bTieneSigno = true;
             sCantidad = sCantidad.substring(1,sCantidad.length());
         }
     }
     if ( sCantidad.length() == 0 )
         sCantidad = sCero + sCero; // los dos decimales ...
 
     if ( bTieneSigno )
         sCantidad = sSigno + sCantidad;
 
     return sCantidad;
 }
 
 public String rpad(String data, int length) {
     return rpad(data, length, " ");
 }
 
 
 
 public String rpad(String data, int length, String filler) {
     return Rellena(data, filler, length, 1);
 }
 
 
 
 public String lpad(String data, int length) {
     return lpad(data, length, "0");
 }
 
 
 
 public String lpad(String data, int length, String filler) {
     return Rellena(data, filler, length, 2);
 }
 
   
    public String getFechaActual(String formato) throws ParseException{ 
//        String zonaHoraria = ObtenerParametroProperties("Timezone", "ConfiguracionServiciosWeb.properties");
           
       TimeZone tz = TimeZone.getTimeZone(Timezone); 
           
       SimpleDateFormat DateFormatter = new SimpleDateFormat(formato);
       DateFormatter.setTimeZone(tz);
       fechaString = DateFormatter.format(new Date());      
       fechaDate = DateFormatter.parse(fechaString);
       return fechaString;
    } 
    
    public String getFechaAyer(String formato) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(formato);
           Calendar cal = Calendar.getInstance();
           cal.add(Calendar.DATE, -1);    
           return dateFormat.format(cal.getTime());
   }
    
   // Obtener el día de la semana, dada una Fecha
    public int getDayOfTheWeek(Date d){
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(d);
        return cal.get(Calendar.DAY_OF_WEEK);        
    }
    
    /*
    *    FIN Funciones Genericas para Fechas y Validacion de Horario       
    * */
    
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
//         long min = minutos - (horas*60);                     
         
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
    
    
    /**
     * 
     * @param RemoteHost
     * @param RemotePort
     * @param ConnectionTimeOut
     * @param ReadWriteTimeOut
     * @param Message2Send
     * @return
     */
    public String SendReceiveSocketMessageIO(String RemoteHost, int RemotePort, int ConnectionTimeOut, int ReadWriteTimeOut, String Message2Send)
    {
        String OutPutMessage = "";
        
        Debug("[Functions - SendReceiveSocketMessageIO] (init)", "Standard");
        Debug("[Functions - SendReceiveSocketMessageIO] Host: " + RemoteHost + ", Port: "+ String.valueOf(RemotePort) + ", TimeOut: "+ String.valueOf(ConnectionTimeOut) + " (ms)", "Trace");    
        Debug("[Functions - SendReceiveSocketMessageIO] Input Message: " + Message2Send, "Trace");
        
        Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - Intentando conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
        
        if( SocketRequest.OpenConnection(RemoteHost, RemotePort, ConnectionTimeOut, ReadWriteTimeOut) )
        {
            Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - conexion exitosa con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
            Debug("[Functions - SendReceiveSocketMessageIO (SendReceiveMessage)] - Enviando mensaje formateado: " + Message2Send, "Standard"); 
            
            OutPutMessage = SocketRequest.SendReceiveMessageIO(Message2Send);
            
            if( SocketRequest.IsSocketWithError() )
            {
                Debug("[Functions - SendReceiveSocketMessageIO (SendReceiveMessage)] - Problemas al enviar/recibir mensaje '[" + SocketRequest.GetErrorMessage() + "'].", "Standard");
                
                SocketIsOK = false;
                OutPutMessage = "";
            }
            else
            {
                SocketIsOK = true;
                Debug("[Functions - SendReceiveSocketMessageIO] - Se ha recibido el siguiente mensaje: '" + OutPutMessage + "'", "Standard");
            }
            
            Debug("[Functions - SendReceiveSocketMessageIO (CloseConnection)] - Cerrando conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");        
            SocketRequest.CloseConnection();
            
            if( SocketRequest.IsSocketWithError() )
                Debug("[Functions - SendReceiveSocketMessageIO (CloseConnection)] - Problemas al cerrar conexi�n '[" + SocketRequest.GetErrorMessage() + "'].", "Standard");               
        }
        else
        {
            Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - Problemas al realizar conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
            
            if( SocketRequest.IsSocketWithError() )
                Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - " + SocketRequest.GetErrorMessage() + "'.", "Standard");
            
            SocketIsOK = false;
        }
        
        Debug("[Functions - SendReceiveSocketMessageIO] (stop)", "Standard");
        
        return OutPutMessage;
    }    
    
    public boolean WriteLocalFile(String InputFileName, String InputMessage, boolean AppendMessage)
    {
        return ( WriteLocalFile(InputFileName, InputMessage, AppendMessage, true) );
    }
    
    public boolean WriteLocalFile(String InputFileName, String InputMessage, boolean AppendMessage, boolean PrintDetails)
    {
        boolean                IsOK                = false;
        FileOutputStream     oFileOutputStream    = null;
        
        Debug("[Functions - WriteLocalFile] (init)", "Standard");
        
        try
        {
            File oLocalFile    = new File(InputFileName);
 
            oLocalFile.createNewFile();
 
            if( oLocalFile.canWrite() )
            {
                if( AppendMessage )
                    oFileOutputStream = new FileOutputStream( oLocalFile, true );
                else
                    oFileOutputStream = new FileOutputStream( oLocalFile, false );
                                
                oFileOutputStream.write( InputMessage.getBytes() );
                oFileOutputStream.close();
                
                IsOK = true;
                
                if (PrintDetails == true )
                    Debug("[Functions - WriteLocalFile] - Se ha registrado la informacion '" + InputMessage + "' en forma exitosa.", "Standard");
            }
            else
            {
                IsOK = false;
                Debug("[Functions - WriteLocalFile] - Problemas al escribir archivo local '" + InputFileName + "'.", "Standard");
            }
        }
 
        catch( Exception e )
        {
            IsOK = false;
            Debug("[Functions - WriteLocalFile] - Se ha detectado el siguiente Error: " + e.getMessage(), "Trace");          
        }        
        
        Debug("[Functions - WriteLocalFile] (stop)", "Standard");
        
        return IsOK;
    }
    
    /**
     * 
     * @param pConnectionURL
     * @param pUserName
     * @param pPassword
     * @param DriverName
     * @param pReturnRecorset
     * @param pSQLQuery
     * @param ConnectionTimeOut
     * @param QueryTimeOut
     * @return
     */
    public boolean ExecuteSQLQuery(String pConnectionURL, String pUserName, String pPassword, String DriverName, boolean pReturnRecorset, String pSQLQuery, int ConnectionTimeOut, int QueryTimeOut)
    {
        boolean                Retorno                = false;
        int                    oIndex                = 0;
        int                    oRowsAffected        = 0;
        String                RecordName            = "";
        String                RecordValue         = "";
        DBAccess            LocalDAP            = new DBAccess();
        ArrayList<String>    RecordFieldName        = new ArrayList<String>(1);        
        ResultSet            OutputRecorset        = null;
        ResultSetMetaData    eResultSetMetaData    = null;
 
        Debug("[Functions - ExecuteSQLQuery] (init)", "Standard");
        Debug("[Functions - ExecuteSQLQuery] *** ConnectionURL:" + pConnectionURL, "Trace");
        Debug("[Functions - ExecuteSQLQuery] *** DriverName   :" + DriverName, "Trace");
        Debug("[Functions - ExecuteSQLQuery] *** UserName     :" + pUserName, "Trace");
        Debug("[Functions - ExecuteSQLQuery] *** Password     :" + pPassword, "Trace");
        Debug("[Functions - ExecuteSQLQuery] *** Out Recordset:" + String.valueOf(pReturnRecorset), "Trace");
        Debug("[Functions - ExecuteSQLQuery] *** Connection TO:" + String.valueOf(ConnectionTimeOut), "Trace");
        Debug("[Functions - ExecuteSQLQuery] *** Query TimeOut:" + String.valueOf(QueryTimeOut), "Trace");
        Debug("[Functions - ExecuteSQLQuery] *** SQL Sentence :" + pSQLQuery, "Trace");
        
        RecordSetKVPs.clear();
        
        if( LocalDAP.OpenDataBase(pConnectionURL, DriverName, pUserName, pPassword, ConnectionTimeOut) )
        {
            Debug("[Functions - ExecuteSQLQuery] - conexion exitosa con la Base de Datos '" + pConnectionURL + "',", "Standard");         
 
            Debug("[Functions - ExecuteSQLQuery] - Ejecutando Query en la Base de Datos '" + pSQLQuery + "'", "Standard");
 
            if( pReturnRecorset == true )
            {
                OutputRecorset = LocalDAP.ExecuteQuery(pSQLQuery, QueryTimeOut);
            }
            else
            {
                oRowsAffected     = LocalDAP.ExecuteUpdate( pSQLQuery, QueryTimeOut);
 
                Retorno         = true;
                OutputRecorset     = null;
 
                Debug("[Functions - ExecuteSQLQuery] - Rows Affected: " + String.valueOf(oRowsAffected) + "',", "Standard");
            }
 
            if( OutputRecorset != null )
            {
                try {
                    eResultSetMetaData = OutputRecorset.getMetaData();
 
                    for( oIndex = 1; oIndex <= eResultSetMetaData.getColumnCount(); oIndex++ )
                        RecordFieldName.add( eResultSetMetaData.getColumnName( oIndex ) );
 
                    while( OutputRecorset.next() )
                    {
                        for( oIndex = 0; oIndex < RecordFieldName.size(); oIndex++ )
                        {
                            RecordName    = RecordFieldName.get(oIndex);                                                                
                            RecordValue    = OutputRecorset.getString( RecordName );
   
                            if( RecordName != null )
                            {
                                if( RecordValue != null )
                                    RecordSetKVPs.add( RecordName, RecordValue );
                                else
                                    RecordSetKVPs.add( RecordName, "" );
                            }
                        }
                    }
                    Retorno = true;
                } 
                catch (SQLException e) 
                {
                    Retorno = false;
                    Debug("[Functions - ExecuteSQLQuery] - Error: " + e.getMessage(), "Standard");
                }
 
                Debug("[Functions - ExecuteSQLQuery] - Resultado exitoso de la Query en la Base de Datos", "Standard");
                Debug("[Functions - ExecuteSQLQuery] - Numero Total de KVPairs: " + String.valueOf(RecordSetKVPs.count()), "Standard");
 
                if( RecordSetKVPs.count() > 0 )
                {
                    for ( oIndex = 0; oIndex < RecordSetKVPs.count(); oIndex++ ) 
                    {
                        Debug("[Functions - ExecuteSQLQuery] - KeyName: " + RecordSetKVPs.getKey(oIndex) +", KeyValue: " + RecordSetKVPs.getValue(oIndex), "Standard");    
                    }
                }
                LocalDAP.CloseDataBase();
            }
            else
            {
                if( LocalDAP.GetErrorMessage() != "" )
                {
                    Debug("[Functions - ExecuteSQLQuery] - Problemas al ejecutar query '" + pSQLQuery + "'", "Standard");
                    Debug("[Functions - ExecuteSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
                }
 
                if( pReturnRecorset )
                {
                    Retorno            = false;
                    RecordSetKVPs    = null;
                }
 
                LocalDAP.CloseDataBase();
            }
        }
        else
        {
            Debug("[Functions - ExecuteSQLQuery] - Problemas al conectar con la Base de Datos '" + pConnectionURL + "'.", "Standard");
            Debug("[Functions - ExecuteSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
        }
        Debug("[Functions - ExecuteSQLQuery] (stop)", "Standard");
    
        return Retorno;
    }    
    
    
    public boolean ExecutePLSQLQuery(String pConnectionURL, String pUserName, String pPassword, boolean pReturnRecorset, String pSQLQuery, int ConnectionTimeOut, int QueryTimeOut)
    {
        boolean                Retorno                = false;
        int                    oIndex                = 0;
        int                    oRowsAffected        = 0;
        String                RecordName            = "";
        String                RecordValue         = "";
        OracleDBAccess        LocalDAP            = new OracleDBAccess();
        ArrayList<String>    RecordFieldName        = new ArrayList<String>(1);        
        ResultSet            OutputRecorset        = null;
        ResultSetMetaData    eResultSetMetaData    = null;
 
        Debug("[Functions - ExecutePLSQLQuery] (init)", "Standard");
        Debug("[Functions - ExecutePLSQLQuery] *** ConnectionURL:" + pConnectionURL, "Trace");
        Debug("[Functions - ExecutePLSQLQuery] *** UserName     :" + pUserName, "Trace");
        Debug("[Functions - ExecutePLSQLQuery] *** Password     :" + pPassword, "Trace");
        Debug("[Functions - ExecutePLSQLQuery] *** Out Recordset:" + String.valueOf(pReturnRecorset), "Trace");
        Debug("[Functions - ExecutePLSQLQuery] *** Connection TO:" + String.valueOf(ConnectionTimeOut), "Trace");
        Debug("[Functions - ExecutePLSQLQuery] *** Query TimeOut:" + String.valueOf(QueryTimeOut), "Trace");
        Debug("[Functions - ExecutePLSQLQuery] *** SQL Sentence :" + pSQLQuery, "Trace");
        
        RecordSetKVPs.clear();
        
        if( LocalDAP.OpenDataBase(pConnectionURL, pUserName, pPassword, ConnectionTimeOut) )
        {
            Debug("[Functions - ExecutePLSQLQuery] - conexion exitosa con la Base de Datos '" + pConnectionURL + "',", "Standard");         
 
            Debug("[Functions - ExecutePLSQLQuery] - Ejecutando Query en la Base de Datos '" + pSQLQuery + "'", "Standard");
 
            if( pReturnRecorset == true )
            {
                OutputRecorset = LocalDAP.ExecuteQuery(pSQLQuery, QueryTimeOut);
            }
            else
            {
                oRowsAffected     = LocalDAP.ExecuteUpdate( pSQLQuery, QueryTimeOut);
 
                Retorno         = true;
                OutputRecorset     = null;
 
                Debug("[Functions - ExecutePLSQLQuery] - Rows Affected: " + String.valueOf(oRowsAffected) + "',", "Standard");
            }
 
            if( OutputRecorset != null )
            {
                try {
                    eResultSetMetaData = OutputRecorset.getMetaData();
 
                    for( oIndex = 1; oIndex <= eResultSetMetaData.getColumnCount(); oIndex++ )
                        RecordFieldName.add( eResultSetMetaData.getColumnName( oIndex ) );
 
                    while( OutputRecorset.next() )
                    {
                        for( oIndex = 0; oIndex < RecordFieldName.size(); oIndex++ )
                        {
                            RecordName    = RecordFieldName.get(oIndex);                                                                
                            RecordValue    = OutputRecorset.getString( RecordName );
   
                            if( RecordName != null )
                            {
                                if( RecordValue != null )
                                    RecordSetKVPs.add( RecordName, RecordValue );
                                else
                                    RecordSetKVPs.add( RecordName, "" );
                            }
                        }
                    }
                    Retorno = true;
                } 
                catch (SQLException e) 
                {
                    Retorno = false;
                    Debug("[Functions - ExecutePLSQLQuery] - Error: " + e.getMessage(), "Standard");
                }
 
                Debug("[Functions - ExecutePLSQLQuery] - Resultado exitoso de la Query en la Base de Datos", "Standard");
                Debug("[Functions - ExecutePLSQLQuery] - Numero Total de KVPairs: " + String.valueOf(RecordSetKVPs.count()), "Standard");
 
                if( RecordSetKVPs.count() > 0 )
                {
                    for ( oIndex = 0; oIndex < RecordSetKVPs.count(); oIndex++ ) 
                    {
                        Debug("[Functions - ExecutePLSQLQuery] - KeyName: " + RecordSetKVPs.getKey(oIndex) +", KeyValue: " + RecordSetKVPs.getValue(oIndex), "Standard");    
                    }
                }
                LocalDAP.CloseDataBase();
            }
            else
            {
                if( LocalDAP.GetErrorMessage() != "" )
                {
                    Debug("[Functions - ExecutePLSQLQuery] - Problemas al ejecutar query '" + pSQLQuery + "'", "Standard");
                    Debug("[Functions - ExecutePLSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
                }
 
                if( pReturnRecorset )
                {
                    Retorno            = false;
                    RecordSetKVPs    = null;
                }
 
                LocalDAP.CloseDataBase();
            }
        }
        else
        {
            Debug("[Functions - ExecutePLSQLQuery] - Problemas al conectar con la Base de Datos '" + pConnectionURL + "'.", "Standard");
            Debug("[Functions - ExecutePLSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
        }
        Debug("[Functions - ExecutePLSQLQuery] (stop)", "Standard");
    
        return Retorno;
    }    
    
    public String ExecOracleStoreProcedure(String pConnectionURL, String pUserName, String pPassword, int pNumberOutParameters, String pDelimiterOutParameters, String SQLQuery, int ConnectionTimeOut, int QueryTimeOut)
    {
        String OutputValue = "";
        
           Debug("[Functions - ExecOracleStoreProcedure] (init)", "Standard");
        Debug("[Functions - ExecOracleStoreProcedure] *** ConnectionURL :" + pConnectionURL, "Trace");
        Debug("[Functions - ExecOracleStoreProcedure] *** UserName      :" + pUserName, "Trace");
        Debug("[Functions - ExecOracleStoreProcedure] *** Password      :" + pPassword, "Trace");
        Debug("[Functions - ExecOracleStoreProcedure] *** Out Parameters:" + String.valueOf(pNumberOutParameters), "Trace");
        Debug("[Functions - ExecOracleStoreProcedure] *** Procedure     :" + SQLQuery, "Trace");
        
        if( DataAccessPointOracle.OpenDataBase(pConnectionURL, pUserName, pPassword, ConnectionTimeOut) )
        {
            Debug("[Functions - ExecOracleStoreProcedure] - conexion exitosa con la Base de Datos '" + pConnectionURL + "',", "Standard");
            
            Debug("[Functions - ExecOracleStoreProcedure] - Ejecutando Store Procedure en la Base de Datos '" + SQLQuery + "'", "Standard");
            
            if( DataAccessPointOracle.ExecuteCallableStatement(SQLQuery, pNumberOutParameters, pDelimiterOutParameters, QueryTimeOut) )
            {
                OutputValue = DataAccessPointOracle.GetResultadoSP();
                
                Debug("[Functions - ExecOracleStoreProcedure] - Se ha ejecutado en forma correcta store procedure '" + SQLQuery + "'", "Standard");
               Debug("[Functions - ExecOracleStoreProcedure] - Parametros obtenidos de store procedure '" + OutputValue + "'", "Trace");
            }
            else
            {
               Debug("[Functions - ExecOracleStoreProcedure] - Problemas al ejecutar store procedure '" + SQLQuery + "'", "Standard");
               Debug("[Functions - ExecOracleStoreProcedure] - Error: " + DataAccessPointOracle.GetErrorMessage(), "Trace");
            }
            
            DataAccessPointOracle.CloseDataBase();
        }
        else
        {
            Debug("[Functions - ExecOracleStoreProcedure] - Problemas al conectar con la Base de Datos '" + pConnectionURL + "'.", "Standard");
           Debug("[Functions - ExecOracleStoreProcedure] - Error: " + DataAccessPointOracle.GetErrorMessage(), "Trace");
        }
        
        Debug("[Functions - ExecOracleStoreProcedure] (stop)", "Standard");
        
        return OutputValue;
    }
    
    /**
     * Metodo utilizado para Leer un Archivo INI
     * 
    * 
    *
    * @param directory : Directorio en la carpeta donde se ubica el archivo (Raiz: %catalina_home%\lib\
    * @param fileName  : Nombre del archivo a leer
    * @return JSONArray - Cada Fila es un JSONObject
     * */
    public JSONArray ReadIniFile(String directory, String fileName, boolean hasHeaders){
        List<String> keys = new ArrayList<String>();
       JSONArray jsonArray = new JSONArray();
       
       
       String iniFile = directory+"/"+fileName;
       
       String catalina = System.getProperty("catalina.base");
       System.out.println("CATALINA:["+catalina+"]");
/*        if(catalina != null)
           iniFile = catalina+"\\lib\\"+directory+"\\"+fileName;
       else
           iniFile = directory+"\\"+fileName;
*/        
       try {
            ArrayList<String> valorPAN = new ArrayList<String>();
           FileInputStream fis = new FileInputStream(iniFile);
           BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        
           String line = null;
           int i = 0;
           while ((line = br.readLine()) != null) {
               if (hasHeaders && (i == 0)){
                   String[] auxkeys = line.split(";");
                   for (String key : auxkeys) {
                       keys.add(key.trim());
                   }
               }else{
                   String[] values = line.split(";");
                   int cont = 0;
                   JSONObject json = new JSONObject();
                   if (hasHeaders){
                       for (String value : values) {                        
                           json.put(keys.get(cont), value.trim());
                           cont++;                        
                       }
                   }else{
                       for (String value : values) {                        
                           json.put("Key_"+cont, value.trim());
                           cont++;                        
                       }
                   }
                   jsonArray.put(json);
               }                
               i++;
           }        
           
           br.close();            
       } catch (IOException | JSONException e) {
           DebugError("Error en ReadIniFile "+e.getMessage());
           e.printStackTrace();            
       }
       return jsonArray;
    }
    
    
    public String getParametro(String key) {
       return Params.GetValue(key);
   }
 
    
    public int generarCodigo(){    
        return (int)(100000 * Math.random()+ 1);
   
    }
    
  
}