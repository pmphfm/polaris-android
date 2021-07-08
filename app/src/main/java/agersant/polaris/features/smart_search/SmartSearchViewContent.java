package agersant.polaris.features.smart_search;

import android.content.Context;
import android.widget.FrameLayout;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

import java.util.ArrayList;

import agersant.polaris.CollectionItem;


abstract class SmartSearchViewContent extends FrameLayout {

    public SmartSearchViewContent(Context context) {
        super(context);
    }

    void setItems(ArrayList<? extends CollectionItem> items) {
    }

    void setOnRefreshListener(SwipyRefreshLayout.OnRefreshListener listener) {

    }
}
