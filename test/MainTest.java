import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

import SequenceMining.SequenceMining;
import SequenceMining.SequenceMining.Pattern;
import SequenceMining.SequenceMining.Element;
import SequenceMining.SequenceMining.PatternSequence;

class MainTest {

	@Test
	void testINIT() {
		List<Element<Integer>> dataSet = new ArrayList<Element<Integer>>();
		dataSet.add(new Element<Integer>(2));
		dataSet.add(new Element<Integer>(5));
		dataSet.add(new Element<Integer>(3));
		dataSet.add(new Element<Integer>(7));
		dataSet.add(new Element<Integer>(5));
		dataSet.add(new Element<Integer>(3));
		
		HashMap<Pattern<Integer>, Integer> initPatterns = SequenceMining.INIT(dataSet);
		initPatterns.keySet().stream().forEach(key -> {
			for (Pattern<Integer> pattern : key.getPattern()) {
				System.out.print(pattern.toString() + ", ");
			}
			System.out.println(initPatterns.get(key));
		});
		
		PatternSequence<Integer> existingPattern = new PatternSequence<Integer>();
		existingPattern.add(new Element<Integer>(5));
		existingPattern.add(new Element<Integer>(3));
		
		//assertTrue(initPatterns.containsKey(existingPattern));
		assertThat(initPatterns.get(existingPattern), is(1));
	}

}
