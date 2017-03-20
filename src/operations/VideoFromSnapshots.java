package operations;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.MultimediaContent;
import pelarsServer.OpDetail;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * this operation calls uses ffmpeg in order to create a video from the snapshots captured during a session
 * paramters are input and output frame rates
 */

public class VideoFromSnapshots extends Operation{

	public Double framerate;
	public Double fps;
	public String view;
	public MultimediaContent video;
	//optional parameter indicating the first frame of the video
	public int start;
	public String timestamps = "";

	String message = "";


	public VideoFromSnapshots(JSONObject content) throws JSONException{
		super(content);

		try{
			framerate = content.getDouble("input_framerate");
		}catch(JSONException e){}

		try{
			fps = content.getDouble("output_framerate");
		}catch(JSONException e){}

		try{
			view = content.getString("view");
		}catch(JSONException e){view = "workspace";}

		start = 1;
		try{
			start = content.getInt("start");
		}catch(JSONException e){}
	}

	@Override
	public List<? extends Data> extract() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeResult() throws Exception {
		OpDetail p = Util.doQueryUnique(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 

		//put the URL of the video as response of the operation
		p.result = "http://pelars.sssup.it/pelars/multimedia/" + cur_session + "/" + video.getId();// + "  " + this.message;
		Util.update(my_session,p); 
	}

	@Override
	public List<? extends BaseData> getResult() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void run(List<? extends Data> objs) throws Exception {

		String query = "SELECT M FROM MultimediaContent AS M WHERE M.session = :ses AND M.type = 'image' AND M.view = :mview";	
		List<MultimediaContent> all_snapshots = servlets.Util.doQuery(my_session,query, "ses", cur_session, "mview", view);

		if(all_snapshots == null){
			throw new Exception("No images, couldn't generate video");
		}

		//take snapshots from start parameter
		all_snapshots = all_snapshots.subList(start, all_snapshots.size()-1);

		if(all_snapshots.size() <= 1){
			throw new Exception("Not enough images to produce the video");
		}

		int count = 0;
		String prefix = System.getProperty("user.home");
		prefix = prefix + "/tempImage/" + cur_session;
		new File(prefix).mkdirs();

		//Handle the case in which video directory is not present
		File thedir = new File(System.getProperty("upload.location") + this.cur_session + "/video");
		if(!thedir.exists()){
			thedir.mkdir();
		}

		double max_digits = Math.floor(Math.log10((double) all_snapshots.size())) + 1;
		double current_digits = 0.0;

		for(MultimediaContent m : all_snapshots){

			timestamps = timestamps + (long)m.time + '\n';

			count ++;
			current_digits = Math.ceil(Math.log10((double)count+1));
			String zeros = "";
			for(int d=0; d<max_digits-current_digits;++d){
				zeros = zeros + "0";
			}

			String src = System.getProperty("upload.location") + m.getSession().getId() + "/" + m.getType() + "/" + m.getId() + "." + m.getMimetype();
			String dst = prefix + "/" + zeros + count + ".jpeg";
			Files.copy(Paths.get(src), Paths.get(dst), StandardCopyOption.REPLACE_EXISTING);

		}

		video = new MultimediaContent();

		video.setType("video");
		video.setMimetype("mp4");
		video.setTime(new Date().getTime());
		video.setView(this.view);
		video.setCreator("server");
		video.setSession(this.getSession());

		Util.save(my_session, video);

		//TODO: we can pass to the operation also some ffmpeg parameters, e.g: framerate

		String fr = (this.framerate != null) ? "-framerate " + String.valueOf(this.framerate) : "";
		String fp = (this.fps != null) ? "-r " + String.valueOf(this.fps) : "";

		String command = "ffmpeg " + fr + " -i "+ prefix + "/%0" + (int)max_digits+"d.jpeg -c:v libx264 "+ fp + " " + System.getProperty("upload.location")  + "/" + cur_session +"/"+video.getType() 
				+ "/" + video.getId() + ".mp4";

		this.message =  servlets.Util.executeCommand(command);

		servlets.Util.executeCommand("rm -r " + prefix);
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
