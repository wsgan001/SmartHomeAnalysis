package SequenceMining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class SequenceMining {

	/**
	 * Generate a pattern hierarchy
	 * 
	 * @param dataSet
	 */
	public static <E> void PATTERN_HIERARCHY(List<Element<E>> dataSet) {

		HashMap<Pattern<E>, Integer> patterns = PATTERN_DISCOVERY(dataSet, 2);
		System.out.println("Hi");
		patterns.keySet().stream()
				.forEach(entry -> System.out.println(entry.getPattern() + " with SUPPORT: " + patterns.get(entry)));

		return;
	}

	/**
	 * Algorithm 2 INIT Find the pairs of elements which form the shortest patterns
	 * 
	 * @param dataSet
	 * @return
	 */
	public static <E> HashMap<Pattern<E>, Integer> INIT(List<Element<E>> dataSet) {
		HashMap<Pattern<E>, Integer> patterns = new HashMap<Pattern<E>, Integer>();
		Element<E> oldSensorID = null;
		for (Element<E> element : dataSet) {
			if (oldSensorID != null) {
				PatternSequence<E> pattern = new PatternSequence<E>();
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

	/**
	 * Algorithm 3 CANDIDATE-GENERATION Generate a set of possible joint patterns.
	 * 
	 * @param discoveredPatterns
	 * @return
	 */
	public static <E> ArrayList<Pattern<E>> CANDIDATE_GENERATION(HashMap<Pattern<E>, Integer> discoveredPatterns) {
		ArrayList<Pattern<E>> candidatePatterns = new ArrayList<Pattern<E>>();

		for (Pattern<E> fromPattern : discoveredPatterns.keySet()) {
			for (Pattern<E> toPattern : discoveredPatterns.keySet()) {
				if (fromPattern.getPattern().size() != toPattern.getPattern().size() || fromPattern.equals(toPattern)) {
					continue; // skip if patterns are different lengths
				}
				boolean isNewPattern = true;
				for (int i = 1; i < fromPattern.getPattern().size(); i++) {
					if (fromPattern.getPattern().get(i) != toPattern.getPattern().get(i - 1)) {
						isNewPattern = false;
						break;
					}
				}
				if (isNewPattern) {
					PatternSequence<E> newPattern = new PatternSequence<E>();
					newPattern.add(fromPattern.getPattern().get(0));
					newPattern.getPattern().addAll(toPattern.getPattern());
					if (!candidatePatterns.contains(newPattern)) {
						candidatePatterns.add(newPattern);
					}
				}
			}
		}

		return candidatePatterns;
	}

	/**
	 * Algorithm 1 PATTERN-DISCOVERY Starting with the initial set of pair patterns,
	 * generate a set of extended patterns to add. New patterns below the min
	 * support are ignored and ones that satisfy the minimum support replace the
	 * sub-patterns they were generated from.
	 * 
	 * @param dataSet
	 * @param minSupport
	 * @return
	 */
	public static <E> HashMap<Pattern<E>, Integer> PATTERN_DISCOVERY(List<Element<E>> dataSet, int minSupport) {
		HashMap<Pattern<E>, Integer> discovered = INIT(dataSet);
		List<Pattern<E>> candidates = CANDIDATE_GENERATION(discovered);

		while (!candidates.isEmpty()) {
			for (Pattern<E> pattern : candidates) {
				int support = EVALUATE_SUPPORT(pattern, dataSet);
				if (support >= minSupport) {
					discovered.put(pattern, support);

					Iterator<Pattern<E>> iterator = discovered.keySet().iterator();
					while (iterator.hasNext()) {
						Pattern<E> subPattern = iterator.next();
						if (pattern.getPattern().size() - 1 == subPattern.getPattern().size()) {
							if (!(Collections.indexOfSubList(pattern.getPattern(), subPattern.getPattern()) < 0)) {
								iterator.remove();
							}
						}
					}
				}
			}
			candidates = CANDIDATE_GENERATION(discovered);
		}
		return discovered;
	}

	/**
	 * Count the frequency of a pattern showing up in the dataset known as the
	 * "support"
	 * 
	 * @param pattern
	 * @param dataSet
	 * @return
	 */
	public static <E> int EVALUATE_SUPPORT(Pattern<E> pattern, List<Element<E>> dataSet) {
		return (int) IntStream.range(pattern.getPattern().size(), dataSet.size()).mapToObj(index -> {
			return dataSet.subList(index - pattern.getPattern().size(), index);
		}).map(sublist -> {
			if (sublist.equals(pattern.getPattern())) {
				return 1;
			} else {
				return 0;
			}
		}).count();
	}

	/**
	 * Generate a pattern hierarchy
	 * 
	 * @param discoveredPatterns
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> patternHierarchy(
			HashMap<ArrayList<Integer>, Integer> discoveredPatterns) {
		ArrayList<ArrayList<Integer>> structuredPatterns = new ArrayList<ArrayList<Integer>>();

		return structuredPatterns;
	}

	/**
	 * Algorithm 1 PATTERN-DISCOVERY Starting with the initial set of pair patterns,
	 * generate a set of extended patterns to add. New patterns below the min
	 * support are ignored and ones that satisfy the minimum support replace the
	 * sub-patterns they were generated from.
	 * 
	 * @param dataSet
	 * @param minimumSupport
	 * @return
	 */
	public static HashMap<ArrayList<Integer>, Integer> patternDiscovery(List<Integer> dataSet, int minimumSupport) {
		HashMap<ArrayList<Integer>, Integer> discoveredPatterns = init(dataSet);
		ArrayList<ArrayList<Integer>> candidatePatterns = candGen(discoveredPatterns);

		while (!candidatePatterns.isEmpty()) {

			for (ArrayList<Integer> pattern : candidatePatterns) {
				int support = evaluateSupport(pattern, dataSet);
				if (support >= minimumSupport) {
					discoveredPatterns.put(pattern, support);
					Iterator<ArrayList<Integer>> iterator = discoveredPatterns.keySet().iterator();
					while (iterator.hasNext()) {
						ArrayList<Integer> subPattern = iterator.next();
						if (pattern.size() - 1 == subPattern.size()
								&& !(Collections.indexOfSubList(pattern, subPattern) < 0)) {
							iterator.remove();
						}
					}
				}
			}

			candidatePatterns = candGen(discoveredPatterns);
		}

		return discoveredPatterns;
	}

	/**
	 * Algorithm 3 CANDIDATE-GENERATION Generate a set of possible joint patterns.
	 * 
	 * @param discoveredPatterns
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> candGen(HashMap<ArrayList<Integer>, Integer> discoveredPatterns) {
		ArrayList<ArrayList<Integer>> candidatePatterns = new ArrayList<ArrayList<Integer>>();

		for (ArrayList<Integer> fromPattern : discoveredPatterns.keySet()) {
			for (ArrayList<Integer> toPattern : discoveredPatterns.keySet()) {
				if (fromPattern.size() != toPattern.size() || fromPattern.equals(toPattern)) {
					continue; // skip if patterns are different lengths
				}
				boolean isNewPattern = true;
				for (int i = 1; i < fromPattern.size(); i++) {
					if (fromPattern.get(i) != toPattern.get(i - 1)) {
						isNewPattern = false;
						break;
					}
				}
				if (isNewPattern) {
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

	/**
	 * Algorithm 2 INIT Find the pairs of elements which form the shortest patterns
	 * 
	 * @param dataSet
	 * @return
	 */
	public static HashMap<ArrayList<Integer>, Integer> init(List<Integer> dataSet) {
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

	/**
	 * Count the frequency of a pattern showing up in the dataset known as the
	 * "support"
	 * 
	 * @param pattern
	 * @param dataSet
	 * @return
	 */
	public static int evaluateSupport(List<Integer> pattern, List<Integer> dataSet) {
		int frequency = -1;
		int index = 0;
		List<Integer> subList = dataSet;
		while (index >= 0) {
			frequency++;
			subList = subList.subList(index + 1, subList.size());
			index = Collections.indexOfSubList(subList, pattern);
		}

		return frequency;
	}

	public interface Pattern<E> {
		List<Pattern<E>> getPattern();
	}

	public static class PatternSequence<E> implements Pattern<E> {
		List<Pattern<E>> sequence;

		public PatternSequence() {
			sequence = new ArrayList<Pattern<E>>();
		}

		public PatternSequence(List<Pattern<E>> sequence) {
			this.sequence = sequence;
		}

		public List<Pattern<E>> getPattern() {
			return this.sequence;
		}

		public void add(Pattern<E> element) {
			sequence.add(element);
		}

		public int size() {
			return sequence.size();
		}

		@Override
		public String toString() {
			return sequence.toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Pattern)
				return sequence.equals(((Pattern<E>) obj).getPattern());
			else
				return false;
		}

		@Override
		public int hashCode() {
			return sequence.hashCode();
		}
	}

	public static class Element<E> implements Pattern<E> {
		E element;

		public Element(E element) {
			this.element = element;
		}

		@Override
		public List<Pattern<E>> getPattern() {
			return Collections.singletonList(this);
		}

		@Override
		public String toString() {
			return element.toString();
		}

		// @Override
		// public boolean equals(Object obj) {
		// if (obj instanceof Pattern) {
		// Pattern patternObj = (Pattern)obj;
		// if (patternObj.getPattern().size() == 1 &&
		// patternObj.getPattern().contains(element)) {
		// return true;
		// }
		// }
		// return false;
		// }

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Pattern)
				return this.getPattern().equals(((Pattern<E>) obj).getPattern());
			else
				return false;
		}

		@Override
		public int hashCode() {
			return element.hashCode();
		}
	}
}
