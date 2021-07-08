package agersant.polaris.features.smart_search;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import agersant.polaris.CollectionItem;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.R;
import agersant.polaris.api.API;


class SmartSearchItemHolderDiscography extends SmartSearchItemHolder {

    private final ImageView artwork;
    private final TextView artist;
    private final TextView album;

    SmartSearchItemHolderDiscography(API api, PlaybackQueue playbackQueue, SmartSearchAdapter adapter, View itemView, View itemQueueStatusView) {
        super(api, playbackQueue, adapter, itemView, itemQueueStatusView);
        artwork = itemView.findViewById(R.id.artwork);
        artist = itemView.findViewById(R.id.artist);
        album = itemView.findViewById(R.id.album);
        itemView.setOnClickListener(this);
    }

    @Override
    void bindItem(CollectionItem item) {
        super.bindItem(item);

        String artistValue = item.getArtist();
        if (artistValue != null) {
            artist.setText(artistValue);
        }

        String albumValue = item.getAlbum();
        if (albumValue != null) {
            album.setText(albumValue);
        }

        api.loadImageIntoView(item, artwork);
    }
}
