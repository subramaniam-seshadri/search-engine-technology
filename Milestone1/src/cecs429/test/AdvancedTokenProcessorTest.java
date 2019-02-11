package cecs429.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cecs429.text.AdvancedTokenProcessor;

class AdvancedTokenProcessorTest {

	@Test
	void testPhraseProcessing() {
		AdvancedTokenProcessor tokenProcesser = null;
		try {
			tokenProcesser = new AdvancedTokenProcessor();
		} catch (Throwable e) {
		}
		String phraseTest = "hewlett-packard";
		List<String> actualResult = tokenProcesser.processToken(phraseTest);
		List<String> expectedResult = new ArrayList<String>();
		expectedResult.add("hewlett");
		expectedResult.add("packard");
		expectedResult.add("hewlettpackard");
		assertEquals(expectedResult, actualResult);
	}

	@Test
	void testAlphanumeric() {
		AdvancedTokenProcessor tokenProcesser = null;
		try {
			tokenProcesser = new AdvancedTokenProcessor();
		} catch (Throwable e) {
		}
		String alphaNumericTest1 = ".,Hello.";
		String alphaNumericTest2 = "192.168.1.1";
		List<String> actualResult = tokenProcesser.processToken(alphaNumericTest2);
		List<String> expectedResult = new ArrayList<String>();
		expectedResult.add("192.168.1.1");
		assertEquals(expectedResult, actualResult);
	}

	@Test
	void testApostrophes() {
		AdvancedTokenProcessor tokenProcesser = null;
		try {
			tokenProcesser = new AdvancedTokenProcessor();
		} catch (Throwable e) {
		}
		String apostropheTest = "Mark's";
		List<String> actualResult = tokenProcesser.processToken(apostropheTest);
		List<String> expectedResult = new ArrayList<String>();
		expectedResult.add("mark");
		// Mark's becomes mark after removing apostrophe and stemming
		assertEquals(expectedResult, actualResult);
	}

	@Test
	void testLowerCase() {
		AdvancedTokenProcessor tokenProcesser = null;
		try {
			tokenProcesser = new AdvancedTokenProcessor();
		} catch (Throwable e) {
		}
		String apostropheTest = "MARK";
		List<String> actualResult = tokenProcesser.processToken(apostropheTest);
		List<String> expectedResult = new ArrayList<String>();
		expectedResult.add("mark");
		assertEquals(expectedResult, actualResult);
	}

	@Test
	void testStemTokenJava() {
		fail("Not yet implemented");
	}

}
