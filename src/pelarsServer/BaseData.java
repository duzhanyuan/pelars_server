package pelarsServer;

import java.util.HashMap;

/**
 * 
 * @author Lorenzo Landolfi, Giacomo Dabisias
 * abstract class representing non multimedia data
 *	
 */
public abstract class BaseData extends Data {

	public static HashMap<String,Integer> classtoid = new HashMap<String,Integer>();

	static{
		classtoid.put("Audio", 1);
		classtoid.put("Face", 2);
		classtoid.put("Hand", 3);
		classtoid.put("Ide", 4);
	}

	public long getSession_time() {

		//retrieve starting time of the associated session 
		return (long)this.time - Long.parseLong(this.getSession().getStart());
	}

	public Object clone(){  
		try{  
			return super.clone();  
		}catch(Exception e){ 
			return null; 
		}
	}

	public void setEpochTime() {

		//considering that the session start time is in epoch

		if (this.time < 1400000000){
			this.time = Long.parseLong(this.getSession().getStart()) + this.time;
		}

		if(this.getSession().getId() < 476){
			Double second_time = this.time - Long.parseLong(this.getSession().getStart());
			this.time = (second_time * 1000) + Long.parseLong(this.getSession().getStart());
		}

	}


	public BaseData(BaseData other){
		this.session = other.session;
		this.time = other.time;
		this.id = other.id;
	}

	public BaseData(){}

	public boolean equals(BaseData b){
		return time == b.time;
	}

	public boolean presence(){
		return true;
	}

	public double getDistance(BaseData other){
		return 0.0;
	}

	public double getAngle(BaseData other){
		return 0.0;
	}

	public int getNum(){
		return 0;
	}

}
