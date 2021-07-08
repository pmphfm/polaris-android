package agersant.polaris.features.smart_search;

import java.util.ArrayList;

import agersant.polaris.CollectionItem;
import androidx.recyclerview.widget.RecyclerView;


abstract class SmartSearchAdapter
    extends RecyclerView.Adapter<SmartSearchItemHolder> {

    ArrayList<? extends CollectionItem> items;

    SmartSearchAdapter() {
        super();
        setItems(new ArrayList<>());
    }

    void setItems(ArrayList<? extends CollectionItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(SmartSearchItemHolder holder, int position) {
        holder.bindItem(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
