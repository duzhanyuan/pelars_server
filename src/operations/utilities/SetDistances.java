package operations.utilities;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.Face;
import servlets.Util;

public class SetDistances extends operations.Operation {

	public SetDistances(JSONObject content) throws JSONException {
		super(content);
	}

	@Override
	public List<? extends Data> extract() throws Exception {
		// TODO Auto-generated method stub
		
		
		List<Face> faces = Util.doQuery(my_session, "SELECT F From Face AS F");
		
		for (int i = 0; i < faces.size(); i++){
			faces.get(i).setDistance(faces.get(i).getDistanceFromC920() / 1000.0);
		}
		
		Util.update(my_session, faces.toArray(new Face[faces.size()]));
		
		return faces;
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
