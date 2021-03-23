import java.io.File;

public class TestJava{

     public static void main(String []args){
            	 File fDebugFile = new File("/Users/mreyes/Downloads/log28/banco-28012021.gvp5");
         	 File directory = new File(fDebugFile.getParent());
           	 if(directory.exists()){

           	     File[] listFiles = directory.listFiles();           
           	     long purgeTime = System.currentTimeMillis() - (20 * 24 * 60 * 60 * 1000);
        	     System.out.printf("purgeTime: %d \n ",purgeTime);
           	     for(File listFile : listFiles) {
        	     System.out.printf("file :: %d \n",listFile.lastModified());
           	         if(listFile.lastModified() < purgeTime) {
           	             if(!listFile.delete()) {
           	                 System.err.println("Unable to delete file: " + listFile);
           	             }
           	          }
           	       }
           	    }
         
     }
}
