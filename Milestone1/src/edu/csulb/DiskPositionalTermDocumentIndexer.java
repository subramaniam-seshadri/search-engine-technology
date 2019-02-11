package edu.csulb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import cecs429.cluster.ClusterDoc;
import cecs429.cluster.ClusterPruning;
import cecs429.cluster.ClusterSearch;
import cecs429.common.CommonFunctions;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.ClusterIndexDiskWriter;
import cecs429.index.ClusterPositionalIndex;
import cecs429.index.DefaultWeightingScheme;
import cecs429.index.DiskIndexWriter;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.IWeightingScheme;
import cecs429.index.OkapiBM25WeightingScheme;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.index.TfIdfWeightingScheme;
import cecs429.index.WackyWeightingScheme;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.rankedretrieval.RankedResults;
import cecs429.rankedretrieval.RankedRetrieval;
import cecs429.statistics.Statistics;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

public class DiskPositionalTermDocumentIndexer {

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		DocumentCorpus corpus = null;
		String corpusDirectory = null;
		// Menu to build index or read from index
		boolean indexModeBool = true;
		int indexMode = 0;
		while (indexModeBool) {
			
				System.out.println(
						"Select mode:\n1. Build index on disk.\n2. Process queries from index on disk.\n3. Statistics \n4. Exit");
				indexMode = Integer.parseInt(br.readLine());

				switch (indexMode) {
				case 1: {
					System.out.println("Please enter the path of the corpus:");

					try {
					    //corpusDirectory = Paths.get(br.readLine()).toString();
						corpusDirectory = "G:\\My Research\\CECS 529\\Homework3\\relevance_cranfield";
						corpus = buildCorpus(corpusDirectory); // build corpus for documents in corpusDirectory
					} catch (Exception e) {
						System.out.println("Please enter a valid directory path");
					}

					System.out.println("Building index for corpus in : " + corpusDirectory + " path");
					buildIndex(corpusDirectory, corpus, br); // build index on the corpus
					break;
				}
				// process queries from an on disk index
				case 2: {
					System.out.println("Please enter the path of the corpus:");

					try {
						//corpusDirectory = Paths.get(br.readLine()).toString();
						corpusDirectory = "G:\\My Research\\CECS 529\\Homework3\\relevance_cranfield";
						corpus = buildCorpus(corpusDirectory); // build corpus for documents in corpusDirectory
					} catch (Exception e) {
						System.out.println("Please enter a valid directory path");
					}
					System.out.println("Please enter the path of the index on disk:");
					 String indexPath = "src/index";
					//String indexPath = Paths.get(br.readLine()).toString();
					processQueries(br, indexPath, corpus);
					break;
				}
				case 3: {
					System.out.println("Please enter the path of the corpus:");
					try {
						//corpusDirectory = Paths.get(br.readLine()).toString();
						corpusDirectory = "G:\\My Research\\CECS 529\\Homework3\\relevance_cranfield";
						corpus = buildCorpus(corpusDirectory); // build corpus for documents in corpusDirectory
					} catch (Exception e) {
						System.out.println("Please enter a valid directory path");
					}
					System.out.println("Please enter the path of the index on disk:");
					 String indexPath = "src/index";
					//String indexPath = Paths.get(br.readLine()).toString();
					System.out.println("Displaying Statistics for the search engine system.");
					Statistics stat = new Statistics();
					
					//stat.getRankedStatistics(indexPath, corpus, corpusDirectory);
					//stat.getClusterStatistics(indexPath, corpus, corpusDirectory);
					stat.getRankedThroughPut(indexPath, corpus, corpusDirectory);
					stat.getClusterThroughPut(indexPath, corpus, corpusDirectory);
					
					break;
				}

				case 4: {
					System.out.println("Exiting system...");
					indexModeBool = false;
					break;
				}

				default:
					System.out.println("Please enter a valid choice");
					break;
				}
		}
	}

	/**
	 * Method which handles special query operations entered by the user.
	 * @param query - The query entered by the user.
	 * @param corpus - The corpus object which represents the collection of documents in a directory.
	 * @param br - The buffered reader object to get further inputs from the user.
	 * @param indexPath - The path of the index on disk.
	 * @return
	 */
	private static boolean executeSpecialQuery(String query, DocumentCorpus corpus, BufferedReader br,
			String indexPath) {
		boolean booleanMode = true;
		if (query.equals(":q")) {
			System.out.println("Exiting System...");
			System.exit(0);
			booleanMode = false;
			return booleanMode;
		} else if (query.contains(":stem")) {
			String stemmedToken = "";
			try {
				stemmedToken = AdvancedTokenProcessor.stemTokenJava(query.split("\\s+")[1]);
				System.out.println("Stemmed token is :" + stemmedToken);
				return booleanMode;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if (query.contains(":index")) {
			System.out.println("Indexing...");
			String directoryPath = Paths.get(query.split("\\s+")[1]).toString();
			buildIndex(directoryPath, corpus, br);
			return booleanMode;
		} else if (query.contains(":vocab")) {
			DiskPositionalIndex di = null;
			try {
				di = new DiskPositionalIndex(indexPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			List<String> vocabulary = di.getVocabulary();
			System.out.println("First 1000 terms in vocabulary are as follows:");
			int vocabSize = vocabulary.size();
			if (vocabSize > 1000)
				vocabSize = 1000;
			for (int i = 0; i < vocabSize; i++) {
				System.out.println(vocabulary.get(i));
			}
			return booleanMode;
		} else if (query.contains(":exitMode")) {
			booleanMode = false;
			return booleanMode;
		}
		return booleanMode;
	}

	/**
	 * This method is used to index the corpus and write it to disk.
	 * @param indexDirectory - The directory where the index will be stored on disk.
	 * @param corpus - The corpus object which represents the collection of documents on disk.
	 */
	private static void indexCorpus(String indexDirectory, DocumentCorpus corpus) {
		TokenProcessor processor = null;
		try {
			processor = new AdvancedTokenProcessor();
		} catch (Throwable e) {
		}
		Iterable<Document> documentList = corpus.getDocuments();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		PositionalInvertedIndex leaderIndex = new PositionalInvertedIndex();
		List<Long> avgDocLength = new ArrayList<Long>();
		List<Long> avgDocLengthLeader = new ArrayList<Long>();
		DiskIndexWriter dw = new DiskIndexWriter(indexDirectory);
		ClusterIndexDiskWriter cw = new ClusterIndexDiskWriter(indexDirectory);

		// square root of number of docs in corpus becomes number of leaders in the
		// cluster
		int numberOfLeaders = (int) Math.sqrt(corpus.getCorpusSize());
		Random randomNum = new Random();
		HashSet<Integer> setOfLeaders = new HashSet<Integer>();

		for (int i = 0; i < numberOfLeaders; i++) {
			setOfLeaders.add(randomNum.nextInt(corpus.getCorpusSize()));
		}
		System.out.println("Number of Leaders:" + numberOfLeaders);
		
		// for every document call createDocWeights method in DiskIndexWriter
		CommonFunctions cf = new CommonFunctions();
		ClusterDoc cd = new ClusterDoc();
		HashMap<Integer, ClusterDoc> leaderDocumentsMap = new HashMap<Integer, ClusterDoc>();
		HashMap<Integer, ClusterDoc> otherDocumentsMap = new HashMap<Integer, ClusterDoc>();
		for (Document doc : documentList) {
			//System.out.println("Indexing Document :" + doc.getTitle() + " DocID: " + doc.getId());
			
			HashMap<String, Integer> termFrequencyMap = createTermFrequencyMap(processor, index, doc);
			
			if (!setOfLeaders.contains(doc.getId())) { // document is not a leader
				cd = cf.getDocValues(termFrequencyMap);
				cd.setDocSize(doc.getDocSize());
				otherDocumentsMap.put(doc.getId(), cd);
			} else { // document is a leader
				cd = cf.getDocValues(termFrequencyMap);
				cd.setDocSize(doc.getDocSize());
				cd.setDocId(doc.getId());
				leaderDocumentsMap.put(doc.getId(), cd);
				cw.createLeaderDocWeightsFile(doc, cd);
				avgDocLengthLeader.add(cd.getDocLength());
				HashMap<String, Integer> leaderTermFrequencyMap = createTermFrequencyMap(processor, leaderIndex, doc);
			}
			
			
			dw.createDocWeightsFile(doc, cd);
			avgDocLength.add(cd.getDocLength());
		}
		// write docLengthA - avg number of tokens in all documents in the corpus
		dw.writeAvgDocLength(avgDocLength);
		
		ClusterPruning cp = new ClusterPruning();
		//System.out.println("Leader Map Size:" + leaderDocumentsMap.size());
		//System.out.println("Other Map Size:" + otherDocumentsMap.size());
		cp.formClusters(leaderDocumentsMap, otherDocumentsMap, indexDirectory);
		
		cw.setIndex(leaderIndex);
		cw.writeAvgDocLength(avgDocLengthLeader);
		//cw.readDocValues(indexDirectory, leaderDocumentsMap.size());
		cw.writeLeaderIndex();
		
		dw.setIndex(index);
		dw.writeIndex();	
	}
	

	/** This method is used to create the term-frequecy mapping for each of the documents in the corpus.
	 * @param processor - The token processor used to process each of the tokens from the document.
	 * @param index - The index used to represent the documents.
	 * @param doc -  The document for which the term-frequency mapping is to be done.
	 * @return - Returns the term-frequency mapping for the document.
	 */
	private static HashMap<String, Integer> createTermFrequencyMap(TokenProcessor processor,
			PositionalInvertedIndex index, Document doc) {
		HashMap<String, Integer> termFrequencyMap = new HashMap<String, Integer>(); // create termFrequencyMap which
																					// maps the term
		// frequencies for terms occurring in the document during indexing
		EnglishTokenStream docStream = new EnglishTokenStream(doc.getContent());
		Iterable<String> docTokens = docStream.getTokens();
		int i = 0;
		for (String tokens : docTokens) {
			i += 1;
			List<String> processedTokens = processor.processToken(tokens);
			for (String processedToken : processedTokens) {
				if (termFrequencyMap.containsKey(processedToken)) {
					int termFrequency = termFrequencyMap.get(processedToken);
					termFrequency += 1;
					termFrequencyMap.put(processedToken, termFrequency);
				} else {
					termFrequencyMap.put(processedToken, 1);
				}
				index.addTerm(processedToken, doc.getId(), i);
			}
		}
		return termFrequencyMap;
	}

	
	/**
	 * The method used to build the index, calls the indexCorpus method internally.
	 * @param directoryPath - The directory where the index is to be stored. 
	 * @param corpus - The corpus object which represents the collection of documents in a directory.
	 * @param br - The buffered reader object to get further inputs from the user.
	 */
	public static void buildIndex(String directoryPath, DocumentCorpus corpus, BufferedReader br) {
		//DiskPositionalIndex dp = null;
		long start = System.nanoTime();
		System.out.println(
				"\nFound " + corpus.getCorpusSize() + " documents in the directory. Indexing the documents...\n");
		System.out.println("Enter the path where you want to store the index on disk:");
		String writeDirectory = "";
		try {
			writeDirectory = Paths.get(br.readLine()).toString();
			writeDirectory = "src/index";
		} catch (IOException e1) {
			e1.printStackTrace();
		} 
				indexCorpus(writeDirectory, corpus); // create index
		
		long end = System.nanoTime();
		// stop the timer
		System.out.println("Index created on disk on path: " + writeDirectory);
		long elapsedTime = end - start;
		double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
		// Print out the time taken to load and index the documents
		System.out.println("Time taken to load documents and index corpus in seconds:" + executionTime);
	}

	public static void processQueries(BufferedReader br, String indexPath, DocumentCorpus corpus) {
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
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		boolean modeBool = true;
		int searchMode = 0;
		while (modeBool) {
			try {
				System.out.println(
						"\nPlease select search mode:\n1. Boolean query mode\n2. Ranked query mode\n3. Cluster Search\n4. Exit Search mode");
				searchMode = Integer.parseInt(br.readLine());
				modeBool = true;
				switch (searchMode) {
				case 1: {
					boolean choice = true;
					do {
						System.out.println("\nSpecial terms to search for operations:");
						System.out.println(":q - exit the program.");
						System.out.println(":stem 'token' - stems the token and prints the stemmed token.");
						System.out.println(":index directoryname - index the directoryname.");
						System.out.println(":vocab - print first 1000 terms in the vocabulary sorted alphabetically.");
						System.out.println(":exitMode");
						System.out.print("\nPlease enter query to be searched: ");
						String query = "";
						try {
							query = br.readLine();
							if (query.contains(":q") || query.contains(":stem") || query.contains(":index")
									|| query.contains(":vocab") || query.contains(":exitMode")) {
								choice = executeSpecialQuery(query, corpus, br, indexPath);
							} else if (query != null && !query.isEmpty()) {
								BooleanQueryParser queryParser = new BooleanQueryParser();
								QueryComponent userQuery = queryParser.parseQuery(query);
								int count = 0;

								for (Posting p : userQuery.getPostings(dp)) {
									count += 1;
									System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle()
											+ "- " + " Document ID:" + corpus.getDocument(p.getDocumentId()).getId());
								}
								System.out.println("Found " + count + " documents containing the terms:" + query);
								if (count > 0) {
									System.out.println("Would you like to view a document from the results?(Y/N):");
									char c = br.readLine().charAt(0);
									if (c == 'Y' || c == 'y') {
										try {
											System.out.println("Please enter the document ID you'd like to view :");
											int documentID = Integer.parseInt(br.readLine());
											EnglishTokenStream docStream = new EnglishTokenStream(
													corpus.getDocument(documentID).getContent());
											Iterable<String> docTokens = docStream.getTokens();
											for (String tokens : docTokens)
												System.out.print(tokens + " ");

										} catch (Exception e) {

										}
									} else
										continue;
								}

							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					} while (choice);

					break;
				}
				case 2: {
					performRankedRetrieval(indexPath, dp, br, processor, corpus);
					break;
				}
				case 3:{
					IWeightingScheme weightingScheme = new DefaultWeightingScheme(indexPath, dp);
					System.out.print("\nPlease enter query to be searched: ");
					String query = "";
					try {
						query = br.readLine();
						Integer numberOfDocs = dp.getDocIDWeightsMap().size();
						ClusterPruning cp = new ClusterPruning();
						HashMap<Integer, HashSet<Integer>> clusterMap = cp.readClusters(indexPath);
						System.out.println("Read Cluster size:" + clusterMap.size());
						cpi.readDocValues(clusterMap.size());
						Integer numberOfDocs1 = clusterMap.size();
						ClusterSearch cs = new ClusterSearch(weightingScheme, cpi, clusterMap, numberOfDocs1);
						cs.setQuery(query);
						HashMap<Integer, HashSet<Integer>> resultMap = cs.getClusterDocuments(processor);
						if(resultMap == null) {
							// no results found from cluster search
							performRankedRetrieval(indexPath, dp, br, processor, corpus);
						}
						else {
							List<RankedResults> resultList = cs.clusterRankedRetrieval(query, processor, dp, numberOfDocs, resultMap, 10);
							for (RankedResults r : resultList) {
								System.out.println("Document ::" + corpus.getDocument(r.getDocumentID()).getTitle() + "- "
										+ " Document ID: " + corpus.getDocument(r.getDocumentID()).getId() + " Score :  "
										+ r.getRetrievalScore());
							}
						}
							
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
					}

				case 4: {
					modeBool = false;
					break;
				}
				default:
					System.out.println("Please enter a valid choice");
					break;
				}
			} catch (Exception e) {
				System.out.println("Please enter a valid number");
			}
		}
	}

	/**
	 * This method is used to build a corpus, given a corpus directory path as
	 * string
	 * 
	 * @param corpusDirectory - String path of the directory where corpus is present
	 *                        on disk.
	 * @return - corpus formed over all the documents in the given directory
	 */
	public static DocumentCorpus buildCorpus(String corpusDirectory) {
	    // DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(corpusDirectory).toAbsolutePath(), ".txt");

		// Load files from directory and read
		DocumentCorpus corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get(corpusDirectory), ".json");
		System.out.println("\nFound " + corpus.getCorpusSize() + " documents in the directory :" + corpusDirectory);
		System.out.println("Building corpus...");
		return corpus;
	}
	
	
	/**
	 * This method handles all the operations for performing ranked retrieval on user search queries.
	 * @param indexPath - The path where the index is stored on the disk.
	 * @param dp - The disk positional index object used to read values from the disk.
	 * @param br - The buffered reader object used for further input from user if required.
	 * @param processor - The token processor object used to process the query.
	 * @param corpus - The corpus object which represents the collection of documents in a directory.
	 */
	public static void performRankedRetrieval(String indexPath, DiskPositionalIndex dp, BufferedReader br, TokenProcessor processor, DocumentCorpus corpus) {
		IWeightingScheme weightingScheme = null;
		System.out.println("\nPlease select weighting scheme");
		System.out.println("1. Default Weighting Scheme");
		System.out.println("2. Tf-Idf Weighting Scheme");
		System.out.println("3. OKAPI BM 25 Weighting Scheme");
		System.out.println("4. Wacky Weighting Scheme");
		int weightingSchemeOption = 0;
		try {
			weightingSchemeOption = Integer.parseInt(br.readLine());
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		switch (weightingSchemeOption) {
		case 1: {
			weightingScheme = new DefaultWeightingScheme(indexPath, dp);
			break;
		}
		case 2: {
			weightingScheme = new TfIdfWeightingScheme(indexPath, dp);
			break;
		}
		case 3: {
			weightingScheme = new OkapiBM25WeightingScheme(indexPath, dp);
			break;
		}
		case 4: {
			weightingScheme = new WackyWeightingScheme(indexPath, dp);
			break;
		}
		default: {
			System.out.println("No weighting scheme selected. Default weighting scheme will be used.");
			weightingScheme = new DefaultWeightingScheme(indexPath, dp);
			break;
		}
		}

		System.out.print("\nPlease enter query to be searched: ");
		String query = "";
		try {
			query = br.readLine();
			Integer numberOfDocs = dp.getDocIDWeightsMap().size();
			RankedRetrieval rr = new RankedRetrieval(weightingScheme, numberOfDocs, dp);
			rr.setQuery(query);
			List<RankedResults> resultList = rr.getRankedDocuments(processor, 10);
			for (RankedResults r : resultList) {
				System.out.println("Document ::" + corpus.getDocument(r.getDocumentID()).getTitle() + "- "
						+ " Document ID: " + corpus.getDocument(r.getDocumentID()).getId() + " Score :  "
						+ r.getRetrievalScore());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
