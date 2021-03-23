package Santander.test;

import org.json.JSONArray;
import org.json.JSONObject;

import Santander.FunctionsSantander;

public class Main {

	public static void main(String[] args) {
		
		FunctionsSantander c1 = new FunctionsSantander("FunctionsGVP.IVR_General.properties");
	
		   JSONObject result = new JSONObject();
		try {
		//	result=c1.getListaNegraSP("931494924");
			//result=c1.getValidaOfertaSP("124626692");
			//result=c1.getHorarioSP("ivr");
		
			
			//Prueba Subsegmento
		/*	 result =c1.getSubSegmento("001", "GAUDI", "301");
			   if (result.getString("resWS").equals("OK")){      
		            result.put("DescripcionSubsegmento", result.getString("subsegmento"));
		        	
		        	}else{
		            result.put("trx_codigoError",(!result.isNull("CODERROR") ? result.getString("CODERROR") : ""));
		            result.put("DescripcionSubsegmento", "");
		           
		         }
		 */
			//Prueba de SMAMP005
		      // result =  c1.getConsultaSaldos("001", "GHOBJ", "00350205800058097109" , "https://dgstcaqtb20.cl.bsch:11150/Services/wsCL1MDMPV3_CONSaldos");

			
			//Prueba de AHRBG050
		   //    result =  c1.getSaldoAhorros("001", "GHOBI", "00350179001001273627" , "https://dgstcaqtb40.cl.bsch:10300/Services/wsCL1MDKA01-CONSaldoAhorros");

			//Prueba de CCCKC157
	       // result =  c1.getCartolaOnLine("001", "GHOBJ", "000000016144" , "https://dgstcaqtb40.cl.bsch:10300/Services/wsCL1MDKC02-CONCartolaOnLine");
			
			
			//Prueba de CCCKC174
	       //  result =  c1.getCLIMDKC01_ConsultaSaldos("001", "GHOBJ", "00350002007085118980" , "https://dgstcaqtb40.cl.bsch:11110/Services/wsCL1MDKC01-CONSaldos");
					
			
			
			//Prueba de PNJPE336

		/*	result=c1.getCrucePreHechoIVR("003", "GAUDI", "0012250834K", "https://dgbmrfctb01.cl.bsch:14080/Services/AppIVR/wsCL1CO-CONCrucePreHechoIVR");
       	 
			 
	 	    if (result.getString("resWS").equals("OK")){      
	    	 result.put("con_productos", "SI");
	    	   
    		   if(!result.getString("trx_datos_respuesta_cuenta_corriente").isEmpty()){
    			 JSONObject cuenta_corriente =result.getJSONObject("trx_datos_respuesta_cuenta_corriente");
    			 result.put("trx_datos_respuesta_cuenta_corriente", cuenta_corriente);
    			 
    			 JSONArray detArray = (cuenta_corriente.has("detalle") ) ? cuenta_corriente.getJSONArray("detalle") : new JSONArray();    			
    				for(int j = 0; j < detArray.length(); ++j) { // asi saco la data
    					String dato=detArray.getString(j);
    					//JSONArray jObj = detArray.getJSONArray(j);
    					//for (int i = 0; i < jObj.length(); ++i) {
    					//	String data=jObj.getString(i);											
    					//}
    				}
    			
    					
    		   }
    		   
   	    	}else{
	        result.put("con_productos", "NO");
	        result.put("trx_codigoError",result.getString("CODERROR"));        
	       
	        }
	 	   */
			
			//Prueba de AESTADOSERVICIO
			 //result =  c1.getEstadoServicio( "001", "GAUDI", "00036355301", "IT",  "A", "https://dgstcaqtb60.cl.bsch:14840/Services/AppGenericosGeneral2/wsCL1MDLDAP0004-MANEstadoServicio");
			 
			//Prueba de CAPBG574-->WSRutTitularYBeneficiario
		   //result=c1.getCONPorRutTitularYBeneficiario("001", "GAUDI", "00036355301", "https://dgstcaqtb20.cl.bsch:10300/Services/wsCL1MDBP3K_CONPorRutTitularYBeneficiario");
		    
			
			//Prueba de PNJPE221 -->WsDatosPersonas  
		    result=c1.getDatosBasicosPersona("001", "GAUDI", "00261072157", "https://dgstcaqtb40.cl.bsch:11180/Services/wsCL1MDPE68-CONDatosBasicosPersonas");
		    		
		     
		    if (result.getString("resWS").equals("OK")){      
		    	result.put("trx_cliente", "SI");
		    	result.put("subsegmento", result.getString("subsegmento"));
		    	result.put("A_PATERNO", result.getString("A_PATERNO"));
		    	result.put("A_MATERNO", result.getString("A_MATERNO"));
		    	result.put("NOMBRE", result.getString("NOMBRE"));
		    	}else{
		        result.put("trx_cliente", "NO");
		        result.put("trx_codigoError",(!result.isNull("CODERROR") ? result.getString("CODERROR") : ""));
		        result.put("subsegmento", "");
		        result.put("A_PATERNO", result.getString(""));
		    	result.put("A_MATERNO", result.getString(""));
		    	result.put("NOMBRE", result.getString(""));
		        }
		    
		 	//LDAPKX02-->WSConsultaServiciosCliente 
	/*result=c1.getConsultaServiciosCliente("001", "GAUDI", "00051731034", "https://dgstcaqtb60.cl.bsch:14840/Services/AppGenericosGeneral2/wsCL1MDLDAP0003-CONServiciosCliente");
			
		     if (result.getString("COD").equals("OK")){      
		    		result.put("retorno", "SI");
		    	}else if (result.getString("COD").equals("IT")){
		        	result.put("retorno", "NO");
		        	result.put("trx_codigoError",result.getString("CODERROR"));
		        }else if (result.getString("COD").equals("AU")){
		            result.put("retorno", "SI");
		        }else{
		            	 result.put("retorno", "NO");
		            	 result.put("trx_codigoError",result.getString("CODERROR"));
		            	 
		            }
		    */
		   //result= c1.getWsCrucePreHechoIVR("001", "GAUDI", "0012250834K", "https://dgstcaqtb60.cl.bsch:14840/Services/AppIVR/wsCL1CO-CONCrucePreHechoIVR");
		 //  result =c1.getWSLogin("00122368769", "1357");
			
			
			// webservice latinia
	     /*   String ani = "93457383";
	    	String rut ="222222222";
	    	String motivo = "IVR Alerta Fraude Santander Pass Configuración Apoyada por un tercero Rut Cliente ";
	      	String correoto= "andres.vidal@santander.cl";
	    	String correofrom="mensajeria@santander.cl";
	    	String nombre = "Prueba"; 
	    	
	    	
	  
	        Date fechaActual = new Date();
	        DateFormat formatoHora = new SimpleDateFormat("HH/mm/ss");
	        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
	        String fechaReg=formatoFecha.format(fechaActual)+ " - "+ formatoHora.format(fechaActual);
	        
	        
	        DateFormat formatoCompleto = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");
	        Date fechaInicial=formatoCompleto.parse("01-01-1970 00:00:00:000");
	        String fechaF=formatoCompleto.format(fechaActual);
	        Date fechafinal=formatoCompleto.parse(fechaF);
	        long tiempo=(long) ((fechafinal.getTime()-fechaInicial.getTime()));
	        
	        String ts= String.valueOf(tiempo);

	    	StringBuilder datos = new StringBuilder();
	    
	    	datos.append("<message id='um1o5axykzckdev8cvs584l9' ts='").append(ts).append("'>").append("\n")
	    	     .append("<head> " ).append("\n")
	    	     .append("<type ref='inot'> " ).append("\n")
	    	     .append("<format>inot</format>" ).append("\n")
	    	     .append("<mroute>out</mroute>" ).append("\n")	    	     
	    	     .append("</type>" ).append("\n")
	    	     .append("<info>" ).append("\n")
	    	     .append("<loginEnterprise>SCHCL</loginEnterprise>" ).append("\n")
	    	     .append("<refContract>SantanderPASS</refContract>" ).append("\n")
	    	     .append("<customer Keyname='' KeyValue=''/>" ).append("\n")
	    	     .append("<deliveryChannels max='1'>email</deliveryChannels>" ).append("\n")	    	    
	    	     .append("</info>" ).append("\n")
	    	     .append("<address-list>" ).append("\n")
	    	     .append("<address type='to' class='email' ref='" ).append(correoto).append("'/>").append("\n")
	    	     .append("<address type='from' class='email' ref='" ).append(correofrom).append("'/>").append("\n")
	    	     .append("</address-list>" ).append("\n")
	    	     .append("</head>" ).append("\n")
	    	     .append("<body>" ).append("\n")
	    	     .append("<content refTemplate='SantanderPASS'>SantanderPASS</content>" ).append("\n")
	    	     .append("<templateParams ref='#default'>" ).append("\n")
	      	     .append("<param name='canal'>IVR</param>" ).append("\n")  	
	      	     .append("<param name='telefono'>").append(ani).append("</param>").append("\n")  		
	      	     .append("<param name='rut'>").append(rut).append("</param>").append("\n") 
	      	     .append("<param name='Nombre'>").append(nombre).append("</param>").append("\n") 
	      	     .append("<param name='fechaReg'>").append(fechaReg).append("</param>").append("\n")
	      	     .append("<param name='Motivo'>").append(motivo).append("</param>").append("\n")			
	    	     .append("</templateParams>" ).append("\n")
	    	     .append("</body>" ).append("\n")	
	    	     .append("</message>" ).append("\n");

			result=c1.getAdaptorAutht(datos.toString().trim(), "limspae-msg", "latinia", "http://dintmnswa01.cl.bsch:9084/wsAdaptor/wsAdaptorAuth");
		   */
			
			
		  // result =  c1.getConsultaTelefono("931494906", "http://dgstcaqtb20.cl.bsch:13500/Services/wsCL1SPSPORAC-CONConsultaTelefono");
	        //result =  c1.getValidacionPin("GHOBJ", "001", "001", "1938", "2929", "00156926713", "https://dgbmrfctb01.cl.bsch:14080/Services/AppIVR/wsCL1CO-CONPinSMAMP228");
   	       //result = c1.getListaNegra("931494906") ;
		
	   //WebServices para Santander Pass
			 result =  c1.getUserTokenList("00031058546");
			 result.put("listado", "");
			 if (result.getString("status").equals("SUCCESS")){
					result.put("resWS", "OK");
				}else{
					result.put("resWS", "NOK");
				}
			 
			 
			 if (result.getString("resWS").equals("OK")){      
				 JSONArray  detArray =result.getJSONArray("tokenList");			
				 if(!detArray.equals("") && detArray.length()>0){		 
					 for(int j = 0; j < detArray.length(); ++j) {					
						 JSONObject jObj = detArray.getJSONObject(j);
					
								if(jObj.getString("state").equals("CURRENT") && jObj.getString("activationState").equals("ACTIVATED")){
						    	  	result.put("listado", "Activo");
						    	  	break;
						    	} else if(jObj.getString("state").equals("ACTIVATED") && jObj.getString("activationState").equals("HOLD_PENDING")){
						    		result.put("listado", "PendienteActivacionFinal");
						    		break;
						    	}else if(jObj.getString("state").equals("PENDING") && jObj.getString("activationState").equals("CREATED")){
						    		result.put("listado", "NohayActivacion");
						    		break;
						    	}		
					 }
				 }else{
					 result.put("listado", "");
				 }
			 }
		    	
	//Webservice wsCL1CO-CONEstadoDispositivo
			
			/* result =  c1.getEstadoDispositivo("001","GAUDI","00122368769");
		    	
			 if (result.getString("resWS").equals("OK")){      
	    		 JSONArray  detArray =result.getJSONArray("tokenList");			
	    						 
	    			 for(int j = 0; j < detArray.length(); ++j) {					
	    				 JSONObject jObj = detArray.getJSONObject(j);
	    			
	    						if(jObj.getString("state").equals("HOLD_PENDING") && jObj.getString("activationState").equals("ACTIVATED")){
	    				    	  	result.put("devuelve", "valida");
	    				    	     break;
	    				    	}else {
	    				    		result.put("devuelve", "novalida");
	    				    		
	    				    	}		
	    			 }
	    	 
	        	
	        	}else{
	      
	            result.put("trx_codigoError",(!result.isNull("CODERROR") ? result.getString("CODERROR") : ""));
	         
	            }*/
			
	  //wsCL1CO-CONCambioNumeroCelular
			
		/*	result =  c1.getCambioNumeroCelular("001","GAUDI","00122368769");
			String ani = "931494906";
			  if (result.getString("resWS").equals("OK")){ 
						      
				 if(result.getString("numero").equals(ani)){
					 result.put("respuesta", "correcta");
				 }else{
					 result.put("respuesta", "incorrecta");
				 }
			   		    	
			     
		   	    }else{   
		   	    	
		   	     if(result.getString("MSGERROR").contains("48")){
		   	    	 result.put("respuesta", "antiguedad");		
		   	     }
		         result.put("trx_codigoError",(!result.isNull("CODERROR") ? result.getString("CODERROR") : ""));      
		        }
		    */
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
