package toidiu.com.fieldnotebook.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.ui.extras.FileArchiveListInterface;

/**
 * Created by toidiu on 4/2/16.
 */
public class FileArchiveViewHolder extends RecyclerView.ViewHolder {

    public final View claimContainerView;
    public final TextView titleView;
    public final TextView claimUserView;
    public final View unArchiveView;

    public FileArchiveViewHolder(View itemView) {
        super(itemView);

        claimContainerView = itemView.findViewById(R.id.claim_container);
        titleView = (TextView) itemView.findViewById(R.id.title);
        claimUserView = (TextView) itemView.findViewById(R.id.claim_user);
        unArchiveView = itemView.findViewById(R.id.unarchive);
    }

    public void bind(final FileObj item, final FileArchiveListInterface fileListInterface) {
        this.titleView.setText(item.title);
        this.unArchiveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileListInterface.unarchiveFile(item);
            }
        });


        if (item.claimUser != null) {
            this.claimUserView.setText(item.claimUser);
            this.claimContainerView.setVisibility(View.VISIBLE);
        } else {
            this.claimContainerView.setVisibility(View.GONE);
        }
    }
}
