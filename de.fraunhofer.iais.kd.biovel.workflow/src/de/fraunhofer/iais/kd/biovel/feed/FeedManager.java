package de.fraunhofer.iais.kd.biovel.feed;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import de.fraunhofer.iais.kd.biovel.util.WorkflowHelper;
import de.fraunhofer.iais.kd.biovel.util.WorkflowProperty;

public class FeedManager {
    private static final Logger LOG = Logger.getLogger(FeedManager.class.getName());

    static Map<String, Entry> theFeed = Collections.synchronizedMap(new LinkedHashMap<String, Entry>());

    public FeedManager(Properties props) {
        String dirName = props.getProperty(WorkflowProperty.DATA_DIR.toString());
        File dir = WorkflowHelper.useAsWorkingDirectory(dirName);
        LOG.info("Feed working dir: " + dir.getAbsolutePath());
    }

    public String getFeed() {
        return theFeed.toString();
    }

    public String makeNewBiovelEntryId() {
        String entryId = WorkflowHelper.idOfCurrentTime();
        return entryId;
    }

    protected String makeReplyIdToEntry(String entryId) {
        return entryId + "_reply";
    }

    public boolean isReplyId(String id) {
        return id.endsWith("_reply");
    }

    public boolean putEntry(String entryId, String content) {
        if (containsEntry(entryId)) {
            return false;
        }
        Entry entry = new Entry();
        entry.setId(entryId);
        List<String> contents = new ArrayList<String>();
        contents.add(content);
        entry.setContents(contents);
        FeedManager.theFeed.put(entryId, entry);
        return true;
    }

    /**
     * returns the content of the entry, <code>null</code> if no entry exists.
     * 
     * @param entryId
     * @return as described.
     */
    public String getEntry(String entryId) {
        Entry entry = theFeed.get(entryId);
        @SuppressWarnings("unchecked")
        String result = entry.getContents().get(0); // XXX KHS remove unchecked
        return result;
    }

    public boolean containsEntry(String id) {
        return theFeed.containsKey(id);
    }

    public boolean containsReplyToEntry(String entryId) {
        return containsEntry(makeReplyIdToEntry(entryId));
    }

    public boolean putReplyTo(String entryId, String replyContent) {
        String replyId = makeReplyIdToEntry(entryId);
        if (containsEntry(replyId)) {
            return false;
        }
        putEntry(replyId, replyContent);
        return true;
    }

    /**
     * returns the content of the reply, <code>null</code> if no reply exists.
     * 
     * @param entryId
     * @return as described.
     */
    public String getReplyTo(String entryId) {
        String replyId = makeReplyIdToEntry(entryId);
        return getEntry(replyId);
    }

    static class Entry {
        private String id;
        private List<String> contents;

        public void setId(String entryId) {
            this.id = entryId;
        }

        public List<String> getContents() {
            return this.contents;
        }

        public void setContents(List<String> contents) {
            this.contents = contents;
        }

        @Override
        public String toString() {
            return " {id: " + this.id + ", content: " + this.contents.get(0) + "}\n";
        }
    }

    public void deleteEntry(String entryId) {
        theFeed.remove(makeReplyIdToEntry(entryId));
        theFeed.remove(entryId);
    }
}
