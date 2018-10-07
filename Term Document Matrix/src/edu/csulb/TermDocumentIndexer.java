package edu.csulb;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.index.TermDocumentIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.TokenProcessor;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class TermDocumentIndexer {
	/**
	 Indexes all .txt files in the specified directory. First builds a dictionary
	 of all terms in those files, then builds a boolean term-document matrix as
	 the index.
	 
	 @param directory the Path of the directory to index.
	 */
	public static Index indexDirectory(final Path directory) {
		// will need a data structure to store all the terms in the document
		// HashSet: a hashtable structure with constant-time insertion; does not
		// allow duplicate entries; stores entries in unsorted order.
		final HashSet<String> dictionary = new HashSet<>();
		final List<String> files = new ArrayList<>();
		final TokenProcessor processor = new BasicTokenProcessor();
		
		try {
			// go through each .txt file in the working directory
			System.out.println("Indexing " + directory.toString());
			
			
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				
				public FileVisitResult preVisitDirectory(Path dir,
				                                         BasicFileAttributes attrs) {
					// make sure we only process the current working directory
					if (directory.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}
				
				public FileVisitResult visitFile(Path file,
				                                 BasicFileAttributes attrs) {
					// only process .txt files
					if (file.toString().endsWith(".txt")) {
						buildDictionary(file, dictionary, processor);
						files.add(file.getFileName().toString());
					}
					return FileVisitResult.CONTINUE;
				}
				
				// don't throw exceptions if files are locked/other errors occur
				public FileVisitResult visitFileFailed(Path file,
				                                       IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
			
			TermDocumentIndex index = new TermDocumentIndex(dictionary, files.size());
			
			// Walk back through the files -- a second time!!
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				public FileVisitResult preVisitDirectory(Path dir,
				                                         BasicFileAttributes attrs) {
					if (directory.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}
				
				public FileVisitResult visitFile(Path file,
				                                 BasicFileAttributes attrs) {
					
					if (file.toString().endsWith(".txt")) {
						// add entries to the index matrix for this file
						System.out.println("Indexing file " + file.getFileName().toString());
						indexFile(file, files, index, processor);
					}
					return FileVisitResult.CONTINUE;
				}
				
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
				
			});
			return index;
		}
		catch (Exception ex) {
			System.out.println("ERROR: " + ex.toString());
		}
		return null;
	}
	
	// reads the file given by Path; adds each term from file to the dictionary
	private static void buildDictionary(Path file, HashSet<String> dictionary, TokenProcessor processor) {
		try {
			try (Scanner scan = new Scanner(file)) {
				while (scan.hasNext()) {
					// read one word at a time; process and add it to dictionary.
					
					String word = processor.processToken(scan.next());
					if (word.length() > 0) {
						dictionary.add(word);
					}
				}
			}
		}
		catch (IOException ex) {
		}
		
	}
	
	// Reads tokens from the given file and inserts its terms into the given index.
	private static void indexFile(Path file, List<String> fileNames, TermDocumentIndex index, TokenProcessor processor) {
		try {
			int fileIndex = fileNames.indexOf(file.getFileName().toString());
			// read one word at a time; process and update the matrix.
			try (Scanner scan = new Scanner(file)) {
				while (scan.hasNext()) {
					String word = processor.processToken(scan.next());
					if (word.length() > 0) {
						index.addTerm(word, fileIndex);
					}
				}
			}
		}
		catch (IOException ex) {
		}
		
	}
	
	public static void main(String[] args) {
		// Index the current working directory.
		final Path currentWorkingPath = Paths.get("").toAbsolutePath();
		Index index = indexDirectory(currentWorkingPath);
		
		// We aren't ready to use a full query parser; for now, we'll only support single-term queries.
		Scanner input = new Scanner(System.in);
		System.out.print("Please enter word to be searched: ");
		TokenProcessor processor = new BasicTokenProcessor();
		String query = processor.processToken(input.next());
		//String query = "whale"; // hard-coded search for "whale"
		for (Posting p : index.getPostings(query)) {
			System.out.println("Document ID " + p.getDocumentId());
		}
		
		// TODO: fix this application so the user is asked for a term to search.
	}
}
