package agersant.polaris.features.smart_search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import agersant.polaris.PlaybackQueue;
import agersant.polaris.R;
import agersant.polaris.api.API;


class SmartSearchAdapterExplorer extends SmartSearchAdapter {

    private final API api;
    private final PlaybackQueue playbackQueue;

    SmartSearchAdapterExplorer(API api, PlaybackQueue playbackQueue) {
        super();
        this.api = api;
        this.playbackQueue = playbackQueue;
    }

    @Override
    public SmartSearchItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemQueueStatusView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_smart_search_item_queued, parent, false);
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_smart_search_explorer_item, parent, false);
        return new SmartSearchItemHolderExplorer(api, playbackQueue, this, itemView, itemQueueStatusView);
    }

}
