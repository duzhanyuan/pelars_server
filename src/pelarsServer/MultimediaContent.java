package pelarsServer;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import authorization.PasswordService;

import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 * class representing a multimedia data (video or image)
 *
 */
public class MultimediaContent extends Data{

	public enum VIEW {
		mobile, screen, people, workspace
	}

	public static boolean contains(String test){
		for (VIEW c : VIEW.values()) {
			if (c.name().equals(test)) {
				return true;
			}
		}
		return false;
	}

	public String type;
	public String view;
	public String creator;
	public String triggering;
	public String mimetype;
	public Blob thumbnail;

	public String getCreator(){
		return creator;
	}

	public void setCreator(String creator){
		this.creator = creator;
	}

	public String getTriggering() {
		return triggering;
	}


	public void setTriggering(String triggering) {
		this.triggering = triggering;
	}


	public void setThumbnail(Blob thumbnail) {
		this.thumbnail = thumbnail;
	}


	public Blob getThumbnail() {
		return thumbnail;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}


	public String getType(){
		return type;
	}

	public void setType(String type){
		this.type = type;
	}

	public Object clone(){  
		try{  
			return super.clone();  
		}catch(Exception e){ 
			return null; 
		}
	}

	public byte[] getData() throws IOException{

		Path path = Paths.get(System.getProperty("upload.location") + this.getSession().getId() + "/" + this.getType() + "/" + this.getId() + "." + this.getMimetype());
		return Files.readAllBytes(path);
	}

	public void setData(byte[] b) throws IOException{

		ByteArrayInputStream bis = new ByteArrayInputStream(b);
		Path path = Paths.get(System.getProperty("upload.location") + this.getSession().getId() + "/" + this.getType() + "/" + this.getId() + "." + this.getMimetype());
		Files.copy(bis, path);
	}

	public String getMimetype(){
		return mimetype;
	}

	public void setMimetype(String s){
		mimetype = s;
	}

	public long getSize(){

		File file = new File(System.getProperty("upload.location") + this.getSession().getId() + "/" + this.getType() + "/" + this.getId() + "." + this.getMimetype());
		return (int)file.length();
	}

	public JSONObject toJson() {
		JSONObject jo = new JSONObject();
		try{
			jo.put("id", id);
			jo.put("type", type);
			jo.put("mimetype", mimetype);
			jo.put("time", time);
			jo.put("view", view);
			jo.put("trigger", triggering);
			jo.put("creator", creator);
			jo.put("session", this.getSession().getId());
			jo.put("size", this.getSize());
			jo.put("data", "http://pelars.sssup.it/pelars/multimedia/" + session.getId() + "/"+id);			
		}catch (JSONException e){
			e.printStackTrace();
		}
		return jo;
	}
	
	/**this methods writes html string in the data field of the returned json message.
	 */
	public JSONObject toJson(boolean html_url){

		if(html_url == false){
			return this.toJson();
		}
		else{
			JSONObject jo = new JSONObject();
			try{
				jo.put("id", id);
				jo.put("type", type);
				jo.put("mimetype", mimetype);
				jo.put("time", time);
				jo.put("size", this.getSize());
				jo.put("session", this.getSession().getId());
				jo.put("view", view);
				jo.put("trigger", triggering);
				jo.put("creator", creator);
				//			jo.put("phase", phase);
				jo.put("data", "<a href=\"multimedia/" + session.getId() + "/"+ id + "\"  target=\"_blank\"> http://pelars.sssup.it/pelars/multimedia/" + session.getId() + "/"+ id + "</a>");			
			}catch (JSONException e){
				e.printStackTrace();
			}
			return jo;
		}

	}

	//TODO: must be changed, take data from file instead of database
	public String getDataString() throws SQLException, IOException{

		return new String(this.getData());

	}

	/**
	 * 
	 * @param session
	 * @throws IOException
	 * @throws SQLException
	 * generates thumbnail from the original data. Type must be image
	 */
	/*public void generateThumbnail(Session session) throws IOException, SQLException{

		byte[] b;
		//TODO VERY TEMPORARY
		if(this.getSession() == 1350 && this.view.equals("mobile")){

		File initialFile = new File(System.getProperty("upload.location") + "images/" + this.getSession() + "_" + this.getId() + "." + this.getMimetype());
		InputStream targetStream = new FileInputStream(initialFile);
		b = IOUtils.toByteArray(targetStream);
		}
		else{

		//get the Base64 representation of the content data
		String s = new String(data.getBytes(1, (int) data.length()));
		//decode it as an array of bytes
		b = PasswordService.base64ToByte(s);
		}

		ByteArrayInputStream bis = new ByteArrayInputStream(b);

		BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

		img.createGraphics().drawImage(ImageIO.read(bis).getScaledInstance(100, 100, Image.SCALE_SMOOTH),0,0,null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( img, this.mimetype, baos );
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();

		this.thumbnail = Hibernate.getLobCreator(session).createBlob(imageInByte);

	}*/

	/**
	 * 
	 * @param session
	 * @throws IOException
	 * @throws SQLException
	 * generates thumbnail from the passed data. Type must be image
	 */
	public void generateThumbnail(Session session, Blob blob) throws IOException, SQLException{

		byte[] b;	

		//get the Base64 representation of the content data
		String s = new String(blob.getBytes(1, (int) blob.length()));
		//decode it as an array of bytes
		b = PasswordService.base64ToByte(s);

		ByteArrayInputStream bis = new ByteArrayInputStream(b);

		BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

		img.createGraphics().drawImage(ImageIO.read(bis).getScaledInstance(100, 100, Image.SCALE_SMOOTH),0,0,null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( img, this.mimetype, baos );
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();

		this.thumbnail = Hibernate.getLobCreator(session).createBlob(imageInByte);

	}

	/**
	 * 
	 * @param session
	 * @param blob byte[] needed only in operation to generate thumbnails in case of failure
	 * @throws IOException
	 * @throws SQLException
	 */
	public void generateThumbnail(Session session, byte[] blob, int width, int height) throws IOException, SQLException{

		ByteArrayInputStream bis = new ByteArrayInputStream(blob);

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		img.createGraphics().drawImage(ImageIO.read(bis).getScaledInstance(width, height, Image.SCALE_SMOOTH),0,0,null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( img, this.mimetype, baos );
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();

		this.thumbnail = Hibernate.getLobCreator(session).createBlob(imageInByte);

	}

	@Override
	public boolean belongs(User u, Session session) {
		//TODO check u is equal to my associated user
		//retrieve my session and then the user
		String query = "SELECT S FROM PelarsSession AS S WHERE S.id = :id";

		PelarsSession s = null;

		try {
			s = Util.doQueryUnique(session, query, "id", this.session.getId());
		} catch (Exception e) {}

		return s.belongs(u, session);
	}

	@Override
	public boolean belongsToGroup(User u, Session session) {

		String query = "SELECT S FROM PelarsSession AS S WHERE S.id = :id";

		PelarsSession s = null;

		try {
			s = Util.doQueryUnique(session, query, "id", this.session.getId());
		} catch (Exception e) {}

		return s.belongsToGroup(u, session);
	}
}
