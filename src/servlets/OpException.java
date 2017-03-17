package servlets;

public class OpException extends Exception{

	
	private static final long serialVersionUID = 1L;
	
	public OpException() { super(); }
	  public OpException(String message) { super(message); }
	  public OpException(String message, Throwable cause) { super(message, cause); }
	  public OpException(Throwable cause) { super(cause); }
}
