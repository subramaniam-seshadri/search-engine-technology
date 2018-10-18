package edu.csulb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskIndexWriter;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

public class PositionalTermDocumentIndexer {

	public static void main(String[] args) throws IOException {

		System.out.println("Please enter the path of the directory to index:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String directoryPath = "";
		try {
			// directoryPath = Paths.get(input.nextLine()).toAbsolutePath().toString();
			//directoryPath = "G:\\My Research\\CECS 529\\Homework3\\Output";
		} catch (InvalidPathException e) {
			System.out.println("Invalid path! Please enter a valid directory path");
		}

		// start timer
		long start = System.nanoTime();
		 DocumentCorpus corpus =
		 DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".txt");

		// Load files from directory and read
		//DocumentCorpus corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get(directoryPath), ".json");
		System.out.println(
				"\nFound " + corpus.getCorpusSize() + " documents in the directory. Indexing the documents...\n");
		// Index the corpus by calling indexCorpus() method
		Index index = indexCorpus(corpus);
		// stop the timer
		long end = System.nanoTime();

		long elapsedTime = end - start;
		double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
		// Print out the time taken to load and index the documents
		System.out.println("Time taken to load documents and index corpus in seconds:" + executionTime);
		DiskIndexWriter dw = new DiskIndexWriter();
		dw.writeIndex(index, "src/index");
		/*boolean choice = true;
		do {
			System.out.println("\nSpecial terms to search for operations:");
			System.out.println(":q - exit the program.");
			System.out.println(":stem 'token' - stems the token and prints the stemmed token.");
			System.out.println(":index directoryname - index the directoryname.");
			System.out.println(":vocab - print first 1000 terms in the vocabulary sorted alphabetically.");

			System.out.print("\nPlease enter query to be searched: ");
			String query = br.readLine();
			if (query.contains(":q") || query.contains(":stem") || query.contains(":index") || query.contains(":vocab"))
				executeSpecialQuery(query, index);
			else if (query != null && !query.isEmpty()) {
				BooleanQueryParser queryParser = new BooleanQueryParser();
				QueryComponent userQuery = queryParser.parseQuery(query);

				int count = 0;
				for (Posting p : userQuery.getPostings(index)) {
					count += 1;
					System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle() + "- "
							+ " Document ID:" + corpus.getDocument(p.getDocumentId()).getId());
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

			} else {
				System.out.println("Please enter a valid search term!");
			}
		} while (choice);*/

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

	private static Index indexCorpus(DocumentCorpus corpus) {
		TokenProcessor processor = new AdvancedTokenProcessor();
		Iterable<Document> documentList = corpus.getDocuments();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		for (Document doc : documentList) {
			System.out.println("Indexing Document :" + doc.getTitle());
			EnglishTokenStream docStream = new EnglishTokenStream(doc.getContent());

			Iterable<String> docTokens = docStream.getTokens();

			int i = 0;
			for (String tokens : docTokens) {
				i += 1;
				List<String> processedTokens = processor.processToken(tokens);
				for (String processedToken : processedTokens) {
					index.addTerm(processedToken, doc.getId(), i);
				}
			}
		}
		return index;
	}
}
