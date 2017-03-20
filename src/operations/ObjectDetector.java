package operations;


import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Box;
import pelarsServer.Data;
import pelarsServer.MultimediaContent;
import pelarsServer.OpDetail;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * this class invokes an external executable used to track objects on the table
 *
 */

public class ObjectDetector extends Operation{

	private JSONArray input;
	public List<Box> boxes;
	public boolean outvideo;
	public String jsonoutput = "";
	public int start;
	public int filter_length;
	public long video_id;
	public String exepath= null;


	public ObjectDetector(JSONObject content) throws JSONException{
		super(content);

		JSONArray jboxes = new JSONArray();
		jboxes = content.getJSONArray("boxes");

		for(int i=0; i< jboxes.length(); i++){

			Box sq = new Box();
			JSONObject curr_square = jboxes.getJSONObject(i);

			sq.x = curr_square.getInt("x");
			sq.y = curr_square.getInt("y");
			sq.width = curr_square.getInt("width");
			sq.height = curr_square.getInt("height");
		}

		input = jboxes;

		outvideo = true;
		try{
			outvideo = content.getBoolean("video");
		}catch(JSONException e){}

		video_id = 0;
		try{
			video_id = content.getLong("input");
		}catch(JSONException e){}

		start = 0;
		try{
			start = content.getInt("start");
		}catch(JSONException e){}
		
		try{
			exepath = content.getString("path");
		}catch(JSONException e){}

		filter_length = 5;
		try{
			filter_length = content.getInt("filter_length");
		}catch(JSONException e){}
	}


	@Override
	public void storeResult() throws Exception {

		//SAVE OUTPUT
		OpDetail p = Util.doQueryUnique(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 

		//put the URL of the video as response of the operation
		p.result = jsonoutput;

		Util.update(my_session,p);	

	}

	@Override
	public List<? extends BaseData> getResult() {
		// TODO Auto-generated method stub
		return null;
	}
	//TODO: now it is possible to pass a video by its database ID. Must add feature to pass the video by URL since complete session videos are not tracked in the DB but present 
	//in the server. Moreover when a complete video is passed must solve time-stamps issue.
	//TODO: transform pixel coordinates into 2D coordinates
	@Override
	public void run(List<? extends Data> objs) throws Exception {

		String prefix = System.getProperty("user.home");
		prefix = prefix + "/tempJSON/" + cur_session;
		new File(prefix).mkdirs();
		String dst = prefix + "/input" + this.id + ".json";

		File f = new File(dst);
		f.getParentFile().mkdirs();
		PrintWriter	o = new PrintWriter(dst);
		o.println(input.toString(4));
		o.close();
		
		String exec_path = exepath;
		
		if(exec_path == null){
			exec_path = System.getProperty("exec.path");
		}
		
		String mask_path = System.getProperty("upload.location");
		mask_path = mask_path + this.cur_session + "/image/mask.png";

		String input_path = dst;

		//TODO: generate the video or pass it through the pipeline or as input as id
		String video_path = "";
		File tempstamps = null;
		MultimediaContent nvideo = new MultimediaContent();

		if(video_id == 0){	

			JSONObject sub_input = new JSONObject();
			sub_input.put("session", this.cur_session);
			sub_input.put("type", "video_snapshots");
			sub_input.put("start", this.start);

			VideoFromSnapshots sub_op = new VideoFromSnapshots(sub_input);
			sub_op.my_session = this.my_session;
			sub_op.run(null);
			this.video_id = sub_op.video.getId();
			Util.delete(my_session, sub_op.video);

			//TODO get the timestamps and write them into a temporary file
			tempstamps = new File(prefix + "/timestamps.txt");
			PrintWriter writer = new PrintWriter(tempstamps);
			writer.print(sub_op.timestamps);
			writer.close();


		}

		//default: generated video should be removed after this.run()
		video_path = System.getProperty("upload.location");
		video_path += this.cur_session + "/video/" + video_id + ".mp4";

		if(outvideo){
			//TODO: add output video path
			String output_path = System.getProperty("upload.location"); 
			output_path += this.cur_session + "/video/";

			//TODO create a metadata associated to the file
			nvideo.creator = "server";
			nvideo.type = "video";
			nvideo.mimetype = "mp4";
			nvideo.session = this.getSession();
			nvideo.view = "workspace";

			Util.save(my_session, nvideo);


			output_path += nvideo.getId() + ".h264";
			//jsonoutput = exec_path + "video_recognizer_json " + video_path + " " + mask_path + " " + input_path + " " + output_path;

			jsonoutput = Util.executeCommand(exec_path + "video_recognizer_json " + video_path + " " + tempstamps.getAbsolutePath() + " " + mask_path + " " + input_path + " " + output_path);
			//jsonoutput = exec_path + "video_recognizer_json " + video_path + " " + tempstamps.getAbsolutePath() + " " + mask_path + " " + input_path + " " + output_path;
		}
		else{
			jsonoutput = Util.executeCommand(exec_path + "video_recognizer_json " + video_path + " " + tempstamps.getAbsolutePath() + " " + mask_path + " " + input_path);
		}

		JSONArray jsa = new JSONArray(jsonoutput);

		JSONObject jobj = new JSONObject();
		jobj.put("url", System.getProperty("host.url") + "/multimedia/" + this.cur_session + "/" + nvideo.getId());
		jobj.put("boxes", jsa);
		jsonoutput = jobj.toString(4);

		f.delete();
		File fvideo = new File(video_path);
		fvideo.delete();

		if(tempstamps != null){
			tempstamps.delete();
		}

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


	@Override
	public List<? extends BaseData> extract() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
