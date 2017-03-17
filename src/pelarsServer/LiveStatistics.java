package pelarsServer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Emanuele Ruffaldi, Lorenzo Landolfi
 * computes statistical values from a stream of elements
 */
public class LiveStatistics{

	public long count;
	double sum;
	public double avg;
	public double variance;
	public double stddev;
	public double min;
	public double max;
	double m2; 

	String unit;

	public LiveStatistics(){
		count = 0;
		avg = variance = stddev = min = max = m2 = 0;
	}

	public LiveStatistics(String unit){
		this();
		this.unit = unit;
	}

	public void add (double v){

		if(count == 0)
		{
			count = 1;
			min = v;
			max = v;
			sum = v;
			avg = v;
			stddev = 0;
			m2 = 0;
		}
		else
		{
			count++;
			if(v < min)
				min = v;
			if(!(v < max)) 
				max = v;

			double dvold = v-avg;
			avg += dvold/count;
			m2  += (v-avg)*dvold;
			sum += v;

			stddev = m2/(count-1);
		}	  
	}

	public JSONObject toJson(){
		JSONObject jo = new JSONObject();
		try{
			jo.put("elements" , count);
			jo.put("mean", avg);
			jo.put("variance", variance);
			jo.put("stddev", stddev);
			jo.put("variance", stddev*stddev);
			jo.put("min", min);
			jo.put("max", max);
			jo.putOpt("unit", unit);
		}catch (JSONException e){
		}
		return jo;
	}

	public String toString(){
		return this.toJson().toString();
	}

	public long getCount(){
		return count;
	}

	public void setCount(long c){
		count = c;
	}

	public double getAvg(){
		return avg;
	}

	public void setAvg(double d){
		avg = d;
	}

	public double getVariance(){
		return variance;
	}

	public void setVariance(double v){
		variance = v;
	}

	public double getMin(){
		return min;
	}

	public void setMin(long m){
		min = m;
	}

	public double getMax(){
		return max;
	}

	public void setMax(double m){
		max = m;
	}	
}
