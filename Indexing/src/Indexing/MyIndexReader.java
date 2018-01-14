package Indexing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Classes.Path;

/**
 *  Class for Assignment 2 of INFSCI2140, 2016 Spring.
 */
public class MyIndexReader {
	//you are suggested to write efficient code here, otherwise, your memory cannot hold the whole corpus...
	private BufferedReader reader;

	public MyIndexReader( String type ) throws IOException {
		//read the index files you generated in task 1
		//remember to close reader when you finish using them
		//use appropriate structure to maintain the index
		if (type.equals("trectext")) {
			reader = new BufferedReader(new FileReader(Path.IndexTextDir));
		} else if (type.equals("trecweb")) {
			reader = new BufferedReader(new FileReader(Path.IndexWebDir));
		}
	}
	
	//get the non-negative integer dociId for the requested docNo
	//If the requested docno does not exist in the index, return -1
	public int getDocid( String docno ) {
		
		String id = "";
		for (int i = 0; i < docno.length(); i++) {
			if (docno.charAt(i) >= 48 && docno.charAt(i) <= 57) {  //remove all the characters except numbers
				id += docno.charAt(i);
			}
		}
		int docid = Integer.parseInt(id);	// convert
		return docid;		
		//return -1;
	}

	// Retrieve the docno given the integer docid
	public String getDocno( int docid ) {
		String id = Integer.toString(docid);  // convert integer docid to string
		String docno; // initialize docno
		int idNum = 1190000000;               
		// set the threshold of docid, because docno of trecweb is like lists-118-1234567, docno of trectext is like XIE19960101.1234
		if (docid < idNum) {
			docno = "lists" + "-" + id.substring(0, 3) + "-" + id.substring(3,10);
		}
		else {
			docno = "XIE" + id.substring(0,8) + "." + id.substring(8, 12);
		}
		return docno;	
		//return null;
	}
	
	public int[] Find(String token) throws IOException{
        String line = new String();
        int[] find = null;
        Pattern Cfreq = Pattern.compile("#[0-9]{1,}");
        Pattern Pointer = Pattern.compile("@[0-9]{1,}");
        
        
        while((line = reader.readLine()) != null){
        if(line.startsWith("@")){
        break;
        }
        String s1 = "TOKEN"+token;    
        if (line.startsWith(s1)){
        find = new int[2];             
          Matcher pointermatcher = Pointer.matcher(line);
          Matcher freqmatcher = Cfreq.matcher(line);
          if (pointermatcher.find() && freqmatcher.find() ){
            find[0] = Integer.parseInt(pointermatcher.group().substring(1));
            find[1] = Integer.parseInt(freqmatcher.group().substring(1));  
            }
          }
        }
        return find;
        
    }
	/**
	 * Get the posting list for the requested token.
	 * 
	 * The posting list records 1.the documents' docids which contain given token and 2.corresponding frequencies of the term, such as:
	 *  
	 *  [docid]		[freq]
	 *  1			3
	 *  5			7
	 *  9			11
	 *  13			19
	 * 
	 * ...
	 * 
	 * In the returned 2-dimension array, the first dimension refers to each document, and the second dimension records the docid and frequency.
	 * 
	 * For example:
	 * array[0][0] records the docid of the first document the token appears.
	 * array[0][1] records the frequency of the token in the documents with docid = array[0][0]
	 * ...
	 * 
	 * NOTE that the returned posting list should be ranked by docid from the smallest to the largest.
	 * 
	 * @param token
	 */
	public int[][] getPostingList( String token ) throws IOException {
		int[] finditem = Find(token);
        
        if (finditem != null) {
        	int pointer = finditem[0];
        	HashMap<Integer,Integer> Map = new HashMap();
        	for (int i = 1; i < pointer; i++) {
        		reader.readLine();
        	}
        	
        String getposting = reader.readLine();
        Pattern frequency = Pattern.compile("#[0-9]{1,}");
        Pattern docid = Pattern.compile("@[0-9]{1,}");
        Matcher idmatcher = docid.matcher(getposting);
        Matcher freqmatcher = frequency.matcher(getposting);
        while (idmatcher.find() && freqmatcher.find()) {
        	int DID = Integer.parseInt(idmatcher.group().substring(1));
            int DFreq = Integer.parseInt(freqmatcher.group().substring(1));
            Map.put(DID, DFreq);
        }
        int[][] result = new int[Map.size()][2];
        int i = 0;
        for(Map.Entry<Integer,Integer> entry : Map.entrySet()){
        result[i][1] = entry.getValue();
        result[i][0] = entry.getKey();
        
        i++;
        }
        return result;
        }
        else{
        	int[][] result = {{0,0}};
            return result;
        }
		
		
		//return null;
	}

	// Return the number of documents that contain the given token.
	public int DocFreq( String token ) throws IOException {
		int result = 0;
        int[][] data = getPostingList(token);
        if (data != null){
            result = data.length;
        }
        return result;
	}
	
	// Return the total number of times the token appears in the collection.
	public int CollectionFreq( String token ) throws IOException {
		  int result = 0;
          int[] data = Find(token);
          if (data != null){
              result = data[1];
          }
         return result;
	}
	
	public void close() throws IOException {
		
		reader.close();
	}
	
}