package authorization;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 *@author Lorenzo Landolfi
 * This filter: checks if the user has been authenticated and it is not assigned "unauthorized" role
 */
public class AuthFilter implements Filter {

	@Override
	public void destroy() {
	}


	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;


		//auth attribute set by UserFilter class. If the previous filter sets auth attribute pass to the next filter
		if ((request.getAttribute("auth") != null && !((boolean)request.getAttribute("auth")))) {
			chain.doFilter(req, res);
			return;
		}

		String m_token = request.getParameter("token");
		String h_token = request.getHeader("X-Auth-Token");


		boolean token_valid = false;
		Token t = null;

		//if there is not the parameter token or in the header, then it must be in a cookie
		if(m_token == null && h_token == null){

			String token = null;
			Cookie[] cookies = request.getCookies();

			if(cookies != null){
				for(Cookie cookie : cookies){
					if(cookie.getName().equals("token")) {
						token = cookie.getValue();
						t = new Token(token);
					}
				}
			}
		}

		//there is the token as parameter or as header
		else {
			if (h_token != null){
				t = new Token(h_token);
			}
			else{
				t = new Token(m_token);
			}
		}

		try {
			//check if the token is valid and the role is not unauthorized
			if (t != null){
				token_valid = TokenService.isValid(t.getValue(), request.getRemoteAddr()) && !t.getRole().equals("unauthorized");
			}
		} catch (Exception e) {
			response.setStatus(500);
			return;
		}

		//if didn't find the token or it was not valid redirect to login page
		if(token_valid == false){
			
			//get the names of the parameters
			Enumeration<String> e = request.getParameterNames();
			String path_params = "?";
			while(e.hasMoreElements()){
				String p = e.nextElement();
				String val = request.getParameter(p);
				
				path_params = path_params + p + "=" + val + "&";
			}	
			
			//remove last &
			path_params = path_params.substring(0, path_params.length()-1);

			response.setStatus(401);
			response.sendRedirect("/pelars/unauthorized.jsp?p-url="+request.getRequestURL().toString() + path_params);
			return;
		}

		chain.doFilter(req, res);
	}


	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}
