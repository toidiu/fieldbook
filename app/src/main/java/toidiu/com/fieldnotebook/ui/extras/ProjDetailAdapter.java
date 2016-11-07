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
import toidiu.com.fieldnotebook.views.ProjDetailViewHolder;


/**
 * Created by toidiu on 4/2/16.
 */
public class ProjDetailAdapter extends RecyclerView.Adapter {

    private final FileClickInterface fileClickInterface;

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private List<FileObj> list = new ArrayList<>();

    public ProjDetailAdapter(FileClickInterface fileClickInterface) {
        this.fileClickInterface = fileClickInterface;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_proj_detail, parent, false);
        return new ProjDetailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FileObj item = list.get(position);
        ProjDetailViewHolder view = (ProjDetailViewHolder) holder;
        view.bind(item, fileClickInterface);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void refreshView(@NonNull List<FileObj> data) {
        list = data;
        notifyDataSetChanged();
    }

    public List<FileObj> getList() {
        return list;
    }

}
