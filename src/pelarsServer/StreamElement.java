package pelarsServer;

import org.json.JSONObject;

public class StreamElement{
	
	public long id;
	public long task;
	public BaseData data;

	public StreamElement(){}

	public StreamElement(BaseData f,long id){
		data = f;
		task = id;
	}
	
	public long getId(){
		return id;
	}

	public void setId(long i){
		id = i;
	}

	public long getTask(){
		return task;
	}

	public void setTask(long t){
		task = t;
	}

	public void setData(BaseData f){
		data = f;
	}

	public BaseData getData(){
		return data;
	}

	public JSONObject toJson(){
		return this.getData().toJson();
	}
}
