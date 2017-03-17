package pelarsServer;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Lorenzo Landolfi, Giacomo dabisias
 * class used to incapsulate estrinsic, intrinsic and distorsion parameters of a camera
 *
 */
public class Calibration {

	public long id;
	public String type;
	public PelarsSession session;
	public List<Double> parameters;
	public List<Double> intrinsic_parameters;
	public List<Double> distorsion_parameters;

	public Calibration(PelarsSession s, String type){
		this.type = type;
		session = s;
	}

	public Calibration(Calibration z){
		this.type = z.type;
		this.session = z.session;
		parameters = new ArrayList<Double>(z.parameters);
		intrinsic_parameters = new ArrayList<Double>(z.intrinsic_parameters);
		distorsion_parameters = new ArrayList<Double>(z.distorsion_parameters);
	}
	
	public Calibration(JSONObject obj, PelarsSession cur_session) throws JSONException{
	
		type = obj.getString("camera");
		session = cur_session;

		JSONArray array = obj.getJSONArray("parameters");
		parameters = new ArrayList<Double>();
		intrinsic_parameters = new ArrayList<Double>();
		distorsion_parameters = new ArrayList<Double>();
		
		for (int i = 0; i < array.length(); i++){
			parameters.add(array.getDouble(i));
		}
		
		array = obj.getJSONArray("intrinsics");
		
		for(int i = 0; i < array.length(); i++){
			intrinsic_parameters.add(array.getDouble(i));
		}
		
		array = obj.getJSONArray("dist");
		
		for(int i = 0; i < array.length(); i++){
			distorsion_parameters.add(array.getDouble(i));
		}
	}

	public Calibration(){
		parameters = new ArrayList<Double>();
		intrinsic_parameters = new ArrayList<Double>();
		distorsion_parameters = new ArrayList<Double>();
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public PelarsSession getSession() {
		return session;
	}
	public void setSession(PelarsSession session) {
		this.session = session;
	}
	public List<Double> getParameters() {
		return parameters;
	}
	public void setParameters(List<Double> parameters) {
		this.parameters = parameters;
	}

	public List<Double> getIntrinsic_parameters() {
		return intrinsic_parameters;
	}

	public void setIntrinsic_parameters(List<Double> intrinsic_parameters) {
		this.intrinsic_parameters = intrinsic_parameters;
	}

	public List<Double> getDistorsion_parameters() {
		return distorsion_parameters;
	}

	public void setDistorsion_parameters(List<Double> distorsion_parameters) {
		this.distorsion_parameters = distorsion_parameters;
	}

	public boolean belongs(User u, Session session) {

		return this.getSession().getUser().equals(u);
	}

	public boolean belongsToGroup(User u, Session session){
		return this.getSession().getUser().getNamespace().equals(u.namespace);
	}

	public double[][] getCalibrationMatrix(){

		double[][] calib_matrix = new double[4][4];

		for(int i=0; i < 3; ++i){
			for(int j=0; j < 4; ++j){
				calib_matrix[i][j] = parameters.get((4*i) + j);
			}
		}

		for(int j = 0; j < 3; ++j){
			calib_matrix[3][j] = 0;
		}

		calib_matrix[3][3] = 1;

		return calib_matrix;
	}
	
	public List<Double> getRotation(){
		
		List<Double> rotation = new ArrayList<Double>(9);
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				rotation.add(parameters.get((4*i) + j));
			}
		}
		return rotation;
	}
	
	public List<Double> getTranslation(){
		
		List<Double> translation = new ArrayList<Double>(3);
		for(int i=0; i<3; i++){
			for(int j=3; j<4; j++){
				translation.add(parameters.get((4*i)+j));
			}
		}
		return translation;
	}

	public JSONObject toJson(){

		JSONObject json = new JSONObject();
		//	JSONArray jparam = new JSONArray();

		//	jparam.put(parameters);

		try {
			json.put("type", type);
			json.put("session", this.getSession().getId());
			//split parameters in rotation and translation
			json.put("rotation", this.getRotation());
			json.put("translation", this.getTranslation());
			//json.put("parameters", this.getParameters());
			json.put("intrinsic_parameters", this.getIntrinsic_parameters());
			json.put("distorsion_parameters", this.getDistorsion_parameters());
		} catch (JSONException e) {}

		return json;
	}

}
