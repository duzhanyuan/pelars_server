package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import authorization.TokenService;


/**
 * Servlet implementation for logout
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		Cookie loginCookie = null;
		Cookie[] cookies = request.getCookies();

		//get the cookie token
		if(cookies != null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals("token")){
					loginCookie = cookie;
					break;
				}
			}
		}
		//makes token not valid setting max age to zero
		if(loginCookie != null){
			loginCookie.setMaxAge(0);
			String token = loginCookie.getValue();
			//and removes it from the list of tokens

			TokenService.remove(token);
			
			 //invalidate the session if exists
	        HttpSession session = request.getSession(false);
	
	        if(session != null){
	            session.invalidate();
	        }

			response.addCookie(loginCookie);
		}
		response.sendRedirect("logout.html");
	}

}
