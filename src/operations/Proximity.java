package operations;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.LiveStatistics;
import pelarsServer.OpDetail;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * this class estimates statistical values regarding proximity of hands or faces during the session or phase
 *
 */
public class Proximity extends OperationSingleValue{

	String mtable;
	boolean avg;
	LiveStatistics ls;

	public Proximity(JSONObject content) throws JSONException{
		super(content);

		avg = false;
		mtable = "Face";

		try{
			mtable = content.getString("table");
		}catch(JSONException e){}

		try{
			avg = content.getBoolean("statistics");
		}catch(JSONException e){}

		if(avg){
			ls = new LiveStatistics();
		}

	}

	@Override
	public List<? extends BaseData> extract() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeResult() throws Exception {
		// TODO Auto-generated method stub

	}

	
	public void run(List<? extends Data> objs) throws Exception {		

		JSONArray jresult = new JSONArray();

		//TODO: get faces with same time and different num, put them in a list or array
		String query = "SELECT M FROM " + mtable + "  AS M WHERE M.session.id = :ses order by M.time, M.num";	
		List<BaseData> new_faces = Util.doQuery(my_session, query, "ses", cur_session);

		if(new_faces != null && new_faces.size() == 0){
			throw new Exception ("Not enough data to estimate");
		}

		for(int i=0; i<new_faces.size(); i++){

			LinkedList<BaseData> cur_faces = new LinkedList<BaseData>();
			cur_faces.add(new_faces.get(i));

			int count = i;
			while(count+1 < new_faces.size() && new_faces.get(count).time == new_faces.get(count+1).time){
				cur_faces.add(new_faces.get(count+1));
				count ++;
			}

			i = count;

			//cur_faces have the block of faces with same timestamp

			for(int j=0; j<cur_faces.size()-1; j++){
				BaseData f = cur_faces.get(j);

				for(int k=j+1; k<cur_faces.size();k++){

					BaseData f2 = cur_faces.get(k);
					double angle = f.getAngle(f2);
					double distance = f.getDistance(f2);

					JSONObject jo = new JSONObject();
					jo.put("num1", f.getNum());
					jo.put("num2", f2.getNum());
					jo.put("angle", angle);
					jo.put("distance", distance);
					jo.put("time", f.getTime());

					jresult.put(jo);

					if(ls != null){
						ls.add(distance);
					}

				}	
			}
		}

		if(ls != null && ls.getCount() == 0){
			throw new Exception("not enough data to estimate");
		}

		OpDetail p = Util.doQueryUnique(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 

		if(ls == null){
			JSONObject unit = new JSONObject();
			unit.put("unit", "meters");
			jresult.put(unit);
			p.result = jresult.toString(4);
		}
		else{
			p.result = ls.toJson().put("unit", "meters").toString(4);
		}

		Util.update(my_session,p);	 

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
