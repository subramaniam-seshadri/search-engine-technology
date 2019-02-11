package cecs429.rankedretrieval;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.commons.collections4.CollectionUtils;

import cecs429.common.CommonFunctions;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.IWeightingScheme;
import cecs429.index.Posting;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

public class RankedRetrieval {

	private IWeightingScheme weightingScheme;
	private String query;
	private Integer numberDocs;
	private DiskPositionalIndex dp;

	/**
	 * Constructor to initialize the dependencies needed for efficient ranked retrieval of documents.
	 * @param weightingScheme - The weighting scheme selected by the user.
	 * @param numberDocs - The number of documents over which ranked retrieval will be done.
	 * @param dp - The disk positional index object used to read values from the disk.
	 */
	public RankedRetrieval(IWeightingScheme weightingScheme, Integer numberDocs, DiskPositionalIndex dp) {
		this.weightingScheme = weightingScheme;
		this.numberDocs = numberDocs;
		this.dp = dp;
	}

	
	/**
	 * Setter used to set query.
	 * @param query - The user query to be searched.
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * This method is used to get the top K ranked documents based on a search query entered by the user.
	 * @param processor - Token processor used to process the tokens.
	 * @return - Returns the list of ranked results, with the results with highest
	 *         scores at the top.
	 */
	public List<RankedResults> getRankedDocuments(TokenProcessor processor, Integer numberOfResults) {
		EnglishTokenStream queryStream = new EnglishTokenStream(new StringReader(query));
		Iterable<String> queryTokens = queryStream.getTokens();
		HashMap<Integer, Double> accumulatorDocMap = new HashMap<Integer, Double>();
		CommonFunctions cf = new CommonFunctions();
		for (String term : queryTokens) {
			List<String> processedTokens = processor.processToken(term);
			for (String processedQueryToken : processedTokens) {
				List<Posting> termPostings = dp.getPostings(processedQueryToken);
				if (!CollectionUtils.isEmpty(termPostings)) {

					Integer dft = termPostings.size();
					Double wqt = weightingScheme.getWqt(numberDocs, dft);
					for (Posting p : termPostings) {
						// if map contains the document ID increment accumulator value.
						if (accumulatorDocMap.containsKey(p.getDocumentId())) {
							double accumulatorValue = accumulatorDocMap.get(p.getDocumentId());

							Double wdt = cf.getWdt(weightingScheme, p);
							accumulatorValue = (accumulatorValue + (wqt * wdt));
							accumulatorDocMap.put(p.getDocumentId(), accumulatorValue);
						} else {
							// else acquire new value for accumulator.
							Double wdt = cf.getWdt(weightingScheme, p);
							double accumulatorValue = 0.0;
							accumulatorValue = (accumulatorValue + (wqt * wdt));
							accumulatorDocMap.put(p.getDocumentId(), accumulatorValue);
						}
					}
				}
			}
		}
		// If accumulator value is greater than 0, divide it by corresponding document
		// weight value.
		for (Integer key : accumulatorDocMap.keySet()) {
			if (accumulatorDocMap.get(key) > 0.0) {
				Double Ld = weightingScheme.getLd(key);
				Double accumulatorValue = accumulatorDocMap.get(key);
				accumulatorValue = (accumulatorValue / Ld);
				accumulatorDocMap.put(key, accumulatorValue);
			}
		}

		PriorityQueue<Map.Entry<Integer, Double>> heap = new PriorityQueue<Map.Entry<Integer, Double>>(
				new Comparator<Map.Entry<Integer, Double>>() {
					@Override
					public int compare(Map.Entry<Integer, Double> entry1, Map.Entry<Integer, Double> entry2) {
						if (entry2.getValue() > entry1.getValue())
							return 1;
						else
							return -1;
					}
				});

		for (Map.Entry<Integer, Double> entry : accumulatorDocMap.entrySet()) {
			heap.offer(entry);
		}

		List<RankedResults> resultList = new ArrayList<RankedResults>();
		int k = 0;
		if (heap.size() > numberOfResults)
			k = numberOfResults;
		else
			k = heap.size();
		for (int i = 0; i < k; i++) {
			Map.Entry<Integer, Double> entry = heap.poll();
			RankedResults rr = new RankedResults();
			rr.setDocumentID(entry.getKey());
			rr.setRetrievalScore(entry.getValue());
			resultList.add(rr);
		}
		return resultList;
	}
	
	
	/**
	 * This method is used to get the top K ranked documents based on a search query entered by the user. This method is used for evaluation purposes
	 * and to get the statistics.
	 * @param processor - Token processor used to process the tokens.
	 * @return - Returns the list of ranked results, with the results with highest
	 *         scores at the top.
	 */
	public List<RankedResults> getRankedDocumentsStatistics(TokenProcessor processor, Integer numberOfResults) {
		EnglishTokenStream queryStream = new EnglishTokenStream(new StringReader(query));
		Iterable<String> queryTokens = queryStream.getTokens();
		HashMap<Integer, Double> accumulatorDocMap = new HashMap<Integer, Double>();
		CommonFunctions cf = new CommonFunctions();
		int accumulatorCount = 0;
		for (String term : queryTokens) {
			List<String> processedTokens = processor.processToken(term);
			for (String processedQueryToken : processedTokens) {
				List<Posting> termPostings = dp.getPostings(processedQueryToken);
				if (!CollectionUtils.isEmpty(termPostings)) {

					Integer dft = termPostings.size();
					Double wqt = weightingScheme.getWqt(numberDocs, dft);
					for (Posting p : termPostings) {
						// if map contains the document ID increment accumulator value.
						if (accumulatorDocMap.containsKey(p.getDocumentId())) {
							double accumulatorValue = accumulatorDocMap.get(p.getDocumentId());

							Double wdt = cf.getWdt(weightingScheme, p);
							accumulatorValue += (wqt * wdt);
							accumulatorDocMap.put(p.getDocumentId(), accumulatorValue);
						} else {
							// else acquire new value for accumulator.
							Double wdt = cf.getWdt(weightingScheme, p);
							double accumulatorValue = 0.0;
							accumulatorValue += (wqt * wdt);
							accumulatorDocMap.put(p.getDocumentId(), accumulatorValue);
						}
					}
				}
			}
		}
		// If accumulator value is greater than 0, divide it by corresponding document
		// weight value.
		for (Integer key : accumulatorDocMap.keySet()) {
			if (accumulatorDocMap.get(key) > 0.0) {
				accumulatorCount += 1;
				Double Ld = weightingScheme.getLd(key);
				Double accumulatorValue = accumulatorDocMap.get(key);
				accumulatorValue = (accumulatorValue / Ld);
				accumulatorDocMap.put(key, accumulatorValue);
			}
		}

		PriorityQueue<Map.Entry<Integer, Double>> heap = new PriorityQueue<Map.Entry<Integer, Double>>(
				new Comparator<Map.Entry<Integer, Double>>() {
					@Override
					public int compare(Map.Entry<Integer, Double> entry1, Map.Entry<Integer, Double> entry2) {
						if (entry2.getValue() > entry1.getValue())
							return 1;
						else
							return -1;
					}
				});

		for (Map.Entry<Integer, Double> entry : accumulatorDocMap.entrySet()) {
			heap.offer(entry);
		}

		List<RankedResults> resultList = new ArrayList<RankedResults>();
		int k = 0;
		if (heap.size() > numberOfResults)
			k = numberOfResults;
		else
			k = heap.size();
		for (int i = 0; i < k; i++) {
			Map.Entry<Integer, Double> entry = heap.poll();
			RankedResults rr = new RankedResults();
			rr.setDocumentID(entry.getKey());
			rr.setRetrievalScore(entry.getValue());
			resultList.add(rr);
		}
		RankedResults rr = new RankedResults();
		rr.setAccumulatorCount(accumulatorCount);
		resultList.add(rr);
		return resultList;
	}
}
