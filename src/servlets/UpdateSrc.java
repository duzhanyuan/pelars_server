package servlets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pelarsServer.Error;

@WebServlet("/deploy/*")

/**
 * 
 * @author Lorenzo Landolfi
 * This servlet is used to update source code and rebuild of a tomcat app
 * source code absolute path and tomcat root directory can be specified as http parameters,
 * else, they are searched in the APPSOURCE and TOMCAT variables in .basrc
 */
public class UpdateSrc extends HttpServlet{

	//path to pelars server source code
	String src_path = null;

	//path to the dir where .bashrc is located
	String home_dir = null;

	//path to tomcat directory
	String tomcat_dir = null;

	boolean pull = true;

	static PrintWriter out;

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		out = response.getWriter();

		if (!ACL_RuleManager.Check(Util.getUser(request), "DEPLOY")){
			response.setStatus(403);
			out.println(new Error(135).toJson());
			return;
		}

		pull = (request.getParameter("nopull") != null) ? false : true;

		src_path = request.getParameter("src");			

		//TODO: give default directory to home dir :/home/poweredge

		home_dir = System.getProperty("user.home");

		if(!home_dir.endsWith("/")){
			home_dir = home_dir + "/";
		}

		//TODO: give default directory to tomcat dir: /home/poweredge/tools/...

		tomcat_dir = request.getParameter("tomcat");

		autoCompile(out, response, request);
	}

	/**
	 * 
	 * @param out
	 * @param response
	 * @throws IOException
	 * checks the system variables PELARS for the source directory and TOMCAT for the Tomcat installation directory, works only in linux because searches only in ~/.bashrc
	 */
	private void autoCompile(PrintWriter out , HttpServletResponse response, HttpServletRequest request) throws IOException{

		//parse bashrc to find the PELARS variable
		PrintWriter out_p = response.getWriter();

		if (src_path == null || tomcat_dir == null){

			//if MACOS is .profile

			try (BufferedReader br = new BufferedReader(new FileReader(home_dir + ".bashrc"))) {

				String line;

				while ((line = br.readLine()) != null) {
					if(line.toLowerCase().contains("APPSOURCE".toLowerCase()) && src_path == null){
						if(line.charAt(line.toLowerCase().indexOf("pelars") + 6) == '='){
							src_path = line.substring(line.lastIndexOf("=")+1);
						}
					}
					if(line.toLowerCase().contains("TOMCAT".toLowerCase()) && tomcat_dir == null){
						if(line.charAt(line.toLowerCase().indexOf("tomcat") + 6) == '='){
							tomcat_dir = line.substring(line.lastIndexOf("=")+1);
						}
					}
				}
			} catch (IOException e) {
				response.setStatus(500);
				out.println("ERROR!");
				return;
			}
		}
		out.println("pulling sources");

		out.println(executeCommand("git -C " + src_path + " pull origin master", out_p));
		out.println(executeCommand("git -C " + src_path + " submodule foreach git pull origin master", out_p));

		//would like also to simply copy the pages without generating a .war
		if(request.getParameter("copy") != null){
			out.println("copying files");
			out.print(executeCommand("cp " + src_path +"/Html/* " + tomcat_dir + "/webapps/pelars/", out_p));
			out.print(executeCommand("cp " + src_path +"/Js/* " + tomcat_dir + "/webapps/pelars/", out_p));
			out.print(executeCommand("cp " + src_path +"/Jsp/* " + tomcat_dir + "/webapps/pelars/", out_p));
			out.print(executeCommand("cp " + src_path +"/Css/* " + tomcat_dir + "/webapps/pelars/", out_p));
		}
		else{
			out.println("compiling and deploying");
			out.print(executeCommand("ant -buildfile " + src_path +"build.xml -Dtomcat_path=" + tomcat_dir, out_p));
		}

	}
	
	/**
	 * 
	 * Executes a generic shell command, captures std output and error and put them in the servlet response
	 */
	public String executeCommand(String command, PrintWriter out) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader_error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}
			while ((line = reader_error.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			out.println(e.getMessage());
		}

		return output.toString();

	}
}
