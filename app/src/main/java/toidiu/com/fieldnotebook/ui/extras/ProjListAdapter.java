package toidiu.com.fieldnotebook.ui.extras;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.views.ProjViewHolder;


/**
 * Created by toidiu on 4/2/16.
 */
public class ProjListAdapter extends RecyclerView.Adapter {

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private final ProjClickInterface projClickInterface;
    private List<FileObj> list = new ArrayList<>();

    public ProjListAdapter(ProjClickInterface projClickInterface) {
        this.projClickInterface = projClickInterface;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_proj_list, parent, false);
        return new ProjViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FileObj item = list.get(position);
        ProjViewHolder view = (ProjViewHolder) holder;
        view.bind(item, projClickInterface);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void refreshView(@NonNull List<FileObj> data) {
        list = data;
        notifyDataSetChanged();
    }

}
