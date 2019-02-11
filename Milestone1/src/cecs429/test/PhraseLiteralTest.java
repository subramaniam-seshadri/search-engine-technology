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

class PhraseLiteralTest {
	private static String phraseQueryPositive = "\"subbu and ronit\"";
	private static String phraseQueryNegative = "\"are partner\"";
	private BooleanQueryParser queryParser = new BooleanQueryParser();
	private static Index index;
	private static DocumentCorpus corpus;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get("src/testdirectory"), ".json");
		index = indexCorpus(corpus);
	}

	@Test
	void testPhraseQueryPositive() {
		// search for documents that contain phrase defined in phraseQueryPositive

		QueryComponent userQuery = queryParser.parseQuery(phraseQueryPositive);
		int i = 0;
		try {
			for (Posting p : userQuery.getPostings(index)) {
				++i;
				System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
			}

		} catch (Exception e) {
		}
		;
		// phraseQueryPositive appears exactly in document3, hence this should pass
		assertEquals(i, 1);
	}

	@Test
	void testPhraseQueryNegative() {
		// search for documents that contain phrase defined in phraseQueryNegative
		QueryComponent userQuery = queryParser.parseQuery(phraseQueryNegative);
		int i = 0;
		try {
			for (Posting p : userQuery.getPostings(index)) {
				++i;
				System.out.println("\nResult Document/s ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
			}

		} catch (Exception e) {
		}
		;
		// phraseQueryNegative doesn't appear in any document, hence this should pass
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
