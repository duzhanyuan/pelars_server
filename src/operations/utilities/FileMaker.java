package operations.utilities;


import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Data;

/*
import pelarsServer.MultimediaContent;
import pelarsServer.OpDetail;
import pelarsServer.PelarsSession;
import servlets.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.util.Date;

import authorization.PasswordService;

import org.hibernate.Hibernate;
*/

/**
 * 
 * @author Lorenzo Landolfi
 * Utility operation to transfer database Data into files
 *
 */
public class FileMaker extends operations.Operation{

	boolean all;

	public FileMaker(JSONObject content) throws JSONException{

		super(content);	
		all = false;

		try{
			all = content.getBoolean("all");
		}catch(JSONException e){}
	}

	@Override
	public List<? extends BaseData> extract() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeResult() throws Exception {

	}

	@Override
	public List<? extends BaseData> getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run(List<? extends Data> objs) throws Exception {

	/*	List<PelarsSession> all_sessions;

		if(all){
			all_sessions = Util.doQuery(my_session, "SELECT S FROM PelarsSession AS S");
		}
		else{
			all_sessions = Util.doQuery(my_session, "SELECT S FROM PelarsSession AS S WHERE S.id = :id",this.cur_session);
		}
		
		String prefix = System.getProperty("upload.location");
		
		List<MultimediaContent> all_images;
		
		for (int i=0; i<all_sessions.size(); i++){

			String query = "SELECT M FROM MultimediaContent AS M WHERE M.session = :ses";	
			all_images = servlets.Util.doQuery(my_session,query,"ses",all_sessions.get(i).getId());
			
				for(MultimediaContent m : all_images){
					
					String cur_prefix = prefix + m.getSession() + "/" + m.getType() + "/";
					new File(cur_prefix).mkdirs();

					String filename = m.getId() + "." + m.mimetype;

					Blob blob = m.getData();
					//get the Base64 representation of the content data
					String s = new String(blob.getBytes(1, (int) blob.length()));
					//decode it as an array of bytes
					byte[] b = PasswordService.base64ToByte(s);

					OutputStream out = new FileOutputStream(cur_prefix + filename);
					out.write(b, 0, b.length);
					out.flush();
					out.close();
				} 
		}

		OpDetail p = Util.doQueryUnique(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 

		//put the URL of the video as response of the operation
		p.result = "done";

		Util.update(my_session,p);*/

	}

	@Override
	public boolean isStream() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getStringResult() {
		// TODO Auto-generated method stub
		return null;
	}

}
