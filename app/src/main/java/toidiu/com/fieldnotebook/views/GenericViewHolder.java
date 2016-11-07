package toidiu.com.fieldnotebook.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.util.Set;

import javax.inject.Inject;

import de.hdodenhof.circleimageview.CircleImageView;
import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.FieldNotebookApplication;
import toidiu.com.fieldnotebook.actions.BaseAction;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.ui.extras.FileClickInterface;
import toidiu.com.fieldnotebook.utils.MergePfdHelper;
import toidiu.com.fieldnotebook.utils.UtilsDate;
import toidiu.com.fieldnotebook.utils.UtilsView;

/**
 * Created by toidiu on 4/2/16.
 */
public class GenericViewHolder extends RecyclerView.ViewHolder {

    public final View containerView;
    public final TextView titleView;
    public final TextView modifiedView;
    public final CircleImageView fileIconView;
    private final View overflowView;
    @Inject
    MergePfdHelper mergePfdHelper;

    public GenericViewHolder(View itemView) {
        super(itemView);
        ((FieldNotebookApplication) FieldNotebookApplication.appContext).getAppComponent().inject(this);

        containerView = itemView.findViewById(R.id.container);
        titleView = (TextView) itemView.findViewById(R.id.title);
        modifiedView = (TextView) itemView.findViewById(R.id.modified);
        fileIconView = (CircleImageView) itemView.findViewById(R.id.file_icon);
        overflowView = itemView.findViewById(R.id.overflow);
    }

    public void bind(final FileObj item, final FileClickInterface fileClickInterface, Set<FileObj> shareMultiple) {
        this.titleView.setText(item.title);
        DateTime parse = DateTime.parse(item.lastModified);
        modifiedView.setText(UtilsDate.dateFormatter.print(parse));

        if (FileObj.isFolder(item.mime)) {
            Picasso.with(FieldNotebookApplication.appContext).load(R.drawable.ic_content_folder).into(fileIconView);
            overflowView.setVisibility(View.GONE);
        } else if (item.mime.equals(BaseAction.MIME_DWG1)) {
            overflowView.setVisibility(View.GONE);
        } else {
            overflowView.setVisibility(View.VISIBLE);
            Picasso.with(FieldNotebookApplication.appContext).load(R.drawable.ic_document).into(fileIconView);
        }

        if (shareMultiple.contains(item)){
            containerView.setBackgroundColor(FieldNotebookApplication.appContext.getResources().getColor(R.color.white_25));
        }else {
            containerView.setBackgroundColor(FieldNotebookApplication.appContext.getResources().getColor(R.color.white_15));
        }

        //Listeners-----------------
        this.containerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.mime.equals(BaseAction.FOLDER_MIME)) {
                    fileClickInterface.folderClicked(item);
                } else {
                    if (mergePfdHelper.isMerging) {
                        mergePfdHelper.origPdf = item;
                        fileClickInterface.doMerge();
                    } else {
                        fileClickInterface.editClicked(item);
                    }
                }
            }
        });

        overflowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mergePfdHelper.isMerging) {
                    UtilsView.fileClickPopup(overflowView, item, fileClickInterface);
                }
            }
        });

        this.containerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!mergePfdHelper.isMerging) {
                    if (!item.mime.equals(BaseAction.FOLDER_MIME)) {
                        UtilsView.fileLongClickPopup(containerView, item, fileClickInterface);
                    }
                }
                return true;
            }
        });

    }
}
