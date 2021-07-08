package agersant.polaris.features.smart_search;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import agersant.polaris.PolarisApplication;
import agersant.polaris.PolarisState;
import agersant.polaris.R;
import agersant.polaris.api.API;
import agersant.polaris.databinding.FragmentSmartSearchCollectionBinding;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


public class CollectionFragment extends Fragment {

    private FragmentSmartSearchCollectionBinding binding;
    private API api;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        PolarisState state = PolarisApplication.getState();
        this.api = state.api;

        binding = FragmentSmartSearchCollectionBinding.inflate(inflater);

        binding.smartSearchButton.setOnClickListener(this::smartSearchDirectories);

        binding.smartSearchField.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER) && !binding.smartSearchField.getText().toString().isEmpty()) {
                    // Perform action on key press
                    smartSearchDirectories(getView());
                    return true;
                }
                return false;
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateButtons();
    }




    public void smartSearchDirectories(View view) {
        Bundle args = new Bundle();
        String query = binding.smartSearchField.getText().toString();
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        args.putSerializable(SmartSearchFragment.NAVIGATION_MODE, SmartSearchFragment.SEARCH);
        args.putSerializable(SmartSearchFragment.SEARCH, query);
        Navigation.findNavController(view).navigate(R.id.action_nav_collection_to_nav_search, args);
    }

    private void updateButtons() {
        boolean isOffline = api.isOffline();

    }
}
