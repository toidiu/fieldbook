package toidiu.com.fieldnotebook.views;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.actions.BaseAction;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.ui.extras.ProjClickInterface;

/**
 * Created by toidiu on 4/2/16.
 */
public class ProjViewHolder extends RecyclerView.ViewHolder {

    public final View containerView;
    public final View claimContainerView;
    public final TextView titleView;
    public final TextView claimUserView;
    public final FloatingActionButton fabView;

    public ProjViewHolder(View itemView) {
        super(itemView);

        containerView = itemView.findViewById(R.id.container);
        claimContainerView = itemView.findViewById(R.id.claim_container);
        titleView = (TextView) itemView.findViewById(R.id.title);
        claimUserView = (TextView) itemView.findViewById(R.id.claim_user);
        fabView = (FloatingActionButton) itemView.findViewById(R.id.fab);
    }

    public void bind(final FileObj item, final ProjClickInterface projClickInterface) {
        titleView.setText(item.title);
        if (item.title.equals(BaseAction.PHOTOS_FOLDER_SETUP) || item.title.equals(BaseAction.ARCHIVE_FOLDER_SETUP)) {
            fabView.setVisibility(View.GONE);
        } else {
            fabView.setVisibility(View.VISIBLE);
        }

        containerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.mime.equals(BaseAction.FOLDER_MIME)) {
                    projClickInterface.folderClicked(item);
                } else {
                    projClickInterface.fileClicked(item);
                }
            }
        });

        if (item.claimUser != null && !item.claimUser.equals("")) {
            claimUserView.setText(item.claimUser);
            claimContainerView.setVisibility(View.VISIBLE);
        } else {
            claimContainerView.setVisibility(View.GONE);
        }
        fabView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                projClickInterface.addClicked(item);
            }
        });
    }
}
