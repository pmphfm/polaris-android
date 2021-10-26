package agersant.polaris;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.security.MessageDigest;

public class CollectionItem implements Cloneable, Serializable {

    private String path;
    private String artist;
    private String title;
    private String artwork;
    private String album;
    private String albumArtist;
    private int trackNumber;
    private int discNumber;
    private int duration;
    private int year;
    private String lyricist;
    private String composer;
    private String genre;
    private String copyright;

    @SuppressWarnings("WeakerAccess")
    boolean isDirectory;

    boolean isAnnouncement = false;

    private CollectionItem() {
    }

    private static String getNameFromPath(String path) {
        String[] chunks = path.split("[/\\\\]");
        return chunks[chunks.length - 1];
    }

    public static CollectionItem directory(String path) {
        CollectionItem item = new CollectionItem();
        item.isDirectory = true;
        item.path = path;
        return item;
    }

    void parseFields(JsonObject fields) {
        path = getOptionalString(fields, "path");
        artist = getOptionalString(fields, "artist");
        title = getOptionalString(fields, "title");
        artwork = getOptionalString(fields, "artwork");
        album = getOptionalString(fields, "album");
        albumArtist = getOptionalString(fields, "album_artist");
        trackNumber = getOptionalInt(fields, "track_number");
        discNumber = getOptionalInt(fields, "disc_number");
        duration = getOptionalInt(fields, "duration");
        year = getOptionalInt(fields, "year");
        lyricist = getOptionalString(fields, "lyricist");
        composer = getOptionalString(fields, "composer");
        genre = getOptionalString(fields, "genre");
        copyright = getOptionalString(fields, "copyright");

    }

    private String getOptionalString(JsonObject fields, String key) {
        if (!fields.has(key)) {
            return null;
        }
        JsonElement element = fields.get(key);
        if (element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }

    private int getOptionalInt(JsonObject fields, @SuppressWarnings("SameParameterValue") String key) {
        if (!fields.has(key)) {
            return -1;
        }
        JsonElement element = fields.get(key);
        if (element.isJsonNull()) {
            return -1;
        }
        return element.getAsInt();
    }

    public String getName() {
        return getNameFromPath(path);
    }

    @Override
    public CollectionItem clone() throws CloneNotSupportedException {
        return (CollectionItem) super.clone();
    }

    public String getPath() {
        return path;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public String getTitle() {
        return title;
    }

    public String getArtwork() {
        return artwork;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getAlbum() {
        return album;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public int getDiscNumber() {
        return discNumber;
    }

    public int getDuration() {
        return duration;
    }

    public int getYear() {
        return year;
    }

    public String getLyricist() {
        return lyricist;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getComposer() {
        return composer;
    }

    public String getGenre() {
        return genre;
    }

    public boolean isAnnouncement() {
        return isAnnouncement;
    }

    public static class Directory extends CollectionItem {
        public static class Deserializer implements JsonDeserializer<CollectionItem> {
            public Directory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                Directory item = new Directory();
                item.isDirectory = true;
                JsonObject fields = json.getAsJsonObject();
                item.parseFields(fields);
                return item;
            }
        }
    }

    public static class Song extends CollectionItem {
        public static class Deserializer implements JsonDeserializer<CollectionItem> {
            public Song deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                Song item = new Song();
                item.isDirectory = false;
                JsonObject fields = json.getAsJsonObject();
                item.parseFields(fields);
                return item;
            }
        }
    }

    public static class Deserializer implements JsonDeserializer<CollectionItem> {
        public CollectionItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            CollectionItem item = new CollectionItem();
            item.isDirectory = json.getAsJsonObject().has("Directory");
            JsonObject fields;
            if (item.isDirectory()) {
                fields = json.getAsJsonObject().get("Directory").getAsJsonObject();
            } else {
                fields = json.getAsJsonObject().get("Song").getAsJsonObject();
            }
            item.parseFields(fields);
            return item;
        }
    }


    public static class Announcement extends CollectionItem {
        CollectionItem prev;
        CollectionItem next;
        CollectionItem next_next;

        // Replace '/' with '-' if item is not null. Else returns 'null'.
        String pathTransform(CollectionItem item) {
            if (item == null) {
                return "null";
            }
            return item.getPath();
        }

        // The max path length on server and android device might not match. Shorten it to something that that is "generally" good.
        String makePath(CollectionItem prev, CollectionItem next, CollectionItem next_next) {

            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                sha.update(pathTransform(prev).getBytes());
                sha.update(pathTransform(next).getBytes());
                sha.update(pathTransform(next_next).getBytes());
                StringBuilder sb = new StringBuilder();
                byte[] bytes = sha.digest();
                for (byte aByte : bytes) {
                    String hex = Integer.toHexString(0xFF & aByte);
                    if (hex.length() == 1) {
                        sb.append('0');
                    }
                    sb.append(hex);
                }
                return "rj_path_" + sb.toString();
            } catch (Exception e) {
                return "rj_path_" + pathTransform(prev) + pathTransform(next) + pathTransform(next_next);
            }
        }

        void makeAnnouncement() {
            isAnnouncement = true;
            // TODO: Read these names from the server. Rj user settings store these to the database.
            super.artist = "Ameen Sayani";
            super.title = "Announcement";
            super.album = "Geetmala";
        }

        Announcement() {
            super();
            isDirectory = false;
            makeAnnouncement();
            super.path = makePath(null, null, null);
        }

        public void setItems(CollectionItem prev, CollectionItem next, CollectionItem next_next) {
            this.prev = prev;
            this.next = next;
            this.next_next = next_next;
            super.path = makePath(prev, next, next_next);
        }

        public CollectionItem getPrev() {
            return prev;
        }

        public CollectionItem getNext() {
            return next;
        }

        public CollectionItem getNextNext() {
            return next_next;
        }
    }
}
