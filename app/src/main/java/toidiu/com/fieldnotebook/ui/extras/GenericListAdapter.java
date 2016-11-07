package toidiu.com.fieldnotebook.ui.extras;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.views.GenericViewHolder;


/**
 * Created by toidiu on 4/2/16.
 */
public class GenericListAdapter extends RecyclerView.Adapter {

    private final FileClickInterface fileClickInterface;
    private Set<FileObj> shareMultiple;
    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private List<FileObj> list = new ArrayList<>();

    public GenericListAdapter(FileClickInterface fileClickInterface, Set<FileObj> shareMultiple) {
        this.fileClickInterface = fileClickInterface;
        this.shareMultiple = shareMultiple;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generic_list, parent, false);
        return new GenericViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FileObj item = list.get(position);
        GenericViewHolder view = (GenericViewHolder) holder;
        view.bind(item, fileClickInterface, shareMultiple);
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
