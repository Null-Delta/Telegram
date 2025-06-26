package org.telegram.ui.ProfileActivityV2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Business.ProfileHoursCell;
import org.telegram.ui.Business.ProfileLocationCell;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;
import org.telegram.tgnet.TLRPC;

public class ProfileActivityListCellFactory implements ProfileActivityListAdapter.CellFactory {
    final private Context context;
    final private Theme.ResourcesProvider resourcesProvider;
    final private RecyclerListView listView;
    final private ProfileActivityV2 fragment;
    final private SharedMediaLayout sharedMediaLayout;

    private ProfileActivityListAdapter listAdapter() {
        return (ProfileActivityListAdapter) listView.getAdapter();
    }
    private LinearLayoutManager layoutManager() {
        return (LinearLayoutManager) listView.getLayoutManager();
    }

    public ProfileActivityListCellFactory(
            Context context,
            Theme.ResourcesProvider resourcesProvider,
            RecyclerListView listView,
            ProfileActivityV2 fragment,
            SharedMediaLayout sharedMediaLayout
    ) {
        this.context = context;
        this.resourcesProvider = resourcesProvider;
        this.listView = listView;
        this.fragment = fragment;
        this.sharedMediaLayout = sharedMediaLayout;
    }

    @Override
    public View cell(int type) {
        View view = new View(context);

        switch (type) {
            case ProfileActivityListAdapter.CellType.TITLE_MULTILINE_SUBTITLE_CELL:
            case ProfileActivityListAdapter.CellType.TITLE_SUBTITLE_CELL:
                final TextDetailCell textDetailCell = new TextDetailCell(context, resourcesProvider, type == ProfileActivityListAdapter.CellType.TITLE_MULTILINE_SUBTITLE_CELL);
                textDetailCell.setContentDescriptionValueFirst(true);
                textDetailCell.setBackgroundColor(fragment.getThemedColor(Theme.key_windowBackgroundWhite));
                view = textDetailCell;
                break;
            case ProfileActivityListAdapter.CellType.BOTTOM_SPACER:
                final View spacer = new View(context) {

                    private int lastPaddingHeight = 0;
                    private int lastListViewHeight = 0;

                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        if (lastListViewHeight != listView.getMeasuredHeight()) {
                            lastPaddingHeight = 0;
                        }
                        lastListViewHeight = listView.getMeasuredHeight();
                        int n = listView.getChildCount();
                        if (n == listAdapter().getItemCount()) {
                            int totalHeight = 0;
                            for (int i = 0; i < n; i++) {
                                View view = listView.getChildAt(i);
                                int p = listView.getChildAdapterPosition(view);
                                if (p >= 0 && listAdapter().getItemViewType(p) != ProfileActivityListAdapter.CellType.BOTTOM_SPACER) {
                                    totalHeight += listView.getChildAt(i).getMeasuredHeight();
                                }
                            }
                            int paddingHeight = (fragment.fragmentView == null ? 0 : fragment.fragmentView.getMeasuredHeight()) - ActionBar.getCurrentActionBarHeight() - AndroidUtilities.statusBarHeight - totalHeight;
                            setMeasuredDimension(listView.getMeasuredWidth(), Math.max(0, lastPaddingHeight = paddingHeight));
                        } else {
                            setMeasuredDimension(listView.getMeasuredWidth(), lastPaddingHeight);
                        }
                    }
                };
                view.setBackground(new ColorDrawable(Color.TRANSPARENT));
                spacer.setBackgroundColor(fragment.getThemedColor(Theme.key_windowBackgroundGray));
                view = spacer;
                break;
            case ProfileActivityListAdapter.CellType.SECTION_DIVIDER:
                View divider = new ShadowSectionCell(context, resourcesProvider);
                divider.setBackgroundColor(fragment.getThemedColor(Theme.key_windowBackgroundGray));
                view = divider;
                break;
            case ProfileActivityListAdapter.CellType.PERSONAL_CHANNEL:
                view = new ProfileChannelCell(fragment, false);
                view.setBackgroundColor(fragment.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case ProfileActivityListAdapter.CellType.SHARED_MEDIA:
                if (sharedMediaLayout.getParent() != null) {
                    ((ViewGroup) sharedMediaLayout.getParent()).removeView(sharedMediaLayout);
                }
                view = sharedMediaLayout;
                break;
            case ProfileActivityListAdapter.CellType.TEXT_CELL:
                TextCell textCell = new TextCell(context, resourcesProvider);
                textCell.setBackgroundColor(fragment.getThemedColor(Theme.key_windowBackgroundWhite));
                view = textCell;
                break;
            case ProfileActivityListAdapter.CellType.ABOUT_LINK_CELL:
                view = new AboutLinkCell(context, fragment, resourcesProvider) {
                    @Override
                    protected void didPressUrl(String url, Browser.Progress progress) {
                        fragment.openUrl(url, progress);
                    }

                    @Override
                    protected void didResizeEnd() {
                        layoutManager().mIgnoreTopPadding = false;
                    }

                    @Override
                    protected void didResizeStart() {
                        layoutManager().mIgnoreTopPadding = true;
                    }
                };
                view.setBackgroundColor(fragment.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case ProfileActivityListAdapter.CellType.LOCATION_CELL:
                view = new ProfileLocationCell(context, resourcesProvider);
                view.setBackgroundColor(fragment.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case ProfileActivityListAdapter.CellType.HOURS_CELL:
                view = new ProfileHoursCell(context, resourcesProvider);
                view.setBackgroundColor(fragment.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case ProfileActivityListAdapter.CellType.USER_CELL:
                view = new UserCell(context, 6, 0, true, resourcesProvider);
                view.setBackgroundColor(fragment.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case ProfileActivityListAdapter.CellType.APP_BUTTON_CELL:
                FrameLayout frameLayout = new FrameLayout(context);
                ButtonWithCounterView button = new ButtonWithCounterView(context, resourcesProvider);
                button.setText(LocaleController.getString(R.string.ProfileBotOpenApp), false);
                button.setOnClickListener(v -> {
                    TLRPC.User bot = fragment.getMessagesController().getUser(fragment.dataProvider.userId);
                    fragment.getMessagesController().openApp(fragment, bot, null, fragment.getClassGuid(), null);
                });
                frameLayout.addView(button, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.FILL, 18, 14, 18, 14));
                view = frameLayout;
                view.setBackgroundColor(fragment.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case ProfileActivityListAdapter.CellType.SHADOW_TEXT_CELL:
                TextInfoPrivacyCell cell = new TextInfoPrivacyCell(context, resourcesProvider);
                cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                cell.getTextView().setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
                cell.getTextView().setMovementMethod(null);
                cell.getTextView().setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));
                cell.setBackgroundColor(fragment.getThemedColor(Theme.key_windowBackgroundGray));
                view = cell;
                break;
        }

        if (type != ProfileActivityListAdapter.CellType.SHARED_MEDIA) {
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        }

        return view;
    }
}
