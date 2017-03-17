package operations;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;
import pelarsServer.MultimediaContent;
import pelarsServer.OpDetail;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 * this operation gives statistics about submitted multimedia content, see doc for more info
 *
 */

public class MultimediaData extends OperationSingleValue{

	List<MultimediaContent> m_mul;
	JSONArray m_results;

	public MultimediaData(JSONObject content) throws JSONException {
		super(content);

		table = "MultimediaContent";
		m_results = new JSONArray();
	}

	public void run (List<? extends Data> objs) throws Exception{
		int videos = 0;
		int images = 0;
		int texts = 0;
		int total_words = 0;

		for (int i=0; i < m_mul.size(); i++){

			MultimediaContent mcur = m_mul.get(i);

			if (m_mul.get(i).type.equals("text")){

				texts ++;
				String sub_text = m_mul.get(i).getDataString().trim();

				result = (double)(sub_text.isEmpty() ? 0 : sub_text.split("\\s+").length);
				total_words += result;

				JSONObject obj = new JSONObject();
				obj.put("id", m_mul.get(i).id);
				obj.put("words", result);

				m_results.put(obj);
			}

			if (m_mul.get(i).type.equals("video")){
				videos ++;
			}
			if(mcur.view != null)
				if (m_mul.get(i).type.equals("image") && mcur.view.equals("mobile")){
					images ++;
				}

		}

		JSONObject obj = new JSONObject();
		obj.put("total_words", total_words);
		m_results.put(obj);

		obj = new JSONObject();
		obj.put("videos", videos);
		m_results.put(obj);

		obj = new JSONObject();
		obj.put("images", images);
		m_results.put(obj);

		obj = new JSONObject();
		obj.put("total_posts", videos + images + texts);
		m_results.put(obj);


	}

	public void storeResult() throws Exception {

		List<OpDetail> results = Util.doQuery(my_session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", id); 
		OpDetail p = results.get(0);
		//result directly stored in the OpDetail object
		p.result = m_results.toString(1);
		Util.update(my_session,p);
	}

}
