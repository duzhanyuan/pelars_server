package operations;

import java.lang.reflect.Field;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pelarsServer.Data;

/**
Outlines a statistical operation performed on a column of a table 
 */
public abstract class StatisticOperation extends OperationSingleValue{

	String field;

	public StatisticOperation(JSONObject content) throws JSONException{
		super(content);

		field = content.getString("field");
	}

	@SuppressWarnings("rawtypes")
	public Statistics fillData(List<? extends Data> objs) throws Exception{

		double[] data = new double[objs.size()];
		Field f = null;

		//ignore the fact that the list is empty
		if(objs.size() == 0){
			return new Statistics(data);
		}
		//find the field identified by the "field" string of this instance
		Class tmpclass = objs.get(0).getClass();
		boolean found = false;

		//iterates over the BaseClass hierarchy until the field is found 
		while(tmpclass != null && !found){
			try{
				f = tmpclass.getDeclaredField(field);

				if(f != null){
					found = true;
				}
			}catch(Exception e){
				//getSuperclass() returns null if called on an instance of Object class
				tmpclass = tmpclass.getSuperclass();
			}
		}

		if(f == null){
			throw new Exception("no such field: " + field);
		}

		for(int i = 0; i < objs.size(); i++){
			try {
				data[i] =  (Float)f.get(objs.get(i));
			}
			catch(Exception e1){
				data[i] =  (Double)f.get(objs.get(i));
			}
		}
		return new Statistics(data);
	}
}