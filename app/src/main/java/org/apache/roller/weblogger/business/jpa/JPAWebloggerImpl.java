/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */
package org.apache.roller.weblogger.business.jpa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.PlanetURLStrategy;
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.DFCache;
import org.apache.roller.weblogger.business.FileContentManager;
import org.apache.roller.weblogger.business.OAuthManager;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerImpl;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.runnable.ThreadManager;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingQueueManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;

/**
 * A JPA specific implementation of the Weblogger business layer.
 */
@com.google.inject.Singleton
public class JPAWebloggerImpl extends WebloggerImpl {

    // a persistence utility class
    private final JPAPersistenceStrategy strategy;
    
    private DFCache dfCache;
    /**
     * Single constructor.
     * @throws org.apache.roller.weblogger.WebloggerException on any error
     */
    @com.google.inject.Inject
    protected JPAWebloggerImpl(
        JPAPersistenceStrategy strategy,
        AutoPingManager      autoPingManager,
        BookmarkManager      bookmarkManager,
        IndexManager         indexManager,
        MediaFileManager     mediaFileManager,
        FileContentManager   fileContentManager,
        PingQueueManager     pingQueueManager,
        PingTargetManager    pingTargetManager,
        PluginManager        pluginManager,
        PropertiesManager    propertiesManager,
        ThemeManager         themeManager,
        ThreadManager        threadManager,
        UserManager          userManager,
        WeblogManager        weblogManager,
        WeblogEntryManager   weblogEntryManager,
        OAuthManager         oauthManager,
		FeedFetcher          feedFetcher,
        PlanetManager        planetManager,
        PlanetURLStrategy    planetUrlStrategy,
        URLStrategy          urlStrategy) throws WebloggerException {
        
        super(
            autoPingManager,
            bookmarkManager,
            indexManager,
            mediaFileManager,
            fileContentManager,
            pingQueueManager,
            pingTargetManager,
            pluginManager,
            propertiesManager,
            themeManager,
            threadManager,
            userManager,
            weblogManager,
            weblogEntryManager,
            oauthManager,
            feedFetcher,
            planetManager,
            planetUrlStrategy,
            urlStrategy);
        
        this.strategy = strategy;
        
        // LAB 2 - Build the DF Cache
        
        this.dfCache = new DFCache();
        List<Weblog> allWeblogs = this.getWeblogManager().getWeblogs(true, true, null, null, 0, Integer.MAX_VALUE);
        
        for(Weblog weblog : allWeblogs){
            try{
            	String blogName = weblog.getId();
                List<WeblogEntry> allEntries = weblog.getRecentWeblogEntries(null, 100);
            	for (WeblogEntry entry : allEntries){
            		String docId = blogName + entry.getAnchor();
            		Set<String> wordSet = getWordsFromDocument(entry);
            		for (String word : wordSet){
            			this.dfCache.addItem(docId, word);
            		}
            	}
            }
            catch(Exception e){
            	System.out.println("Failed to build DF Cache");
            	System.out.println(e.toString());
            	System.out.println(e.getMessage());
            }
         }
        
    }
    
    private Set<String> getWordsFromDocument (WeblogEntry entry){
        
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
    
    public void flush() throws WebloggerException {
        this.strategy.flush();
    }

    
    public void release() {
        super.release();
        // tell JPA to close down
        this.strategy.release();
    }

    
    public void shutdown() {
        // do our own shutdown first
        this.release();

        // then let parent do its thing
        super.shutdown();

        this.strategy.shutdown();
    }
    
    public DFCache getDFCache(){
    	return dfCache;
    }

}
