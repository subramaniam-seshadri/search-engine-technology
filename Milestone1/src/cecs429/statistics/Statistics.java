package cecs429.statistics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;

import cecs429.cluster.ClusterPruning;
import cecs429.cluster.ClusterSearch;
import cecs429.documents.DocumentCorpus;
import cecs429.index.ClusterPositionalIndex;
import cecs429.index.DefaultWeightingScheme;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.IWeightingScheme;
import cecs429.index.OkapiBM25WeightingScheme;
import cecs429.index.TfIdfWeightingScheme;
import cecs429.index.WackyWeightingScheme;
import cecs429.rankedretrieval.RankedResults;
import cecs429.rankedretrieval.RankedRetrieval;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.TokenProcessor;

public class Statistics {

	
	/**
	 * This method is used to read queries from a file where each user query is on a separate line.
	 * @param path - Path of the corpus
	 * @return - List of strings where each element of list is a search query.
	 */
	public static List<String> readQueries(String path) {
		Scanner s = null;
		List<String> queryList = new ArrayList<String>();
		try {
			s = new Scanner(new FileInputStream(path + "\\relevance\\queries"), "UTF-8");
			while (s.hasNextLine()) {
				queryList.add(s.nextLine());
			}
			s.close();
		} catch (FileNotFoundException e) {
			System.out.println("No query file found");
			
		} 
		return queryList;
	}

	
	/**
	 * This method is used to read the set of relevant pertaining to each query from a file.
	 * @param path - Path of the corpus.
	 * @return - A list of set of relevant documents.
	 */
	public static List<HashSet<Integer>> readRelevantDocs(String path) {
		Scanner s = null;
		List<HashSet<Integer>> queryReleventDocs = new ArrayList<HashSet<Integer>>();
		try {
			s = new Scanner(new File(path + "\\relevance\\qrel"));
			while (s.hasNextLine()) {
				String[] fileLine = s.nextLine().split("\\s+");
				HashSet<Integer> docIDs = new HashSet<Integer>();
				for (String docID : fileLine) {
					docIDs.add(Integer.parseInt(docID));
				}
				queryReleventDocs.add(docIDs);
			}
			//s.close();
		} catch (FileNotFoundException e) {
			System.out.println("No query relevance file found");
		}
		return queryReleventDocs;
	}

	
	/**
	 * This method is used to get the statistics for ranked retrieval.
	 * @param indexPath - Path where the index is stored on disk
	 * @param corpus - The corpus object which represents the collection of documents in a specified directory.
	 * @param corpusPath - Path where the corpus is stored.
	 */
	public void getRankedStatistics(String indexPath, DocumentCorpus corpus, String corpusPath) {
		System.out.println("Get Ranked Statistics");
		TokenProcessor processor = null;
		try {
			processor = new AdvancedTokenProcessor();
		} catch (Throwable e2) {
		}
		DiskPositionalIndex dp = new DiskPositionalIndex();
		try {
			dp = new DiskPositionalIndex(indexPath);
			dp.readDocValues(corpus.getCorpusSize());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Choose a ranking strategy:");
		System.out.println("1. Default\n2. Tf-Idf\n3. Okapi\n4. Wacky");
		int strategyChoice = 0;
		IWeightingScheme weightingScheme = null;
		Scanner sc = new Scanner(System.in);
		try {
			strategyChoice = sc.nextInt();
			//sc.close();
		} catch (Exception e) {
			System.out.println("Invalid choice. Selecting Default strategy");
		}
		switch (strategyChoice) {
		case 1:
			weightingScheme = new DefaultWeightingScheme(indexPath, dp);
			break;
		case 2:
			weightingScheme = new TfIdfWeightingScheme(indexPath, dp);
			break;
		case 3:
			weightingScheme = new OkapiBM25WeightingScheme(indexPath, dp);
			break;
		case 4:
			weightingScheme = new WackyWeightingScheme(indexPath, dp);
			break;
		default:
			weightingScheme = new DefaultWeightingScheme(indexPath, dp);
			break;
		}
		List<String> listOfQueries = readQueries(corpusPath);
		List<HashSet<Integer>> relevantResultSet = readRelevantDocs(corpusPath);
		Integer numberOfDocs = dp.getDocIDWeightsMap().size();
		RankedRetrieval rr = new RankedRetrieval(weightingScheme, numberOfDocs, dp);
		int numberOfResults = 50;
		double throughput = 0.0;
		if (!CollectionUtils.isEmpty(relevantResultSet)) {
			List<Integer> accumulatorCountList = new ArrayList<Integer>();
			List<Double> averagePrecisionList = new ArrayList<Double>();
			long start = System.nanoTime();
			for (int i = 0; i < listOfQueries.size(); i++) {
				rr.setQuery(listOfQueries.get(i));
				List<RankedResults> resultList = rr.getRankedDocumentsStatistics(processor, numberOfResults);
				int accumulatorCount = resultList.remove(resultList.size() - 1).getAccumulatorCount();
				accumulatorCountList.add(accumulatorCount);
				Double averagePrecision = getAveragePrecision(resultList, relevantResultSet.get(i), corpus);
				averagePrecisionList.add(averagePrecision);
				/*System.out
						.println("Average Precision for " + "'" + listOfQueries.get(i) + "'" + " :" + averagePrecision);
				System.out.println("Number of Accumulators used: " + accumulatorCount);*/
			}
			long end = System.nanoTime();
			long elapsedTime = end - start;
			double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
			throughput = listOfQueries.size() / executionTime;
			int accumulatorCountSum = accumulatorCountList.stream().mapToInt(Integer::intValue).sum();
			double averageAccumulator = accumulatorCountSum/accumulatorCountList.size();
			double sumAveragePrecision = averagePrecisionList.stream().mapToDouble(Double::doubleValue).sum();
			double meanAveragePrecision = (((double) sumAveragePrecision) / ((double) averagePrecisionList.size()));
			System.out.println("Mean Average Precision for the test set is: " + meanAveragePrecision);
			System.out.println("Average number of non-zero accumulators used in ranked retrieval :" + averageAccumulator);
		} else {
			long start = System.nanoTime();
			for (int i = 0; i < listOfQueries.size(); i++) {
				rr.setQuery(listOfQueries.get(i));
				List<RankedResults> resultList = rr.getRankedDocumentsStatistics(processor, numberOfResults);
				//resultList.remove(resultList.size() - 1);
			}
			long end = System.nanoTime();
			long elapsedTime = end - start;
			double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
			throughput = listOfQueries.size() / executionTime;
		}
		System.out.println("Throughput of the system: " + throughput + "  queries/sec");
		System.out.println("Mean response time :" + 1 / throughput + "  seconds/query");
	}

	
	/**
	 * This method is used to get the average precision for evaluating effectiveness of a search engine.
	 * @param retrievedResultList - The retrieved results from either ranked or cluster retrieval
	 * @param relevantResultList - The list of documents that are relevant to a specific query for which results were retrieved.
	 * @param corpus - The corpus object which represents the collection of documents in a specified directory.
	 * @return - Returns the average precision.
	 */
	public static Double getAveragePrecision(List<RankedResults> retrievedResultList,
			HashSet<Integer> relevantResultList, DocumentCorpus corpus) {
		Double averagePrecision = 0.0;
		Integer countRelevantDocsInResult = 0;

		for (Integer i = 0; i < retrievedResultList.size(); i++) {
			int fileName = Integer.parseInt(FilenameUtils
					.removeExtension(corpus.getDocument(retrievedResultList.get(i).getDocumentID()).getTitle()));
			if (relevantResultList.contains(fileName)) {
				countRelevantDocsInResult += 1;
				double precisionAtK = countRelevantDocsInResult.doubleValue() / (i.doubleValue() + 1);
				averagePrecision += precisionAtK;
			}
		}
		averagePrecision = (averagePrecision / relevantResultList.size());
		return averagePrecision;
	}

	
	/**
	 * This method is used to get the statistics for the cluster pruning approach.
	 * @param indexPath - Path where the index is stored on disk.
	 * @param corpus - Corpus object which contains the collection of documents.
	 * @param corpusPath - The path where the corpus is stored on the disk.
	 */
	public void getClusterStatistics(String indexPath, DocumentCorpus corpus, String corpusPath) {
		System.out.println("\n\nGet cluster statistics");
		TokenProcessor processor = null;
		try {
			processor = new AdvancedTokenProcessor();
		} catch (Throwable e2) {
		}
		DiskPositionalIndex dp = new DiskPositionalIndex();
		ClusterPositionalIndex cpi = new ClusterPositionalIndex();
		try {
			dp = new DiskPositionalIndex(indexPath);
			dp.readDocValues(corpus.getCorpusSize());
			cpi = new ClusterPositionalIndex(indexPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Choose a ranking strategy:");
		System.out.println("1. Default\n2. Tf-Idf\n3. Okapi\n4. Wacky");
		
		int strategyChoice = 0;
		IWeightingScheme weightingScheme = null;
		Scanner sc = new Scanner(System.in);
		try {
			strategyChoice = sc.nextInt();
			//sc.close();
		} catch (Exception e) {
			System.out.println("Invalid choice. Selecting Default strategy");
		}
		switch (strategyChoice) {
		case 1:
			weightingScheme = new DefaultWeightingScheme(indexPath, dp);
			break;
		case 2:
			weightingScheme = new TfIdfWeightingScheme(indexPath, dp);
			break;
		case 3:
			weightingScheme = new OkapiBM25WeightingScheme(indexPath, dp);
			break;
		case 4:
			weightingScheme = new WackyWeightingScheme(indexPath, dp);
			break;
		default:
			weightingScheme = new DefaultWeightingScheme(indexPath, dp);
			break;
		}
		List<String> listOfQueries = readQueries(corpusPath);
		List<HashSet<Integer>> relevantResultSet = readRelevantDocs(corpusPath);
		Integer numberOfDocs = dp.getDocIDWeightsMap().size();
		RankedRetrieval rr = new RankedRetrieval(weightingScheme, numberOfDocs, dp);
		ClusterPruning cp = new ClusterPruning();
		HashMap<Integer, HashSet<Integer>> clusterMap = cp.readClusters(indexPath);
		Integer numberOfDocs1 = clusterMap.size();
		System.out.println("Read Cluster size:" + clusterMap.size());
		cpi.readDocValues(clusterMap.size());
		ClusterSearch cs = new ClusterSearch(weightingScheme, cpi, clusterMap, numberOfDocs1);
		int numberOfResults = 50;
		double throughput = 0.0;
		if (!CollectionUtils.isEmpty(relevantResultSet)) {
			List<Double> averagePrecisionList = new ArrayList<Double>();
			List<Integer> accumulatorCountList = new ArrayList<Integer>();
			long start = System.nanoTime();
			for (int i = 0; i < listOfQueries.size(); i++) {
				cs.setQuery(listOfQueries.get(i));
				HashMap<Integer, HashSet<Integer>> resultMap = cs.getClusterDocuments(processor);
				if (resultMap != null) { // found a leader (map) which has followers
					
					// get ranked retrieval on followers
					List<RankedResults> resultList = cs.clusterRankedRetrievalStatistics( dp, numberOfDocs, resultMap, numberOfResults);
					// get the accumulator count from the cluster
					int accumulatorCount = resultList.remove(resultList.size() - 1).getAccumulatorCount();
					accumulatorCountList.add(accumulatorCount);
					Double averagePrecision = getAveragePrecision(resultList, relevantResultSet.get(i), corpus);
					averagePrecisionList.add(averagePrecision);
					/*System.out.println(
							"Average Precision for " + "'" + listOfQueries.get(i) + "'" + " :" + averagePrecision);
					System.out.println("Number of Accumulators used: " + accumulatorCount);*/
				}else { // resultMap is null, which means leader has no followers
					rr.setQuery(listOfQueries.get(i));
					List<RankedResults> resultList = rr.getRankedDocumentsStatistics(processor, numberOfResults);
					int accumulatorCount = resultList.remove(resultList.size() - 1).getAccumulatorCount();
					Double averagePrecision = getAveragePrecision(resultList, relevantResultSet.get(i), corpus);
					averagePrecisionList.add(averagePrecision);
					accumulatorCountList.add(accumulatorCount);
					/*System.out.println(
							"Average Precision for " + "'" + listOfQueries.get(i) + "'" + " :" + averagePrecision);
					System.out.println("Number of Accumulators used: " + accumulatorCount);*/
				}
			}
			long end = System.nanoTime();
			int accumulatorCountSum = accumulatorCountList.stream().mapToInt(Integer::intValue).sum();
			double averageAccumulator = accumulatorCountSum/accumulatorCountList.size();
			long elapsedTime = end - start;
			double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
			throughput = listOfQueries.size() / executionTime;
			double sumAveragePrecision = averagePrecisionList.stream().mapToDouble(Double::doubleValue).sum();
			double meanAveragePrecision = (((double) sumAveragePrecision) / ((double) averagePrecisionList.size()));
			System.out.println("Mean Average Precision for the test set is: " + meanAveragePrecision);
			System.out.println("Average number of non-zero accumulators used in cluster pruning :" + averageAccumulator);

		} else { // relevant set is empty. Cannot do precision calculations.
			// Do cluster ranked retrieval on set of queries without ranked retrieval.
			long start = System.nanoTime();
			for (int i = 0; i < listOfQueries.size(); i++) {
				cs.setQuery(listOfQueries.get(i));
				HashMap<Integer, HashSet<Integer>> resultMap = cs.getClusterDocuments(processor);
				if (resultMap != null) {
					List<RankedResults> resultList = cs.clusterRankedRetrievalStatistics(dp, numberOfDocs, resultMap, numberOfResults);
					int accumulatorCount = resultList.remove(resultList.size() - 1).getAccumulatorCount();
					//System.out.println("Number of Accumulators used: " + accumulatorCount);
				}
			}
			long end = System.nanoTime();
			long elapsedTime = end - start;
			double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
			throughput = listOfQueries.size() / executionTime;
			System.out.println("Throughput of the cluster system (only): " + throughput + "  queries/sec");
			System.out.println("Mean response time of cluster system :" + 1 / throughput + "  seconds/query");
		}
	}
	
	
	/**
	 * This method is used to get the throughput for ranked retrieval method averaged over 30 iterations.
	 * @param indexPath - Path where the index is stored on disk.
	 * @param corpus - Corpus object which contains the collection of documents.
	 * @param corpusPath - The path where the corpus is stored on the disk.
	 */
	public void getRankedThroughPut(String indexPath, DocumentCorpus corpus, String corpusPath) {
		System.out.println("\n\nGet ranked throughput");
		TokenProcessor processor = null;
		try {
			processor = new AdvancedTokenProcessor();
		} catch (Throwable e2) {
		}
		DiskPositionalIndex dp = new DiskPositionalIndex();
		try {
			dp = new DiskPositionalIndex(indexPath);
			dp.readDocValues(corpus.getCorpusSize());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Choose a ranking strategy:");
		System.out.println("1. Default\n2. Tf-Idf\n3. Okapi\n4. Wacky");
		int strategyChoice = 0;
		IWeightingScheme weightingScheme = null;
		Scanner sc = new Scanner(System.in);
		try {
			strategyChoice = sc.nextInt();
			//sc.close();
		} catch (Exception e) {
			System.out.println("Invalid choice. Selecting Default strategy");
		}
		switch (strategyChoice) {
		case 1:
			weightingScheme = new DefaultWeightingScheme(indexPath, dp);
			break;
		case 2:
			weightingScheme = new TfIdfWeightingScheme(indexPath, dp);
			break;
		case 3:
			weightingScheme = new OkapiBM25WeightingScheme(indexPath, dp);
			break;
		case 4:
			weightingScheme = new WackyWeightingScheme(indexPath, dp);
			break;
		default:
			weightingScheme = new DefaultWeightingScheme(indexPath, dp);
			break;
		}
		List<String> listOfQueries = readQueries(corpusPath);
		List<HashSet<Integer>> relevantResultSet = readRelevantDocs(corpusPath);
		Integer numberOfDocs = dp.getDocIDWeightsMap().size();
		RankedRetrieval rr = new RankedRetrieval(weightingScheme, numberOfDocs, dp);
		int numberOfResults = 50;
		if (!CollectionUtils.isEmpty(relevantResultSet)) {
			long start = System.nanoTime();
			for(int j=0;j<30;j++) {
				for (int i = 0; i < listOfQueries.size(); i++) {
					rr.setQuery(listOfQueries.get(i));
					rr.getRankedDocumentsStatistics(processor, numberOfResults);
				}
				//System.out.println("Iteration : " +j);
			}
			long end = System.nanoTime();
			double throughput = 0.0;
			long elapsedTime = end - start;
			double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
			throughput = (listOfQueries.size()*30) / executionTime;
			System.out.println("Throughput over 30 iterations: " + throughput + "  queries/sec");
			System.out.println("Mean response time of ranked retrieval system :" + 1 / throughput + "  seconds/query");
		}
	}
	
	
	/**
	 * This method is used to get the throughput for clustered ranked retrieval method averaged over 30 iterations.
	 * @param indexPath - Path where the index is stored on disk.
	 * @param corpus - Corpus object which contains the collection of documents.
	 * @param corpusPath - The path where the corpus is stored on the disk.
	 */
	public void getClusterThroughPut(String indexPath, DocumentCorpus corpus, String corpusPath) {
		System.out.println("\n\nGet cluster throughput");
		TokenProcessor processor = null;
		try {
			processor = new AdvancedTokenProcessor();
		} catch (Throwable e2) {
		}
		DiskPositionalIndex dp = new DiskPositionalIndex();
		ClusterPositionalIndex cpi = new ClusterPositionalIndex();
		try {
			dp = new DiskPositionalIndex(indexPath);
			dp.readDocValues(corpus.getCorpusSize());
			cpi = new ClusterPositionalIndex(indexPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Choose a ranking strategy:");
		System.out.println("1. Default\n2. Tf-Idf\n3. Okapi\n4. Wacky");
		int strategyChoice = 0;
		IWeightingScheme weightingScheme = null;
		Scanner sc = new Scanner(System.in);
		try {
			strategyChoice = sc.nextInt();
			//sc.close();
		} catch (Exception e) {
			System.out.println("Invalid choice. Selecting Default strategy");
		}
		switch (strategyChoice) {
		case 1:
			weightingScheme = new DefaultWeightingScheme(indexPath, dp);
			break;
		case 2:
			weightingScheme = new TfIdfWeightingScheme(indexPath, dp);
			break;
		case 3:
			weightingScheme = new OkapiBM25WeightingScheme(indexPath, dp);
			break;
		case 4:
			weightingScheme = new WackyWeightingScheme(indexPath, dp);
			break;
		default:
			weightingScheme = new DefaultWeightingScheme(indexPath, dp);
			break;
		}
		List<String> listOfQueries = readQueries(corpusPath);
		Integer numberOfDocs = dp.getDocIDWeightsMap().size();
		ClusterPruning cp = new ClusterPruning();
		RankedRetrieval rr = new RankedRetrieval(weightingScheme, numberOfDocs, dp);
		HashMap<Integer, HashSet<Integer>> clusterMap = cp.readClusters(indexPath);
		Integer numberOfDocs1 = clusterMap.size();
		System.out.println("Read Cluster size:" + clusterMap.size());
		cpi.readDocValues(clusterMap.size());
		ClusterSearch cs = new ClusterSearch(weightingScheme, cpi, clusterMap, numberOfDocs1);
		int numberOfResults = 50;
		long start = System.nanoTime();
		for(int j=0;j<30;j++) {
			for (int i = 0; i < listOfQueries.size(); i++) {
				cs.setQuery(listOfQueries.get(i));
				HashMap<Integer, HashSet<Integer>> resultMap = cs.getClusterDocuments(processor);
					cs.clusterRankedRetrievalStatistics(dp, numberOfDocs, resultMap, numberOfResults);
			}
		}
		long end = System.nanoTime();
		double throughput = 0.0;
		long elapsedTime = end - start;
		double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
		throughput = (listOfQueries.size()*30) / executionTime;
		System.out.println("Throughput over 30 iterations: " + throughput + "  queries/sec");
		System.out.println("Mean response time of cluster system :" + 1 / throughput + "  seconds/query");
	}
}
