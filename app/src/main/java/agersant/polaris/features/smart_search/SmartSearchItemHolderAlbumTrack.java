package agersant.polaris.features.smart_search;

import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import agersant.polaris.CollectionItem;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.R;
import agersant.polaris.api.API;


class SmartSearchItemHolderAlbumTrack extends SmartSearchItemHolder {

    private final TextView trackNumberText;
    private final TextView titleText;

    SmartSearchItemHolderAlbumTrack(API api, PlaybackQueue playbackQueue, SmartSearchAdapter adapter, View itemView, View itemQueueStatusView) {
        super(api, playbackQueue, adapter, itemView, itemQueueStatusView);
        trackNumberText = itemView.findViewById(R.id.track_number);
        titleText = itemView.findViewById(R.id.title);
    }

    @Override
    void bindItem(CollectionItem item) {
        super.bindItem(item);

        String title = item.getTitle();
        if (title != null) {
            titleText.setText(title);
        } else {
            titleText.setText(item.getName());
        }

        Integer trackNumber = item.getTrackNumber();
        if (trackNumber >= 0) {
            trackNumberText.setText(String.format((Locale) null, "%1$02d.", trackNumber));
        } else {
            trackNumberText.setText("");
        }
    }

}
