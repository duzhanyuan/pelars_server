package servlets;

import javax.servlet.http.*;
import java.io.*;

/**
* Class used to warp the response in order to modify it before the client receives it
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

	private CharArrayWriter writer;

	public ResponseWrapper(HttpServletResponse response){
		super(response);
		writer = new CharArrayWriter();
	}

	public PrintWriter getWriter(){
		return (new PrintWriter(writer));
	}

	public String toString(){
		return writer.toString();
	}

	public char[] toCharArray(){
		return (writer.toCharArray());
	}

}
