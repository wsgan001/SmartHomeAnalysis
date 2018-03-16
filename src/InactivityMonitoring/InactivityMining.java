package InactivityMonitoring;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import main.Entry;
import main.Main.KasterenEntry;

public class InactivityMining {

	/**
	 * This function processes a dataset and returns the level of inactivity for a
	 * set of predefined intervals. The resulting time domain is defined using start
	 * as the start time, interval as the time step and numTimeSteps as the number
	 * of time steps to be sorted into. The value of each sub-interval is
	 * represented by a ramp function whose turning point is represented by
	 * recoveryTimeSec.
	 * 
	 * @param dataSet
	 * @param start
	 * @param interval
	 * @param numTimeSteps
	 * @param recoveryTimeSec
	 * @return an array of values in domain [0 1] representing level of inactivity
	 * @throws Exception
	 */
	public static float[] getInactivity(List<Entry> dataSet, LocalDateTime start, Duration interval, int numTimeSteps,
			int recoveryTimeSec) throws Exception {

		float[] inactivityMetric = new float[numTimeSteps];
		int maxDuration = (int) interval.getSeconds();
		int rampConstant = recoveryTimeSec;

		// Interval array set to final for use with Stream filter
		final LocalDateTime[] intervalDuration = new LocalDateTime[] { start, start.plus(interval) };

		for (int i = 0; i < inactivityMetric.length; i++) {
			// Use Java Stream API to extract relevent entries
			List<Entry> filteredData = dataSet.stream().filter(entry -> {
				return ((KasterenEntry) entry).getTime().isAfter(intervalDuration[0])
						&& ((KasterenEntry) entry).getTime().isBefore(intervalDuration[1]);
			}).sorted(Comparator.comparing(entry -> ((KasterenEntry) entry).getTime())).collect(Collectors.toList());

			// Sum individual intervals to form total value for time period
			if (filteredData.isEmpty()) {
				inactivityMetric[i] = 1;
			} else {
				Duration measurement = Duration.between(intervalDuration[0].toLocalTime(),
						((KasterenEntry) filteredData.get(0)).getTime());
				inactivityMetric[i] = measurement.getSeconds() / (float) maxDuration;
				for (int j = 1; j < filteredData.size(); j++) {
					measurement = Duration.between(((KasterenEntry) filteredData.get(j - 1)).getTime(),
							((KasterenEntry) filteredData.get(j)).getTime());
					inactivityMetric[i] += valueFunction((int) measurement.getSeconds(), maxDuration, rampConstant);
				}
				measurement = Duration.between(((KasterenEntry) filteredData.get(filteredData.size() - 1)).getTime(),
						intervalDuration[1]);
				inactivityMetric[i] += valueFunction((int) measurement.getSeconds(), maxDuration, rampConstant);
			}

			intervalDuration[0] = intervalDuration[1];
			intervalDuration[1] = intervalDuration[1].plus(interval);
		}

		return inactivityMetric;
	}

	/**
	 * Calculates the value of a sub-interval using a ramp function whereby
	 * rampConstant is the point where the function turns from a sloped line to a
	 * constant. Function is defined as; if interval is less than rampConstant
	 * interval^2 / (2 * rampConstant * maxInterval) otherwise (interval -
	 * rampConstant/2) / maxInterval
	 * 
	 * @param interval
	 * @param maxInterval
	 * @param rampConstant
	 * @return the value of the sub-interval relative to the interval
	 * @throws Exception
	 *             if interval is invalid
	 */
	private static float valueFunction(int interval, int maxInterval, int rampConstant) throws Exception {
		if (interval < 0)
			throw new Exception("Value function has an invalid input");

		if (interval < rampConstant)
			return (float) (Math.pow(interval, 2) / (2.0 * rampConstant * maxInterval));
		else
			return (float) (interval - rampConstant / 2.0) / maxInterval;
	}
}
