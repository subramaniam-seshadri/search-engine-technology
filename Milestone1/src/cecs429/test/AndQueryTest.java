package cecs429.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
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

class AndQueryTest {
	private BooleanQueryParser queryParser = new BooleanQueryParser();
	private static Index index;
	private static DocumentCorpus corpus;
	private static String andQueryPositive = "subbu ronit";
	private static String andQueryNegative = "sean ronit";

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get("src/testdirectory"), ".json");
		index = indexCorpus(corpus);
	}

	@Test
	void testAndQueryPositive() {
		// search for documents that contain subbu & ronit
		QueryComponent userQuery = queryParser.parseQuery(andQueryPositive);
		int expectedResult = -1;
		for (Posting p : userQuery.getPostings(index)) {
			expectedResult = p.getDocumentId();
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getId());
		}
		// subbu and ronit appear in document ID 2, hence this should pass
		assertEquals(2, expectedResult);
	}
	
	@Test
	void testAndQueryNegative() {
		// search for documents that contain subbu & ronit
		QueryComponent userQuery = queryParser.parseQuery(andQueryNegative);
		int expectedResult = -1;
		for (Posting p : userQuery.getPostings(index)) {
			expectedResult = p.getDocumentId();
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getId());
		}
		// sean and ronit don't appear in any of the documents, hence this should fail
		assertNotEquals(expectedResult, 2);
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
