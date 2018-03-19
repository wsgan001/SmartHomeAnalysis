package SequenceMining;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import main.Entry;
import main.Main.SequenceEntry;


public class SequenceMining {
	
	
	public static ArrayList<ArrayList<Integer>> patternHierarchy(HashMap<ArrayList<Integer>, Integer> discoveredPatterns) {
		ArrayList<ArrayList<Integer>> structuredPatterns = new ArrayList<ArrayList<Integer>>();
		
		
		
		return structuredPatterns;
	}
	
	
	public static HashMap<ArrayList<Integer>, Integer> patternDiscovery(List<Integer> dataSet, int minimumSupport){
		HashMap<ArrayList<Integer>, Integer> discoveredPatterns = init(dataSet);
		ArrayList<ArrayList<Integer>> candidatePatterns = candGen(discoveredPatterns);
		
		while(!candidatePatterns.isEmpty()){
			
			for (ArrayList<Integer> pattern : candidatePatterns) {
				int support = evaluateSupport(pattern, dataSet);
				if (support >= minimumSupport) {
					discoveredPatterns.put(pattern, support);
					Iterator<ArrayList<Integer>> iterator = discoveredPatterns.keySet().iterator();
					while(iterator.hasNext()){
						ArrayList<Integer> subPattern = iterator.next();
						if (pattern.size()-1 == subPattern.size() && !(Collections.indexOfSubList(pattern, subPattern) < 0)){
							iterator.remove();
						}
					}
				}
			}
			
			candidatePatterns = candGen(discoveredPatterns);
		}
		
		return discoveredPatterns;
	}
	
	public static ArrayList<ArrayList<Integer>> candGen(HashMap<ArrayList<Integer>, Integer> discoveredPatterns){
		ArrayList<ArrayList<Integer>> candidatePatterns = new ArrayList<ArrayList<Integer>>();
		
		for (ArrayList<Integer> fromPattern : discoveredPatterns.keySet()) {
			for (ArrayList<Integer> toPattern : discoveredPatterns.keySet()) {
				if (fromPattern.size() != toPattern.size() || fromPattern.equals(toPattern)) {
					continue; // skip if patterns are different lengths
				}
				boolean isNewPattern = true;
				for (int i=1; i < fromPattern.size(); i++) {
					if (fromPattern.get(i) != toPattern.get(i-1)){
						isNewPattern = false;
						break;
					}
				}
				if (isNewPattern){
					ArrayList<Integer> newPattern = new ArrayList<Integer>();
					newPattern.add(fromPattern.get(0));
					newPattern.addAll(toPattern);
					if (!candidatePatterns.contains(newPattern)) {
						candidatePatterns.add(newPattern);
					}
				}
			}
		}
		
		return candidatePatterns;
	}
	
	public static HashMap<ArrayList<Integer>, Integer> init(List<Integer> dataSet){
		HashMap<ArrayList<Integer>, Integer> patterns = new HashMap<ArrayList<Integer>, Integer>();
		
		int oldSensorID = -1;
		for (int element : dataSet) {
			if (oldSensorID >= 0) {
				ArrayList<Integer> pattern = new ArrayList<Integer>();
				pattern.add(oldSensorID); 
				pattern.add(element);
				Integer support = patterns.get(pattern);
				if (support == null) {
					patterns.put(pattern, 1);
				} else {
					patterns.replace(pattern, ++support);
				}
			}
			oldSensorID = element;
		}
		
		return patterns;
	}
	
	public static int evaluateSupport(List<Integer> pattern, List<Integer> dataSet){
		int frequency = -1;
		int index = 0;
		List<Integer> subList = dataSet;
		while (index >= 0) {
			frequency++;
			subList = subList.subList(index+1, subList.size());
			index = Collections.indexOfSubList(subList, pattern);
		}
		
		return frequency;
	}
	
	public static class Pattern<T extends Entry<?>> implements Entry<Entry<?>>{
		ArrayList<T> sequence;
		
		@Override
		public LocalDateTime getTime() {
			// TODO Auto-generated method stub
			return LocalDateTime.MIN;
		}

		@Override
		public Entry getID() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
