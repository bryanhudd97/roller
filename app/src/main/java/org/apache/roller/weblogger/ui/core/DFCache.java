package org.apache.roller.weblogger.ui.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DFCache {
	private Map<String, Set<String>> map;
	public DFCache(){
		map = new HashMap<String, Set<String>>();
	}
	
	public int getDocumentFrequency(String word){
		// Get document frequency of a given word
		Set<String> docSet = map.get(word);
		if (docSet == null)
			return 0;
		return docSet.size();
	}
	
	public void addItem(String docId, String word){
		// Add a 
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
}
