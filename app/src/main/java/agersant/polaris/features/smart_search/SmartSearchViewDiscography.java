package agersant.polaris.features.smart_search;

import android.content.Context;
import android.view.LayoutInflater;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

import java.util.ArrayList;

import agersant.polaris.CollectionItem;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.R;
import agersant.polaris.api.API;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


class SmartSearchViewDiscography extends SmartSearchViewContent {

    private final SmartSearchAdapter adapter;
    private final SwipyRefreshLayout swipeRefresh;

    public SmartSearchViewDiscography(Context context) {
        super(context);
        throw new UnsupportedOperationException();
    }

    public SmartSearchViewDiscography(Context context, API api, PlaybackQueue playbackQueue) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_smart_search_discography, this, true);

        RecyclerView recyclerView = findViewById(R.id.smart_search_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ItemTouchHelper.Callback callback = new SmartSearchTouchCallback();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        adapter = new SmartSearchAdapterDiscography(api, playbackQueue);
        recyclerView.setAdapter(adapter);

        swipeRefresh = findViewById(R.id.swipe_refresh);
    }

    @Override
    void setItems(ArrayList<? extends CollectionItem> items) {
        adapter.setItems(items);
    }

    @Override
    void setOnRefreshListener(SwipyRefreshLayout.OnRefreshListener listener) {
        swipeRefresh.setEnabled(listener != null);
        swipeRefresh.setOnRefreshListener(listener);
    }
}
