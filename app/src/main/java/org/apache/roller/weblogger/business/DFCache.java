package org.apache.roller.weblogger.business;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;

public class DFCache {
	private Map<String, Set<String>> map;
	public DFCache(List<Weblog> allWeblogs){
		// Initialize map data structure
		map = new HashMap<String, Set<String>>();
		
		// Initialize DFCache by building DF information on all weblogs
		for(Weblog weblog : allWeblogs){
        	String blogName = weblog.getId();
            List<WeblogEntry> allEntries = weblog.getRecentWeblogEntries("nil", 100);
            for (WeblogEntry entry : allEntries){
        		String docId = blogName + entry.getAnchor();
        		Set<String> wordSet = getWordsFromDocument(entry);
        		for (String word : wordSet){
        			this.addItem(docId, word);
        		}
        	}
        }
	}
	
	public int getDocumentFrequency(String word){
		// Get document frequency of a given word
		Set<String> docSet = map.get(word);
		if (docSet == null)
			return 0;
		return docSet.size();
	}
	
	public void addItem(String docId, String word){
		// Add a document-word pair to the DF cache
		Set<String> docSet = map.get(word);
		if (docSet == null){
			docSet = new HashSet<String>();
			map.put(word, docSet);
		}
		docSet.add(docId);
	}
	
	public void deleteDoc(String docId){
		for (String word : map.keySet()){
			Set<String> docSet = map.get(word);
			docSet.remove(docId);
			if (docSet.size() == 0){
				map.remove(word);
			}
		}
	}
	
	public void updateDoc(String docId, Set<String> newWordSet){
		this.deleteDoc(docId);
		for(String word: newWordSet){
			this.addItem(docId, word);
		}
	}
	
	// For testing purposes only
	public String toString(){
		StringBuilder output = new StringBuilder();
		for (String word : map.keySet()){
			output.append(word + ": " + getDocumentFrequency(word) + " documents\n");
		}
		return output.toString();
	}
	
	// helper function
    private static Set<String> getWordsFromDocument (WeblogEntry entry){
        
    	// Create appended string of a weblog entry and all its comments
        StringBuilder sb = new StringBuilder();
        sb.append(entry.getText());
        for(WeblogEntryComment comment : entry.getComments()){
        	sb.append(" ");
        	sb.append(comment.getContent());
        }
        
        // Create array of strings by splitting on word boundaries
    	String[] words = sb.toString().split("\\b|\\W");
    	
    	// Create a set of all words in the document
    	Set<String> set = new HashSet<String>();
    	for (String word : words){
    		if (word.isEmpty())
    			continue; // don't put empty strings on the map
    		set.add(word);
    	}
    	
    	return set;
    }
}
