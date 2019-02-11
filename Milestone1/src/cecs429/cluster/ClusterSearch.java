package cecs429.cluster;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import cecs429.common.CommonFunctions;
import cecs429.index.ClusterPositionalIndex;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.IWeightingScheme;
import cecs429.index.Posting;
import cecs429.rankedretrieval.RankedResults;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

public class ClusterSearch {

	private IWeightingScheme weightingScheme;
	private String query;
	private HashMap<Integer, HashSet<Integer>> clusterMap;
	private Integer numberDocs;
	private ClusterPositionalIndex cp;
	private List<String> processedTokensList = new ArrayList<String>();

	/**
	 * This is a constructer used to initialize all the necessary fields which are required to efficiently perform a clustered search.
	 * @param weightingScheme - The weighting scheme input by the user
	 * @param cp - The cluster disk positional index object to read cluster data
	 * @param clusterMap - The cluster map read from the disk
	 * @param numberDocs - The number of clusters read from the disk.
	 */
	public ClusterSearch(IWeightingScheme weightingScheme, ClusterPositionalIndex cp,
			HashMap<Integer, HashSet<Integer>> clusterMap, Integer numberDocs) {
		super();
		this.weightingScheme = weightingScheme;
		this.cp = cp;
		this.clusterMap = clusterMap;
		this.numberDocs = clusterMap.size();
	}

	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * This method compares a user query with all the cluster leaders and gets the cluster which is most similar to the user query
	 * @param processor - The token processor to process the query
	 * @return - Returns the map which contains the leader and its set of followers
	 */
	public HashMap<Integer, HashSet<Integer>> getClusterDocuments(TokenProcessor processor) {
		HashMap<Integer, Double> leaderScoreMap = new HashMap<Integer, Double>();
		EnglishTokenStream queryStream = new EnglishTokenStream(new StringReader(query));
		Iterable<String> queryTokens = queryStream.getTokens();
		CommonFunctions cf = new CommonFunctions();
		for (String term : queryTokens) {
			List<String> processedTokens = processor.processToken(term);
			for (String processedQueryToken : processedTokens) {
				this.processedTokensList.add(processedQueryToken);
				List<Posting> termPostings = cp.getPostings(processedQueryToken);
				Integer dft = termPostings.size();
				Double wqt = weightingScheme.getWqt(numberDocs, dft);
				for (Posting p : termPostings) {
					// if cluster map contains the document ID, then it is a leader
					if (clusterMap.containsKey(p.getDocumentId())) {
						if (leaderScoreMap.containsKey(p.getDocumentId())) {
							double accumulatorValue = leaderScoreMap.get(p.getDocumentId());
							Double wdt = cf.getWdt(weightingScheme, p);
							accumulatorValue = (accumulatorValue + (wqt * wdt));

							leaderScoreMap.put(p.getDocumentId(), accumulatorValue);
						} else {
							// else acquire new value for accumulator.
							Double wdt = cf.getWdt(weightingScheme, p);
							double accumulatorValue = 0.0;
							accumulatorValue = (accumulatorValue + (wqt * wdt));
							leaderScoreMap.put(p.getDocumentId(), p.getWdtDefaultValue());
						}
					}
				}
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

		for (Map.Entry<Integer, Double> entry : leaderScoreMap.entrySet()) {
			heap.offer(entry);
		}

		Map.Entry<Integer, Double> entry = heap.poll();
		HashMap<Integer, HashSet<Integer>> resultMap = new HashMap<Integer, HashSet<Integer>>();
		if (clusterMap.get(entry.getKey()).isEmpty()) {
			// this leader has no followers
			return null;
		} else {
			resultMap.put(entry.getKey(), clusterMap.get(entry.getKey()));
			// System.out.println("Leader Document ID:" + entry.getKey());
			// System.out.println("Followers:" + clusterMap.get(entry.getKey()));
			return resultMap;
		}
	}

	
	/**
	 * This method performs a ranked retrieval on the followers of a leader
	 * @param query - The search query entered by the user.
	 * @param processor - The token processor to process the query.
	 * @param dp - The disk positional index to read the index on disk.
	 * @param numberOfDocs1 - The number of documents in the corpus
	 * @param resultMap - The map which contains the leader and its set of followers.
	 * @param numberOfResults - The top 'K' number of relevant results to be fetched as decided by the user. 
	 * @return - Returns the list of ranked retrieved results.
	 */
	public List<RankedResults> clusterRankedRetrieval(String query, TokenProcessor processor, DiskPositionalIndex dp,
			Integer numberOfDocs1, HashMap<Integer, HashSet<Integer>> resultMap, Integer numberOfResults) {
		EnglishTokenStream queryStream = new EnglishTokenStream(new StringReader(query));
		Iterable<String> queryTokens = queryStream.getTokens();
		HashSet<Integer> followerSet = new HashSet<>();
		for (Integer leaderID : resultMap.keySet()) {
			followerSet = resultMap.get(leaderID);
		}
		HashMap<Integer, Double> accumulatorDocMap = new HashMap<Integer, Double>();
		CommonFunctions cf = new CommonFunctions();
		for (String term : queryTokens) {
			List<String> processedTokens = processor.processToken(term);
			for (String processedQueryToken : processedTokens) {
				List<Posting> termPostings = dp.getPostings(processedQueryToken);
				Integer dft = termPostings.size();
				Double wqt = weightingScheme.getWqt(numberOfDocs1, dft);
				for (Posting p : termPostings) {
					if (followerSet.contains(p.getDocumentId())) {
						// if true then it is a follower.
						if (accumulatorDocMap.containsKey(p.getDocumentId())) {
							double accumulatorValue = accumulatorDocMap.get(p.getDocumentId());
							Double wdt = cf.getWdt(weightingScheme, p);
							accumulatorValue += (wqt * wdt);
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
				accumulatorValue /= Ld;
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
	 * This method performs a ranked retrieval on the followers of a leader. This method is used to calculate metrics for displaying the statistics of the system.
	 * @param query - The search query entered by the user.
	 * @param processor - The token processor to process the query.
	 * @param dp - The disk positional index to read the index on disk.
	 * @param numberOfDocs1 - The number of documents in the corpus
	 * @param resultMap - The map which contains the leader and its set of followers.
	 * @param numberOfResults - The top 'K' number of relevant results to be fetched as decided by the user. 
	 * @return - Returns the list of ranked retrieved results.
	 */
	public List<RankedResults> clusterRankedRetrievalStatistics(DiskPositionalIndex dp, Integer numberOfDocs1, HashMap<Integer, HashSet<Integer>> resultMap,
			Integer numberOfResults) {
		HashSet<Integer> followerSet = new HashSet<>();
		followerSet = resultMap.get(resultMap.keySet().iterator().next());
		HashMap<Integer, Double> accumulatorDocMap = new HashMap<Integer, Double>();
		CommonFunctions cf = new CommonFunctions();
		int accumulatorCount = 0;
			for (String processedQueryToken : processedTokensList) {
				List<Posting> termPostings = dp.getPostings(processedQueryToken);
				Integer dft = termPostings.size();
				Double wqt = weightingScheme.getWqt(numberOfDocs1, dft);
				for (Posting p : termPostings) {
					if (followerSet.contains(p.getDocumentId())) {
						// if true then it is a follower.
						if (accumulatorDocMap.containsKey(p.getDocumentId())) {
							double accumulatorValue = accumulatorDocMap.get(p.getDocumentId());
							Double wdt = cf.getWdt(weightingScheme, p);
							accumulatorValue += (wqt * wdt);
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
			
		// If accumulator value is greater than 0, divide it by corresponding document
		// weight value.
		for (Integer key : accumulatorDocMap.keySet()) {
			if (accumulatorDocMap.get(key) > 0.0) {
				accumulatorCount += 1;
				Double Ld = weightingScheme.getLd(key);
				Double accumulatorValue = accumulatorDocMap.get(key);
				accumulatorValue /= Ld;
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
		this.processedTokensList.clear();
		return resultList;
	}
}
