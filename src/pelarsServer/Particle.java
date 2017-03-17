package pelarsServer;

import java.sql.Blob;

import org.json.JSONObject;

public class Particle extends BaseData{
	
	public Blob data;
	public String name;
	
	public Blob getData(){
		return data;
	}
	
	public String getName(){
		return name;
	}
	
	public void setData(Blob b){
		data = b;
	}
	
	public void setName(String s){
		name = s;
	}
	
	public boolean equals(Particle p){
		return (super.equals(p) && p.data.equals(data) && p.name.equals(name));
	}
	
	public JSONObject toJson(){
 		JSONObject jo = super.toJson();
		try{
			jo.put("type", "particle");
			jo.put("data", new String(data.getBytes(1l, (int) data.length())));	
			jo.put("name", name);
		}catch (Exception e){
			e.printStackTrace();
		}
		return jo;
 	}
}
