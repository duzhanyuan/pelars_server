package operations.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.BaseData;
import pelarsServer.Data;
import pelarsServer.Face;
import pelarsServer.OpDetail;
import servlets.Util;

public class RestoreFaces extends operations.OperationSingleValue{

	public boolean all_session;

	public RestoreFaces(JSONObject content) throws JSONException {
		super(content);

		try{
			all_session = content.getBoolean("all");
		}catch(JSONException e){all_session = false;}
	}

	@Override
	public void run(List<? extends Data> objs) throws Exception {


		int max = 0;
		//SELECT ALL Images from Database
		String query = "SELECT M FROM Face AS M WHERE M.session.id = :ses";	
		List<Face> all_faces = servlets.Util.doQuery(my_session,query,"ses",cur_session);	
		ArrayList<Integer> nums = new ArrayList<Integer>();

		for(Face f : all_faces){
			nums.add(f.getNum());
		}

		max = Collections.max(nums);

		//face to be copied
		Face[] to_copy = new Face[max+1];

		//Skip until we get the first face with num=0 but take into account the ones seen
		int init = 0;
		while(all_faces.get(init).getNum() != 0){
			to_copy[all_faces.get(init).getNum()] = all_faces.get(init);
			init ++;
		}


		for(int k = init; k < all_faces.size(); k++){

			Face cur = all_faces.get(k);

			to_copy[cur.getNum()] = cur;

			ArrayList<Face> savings = new ArrayList<Face>();

			for(int j=0; j<cur.getNum(); j++){

				//copy and save if they are not null
				if(to_copy[j] != null){

					Face f = new Face(to_copy[j]);
					f.time = cur.time;
					savings.add(f);
				}

				Util.save(my_session, savings.toArray(new Face[savings.size()]));

			}
		}

		//TODO another for in order to get rid of faces with same position
		query = "SELECT M FROM Face AS M WHERE M.session.id = :ses order by M.time, M.num";	
		List<Face> new_faces = Util.doQuery(my_session, query, "ses", cur_session);

		for(int i=0; i<new_faces.size(); i++){

			LinkedList<Face> cur_faces = new LinkedList<Face>();
			cur_faces.add(new_faces.get(i));

			int count = i;
			while(count+1 < new_faces.size() && new_faces.get(count).time == new_faces.get(count+1).time){
				cur_faces.add(new_faces.get(count+1));
				count ++;
			}

			i = count;

			Face last = cur_faces.pollLast();

			for(Face f : cur_faces){

				if(f.time != last.time){
					throw new Exception("different timestamps");
				}

				if (last.getDistance2D(f) < 0.12){
					Util.delete(my_session, last);
					break;
				}
			}

		}

		//OPTIONAL check correctness
		query = "SELECT M FROM Face AS M WHERE M.session.id = :ses order by M.x,M.time";
		List<Face> final_check = Util.doQuery(my_session, query, "ses", cur_session);
		for(int z=0; z<final_check.size()-1; z++){ 	
			if(final_check.get(z).time == final_check.get(z+1).time && final_check.get(z).getDistance2D(final_check.get(z+1)) < 0.12){
				throw new Exception("algorithm wrong " + final_check.get(z).toString() + "\n" + final_check.get(z+1).toString());
			}
		}

		OpDetail p = Util.doQueryUnique(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 

		//put the URL of the video as response of the operation
		p.result = new String("done");

		Util.update(my_session,p);	 
	}

	@Override
	public List<? extends BaseData> extract() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeResult() throws Exception {

	}

}
