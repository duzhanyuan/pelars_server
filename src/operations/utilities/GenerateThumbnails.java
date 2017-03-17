package operations.utilities;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.MultimediaContent;
import servlets.Util;

public class GenerateThumbnails extends operations.Operation{

	int width;
	int height;

	public GenerateThumbnails(JSONObject content) throws JSONException {

		super(content);
		// TODO Auto-generated constructor stub

		width = height = 100;

		try{
			width = content.getInt("width");
		}catch(JSONException e){}

		try{
			height = content.getInt("height");
		}catch(JSONException e){}


	}

	@Override
	public List<? extends Data> extract() throws Exception {

		List<MultimediaContent> all_images = Util.doQuery(my_session, "SELECT S From MultimediaContent AS S WHERE S.type = :image AND S.session = :id", "image", "image", "id", this.getSession());

		for (int i = 0; i < all_images.size(); i++){
			all_images.get(i).generateThumbnail(my_session,all_images.get(i).getData(),width,height);
		}

		Util.update(my_session, all_images.toArray(new MultimediaContent[all_images.size()])); 

		return null;
	}

	@Override
	public void storeResult() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public List<? extends BaseData> getResult() {
		// TODO Auto-generated method stub
		return null;
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
	public void run(List<? extends Data> objs) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
