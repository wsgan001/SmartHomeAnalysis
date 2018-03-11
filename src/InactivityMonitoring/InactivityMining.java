package InactivityMonitoring;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
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
				Duration measurement = Duration.between(startOfInterval[0], ((KasterenEntry)entry).start);
				startOfInterval[0] = ((KasterenEntry)entry).start;
				return rampFunction(measurement, 1.0f);
			}).sum();
			inactivityMetric[i] += rampFunction(Duration.between(startOfInterval[0], intervalDuration[1]), 1.0f);
			
			intervalDuration[0] = intervalDuration[1];
			intervalDuration[1] = intervalDuration[1].plus(interval);
		}
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
		

		final LocalDateTime[] intervalDuration = new LocalDateTime[2];
		intervalDuration[0] = start; // start of interval
		intervalDuration[1] = start.plus(interval); // end of interval
		for (int i=0; i<inactivityMetric.length; i++) {
			List<Entry> filteredData = dataSet.stream()
			.filter(entry -> {
				return ((KasterenEntry)entry).start.isAfter(intervalDuration[0]) && ((KasterenEntry)entry).start.isBefore(intervalDuration[1]);
			}).collect(Collectors.toList());
			
			// apply the metric to relevent points
			LocalDateTime startOfInterval = intervalDuration[0];
			if (filteredData.isEmpty()) {
				inactivityMetric[i] = interval.toMillis() / interval.toMillis();
			} else {
				inactivityMetric[i] = Duration.between(startOfInterval, ((KasterenEntry)filteredData.get(0)).start).toMillis() / normalizationFactor;
				for (int j=1; j< filteredData.size(); j++) {
					Duration measurement = Duration.between(((KasterenEntry)filteredData.get(j-1)).start, ((KasterenEntry)filteredData.get(j)).start);
					inactivityMetric[i] += rampFunction(measurement, normalizationFactor);
				}
				inactivityMetric[i] += rampFunction(Duration.between(startOfInterval, intervalDuration[1]), normalizationFactor);
			}
			
			intervalDuration[0] = intervalDuration[1];
			intervalDuration[1] = intervalDuration[1].plus(interval);
		}
		
		return inactivityMetric;
	}
}
