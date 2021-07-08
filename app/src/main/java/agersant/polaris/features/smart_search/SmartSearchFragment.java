package agersant.polaris.features.smart_search;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;

import agersant.polaris.CollectionItem;
import agersant.polaris.PlaybackQueue;
import agersant.polaris.PolarisApplication;
import agersant.polaris.PolarisState;
import agersant.polaris.R;
import agersant.polaris.api.API;
import agersant.polaris.api.ItemsCallback;
import agersant.polaris.api.remote.ServerAPI;
import agersant.polaris.databinding.FragmentSmartSearchBinding;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class SmartSearchFragment extends Fragment {

    public static final String PATH = "PATH";
    public static final String NAVIGATION_MODE = "NAVIGATION_MODE";
    public static final String SEARCH = "SEARCH";
    private FragmentSmartSearchBinding binding;
    private ProgressBar progressBar;
    private View errorMessage;
    private ViewGroup contentHolder;
    private ItemsCallback fetchCallback;
    private NavigationMode navigationMode;
    private SwipyRefreshLayout.OnRefreshListener onRefresh;
    private ArrayList<? extends CollectionItem> items;
    private API api;
    private ServerAPI serverAPI;
    private PlaybackQueue playbackQueue;
    private String queryString = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        PolarisState state = PolarisApplication.getState();
        api = state.api;
        serverAPI = state.serverAPI;
        playbackQueue = state.playbackQueue;

        binding = FragmentSmartSearchBinding.inflate(inflater);
        errorMessage = binding.smartSearchErrorMessage;
        progressBar = binding.progressBar;
        contentHolder = binding.smartSearchContentHolder;

        binding.smartSearchErrorRetry.setOnClickListener((view) -> loadContent());

        final SmartSearchFragment that = this;
        fetchCallback = new ItemsCallback() {
            @Override
            public void onSuccess(final ArrayList<? extends CollectionItem> items) {
                requireActivity().runOnUiThread(() -> {
                    that.progressBar.setVisibility(View.GONE);
                    that.items = items;
                    that.displayContent();
                });
            }

            @Override
            public void onError() {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    errorMessage.setVisibility(View.VISIBLE);
                });
            }
        };
        Bundle args = getArguments();

        navigationMode = NavigationMode.valueOf(args.getSerializable(NAVIGATION_MODE).toString());
        if (navigationMode == NavigationMode.SEARCH) {
            queryString = (String) requireArguments().getSerializable(SEARCH);
            onRefresh = (SwipyRefreshLayoutDirection direction) -> loadContent();
        }

        loadContent();

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.smart_search, menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_smart_search_queue_all) {
            QueueAll(getView());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    void ShowToast(String message) {
        Context context = getContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @SuppressWarnings("UnusedParameters")
    void QueueAll(final View view) {
        if (items.isEmpty()) {
            ShowToast("Nothing to queue");
            return;
        }
        for (CollectionItem item : items) {
            if (item.isDirectory()) {
                queueDirectory(item);
            } else {
                playbackQueue.addItem(item);
            }
        }
        ShowToast("Queued");
    }

    private void queueDirectory(CollectionItem item) {
        final CollectionItem fetchingItem = item;
        ItemsCallback handlers = new ItemsCallback() {
            @Override
            public void onSuccess(final ArrayList<? extends CollectionItem> items) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    playbackQueue.addItems(items);
                    // if (item == fetchingItem) {
                        // TODO: Show queued success popup.
                    // }
                });
            }

            @Override
            public void onError() {
                new Handler(Looper.getMainLooper()).post(() -> {
                    // if (item == fetchingItem) {
                        // TODO: Show queued failure popup.
                    // }
                });
            }
        };

        api.flatten(item.getPath(), handlers);
    }

    private void loadContent() {
        progressBar.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);
        switch (navigationMode) {
            case PATH: {
                String path = requireArguments().getString(SmartSearchFragment.PATH);
                if (path == null) {
                    path = "";
                }
                loadPath(path);
                break;
            }
            case SEARCH: {
                loadSearch();
                break;
            }
        }
    }

    private void loadPath(String path) {
        api.browse(path, fetchCallback);
    }

    private void loadSearch() {
        api.smartSearch(queryString, fetchCallback);
    }

    private DisplayMode getDisplayModeForItems(ArrayList<? extends CollectionItem> items) {
        if (items.isEmpty()) {
            return DisplayMode.EXPLORER;
        }

        String album = items.get(0).getAlbum();
        boolean allSongs = true;
        boolean allDirectories = true;
        boolean allHaveArtwork = true;
        boolean allHaveAlbum = album != null;
        boolean allSameAlbum = true;
        for (CollectionItem item : items) {
            allSongs &= !item.isDirectory();
            allDirectories &= item.isDirectory();
            allHaveArtwork &= item.getArtwork() != null;
            allHaveAlbum &= item.getAlbum() != null;
            allSameAlbum &= album != null && album.equals(item.getAlbum());
        }

        if (allDirectories && allHaveArtwork && allHaveAlbum) {
            return DisplayMode.DISCOGRAPHY;
        }

        if (album != null && allSongs && allSameAlbum) {
            return DisplayMode.ALBUM;
        }

        return DisplayMode.EXPLORER;
    }

    private enum DisplayMode {
        EXPLORER,
        DISCOGRAPHY,
        ALBUM,
    }

    enum NavigationMode {
        PATH,
        SEARCH,
    }

    private void displayContent() {
        if (items == null) {
            return;
        }

        SmartSearchViewContent contentView = null;
        switch (getDisplayModeForItems(items)) {
            case EXPLORER:
                contentView = new SmartSearchViewExplorer(requireContext(), api, playbackQueue);
                break;
            case ALBUM:
                contentView = new SmartSearchViewAlbum(requireContext(), api, playbackQueue);
                break;
            case DISCOGRAPHY:
                contentView = new SmartSearchViewDiscography(requireContext(), api, playbackQueue);
                break;
        }

        contentView.setItems(items);
        contentView.setOnRefreshListener(onRefresh);

        contentHolder.removeAllViews();
        contentHolder.addView(contentView);
    }
}
