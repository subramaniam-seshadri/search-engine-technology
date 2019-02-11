package cecs429.test;

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

class NearKTest {

	private BooleanQueryParser queryParser = new BooleanQueryParser();
	private static Index index;
	private static DocumentCorpus corpus;
	private static String nearKPositive = "[ronit NEAR/4 partners]";
	private static String nearKNegative = "[partners NEAR/4 ronit]";

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get("src/testdirectory"), ".json");
		index = indexCorpus(corpus);
	}

	@Test
	void testNearKPositive() {
		// search for documents that contain ronit "near" partners

		QueryComponent userQuery = queryParser.parseQuery(nearKPositive);
		int i = 0;
		try {
			for (Posting p : userQuery.getPostings(index)) {
				++i;
				System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
			}
		} catch (Exception e) {
		}
		// ronit and partners appear in one of the documents near each other, hence this
		// should pass
		assertEquals(i, 1);
	}

	@Test
	void testNearKNegative() {
		// search for documents that contain subbu & ronit

		QueryComponent userQuery = queryParser.parseQuery(nearKNegative);
		int i = 0;
		try {
			for (Posting p : userQuery.getPostings(index)) {
				++i;
				System.out.println("\nResult Document/s ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
			}

		} catch (Exception e) {
		}
		// partners and ronit don't appear in any of the documents, hence this should
		// pass when equated to 0
		assertEquals(i, 0);
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
