package cecs429.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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

class OrQueryTest {

	private BooleanQueryParser queryParser = new BooleanQueryParser();
	private static Index index;
	private static DocumentCorpus corpus;
	private static String orQueryPositive = "subbu + ronit";
	private static String orQueryNegative = "sean + john";

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get("src/testdirectory"), ".json");
		index = indexCorpus(corpus);
	}
	
	@Test
	void testOrQueryPositive() {
		// search for documents that contain subbu & ronit
		QueryComponent userQuery = queryParser.parseQuery(orQueryPositive);
		List<Integer> expectedResults = new ArrayList<Integer>();
		for (Posting p : userQuery.getPostings(index)) {
			expectedResults.add(p.getDocumentId());
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getId());
		}
		// subbu or ronit appear in 3 documents, hence this should pass
		List<Integer> actualResults = new ArrayList<Integer>();
		actualResults.add(0);
		actualResults.add(1);
		actualResults.add(2);
		assertEquals(actualResults, expectedResults);
	}
	
	@Test
	void testOrQueryNegative() {
		// search for documents that contain subbu & ronit
		QueryComponent userQuery = queryParser.parseQuery(orQueryNegative);
		List<Integer> expectedResults = new ArrayList<Integer>();
		for (Posting p : userQuery.getPostings(index)) {
			expectedResults.add(p.getDocumentId());
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getId());
		}
		// sean and ronit don't appear in any of the documents, hence this should pass
		assertEquals(Collections.EMPTY_LIST, expectedResults);
	}	

	private static Index indexCorpus(DocumentCorpus corpus) {
		AdvancedTokenProcessor tokenProcesser = null;
		try {
			tokenProcesser = new AdvancedTokenProcessor();
		} catch (Throwable e) {
		}
		Iterable<Document> documentList = corpus.getDocuments();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		for (Document doc : documentList) {
			System.out.println("Indexing Document :" + doc.getTitle());
			EnglishTokenStream docStream = new EnglishTokenStream(doc.getContent());

			Iterable<String> docTokens = docStream.getTokens();

			int i = 0;
			for (String tokens : docTokens) {
				i += 1;
				List<String> processedTokens = tokenProcesser.processToken(tokens);
				for (String processedToken : processedTokens) {
					index.addTerm(processedToken, doc.getId(), i);
				}
			}
		}
		return index;
	}
}
