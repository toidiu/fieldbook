package toidiu.com.fieldnotebook.views;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.actions.BaseAction;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.ui.extras.FileClickInterface;
import toidiu.com.fieldnotebook.utils.UtilsView;

/**
 * Created by toidiu on 4/2/16.
 */
public class ProjDetailViewHolder extends RecyclerView.ViewHolder {

//    public final View containerView;
    public final TextView titleView;
    private final CardView folderCard;

    public ProjDetailViewHolder(View itemView) {
        super(itemView);

//        containerView = itemView.findViewById(R.id.container);
        titleView = (TextView) itemView.findViewById(R.id.title);
        folderCard = (CardView) itemView.findViewById(R.id.folder_card);
    }

    public void bind(final FileObj item, final FileClickInterface fileClickInterface) {
        this.titleView.setText(item.title);
        this.folderCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.mime.equals(BaseAction.FOLDER_MIME)) {
                    fileClickInterface.folderClicked(item);
                } else {
                    UtilsView.fileClickPopup(folderCard, item, fileClickInterface);
                }
            }
        });

        this.folderCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!item.mime.equals(BaseAction.FOLDER_MIME)) {
                    UtilsView.fileLongClickPopup(folderCard, item, fileClickInterface);
                }
                return true;
            }
        });


        if (item.title.matches("(?i)drawing.*|proposal.*|note.*")) {
            colorRed();
        } else if (item.title.matches("(?i).*mail.*|Insurance.*|Transmittal.*")) {
            colorBlue();
        } else if (item.title.matches("(?i).*contract.*|Fab.*|Close.*|Purchase.*")) {
            colorGreen();
        } else if (item.title.matches("(?i)Approval.*|Certificate.*|Inventory.*|Photo.*")) {
            colorTeal();
        } else if (item.title.matches("(?i)Billing.*|Hardware.*|Quote.*")) {
            colorYellow();
        } else {

            int i = Math.abs(item.title.hashCode()) % 4;
            switch (i) {
                case 0:
                    colorYellow();
                    break;
                case 1:
                    colorRed();
                    break;
                case 2:
                    colorTeal();
                    break;
                case 3:
                    colorBlue();
                    break;
                case 4:
                    colorGreen();
                    break;

            }
        }
    }

    private void colorTeal() {
        folderCard.setCardBackgroundColor(itemView.getResources().getColor(R.color.folder_teal));
        titleView.setTextColor(itemView.getResources().getColor(R.color.black));
    }

    private void colorYellow() {
        folderCard.setCardBackgroundColor(itemView.getResources().getColor(R.color.folder_yellow));
        titleView.setTextColor(itemView.getResources().getColor(R.color.black));
    }

    private void colorGreen() {
        folderCard.setCardBackgroundColor(itemView.getResources().getColor(R.color.folder_green));
        titleView.setTextColor(itemView.getResources().getColor(R.color.white));
    }

    private void colorBlue() {
        folderCard.setCardBackgroundColor(itemView.getResources().getColor(R.color.folder_blue));
        titleView.setTextColor(itemView.getResources().getColor(R.color.white));
    }

    private void colorRed() {
        folderCard.setCardBackgroundColor(itemView.getResources().getColor(R.color.folder_red));
        titleView.setTextColor(itemView.getResources().getColor(R.color.white));
    }
}
