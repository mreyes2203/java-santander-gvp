package eContact;

import java.util.List;

public class LogGeneral {

	public String tipoRegistro;
	public String nroLinea;
	public String fechaLlamada; 
	public String horaInicioLlamada;
	public String horaTerminoLlamada;
	public String rut;
	public String tipoTelefono;
	public String anis;
	public String dnis;
	public String condicionTermino;
	public String controlTransacciones;
	public List<LogDetalle> listDetail;
	
	
	public LogGeneral() {
		super();
		// TODO Auto-generated constructor stub
	}


	public String getTipoRegistro() {
		return tipoRegistro;
	}


	public void setTipoRegistro(String tipoRegistro) {
		this.tipoRegistro = tipoRegistro;
	}


	public String getNroLinea() {
		return nroLinea;
	}


	public void setNroLinea(String nroLinea) {
		this.nroLinea = nroLinea;
	}


	public String getFechaLlamada() {
		return fechaLlamada;
	}


	public void setFechaLlamada(String fechaLlamada) {
		this.fechaLlamada = fechaLlamada;
	}


	public String getHoraInicioLlamada() {
		return horaInicioLlamada;
	}


	public void setHoraInicioLlamada(String horaInicioLlamada) {
		this.horaInicioLlamada = horaInicioLlamada;
	}


	public String getHoraTerminoLlamada() {
		return horaTerminoLlamada;
	}


	public void setHoraTerminoLlamada(String horaTerminoLlamada) {
		this.horaTerminoLlamada = horaTerminoLlamada;
	}


	public String getRut() {
		return rut;
	}


	public void setRut(String rut) {
		this.rut = rut;
	}


	public String getTipoTelefono() {
		return tipoTelefono;
	}


	public void setTipoTelefono(String tipoTelefono) {
		this.tipoTelefono = tipoTelefono;
	}


	public String getAnis() {
		return anis;
	}


	public void setAnis(String anis) {
		this.anis = anis;
	}


	public String getDnis() {
		return dnis;
	}


	public void setDnis(String dnis) {
		this.dnis = dnis;
	}


	public String getCondicionTermino() {
		return condicionTermino;
	}


	public void setCondicionTermino(String condicionTermino) {
		this.condicionTermino = condicionTermino;
	}


	public String getControlTransacciones() {
		return controlTransacciones;
	}


	public void setControlTransacciones(String controlTransacciones) {
		this.controlTransacciones = controlTransacciones;
	}


	public List<LogDetalle> getListDetail() {
		return listDetail;
	}


	public void setListDetail(List<LogDetalle> listDetail) {
		this.listDetail = listDetail;
	}
	
	
	
	
	
	
	
}
