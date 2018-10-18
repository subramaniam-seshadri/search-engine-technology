/*package edu.csulb;

import java.nio.file.Paths;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.InvertedIndex;
import cecs429.index.Posting;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

public class InvertedTermDocumentIndexer{

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".txt");
		Index index = indexCorpus(corpus);
		
		Scanner input = new Scanner(System.in);
		int choice;
		do {
			System.out.print("1. Search\n2. Exit\nEnter your choice: ");
			choice = input.nextInt();
			if(choice==1)
			{
				System.out.print("Please enter word to be searched: ");
				TokenProcessor processor = new BasicTokenProcessor();
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
		TokenProcessor processor = new BasicTokenProcessor();
		Iterable<Document> documentList = corpus.getDocuments();
		//TreeMap<String, List<Integer>> vocabulary = new TreeMap<String, List<Integer>>();
		InvertedIndex index = new InvertedIndex();
		for (Document doc : documentList) {
			EnglishTokenStream docStream = new EnglishTokenStream(doc.getContent());

			Iterable<String> docTokens = docStream.getTokens();
			for (String tokens : docTokens) {
				index.addTerm(processor.processToken(tokens), doc.getId());
			}
		}
		return index;
	}

}*/
