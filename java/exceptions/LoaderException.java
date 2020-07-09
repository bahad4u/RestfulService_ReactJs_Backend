package exceptions;

public class LoaderException extends Exception{
	private static final long serialVersionUID = 1L;
	private int errorCode;
	
	public LoaderException(int errorCode,String message,Throwable cause){
		super(message,cause);
		this.errorCode = errorCode;
	}
	
	public LoaderException(String message,Throwable cause){
		super(message,cause);
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
