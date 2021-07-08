package agersant.polaris.features.smart_search;


import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import agersant.polaris.CollectionItem;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.R;
import agersant.polaris.api.API;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class SmartSearchViewAlbum extends SmartSearchViewContent {

    private final SmartSearchAdapter adapter;
    private final ImageView artwork;
    private final TextView artist;
    private final TextView title;
    private final API api;

    public SmartSearchViewAlbum(Context context) {
        super(context);
        throw new UnsupportedOperationException();
    }

    public SmartSearchViewAlbum(Context context, API api, PlaybackQueue playbackQueue) {
        super(context);
        this.api = api;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_smart_search_album, this, true);

        artwork = findViewById(R.id.album_artwork);
        artist = findViewById(R.id.album_artist);
        title = findViewById(R.id.album_title);

        RecyclerView recyclerView = findViewById(R.id.smart_search_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        ItemTouchHelper.Callback callback = new SmartSearchTouchCallback();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        adapter = new SmartSearchAdapterAlbum(api, playbackQueue);
        recyclerView.setAdapter(adapter);
    }

    @Override
    void setItems(ArrayList<? extends CollectionItem> items) {

        Collections.sort(items, new Comparator<CollectionItem>() {
            @Override
            public int compare(CollectionItem a, CollectionItem b) {
                int discDifference = a.getDiscNumber() - b.getDiscNumber();
                if (discDifference != 0) {
                    return discDifference;
                }
                return a.getTrackNumber() - b.getTrackNumber();
            }
        });

        adapter.setItems(items);

        CollectionItem item = items.get(0);

        String artworkPath = item.getArtwork();
        if (artworkPath != null) {
            api.loadImageIntoView(item, artwork);
        }

        String titleString = item.getAlbum();
        if (title != null) {
            title.setText(titleString);
        }

        String artistString = item.getAlbumArtist();
        if (artistString == null) {
            artistString = item.getArtist();
        }
        if (artist != null) {
            artist.setText(artistString);
        }
    }

}
