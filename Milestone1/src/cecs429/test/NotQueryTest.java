package cecs429.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

class NotQueryTest {
	
	private BooleanQueryParser queryParser = new BooleanQueryParser();
	private static Index index;
	private static DocumentCorpus corpus;
	private static String notQueryPositive = "subbu -ronit";
	private static String notQueryNegative = "ronit -sean";

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get("src/testdirectory"), ".json");
		index = indexCorpus(corpus);
	}
	
	@Test
	void testNotQueryPositive() {
		// search for documents that contain subbu but not ronit
		QueryComponent userQuery = queryParser.parseQuery(notQueryPositive);
		List<Integer> expectedResults = new ArrayList<Integer>();
		for (Posting p : userQuery.getPostings(index)) {
			expectedResults.add(p.getDocumentId());
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getId());
		}
		// only subbu appears in document ID 0, hence this should pass
		List<Integer> actualResults = new ArrayList<Integer>();
		actualResults.add(0);
		assertEquals(actualResults, expectedResults);
	}
	
	@Test
	void testNotQueryNegative() {
		// search for documents that contain ronit but not sean
		QueryComponent userQuery = queryParser.parseQuery(notQueryNegative);
		List<Integer> expectedResults = new ArrayList<Integer>();
		for (Posting p : userQuery.getPostings(index)) {
			expectedResults.add(p.getDocumentId());
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getId());
		}
		// sean doesn't appear in any of the documents, but ronit does, in 2 documents. Hence this should pass
		List<Integer> actualResults = new ArrayList<Integer>();
		actualResults.add(1);
		actualResults.add(2);
		assertEquals(actualResults, expectedResults);
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
