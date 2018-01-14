package Search;

import Classes.Query;

import java.util.List;

/**
 * Read and parse TREC queries
 * -- INFSCI 2140: Information Storage and Retrieval Spring 2016
 */
public class ExtractQuery {

	public List<Query> GetQueries() throws Exception {
		//you should extract the 4 queries from the Path.TopicDir
		//NT: the query content of each topic should be 1) tokenized, 2) to lowercase, 3) remove stop words, 4) stemming
		//NT: third topic title is ----Star Trek "The Next Generation"-----, if your code can recognize the phrase marked by "", 
		//    and further process the phrase in search, you will get extra points.
		//NT: you can simply pick up title only for query, or you can also use title + description + narrative for the query content.
		return null;
	}
}
