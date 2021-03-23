package eContact;

public class LogDetalle {

	public String tipoRegistro;
	public String codigoTransaccion;
	public String fechaTransaccion; 
	public String horaInicioTransaccion;
	public String horaTerminoTransaccion;
	public String codigoResultado;
	public String cuentaOrigen;
	public String cuentaDestino;
	public String monto;
	
	

	public LogDetalle() {
		super();
		// TODO Auto-generated constructor stub
	}
	public LogDetalle(String tipoRegistro, String codigoTransaccion, String fechaTransaccion,
			String horaInicioTransaccion, String horaTerminoTransaccion, String codigoResultado, String cuentaOrigen,
			String cuentaDestino, String monto) {
		super();
		this.tipoRegistro = tipoRegistro;
		this.codigoTransaccion = codigoTransaccion;
		this.fechaTransaccion = fechaTransaccion;
		this.horaInicioTransaccion = horaInicioTransaccion;
		this.horaTerminoTransaccion = horaTerminoTransaccion;
		this.codigoResultado = codigoResultado;
		this.cuentaOrigen = cuentaOrigen;
		this.cuentaDestino = cuentaDestino;
		this.monto = monto;
	}
	public String getTipoRegistro() {
		return tipoRegistro;
	}
	public void setTipoRegistro(String tipoRegistro) {
		this.tipoRegistro = tipoRegistro;
	}
	public String getCodigoTransaccion() {
		return codigoTransaccion;
	}
	public void setCodigoTransaccion(String codigoTransaccion) {
		this.codigoTransaccion = codigoTransaccion;
	}
	public String getFechaTransaccion() {
		return fechaTransaccion;
	}
	public void setFechaTransaccion(String fechaTransaccion) {
		this.fechaTransaccion = fechaTransaccion;
	}
	public String getHoraInicioTransaccion() {
		return horaInicioTransaccion;
	}
	public void setHoraInicioTransaccion(String horaInicioTransaccion) {
		this.horaInicioTransaccion = horaInicioTransaccion;
	}
	public String getHoraTerminoTransaccion() {
		return horaTerminoTransaccion;
	}
	public void setHoraTerminoTransaccion(String horaTerminoTransaccion) {
		this.horaTerminoTransaccion = horaTerminoTransaccion;
	}
	public String getCodigoResultado() {
		return codigoResultado;
	}
	public void setCodigoResultado(String codigoResultado) {
		this.codigoResultado = codigoResultado;
	}
	public String getCuentaOrigen() {
		return cuentaOrigen;
	}
	public void setCuentaOrigen(String cuentaOrigen) {
		this.cuentaOrigen = cuentaOrigen;
	}
	public String getCuentaDestino() {
		return cuentaDestino;
	}
	public void setCuentaDestino(String cuentaDestino) {
		this.cuentaDestino = cuentaDestino;
	}
	public String getMonto() {
		return monto;
	}
	public void setMonto(String monto) {
		this.monto = monto;
	}
	
	
	

	
}
