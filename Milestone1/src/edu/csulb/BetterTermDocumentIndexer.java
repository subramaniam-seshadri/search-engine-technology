/*package edu.csulb;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.index.TermDocumentIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;

public class BetterTermDocumentIndexer {
	public static void main(String[] args) {
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".txt");
		Index index = indexCorpus(corpus);
		// We aren't ready to use a full query parser; for now, we'll only support
		// single-term queries.
		// String query = "whale"; // hard-coded search for "whale"
		Scanner input = new Scanner(System.in);
		int choice;
		do {
			System.out.print("1. Search\n2. Exit\nEnter your choice: ");
			choice = input.nextInt();
			if(choice==1)
			{
				System.out.print("Please enter word to be searched: ");
				BasicTokenProcessor processor = new BasicTokenProcessor();
				String query = processor.processToken(input.next());
				for (Posting p : index.getPostings(query)) {
					System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
				}
			}
		
		}
		while(choice == 1);
		System.out.println("Exiting Search..");
		
	}

	private static Index indexCorpus(DocumentCorpus corpus) {
		HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();

		// First, build the vocabulary hash set.
		// Get all the documents in the corpus by calling GetDocuments().
		Iterable<Document> documentList = corpus.getDocuments();

		// Iterate through the documents, and:
		// Tokenize the document's content by constructing an EnglishTokenStream around
		// the document's content.
		for (Document doc : documentList) {
			EnglishTokenStream docStream = new EnglishTokenStream(doc.getContent());

			Iterable<String> docTokens = docStream.getTokens();
			for (String tokens : docTokens) {
				vocabulary.add(processor.processToken(tokens));
			}
		}

		// Iterate through the tokens in the document, processing them using a
		// BasicTokenProcessor,
		// and adding them to the HashSet vocabulary.
		
		// Constuct a TermDocumentMatrix once you know the size of the vocabulary.
		TermDocumentIndex index = new TermDocumentIndex(vocabulary, corpus.getCorpusSize());

		// THEN, do the loop again! But instead of inserting into the HashSet, add terms
		// to the index with addPosting.
		for (Document doc : documentList) {
			EnglishTokenStream docStream = new EnglishTokenStream(doc.getContent());

			Iterable<String> docTokens = docStream.getTokens();
			for (String tokens : docTokens) {
				tokens = processor.processToken(tokens);
				if(tokens.length()>0)
				index.addTerm(tokens, doc.getId());
			}
		}

		return index;
	}
}*/
