package agersant.polaris;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;

import agersant.polaris.api.local.OfflineCache;
import agersant.polaris.api.remote.DownloadQueue;
import agersant.polaris.CollectionItem.Announcement;

public class PlaybackQueue {

    public static final String CHANGED_ORDERING = "CHANGED_ORDERING";
    public static final String QUEUED_ITEM = "QUEUED_ITEM";
    public static final String QUEUED_ITEMS = "QUEUED_ITEMS";
    public static final String OVERWROTE_QUEUE = "OVERWROTE_QUEUE";
    public static final String NO_LONGER_EMPTY = "NO_LONGER_EMPTY";
    public static final String REMOVED_ITEM = "REMOVED_ITEM";
    public static final String REMOVED_ITEMS = "REMOVED_ITEMS";
    public static final String REORDERED_ITEMS = "REORDERED_ITEMS";
    private final String rjEnablePreferenceKey;

    private ArrayList<CollectionItem> content;
    private Ordering ordering;
    private boolean rjEnabled = true;
    final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    PlaybackQueue() {
        content = new ArrayList<>();
        ordering = Ordering.SEQUENCE;

        PolarisApplication application = PolarisApplication.getInstance();
        rjEnablePreferenceKey = application.getResources().getString(R.string.pref_key_enable_rj);
        sharedPreferenceChangeListener = (prefs, key) -> {

            if (key.equals(rjEnablePreferenceKey)) {
                rjEnabled = prefs.getBoolean(rjEnablePreferenceKey, false);
                if (rjEnabled) {
                    rearrangeAnnouncements();
                } else {
                    removeAnnouncementAndRearrange();
                }
            }
        };
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        rjEnabled = prefs.getBoolean(rjEnablePreferenceKey, false);
        prefs.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    ArrayList<CollectionItem> getContent() {
        return content;
    }

    void setContent(ArrayList<CollectionItem> content) {
        this.content = content;
        rearrangeAnnouncements();
        broadcast(PlaybackQueue.OVERWROTE_QUEUE);
    }

    // Return negative value if a is going to play before b, positive if a is going to play after b
    public int comparePriorities(CollectionItem currentItem, CollectionItem a, CollectionItem b) {
        final int currentIndex = content.indexOf(currentItem);
        int playlistSize = content.size();

        int scoreA = playlistSize + 1;
        int scoreB = playlistSize + 1;

        for (int i = 0; i < playlistSize; i++) {
            CollectionItem item = content.get(i);
            final String path = item.getPath();
            final int score = (playlistSize + i - currentIndex) % playlistSize;
            if (score < scoreA && path.equals(a.getPath())) {
                scoreA = score;
            }
            if (score < scoreB && path.equals(b.getPath())) {
                scoreB = score;
            }
        }

        return scoreA - scoreB;
    }

    private void addItemInternal(CollectionItem item) {
        CollectionItem newItem;
        try {
            newItem = item.clone();
        } catch (Exception e) {
            System.err.println("Error while cloning CollectionItem: " + e.toString());
            return;
        }
        content.add(newItem);
    }

    public void addItems(ArrayList<? extends CollectionItem> items) {
        boolean wasEmpty = size() == 0;
        for (CollectionItem item : items) {
            addItemInternal(item);
        }
        rearrangeAnnouncements();
        broadcast(PlaybackQueue.QUEUED_ITEMS);
        if (wasEmpty) {
            broadcast(PlaybackQueue.NO_LONGER_EMPTY);
        }
    }

    public void addItem(CollectionItem item) {
        boolean wasEmpty = size() == 0;
        addItemInternal(item);
        rearrangeAnnouncements();
        broadcast(PlaybackQueue.QUEUED_ITEM);
        if (wasEmpty) {
            broadcast(PlaybackQueue.NO_LONGER_EMPTY);
        }
    }

    public void remove(int position) {
        content.remove(position);
        rearrangeAnnouncements();
        broadcast(REMOVED_ITEM);
    }

    public void clear() {
        content.clear();
        broadcast(REMOVED_ITEMS);
    }

    public void swap(int fromPosition, int toPosition) {
        Collections.swap(content, fromPosition, toPosition);
        rearrangeAnnouncements();
        broadcast(REORDERED_ITEMS);
    }

    public void move(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        int low = Math.min(fromPosition, toPosition);
        int high = Math.max(fromPosition, toPosition);
        int distance = fromPosition < toPosition ? -1 : 1;
        Collections.rotate(content.subList(low, high + 1), distance);
        rearrangeAnnouncements();
        broadcast(REORDERED_ITEMS);
    }

    public int size() {
        return content.size();
    }

    public CollectionItem getItem(int position) {
        return content.get(position);
    }

    CollectionItem getNextTrack(CollectionItem from, int delta) {
        if (content.isEmpty()) {
            return null;
        }

        if (ordering == Ordering.REPEAT_ONE) {
            return from;
        } else {
            int currentIndex = content.indexOf(from);
            if (currentIndex < 0) {
                return content.get(0);
            } else {
                int newIndex = currentIndex + delta;
                if (newIndex >= 0 && newIndex < content.size()) {
                    return content.get(newIndex);
                } else if (ordering == Ordering.REPEAT_ALL) {
                    if (delta > 0) {
                        return content.get(0);
                    } else {
                        return content.get(content.size() - 1);
                    }
                } else {
                    return null;
                }
            }
        }
    }

    public Ordering getOrdering() {
        return ordering;
    }

    public void setOrdering(Ordering ordering) {
        this.ordering = ordering;
        broadcast(CHANGED_ORDERING);
    }

    public boolean hasNextTrack(CollectionItem currentItem) {
        return getNextTrack(currentItem, 1) != null;
    }

    public boolean hasPreviousTrack(CollectionItem currentItem) {
        return getNextTrack(currentItem, -1) != null;
    }

    private void broadcast(String event) {
        PolarisApplication application = PolarisApplication.getInstance();
        Intent intent = new Intent();
        intent.setAction(event);
        application.sendBroadcast(intent);
    }

    public CollectionItem getNextItemToDownload(CollectionItem currentItem, OfflineCache offlineCache, DownloadQueue downloadQueue) {
        final int currentIndex = Math.max(0, content.indexOf(currentItem));

        int bestScore = 0;
        CollectionItem bestItem = null;

        int playlistSize = content.size();

        for (int i = 0; i < playlistSize; i++) {
            final int score = (playlistSize + i - currentIndex) % playlistSize;
            if (bestItem != null && score > bestScore) {
                continue;
            }
            CollectionItem item = content.get(i);
            if (item == currentItem) {
                continue;
            }
            if (offlineCache.hasAudio(item.getPath())) {
                continue;
            }
            if (downloadQueue.isDownloading(item)) {
                continue;
            }
            bestScore = score;
            bestItem = item;
        }

        PolarisApplication application = PolarisApplication.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(application);
        Resources resources = application.getResources();
        String numSongsToPreloadKey = resources.getString(R.string.pref_key_num_songs_preload);
        String numSongsToPreloadString = preferences.getString(numSongsToPreloadKey, "0");
        int numSongsToPreload = Integer.parseInt(numSongsToPreloadString);
        if (numSongsToPreload >= 0 && bestScore > numSongsToPreload) {
            bestItem = null;
        }

        return bestItem;
    }

    public enum Ordering {
        SEQUENCE,
        REPEAT_ONE,
        REPEAT_ALL,
    }


    boolean announcementAtWrongIndex() {
        for (int i = 0; i < content.size() ; i ++) {
            if (content.get(i).isAnnouncement() && (i%4 != 1)) {
                return true;
            }
        }
        return false;
    }

    void removeAnnouncementAndRearrange() {
        boolean removed = false;
        if (rjEnabled) {
            return;
        }
        for (int i = 1; i < content.size() ; i ++) {
            if (content.get(i).isAnnouncement()) {
                content.remove(i);
                i--;
                removed = true;
            }
        }
        if (removed) {
            broadcast(REMOVED_ITEMS);
        }
    }

    // If RJ service is enabled and requested then inserts announcements in the
    // current playlist
    // while queuing new tracks.
    void rearrangeAnnouncements() {
        if (!rjEnabled) {
            return;
        }

        if (announcementAtWrongIndex()) {
            removeAnnouncementAndRearrange();
        }

        boolean added = false;

        // We iterate one more element than length of array to see if we can end the
        // playlist with an announcement.
        for (int i = 1; i < content.size() + 1; i = i + 4) {
            CollectionItem prev, next = null, next_next = null;
            // If this is already an announcement then we may need to update it because
            // previous
            // or next songs might have changed.
            Announcement announcement;
            if (i < content.size() && content.get(i).isAnnouncement()) {
                announcement = (CollectionItem.Announcement) content.get(i);
            } else {
                announcement = new Announcement();
                content.add(i, announcement);
                added = true;
            }
            prev = content.get(i - 1);
            if ((i + 1) < content.size()) {
                next = content.get(i + 1);
                if ((i + 2) < content.size()) {
                    next_next = content.get(i + 2);
                }
            }

            announcement.setItems(prev, next, next_next);
        }

        if (added) {
            broadcast(QUEUED_ITEMS);
            broadcast(REORDERED_ITEMS);
        }
    }
}
