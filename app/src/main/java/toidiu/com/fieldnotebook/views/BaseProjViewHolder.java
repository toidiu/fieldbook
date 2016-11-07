package toidiu.com.fieldnotebook.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.actions.BaseAction;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.ui.extras.BaseProjClickInterface;

/**
 * Created by toidiu on 4/2/16.
 */
public class BaseProjViewHolder extends RecyclerView.ViewHolder {

    public final View containerView;
    public final View ownContainerView;
    public final TextView titleView;
    public final TextView ownUserView;

    public BaseProjViewHolder(View itemView) {
        super(itemView);

        containerView = itemView.findViewById(R.id.container);
        ownContainerView = itemView.findViewById(R.id.own_container);
        titleView = (TextView) itemView.findViewById(R.id.title);
        ownUserView = (TextView) itemView.findViewById(R.id.own_user);
    }

    public void bind(final FileObj item, final BaseProjClickInterface fileClickInterface) {
        this.titleView.setText(item.title);
        this.containerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.mime.equals(BaseAction.FOLDER_MIME)) {
                    fileClickInterface.folderClicked(item);
                }
            }
        });

        if (item.owner != null) {
            this.ownUserView.setText(item.owner);
        } else {
            this.ownContainerView.setVisibility(View.GONE);
        }
    }
}
