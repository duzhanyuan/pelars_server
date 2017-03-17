package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import pelarsServer.Error;

@WebServlet("/completevideo/*")
public class VideoManager extends HttpServlet{


	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		PrintWriter out = response.getWriter();

		response.setHeader("Content-Type", "application/json");

		if(request.getPathInfo() == null){
			out.println(new Error(113).toJson());
			return;
		}

		String [] parameters = request.getPathInfo().split("/");

		if(request.getPathInfo() == null || parameters.length <= 2){
			response.setStatus(404);
			out.println(new Error(113).toJson());
		}
		else{

			if(Util.isInteger(parameters[1])){

				if(parameters.length > 2){

					GetVideo(Long.parseLong(parameters[1]), parameters[2], response, request, out);
				}
			}
			else{
				out.println(new Error(116).toJson());
			}
		}
	}


	public void GetVideo(Long id, String type, HttpServletResponse response, HttpServletRequest request, PrintWriter out) throws IOException, ServletException{

		String token = Util.getToken(request);
		request.setAttribute("local", true);
		response.sendRedirect("/pelars/uploads/session_videos/" + id + "/" + type + ".mp4" + "?token=" + token);

	}
}
