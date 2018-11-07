package edu.csulb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskIndexWriter;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

public class PositionalTermDocumentIndexer {

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Select mode:\n1. Build index on disk.\n2. Process queries from index on disk.");
		boolean indexBool = true;
		int indexMode = 0;
		while (indexBool) {
			try {
				indexMode = Integer.parseInt(br.readLine());
				indexBool = false;
				switch (indexMode) {
				case 1: {
					System.out.println("Please enter the path of the directory to index:");
					String directoryPath = br.readLine();
					buildIndex(directoryPath);
					break;
				}
				case 2: // process queries from an on disk index
				{
					processQueries(br);
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
		/*
		 * Trial to read positional index on disk DiskPositionalIndex di = new
		 * DiskPositionalIndex("src/index"); TokenProcessor processor = new
		 * AdvancedTokenProcessor(); List<String> termToSearch =
		 * processor.processToken("improve");
		 * di.getPositionalPostings(termToSearch.get(0));
		 */

		/*
		 * boolean choice = true; do {
		 * System.out.println("\nSpecial terms to search for operations:");
		 * System.out.println(":q - exit the program."); System.out.
		 * println(":stem 'token' - stems the token and prints the stemmed token.");
		 * System.out.println(":index directoryname - index the directoryname.");
		 * System.out.
		 * println(":vocab - print first 1000 terms in the vocabulary sorted alphabetically."
		 * );
		 * 
		 * System.out.print("\nPlease enter query to be searched: "); String query =
		 * br.readLine(); if (query.contains(":q") || query.contains(":stem") ||
		 * query.contains(":index") || query.contains(":vocab"))
		 * executeSpecialQuery(query, index); else if (query != null &&
		 * !query.isEmpty()) { BooleanQueryParser queryParser = new
		 * BooleanQueryParser(); QueryComponent userQuery =
		 * queryParser.parseQuery(query);
		 * 
		 * int count = 0; for (Posting p : userQuery.getPostings(index)) { count += 1;
		 * System.out.println("Document " +
		 * corpus.getDocument(p.getDocumentId()).getTitle() + "- " + " Document ID:" +
		 * corpus.getDocument(p.getDocumentId()).getId()); } System.out.println("Found "
		 * + count + " documents containing the terms:" + query); if (count > 0) {
		 * System.out.
		 * println("Would you like to view a document from the results?(Y/N):"); char c
		 * = br.readLine().charAt(0); if (c == 'Y' || c == 'y') { try {
		 * System.out.println("Please enter the document ID you'd like to view :"); int
		 * documentID = Integer.parseInt(br.readLine()); EnglishTokenStream docStream =
		 * new EnglishTokenStream( corpus.getDocument(documentID).getContent());
		 * Iterable<String> docTokens = docStream.getTokens(); for (String tokens :
		 * docTokens) System.out.print(tokens + " ");
		 * 
		 * } catch (Exception e) {
		 * 
		 * } } else continue; }
		 * 
		 * } else { System.out.println("Please enter a valid search term!"); } } while
		 * (choice);
		 */

	}

	private static void executeSpecialQuery(String query, Index index) {
		if (query.equals(":q")) {
			System.out.println("Exiting System...");
			System.exit(0);
		} else if (query.contains(":stem")) {
			String stemmedToken = "";
			try {
				stemmedToken = AdvancedTokenProcessor.stemTokenJava(query.split("\\s+")[1]);
				System.out.println("Stemmed token is :" + stemmedToken);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if (query.contains(":index")) {
			System.out.println("Indexing...");
			String directoryPath = Paths.get(query.split("\\s+")[1]).toString();
			DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directoryPath).toAbsolutePath(),
					".txt");
			index = indexCorpus(corpus);
		} else if (query.contains(":vocab")) {
			List<String> vocabulary = index.getVocabulary();
			System.out.println("First 1000 terms in vocabulary are as follows:");
			int vocabSize = vocabulary.size();
			if (vocabSize > 1000)
				vocabSize = 1000;
			for (int i = 0; i < vocabSize; i++) {
				System.out.println(vocabulary.get(i));
			}
		}
	}

	private static void executeSpecialQuery1(String query) {
		if (query.equals(":q")) {
			System.out.println("Exiting System...");
			System.exit(0);
		} else if (query.contains(":stem")) {
			String stemmedToken = "";
			try {
				stemmedToken = AdvancedTokenProcessor.stemTokenJava(query.split("\\s+")[1]);
				System.out.println("Stemmed token is :" + stemmedToken);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if (query.contains(":index")) {
			System.out.println("Indexing...");
			String directoryPath = Paths.get(query.split("\\s+")[1]).toString();
			buildIndex(directoryPath);
		} else if (query.contains(":vocab")) {
			DiskPositionalIndex di = null;
			try {
				di = new DiskPositionalIndex("src/index");
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
		}
	}

	private static Index indexCorpus(DocumentCorpus corpus) {
		TokenProcessor processor = new AdvancedTokenProcessor();
		Iterable<Document> documentList = corpus.getDocuments();
		PositionalInvertedIndex index = new PositionalInvertedIndex();

		
		for (Document doc : documentList) {
			HashMap<String, Integer> termFrequencyMap = new HashMap<String, Integer>(); // create termFrequencyMap which
																						// maps the term
			// frequencies for terms occurring in the document during indexing
			System.out.println("Indexing Document :" + doc.getTitle());
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
			DiskIndexWriter dw = new DiskIndexWriter("src/index", termFrequencyMap);
			try {
				dw.createDocWeightsFile(); // write it out to a file
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return index;
	}

	public static void buildIndex(String directoryPath) {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try {
			// directoryPath = Paths.get(input.nextLine()).toAbsolutePath().toString();
			// directoryPath = "G:\\My Research\\CECS 529\\Homework3\\Output";
		} catch (InvalidPathException e) {
			System.out.println("Invalid path! Please enter a valid directory path");
		}

		long start = System.nanoTime();
		DocumentCorpus corpus = buildCorpus(directoryPath);
		System.out.println(
				"\nFound " + corpus.getCorpusSize() + " documents in the directory. Indexing the documents...\n");
		// Index the corpus by calling indexCorpus() method
		Index index = indexCorpus(corpus); // create index
		// stop the timer
		long end = System.nanoTime();

		long elapsedTime = end - start;
		double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
		// Print out the time taken to load and index the documents
		System.out.println("Time taken to load documents and index corpus in seconds:" + executionTime);

		// clear write directory
		String writeDirectory = "src/index";

		// write index to disk
		DiskIndexWriter dw = new DiskIndexWriter(writeDirectory, index);
		dw.writeIndex();
		System.out.println("Index created on disk on path: " + writeDirectory);
	}

	public static void processQueries(BufferedReader br) {
		System.out.println("Select mode for performing query searches");
		boolean modeBool = true;
		int searchMode = 0;
		while (modeBool) {
			try {
				System.out.println("Please select search mode:\n1. Boolean query mode\n2. Ranked query mode");
				searchMode = Integer.parseInt(br.readLine());
				modeBool = false;
				switch (searchMode) {
				case 1: {
					boolean choice = true;
					do {
						System.out.println("\nSpecial terms to search for operations:");
						System.out.println(":q - exit the program.");
						System.out.println(":stem 'token' - stems the token and prints the stemmed token.");
						System.out.println(":index directoryname - index the directoryname.");
						System.out.println(":vocab - print first 1000 terms in the vocabulary sorted alphabetically.");

						System.out.print("\nPlease enter query to be searched: ");
						String query = "";
						try {
							query = br.readLine();
							if (query.contains(":q") || query.contains(":stem") || query.contains(":index")
									|| query.contains(":vocab")) {
								executeSpecialQuery1(query);
							} else if (query != null && !query.isEmpty()) {

							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					} while (choice);

					break;
				}
				case 2: {

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

		/*
		 * if (query.contains(":q") || query.contains(":stem") ||
		 * query.contains(":index") || query.contains(":vocab"))
		 * executeSpecialQuery(query, index); else if (query != null &&
		 * !query.isEmpty()) { BooleanQueryParser queryParser = new
		 * BooleanQueryParser(); QueryComponent userQuery =
		 * queryParser.parseQuery(query);
		 * 
		 * int count = 0; for (Posting p : userQuery.getPostings(index)) { count += 1;
		 * System.out.println("Document " +
		 * corpus.getDocument(p.getDocumentId()).getTitle() + "- " + " Document ID:" +
		 * corpus.getDocument(p.getDocumentId()).getId()); } System.out.println("Found "
		 * + count + " documents containing the terms:" + query); if (count > 0) {
		 * System.out.
		 * println("Would you like to view a document from the results?(Y/N):"); char c
		 * = br.readLine().charAt(0); if (c == 'Y' || c == 'y') { try {
		 * System.out.println("Please enter the document ID you'd like to view :"); int
		 * documentID = Integer.parseInt(br.readLine()); EnglishTokenStream docStream =
		 * new EnglishTokenStream( corpus.getDocument(documentID).getContent());
		 * Iterable<String> docTokens = docStream.getTokens(); for (String tokens :
		 * docTokens) System.out.print(tokens + " ");
		 * 
		 * } catch (Exception e) {
		 * 
		 * } } else continue; }
		 * 
		 * } else { System.out.println("Please enter a valid search term!"); } } while
		 * (choice);
		 */
	}

	public static DocumentCorpus buildCorpus(String corpusDirectory) {
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".txt");

		// Load files from directory and read
		// DocumentCorpus corpus =
		// DirectoryCorpus.loadJSONFileDirectory(Paths.get(directoryPath), ".json");
		System.out.println(
				"\nFound " + corpus.getCorpusSize() + " documents in the directory. Indexing the documents...\n");
		// Index the corpus by calling indexCorpus() method
		return corpus;
	}
}
