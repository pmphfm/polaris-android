package agersant.polaris.features.browse;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import agersant.polaris.CollectionItem;
import agersant.polaris.R;
import agersant.polaris.api.ServerAPI;
import agersant.polaris.ui.NetworkImage;

/**
 * Created by agersant on 12/11/2016.
 */

public class BrowseDiscographyItemHolder extends BrowseItemHolder {

    private ImageView artwork;
    private TextView artist;
    private TextView album;

    BrowseDiscographyItemHolder(BrowseAdapter adapter, View itemView, View itemQueueStatusView) {
        super(adapter, itemView, itemQueueStatusView);
        artwork = (ImageView) itemView.findViewById(R.id.artwork);
        artist = (TextView) itemView.findViewById(R.id.artist);
        album = (TextView) itemView.findViewById(R.id.album);
        itemView.setOnClickListener(this);
    }

    @Override
    void bindItem(CollectionItem item) {
        super.bindItem(item);

        {
            String artistValue = item.getArtist();
            if (artistValue != null) {
                artist.setText(artistValue);
            }
        }

        {
            String albumValue = item.getAlbum();
            if (albumValue != null) {
                album.setText(albumValue);
            }
        }

        {
            String artworkValue = item.getArtwork();
            if (artworkValue != null) {
                ServerAPI serverAPI = ServerAPI.getInstance(itemView.getContext());
                String url = serverAPI.getMediaURL(artworkValue);
                NetworkImage.load(url, artwork);
            }
        }
    }
}