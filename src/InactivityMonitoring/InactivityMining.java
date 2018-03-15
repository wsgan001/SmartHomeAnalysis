package InactivityMonitoring;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.Entry;
import main.Main.KasterenEntry;

public class InactivityMining {

	List<Entry> dataSet;
	LocalDateTime start;
	Duration interval;
	float[] inactivityMetric;
	
	public void loadData(List<Entry> dataSet) {
		this.dataSet = dataSet;
	}
	
	public void setTimeStep(LocalDateTime start, Duration interval, int numTimeSteps) {
		inactivityMetric = new float[numTimeSteps];
	}
	
	//should add a predicate parameter to find which part of entry to use
	public void processData() {
		final LocalDateTime[] intervalDuration = new LocalDateTime[2];
		intervalDuration[0] = start; // start of interval
		intervalDuration[1] = start.plus(interval); // end of interval
		for (int i=0; i<inactivityMetric.length; i++) {
			Stream<Entry> filteredDataStream = dataSet.stream()
			.filter(entry -> {
				return ((KasterenEntry)entry).start.isAfter(intervalDuration[0]) && ((KasterenEntry)entry).start.isBefore(intervalDuration[1]);
			});
			
			// apply the metric to relevent
			LocalDateTime[] startOfInterval = {intervalDuration[0]};
			inactivityMetric[i] = (float)filteredDataStream.mapToDouble(entry -> {
				Duration measurement = Duration.between(startOfInterval[0], ((KasterenEntry)entry).getStart());
				startOfInterval[0] = ((KasterenEntry)entry).start;
				return rampFunction(measurement, 1.0f);
			}).sum();
			inactivityMetric[i] += rampFunction(Duration.between(startOfInterval[0], intervalDuration[1]), 1.0f);
			
			intervalDuration[0] = intervalDuration[1];
			intervalDuration[1] = intervalDuration[1].plus(interval);
		}
	}
	
	static float rampFunction (long interval, long maxInterval, long rampConstant) {
		//if (interval < 0){
		//	System.out.println("negative interval");
		//	return 0;
		 //} else 
		double h = 1/maxInterval;
		if(interval < rampConstant){ 
			//return (float)(Math.pow(2, interval)/(2.0 * rampConstant * maxInterval));
			double p = 1.0 * interval / rampConstant;
			return (float)(1/2 * p * p * rampConstant * h);
		}else 
			//return (float)(interval - rampConstant/2.0)/maxInterval;
			return (float)(h * (interval - rampConstant/2.0));
	}
	
	static float rampFunction (Duration measurement, float normalizationFactor) {
		return rampFunctionUnit(measurement.toMillis() / normalizationFactor);
	}
	
	static float rampFunctionUnit(float interval) {
		if (interval < 1.0f) {
			return interval * interval / 2.0f;
		} else {
			return interval - 0.5f;
		}
	}
	
	public float[] getInactivity() {
		return inactivityMetric;
	}
	
	public static float[] getInactivity(List<Entry> dataSet, LocalDateTime start, Duration interval, int numTimeSteps, float normalizationFactor) {
		float[] inactivityMetric = new float[numTimeSteps];
		
		long maxDuration = 3600;
		long rampConstant = 300;

		final LocalDateTime[] intervalDuration = new LocalDateTime[] { start, start.plus(interval) };
		
		for (int i=0; i<inactivityMetric.length; i++) {
			List<Entry> filteredData = dataSet.stream()
			.filter(entry -> {
				return ((KasterenEntry)entry).start.isAfter(intervalDuration[0]) && ((KasterenEntry)entry).start.isBefore(intervalDuration[1]);
			}).sorted(Comparator.comparing(entry -> ((KasterenEntry)entry).start))
			.collect(Collectors.toList());
			
			
			// apply the metric to relevent points
			if (filteredData.isEmpty()) {
				inactivityMetric[i] = 1;
			} else {
				long intervalTimeTotal = 0;
				Duration measurement = Duration.between(intervalDuration[0].toLocalTime(), ((KasterenEntry)filteredData.get(0)).getStart());
				intervalTimeTotal += measurement.getSeconds();
				inactivityMetric[i] = measurement.getSeconds();
				for (int j=1; j<filteredData.size(); j++) {
					measurement = Duration.between(((KasterenEntry)filteredData.get(j-1)).getStart(), ((KasterenEntry)filteredData.get(j)).getStart());
					intervalTimeTotal += measurement.getSeconds();
					inactivityMetric[i] += rampFunction(measurement.getSeconds(), maxDuration, rampConstant);
				}
				measurement = Duration.between(((KasterenEntry)filteredData.get(filteredData.size()-1)).getStart(), intervalDuration[1]);
				intervalTimeTotal += measurement.getSeconds();
				inactivityMetric[i] += rampFunction(measurement.getSeconds(), maxDuration, rampConstant);
				
				System.out.println("No. elements: " + filteredData.size() + " and Inactivity: " + inactivityMetric[i]);
				System.out.println(Duration.between(intervalDuration[0], intervalDuration[1]) + " " + Duration.ofSeconds(intervalTimeTotal));
			}
			
			intervalDuration[0] = intervalDuration[1];
			intervalDuration[1] = intervalDuration[1].plus(interval);
		}
		
		return inactivityMetric;
	}
}
