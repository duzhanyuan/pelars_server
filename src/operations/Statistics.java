package operations;

import java.util.Arrays;

/**
Tool to perform various statistical operations
*/
public class Statistics 
{
	double[] data;
	int size;   

	public Statistics(double[] data){
		this.data = data;
		size = data.length;
	}   

	double getMean(){
		double sum = 0.0;
		for(double a : data)
			sum += a;
		return returnHandler(sum);
	}

	double getVariance(){
		double mean = getMean();
		double temp = 0;
		for(double a : data)
			temp += (mean-a)*(mean-a);
		return returnHandler(temp);
	}

	double getStdDev(){
		return Math.sqrt(getVariance());
	}

	public double median() {
		Arrays.sort(data);
		
		if(data.length == 0){
			return 0.0;
		}

		if(data.length % 2 == 0){
			return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
		} 
		else
		{
			return data[data.length / 2];
		}
	}

	/**
	Avoids run-time exceptions 
	*/
	public double returnHandler(double d){
		if(size == 0){
			return 0.0;
		}
		else {
			return d/size;
		}
	}
}
