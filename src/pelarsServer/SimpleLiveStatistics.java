package pelarsServer;

import org.json.JSONObject;

public class SimpleLiveStatistics extends LiveStatistics{

	public SimpleLiveStatistics(){
		super();
	}

	public SimpleLiveStatistics(String unit){
		super(unit);
	}

	public JSONObject toJson(){

		JSONObject js = super.toJson();
		js.remove("variance");
		return js;

	}
}
