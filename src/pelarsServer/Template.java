package pelarsServer;

import java.sql.Blob;
import java.sql.SQLException;

import javax.persistence.Lob;

import org.json.JSONException;
import org.json.JSONObject;

public class Template {
	
	public String description;
	public String category;
	public long id;
	@Lob
	public Blob data;
	public String name;
	public String namespace;
	
	public long getId(){
		return id;
	}
	
	public void setId(long id){
		this.id = id;
	}
	public Blob getData(){
		return data;
	}
	
	public void setData(Blob data){
		this.data = data;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public String getCategory(){
		return category;
	}
	
	public void setCategory(String category){
		this.category = category;
	}
	
	public String getNamespace(){
		return namespace;
	}
	
	public void setNamespace(String namespace){
		this.namespace = namespace;
	}
	
	public JSONObject toJson() throws SQLException{
		JSONObject jo = new JSONObject();
		try{
			jo.put("name", name);
			jo.put("description", description);
			jo.put("identifier", id);
			jo.put("category", category);
			jo.put("namespace", namespace);
			jo.put("data", new String(data.getBytes(1l, (int) data.length())));			
		}catch (JSONException e){
			e.printStackTrace();
		}
		return jo;
	}
}
