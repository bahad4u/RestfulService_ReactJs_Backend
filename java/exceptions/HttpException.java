package exceptions;

public class HttpException extends Exception {
	private static final long serialVersionUID = 1L;
	private int errorCode;

	public HttpException(int code, String msg, Throwable cause) {
		super(msg, cause);
		this.errorCode = code;
	}
	
	public HttpException(int code, String msg) {
		super(msg);
		this.errorCode = code;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
