package Indexing;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import Classes.Path;
/**
 * Please construct your code efficiently, otherwise, your memory cannot hold the whole corpus...
 *
 * Class for Assignment 2 of INFSCI2140, 2016 Spring.
 */
public class MyIndexWriter {
	
	private FileWriter writer;    // initialize the writer to output index files
	private Map<String, HashMap> index = new HashMap<String, HashMap>(); // initialize the index with vocabulary list and posting list
    /**
     This constructor should initiate the FileWriter to output your index files
     remember to close files if you finish writing the index
    */
	public MyIndexWriter(String type) throws IOException {
		if (type.equals("trectext")) {
			writer = new FileWriter(Path.IndexTextDir);
		} 
		else if (type.equals("trecweb")) {
			writer = new FileWriter(Path.IndexWebDir);
		}
	}
	
    /**
     you are strongly suggested to build the index by installments
     in your implementation of the index, you should transform your string docnos into non-negative integer docids !!!
     In MyIndexReader, you will need to request the integer docid for docnos.
    */
	public void index(String docno, String content) throws IOException {
		
		// create docid
		// remain the number in the string, and convert the string type sequence number into integer
		String id = "";
		for (int i = 0; i < docno.length(); i++) {
			if (docno.charAt(i) >= 48 && docno.charAt(i) <= 57) {  //remove all the characters except numbers
				id += docno.charAt(i);
			}
		}
		id = id.substring(2);              // because some exceeds the integer upper limit, here I cut the first two digits
		int docid = Integer.parseInt(id);	// convert
		HashMap<Integer,Integer> postingList = new HashMap();  // intitialize posting list
		
		// traverse every word in content to update the vocabularyList	
		content = content.trim();                            // remove the white space at the end of each line
		String[] words = content.split(" ");                 // split the words in content and store them into array
		
		for (int i = 0; i < words.length; i++) {             // traverse each word
			if (index.containsKey(words[i]) == false) {      // if the word not in vocabularyList (the key of index)
				index.put(words[i], postingList);                    // update the index
				postingList.put(docid,1);                            // update the posting list
                    
			} 
			else {                                           // if the word already in vocabularyList
                if (postingList.containsKey(docid)) {           // if the word already appeared in current posting list
                	int curfreq = postingList.get(docid);
                	postingList.put(docid, curfreq + 1);        // add frequency
                }
                else {
                	postingList.put(docid,1);                   // the word is the first in current posting list
                }
                index.put(words[i], postingList);	            // update the index      
            }
	     }	
		
	}
	
	/**
	 *   close the index writer, and you should output all the buffered content (if any).
     *   and if you write your index into several files, you need to fuse them here.
	 */
	public void close() throws IOException {
		
		// Output the index divide into two parts:dictionary and posting
		int i = index.size();
		for (Map.Entry<String, HashMap> entry : index.entrySet()) {
            i++;
            int frequency = 0;                        // initialize frequency
            String vocabulary = entry.getKey();       // get the vocabulary
            HashMap <Integer,Integer> posting;        
            posting = entry.getValue();               // get the posting list to get the frequency
            
            writer.write("TOKEN" + vocabulary + "#");  // write the vocabulary
            
            for (Map.Entry<Integer,Integer> freq: posting.entrySet()) {
            	frequency += freq.getValue();         // add the frequency
            }
            writer.write(frequency + "@" + i);        // write the frequency collection freq and pointer
            writer.write("\n");
        }
        
        for (String term : index.keySet()) {
        	HashMap<Integer,Integer> docFreq = index.get(term);
        	for (int docid : docFreq.keySet()) {
        		String s = String.valueOf(docid);
        		if (s.startsWith("0")) {
        			writer.write("@" + s + "#" + docFreq.get(docid));          // get docid and document frequency
                }
                else {
                	writer.write("@" + s + "#" + docFreq.get(docid));          // document frequency
                }
        	}
        writer.write("\n");
       }
       if (writer != null) {
    	   try {
        		writer.flush();
        	} 
    	   catch (IOException e) {
        	}
        }
        writer.close();
      }	
}
