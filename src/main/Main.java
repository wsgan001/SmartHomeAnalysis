package main;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import InactivityMonitoring.InactivityMining;
import SequenceMining.SequenceMining;


public class Main {
	
	private static final int SENSOR_ID_INDEX = 2;
	private static final String DATA_PATH = "res/kasterenSenseData.txt";
	
	public static void main(String[] args){
		System.out.println("Gather data...");
		List<Integer> signalDataSet = new ArrayList<Integer>();
		List<Entry> entryDataSet = new ArrayList<Entry>();
		
		// Populate entryDataSet from file
		try(Stream<String> lines = Files.lines(Paths.get(DATA_PATH))){
			entryDataSet = lines.filter(line -> {
				String[] split = line.split("\t");
				if (split.length > 3 && Character.isDigit(split[SENSOR_ID_INDEX].charAt(0)))
					return true;
				return false;
			}).map(line -> {
				String[] split = line.split("\t");
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
				LocalDateTime start = LocalDateTime.parse(split[0], formatter);
				LocalDateTime end = LocalDateTime.parse(split[1], formatter);
				int sensorID = Integer.parseInt(split[2]);
				int value = Integer.parseInt(split[3]);
				return new KasterenEntry(start, end, sensorID, value);
			}).collect(Collectors.toList());
			
			//entryDataSet.stream().forEach(line -> System.out.println(line));
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
		LocalDateTime startTime = LocalDateTime.of(2008, 02, 25, 0, 0);
		Duration interval = Duration.ofHours(1);
		int numTimeSteps = 672; // 24hours * 28days
		float normalizationFactor = 60 * 60 * 1000.0f;
		float[] inactivityData = InactivityMining.getInactivity(entryDataSet, startTime, interval, numTimeSteps, normalizationFactor);

		LocalDateTime localStartTime = startTime;
		for (int i=0; i < inactivityData.length; i++) {
			System.out.println(localStartTime.toLocalDate() + " from " + localStartTime.toLocalTime() + " to " + localStartTime.plus(interval).toLocalTime() + " is " + inactivityData[i]);
			localStartTime = localStartTime.plus(interval);
		}
		
		/*
		// Load the Kasteren Dataset as a list of integers
		try(Stream<String> lines = Files.lines(Paths.get(DATA_PATH))){
			signalDataSet = lines.filter(line -> {
				String[] split = line.split("\t");
				if (split.length > 3 && Character.isDigit(split[SENSOR_ID_INDEX].charAt(0)))
					return true;
				return false;
			}).map(line -> {
				return Integer.parseInt(line.split("\t")[SENSOR_ID_INDEX]);
			}).collect(Collectors.toList());
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		// Use sequence mining to find common patterns.
		HashMap<ArrayList<Integer>, Integer> patterns = SequenceMining.patternDiscovery(signalDataSet, 1);
		patterns.entrySet()
			.stream()
			.sorted((e1, e2) -> {
				return Integer.compare(e1.getKey().size(),e2.getKey().size());
			});
			//.forEach(entry -> System.out.println(entry.getKey() + " = " + entry.getValue()));
		
		
		
		// Load the Kasteren Dataset as a list of integers
		try(Stream<String> lines = Files.lines(Paths.get(DATA_PATH))){
			signalDataSet = lines.filter(line -> {
				String[] split = line.split("\t");
				if (split.length > 3 && Character.isDigit(split[SENSOR_ID_INDEX].charAt(0)))
					return true;
				return false;
			}).map(line -> {
				return Integer.parseInt(line.split("\t")[SENSOR_ID_INDEX]);
			}).collect(Collectors.toList());
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		*/
	}
	
	public static class KasterenEntry implements Entry {
		public LocalDateTime start;
		LocalDateTime end;
		int sensorID;
		int value;
		
		public KasterenEntry(LocalDateTime start, LocalDateTime end, int sensorID, int value){
			this.start = start;
			this.end = end;
			this.sensorID = sensorID;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return start + ", " + end + ", " + sensorID + ", " + value;
		}
	}
}
