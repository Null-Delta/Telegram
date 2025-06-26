package org.telegram.ui.ProfileActivityV2;

import static org.telegram.messenger.LocaleController.getString;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Business.ProfileHoursCell;
import org.telegram.ui.Business.ProfileLocationCell;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;

public class ProfileActivityListCellConfigurator implements ProfileActivityListAdapter.CellConfigurator {
    final private MessagesController messagesController;
    final private ProfileActivityListAdapter listAdapter;

    final private Utilities.Callback0Return<ProfileChannelCell.ChannelMessageFetcher> channelFetcher;

    public ProfileActivityListCellConfigurator(MessagesController messagesController, ProfileActivityListAdapter listAdapter, Utilities.Callback0Return<ProfileChannelCell.ChannelMessageFetcher> channelFetcher) {
        this.messagesController = messagesController;
        this.listAdapter = listAdapter;
        this.channelFetcher = channelFetcher;
    }
    @Override
    public void configure(RecyclerView.ViewHolder holder, ProfileActivityListAdapter.CellData data) {
        switch (holder.getItemViewType()) {
            case ProfileActivityListAdapter.CellType.TITLE_SUBTITLE_CELL:
            case ProfileActivityListAdapter.CellType.TITLE_MULTILINE_SUBTITLE_CELL:
                TextDetailCell cell = (TextDetailCell) holder.itemView;
                ProfileActivityListAdapter.TitleSubtitleCellData cellData = (ProfileActivityListAdapter.TitleSubtitleCellData) data;
                if (cellData == null) { break; }

                cell.valueTextView.setText(cellData.title);
                cell.textView.setText(cellData.subtitle);


                if (cellData.trailImage != 0) {
                    Drawable icon = ContextCompat.getDrawable(cell.getContext(), cellData.trailImage);
                    icon.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_switch2TrackChecked), PorterDuff.Mode.MULTIPLY));

                    cell.setImage(icon);
                } else {
                    cell.setImage(null);
                }

                cell.setImageClickListener((view) -> {
                    cellData.onIconClick.run();
                });
                cell.requestLayout();
                break;
            case ProfileActivityListAdapter.CellType.BOTTOM_SPACER:
                holder.itemView.requestLayout();
                break;
            case ProfileActivityListAdapter.CellType.SECTION_DIVIDER:
                break;
            case ProfileActivityListAdapter.CellType.PERSONAL_CHANNEL:
                ProfileActivityListAdapter.PersonalChannelCellData channelCellData = (ProfileActivityListAdapter.PersonalChannelCellData) data;

                ((ProfileChannelCell) holder.itemView).set(
                        messagesController.getChat(channelCellData.personalChannelId),
                        channelCellData.messageObject
                );
                break;
            case ProfileActivityListAdapter.CellType.SHARED_MEDIA:
                break;
            case ProfileActivityListAdapter.CellType.TEXT_CELL:
                ProfileActivityListAdapter.TextCellData textCellData = (ProfileActivityListAdapter.TextCellData) data;
                TextCell textCell = (TextCell) holder.itemView;

                if (textCellData.value != null) {
                    if (textCellData.leadImageDrawable == null) {
                        textCell.setTextAndValueAndIcon(textCellData.text, textCellData.value, textCellData.leadImage, false);
                    } else {
                        textCell.setTextAndValueAndIcon(textCellData.text, textCellData.value, textCellData.leadImageDrawable, false);
                    }
                } else {
                    if (textCellData.leadImageDrawable == null) {
                        textCell.setTextAndIcon(textCellData.text, textCellData.leadImage, false);
                    } else {
                        textCell.setTextAndIcon(textCellData.text, textCellData.leadImageDrawable, false);
                    }
                }
                textCell.setColors(textCellData.imageColorKey, textCellData.textColorKey);
                textCell.setNeedDivider(textCellData.needDivider);
                break;
            case ProfileActivityListAdapter.CellType.ABOUT_LINK_CELL:
                ProfileActivityListAdapter.AboutLinkCellData aboutLinkCellData = (ProfileActivityListAdapter.AboutLinkCellData) data;
                AboutLinkCell aboutLinkCell = (AboutLinkCell) holder.itemView;
                aboutLinkCell.setTextAndValue(aboutLinkCellData.text, aboutLinkCellData.value, aboutLinkCellData.needParseLinks);
                break;
            case ProfileActivityListAdapter.CellType.LOCATION_CELL:
                ProfileActivityListAdapter.LocationCellData locationCellData = (ProfileActivityListAdapter.LocationCellData) data;

                ((ProfileLocationCell) holder.itemView).set(locationCellData.location, locationCellData.needDivider);
                break;
            case ProfileActivityListAdapter.CellType.HOURS_CELL:
                ProfileHoursCell hoursCell = (ProfileHoursCell) holder.itemView;
                ProfileActivityListAdapter.HoursCellData hoursCellData = (ProfileActivityListAdapter.HoursCellData) data;
                hoursCell.setOnTimezoneSwitchClick(view -> {
                    hoursCellData.hoursShownMine = !hoursCellData.hoursShownMine;
                    if (!hoursCellData.isExpanded) {
                        hoursCellData.isExpanded = true;
                    }
                    view.requestLayout();
                    listAdapter.notifyItemChanged(
                            listAdapter.itemPosition(ProfileActivityListAdapter.HoursCellData.class)
                    );
                });
                hoursCell.set(
                        hoursCellData.workHours,
                        hoursCellData.isExpanded,
                        hoursCellData.hoursShownMine,
                        hoursCellData.needDivider
                );
                break;
            case ProfileActivityListAdapter.CellType.USER_CELL:
                UserCell userCell = (UserCell) holder.itemView;
                ProfileActivityListAdapter.UserCellData userCelLData = (ProfileActivityListAdapter.UserCellData) data;

                userCell.setAdminRole(userCelLData.role);
                userCell.setData(userCelLData.user, null, null, 0, userCelLData.needDivider);
                break;
            case ProfileActivityListAdapter.CellType.APP_BUTTON_CELL:
                break;
            case ProfileActivityListAdapter.CellType.SHADOW_TEXT_CELL:
                TextInfoPrivacyCell shadowTextCell = (TextInfoPrivacyCell) holder.itemView;
                ProfileActivityListAdapter.ShadowTextCellData shadowTextData = (ProfileActivityListAdapter.ShadowTextCellData) data;
                shadowTextCell.setTextGravity(shadowTextData.gravity);
                shadowTextCell.setLinkTextRippleColor(null);

                if (shadowTextData.sb != null) {
                    shadowTextCell.setText(shadowTextData.sb);
                } else {
                    shadowTextCell.setText(shadowTextData.text);
                }
                break;
        }
    }
}
