package Indexing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import Classes.Path;

/**
 * This is a simple corpus reader
 * Class for Assignment 2 of INFSCI2140, 2016 Spring.
 */
public class PreProcessedCorpusReader {

	private BufferedReader reader;
    /*
     This constructor should open the pre-processed corpus file, Path.ResultHM1 + type
     which was generated in assignment 1
     remember to close the file that you opened, when you do not use it any more
     */
	public PreProcessedCorpusReader(String type) throws IOException {
		reader = new BufferedReader (new FileReader(Path.ResultHM1 + type));
	}
	
	/*
     read a line for docNo, then read another line for the word list
     put them in a map, and return it
    */
	public Map<String, String> nextDocument() throws IOException {
		Map<String, String> document = new HashMap<String, String>();
		
		String docno = "";
		String content = "";
		String next = reader.readLine();
		while (next != null) {
			docno = next;
			next = reader.readLine(); // read next line.
			content = next;
			document.put(docno, content);
			return document;
		}
		reader.close();
		return null;
	}
}
