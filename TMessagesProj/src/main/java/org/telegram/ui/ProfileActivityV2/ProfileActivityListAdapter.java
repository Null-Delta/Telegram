package org.telegram.ui.ProfileActivityV2;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.MessageObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import org.telegram.tgnet.TLRPC;

public class ProfileActivityListAdapter extends RecyclerListView.SelectionAdapter {
    public interface CellFactory {
        View cell(int type);
    }

    public interface CellConfigurator {
        void configure(RecyclerView.ViewHolder holder, CellData data);
    }

    public static class CellType {
        final static int
                TITLE_SUBTITLE_CELL = 1,
                TITLE_MULTILINE_SUBTITLE_CELL = 2,
                BOTTOM_SPACER = 3,
                SECTION_DIVIDER = 4,
                PERSONAL_CHANNEL = 5,
                SHARED_MEDIA = 6,
                TEXT_CELL = 7,
                ABOUT_LINK_CELL = 8,
                LOCATION_CELL = 9,
                HOURS_CELL = 10,
                USER_CELL = 11,
                APP_BUTTON_CELL = 12,
                SHADOW_TEXT_CELL = 13;
    }

    public static abstract class CellData {
        public abstract int getType();
        public Utilities.Callback3<View, Integer, Integer> onClick = null;
        public Utilities.Callback3<View, Integer, Integer> onLongClick = null;
        int getContext() { return 0; }
    }
    public static class TitleSubtitleCellData extends CellData {
        public CharSequence title;
        public CharSequence subtitle;
        public int trailImage;
        public boolean isMultiline;
        public Runnable onIconClick;
        @Override
        public int getType() {
            return isMultiline ? CellType.TITLE_MULTILINE_SUBTITLE_CELL : CellType.TITLE_SUBTITLE_CELL;
        }

        public TitleSubtitleCellData(CharSequence title, CharSequence subtitle, boolean isMultiline, int trailImage, Utilities.Callback3<View, Integer, Integer> onClick, Utilities.Callback3<View, Integer, Integer> onLongClick, Runnable onIconClick) {
            this.title = title;
            this.subtitle = subtitle;
            this.isMultiline = isMultiline;
            this.trailImage = trailImage;
            this.onClick = onClick;
            this.onLongClick = onLongClick;
            this.onIconClick = onIconClick;
        }
    }

    public static class TextCellData extends CellData {
        public CharSequence text;
        public CharSequence value;
        public int leadImage;
        public Drawable leadImageDrawable;

        public int textColorKey;
        public int imageColorKey;
        public boolean needDivider;

        @Override
        public int getType() {
            return CellType.TEXT_CELL;
        }

        public TextCellData(CharSequence text, CharSequence value, int leadImage, int textColorKey, int imageColorKey, boolean needDivider, Utilities.Callback0Return onClick) {
            this.text = text;
            this.value = value;
            this.leadImage = leadImage;
            this.leadImageDrawable = null;
            this.textColorKey = textColorKey;
            this.imageColorKey = imageColorKey;
            this.needDivider = needDivider;
            this.onClick = (view, x, y) -> {
                if (onClick == null) return;
                onClick.run();
            };
        }

        public TextCellData(CharSequence text, CharSequence value, Drawable leadImage, int textColorKey, int imageColorKey, boolean needDivider, Utilities.Callback0Return onClick) {
            this.text = text;
            this.value = value;
            this.leadImage = 0;
            this.leadImageDrawable = leadImage;
            this.textColorKey = textColorKey;
            this.imageColorKey = imageColorKey;
            this.needDivider = needDivider;
            this.onClick = (view, x, y) -> {
                if (onClick == null) return;
                onClick.run();
            };
        }
    }

    public static class BottomSpacerCellData extends CellData {
        @Override
        public int getType() { return CellType.BOTTOM_SPACER; }
    }

    public static class SectionDividerCellData extends CellData {
        @Override
        public int getType() { return CellType.SECTION_DIVIDER; }
    }

    public static class SharedMediaCellData extends CellData {
        @Override
        public int getType() { return CellType.SHARED_MEDIA; }
    }


    public static class ShadowTextCellData extends CellData {
        public String text;
        public SpannableStringBuilder sb;
        public int gravity;
        @Override
        public int getType() { return CellType.SHADOW_TEXT_CELL; }

        public ShadowTextCellData(String text, int gravity) {
            this.text = text;
            this.sb = null;
            this.gravity = gravity;
        }

        public ShadowTextCellData(SpannableStringBuilder sb, int gravity) {
            this.text = null;
            this.sb = sb;
            this.gravity = gravity;
        }
    }

    public static class LocationCellData extends CellData {
        public TLRPC.TL_businessLocation location;
        public boolean needDivider;
        @Override
        public int getType() { return CellType.LOCATION_CELL; }

        public LocationCellData(TLRPC.TL_businessLocation location, boolean needDivider, Utilities.Callback3<View, Integer, Integer> onClick, Utilities.Callback3<View, Integer, Integer> onLongClick) {
            this.location = location;
            this.needDivider = needDivider;
            this.onClick = onClick;
            this.onLongClick = onLongClick;
        }
    }

    public static class HoursCellData extends CellData {
        public TL_account.TL_businessWorkHours workHours;

        public boolean isExpanded = false;
        public boolean hoursShownMine = false;
        public boolean needDivider = false;

        @Override
        public int getType() { return CellType.HOURS_CELL; }

        public HoursCellData(
                TL_account.TL_businessWorkHours workHours, Utilities.Callback3<View, Integer, Integer> onClick, Utilities.Callback3<View, Integer, Integer> onLongClick
        ) {
            this.workHours = workHours;
            this.onClick = onClick;
            this.onLongClick = onLongClick;
        }
    }

    public static class AboutLinkCellData extends CellData {
        public String text;
        public String value;
        public boolean needParseLinks;
        @Override
        public int getType() { return CellType.ABOUT_LINK_CELL; }

        public AboutLinkCellData(String text, String value, boolean needParseLinks, Utilities.Callback3<View, Integer, Integer> onClick, Utilities.Callback3<View, Integer, Integer> onLongClick) {
            this.text = text;
            this.value = value;
            this.needParseLinks = needParseLinks;
            this.onClick = onClick;
            this.onLongClick = onLongClick;
        }
    }

    public static class PersonalChannelCellData extends CellData {
        public long personalChannelId;
        public MessageObject messageObject;

        public PersonalChannelCellData(
                long personalChannelId,
                MessageObject messageObject,
                Utilities.Callback3<View, Integer, Integer> onClick
        ) {
            this.personalChannelId = personalChannelId;
            this.messageObject = messageObject;
            this.onClick = onClick;
        }

        @Override
        public int getType() { return CellType.PERSONAL_CHANNEL; }
    }

    public static class UserCellData extends CellData {
        public TLRPC.User user;
        public TLRPC.ChatParticipant part;
        public String role;
        public boolean needDivider;

        public UserCellData(TLRPC.User user, TLRPC.ChatParticipant part, String role, boolean needDivider, Utilities.Callback4<View, Integer, Integer, TLRPC.ChatParticipant> onClick) {
            this.user = user;
            this.part = part;
            this.role = role;
            this.needDivider = needDivider;
            this.onClick = (view, x, y) -> {
                if (onClick == null) return;
                onClick.run(view, x, y, part);
            };
        }

        @Override
        public int getType() { return CellType.USER_CELL; }
    }

    public static class AppButtonCellData extends CellData {
        @Override
        public int getType() { return CellType.APP_BUTTON_CELL; }
    }


    private ArrayList<CellData> cells = new ArrayList<>();
    final private CellFactory cellFactory;
    final private CellConfigurator cellConfigurator;
    final private ProfileActivityV2 fragment;

    private boolean sharedMediaLayoutAttached = false;

    public ProfileActivityListAdapter(
            CellFactory cellFactory,
            ProfileActivityListCellConfigurator cellConfigurator,
            ProfileActivityV2 fragment
    ) {
        this.cellFactory = cellFactory;
        this.cellConfigurator = cellConfigurator;
        this.fragment = fragment;
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        int itemType = holder.getItemViewType();
        return itemType != CellType.BOTTOM_SPACER && itemType != CellType.SECTION_DIVIDER && itemType != CellType.SHARED_MEDIA;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerListView.Holder(cellFactory.cell(viewType));
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        cellConfigurator.configure(holder, cells.get(position));
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder.getItemViewType() == CellType.SHARED_MEDIA) {
            sharedMediaLayoutAttached = true;
        }

        if (holder.itemView instanceof TextDetailCell) {
            ((TextDetailCell) holder.itemView).textView.setLoading(fragment.loadingSpan);
            ((TextDetailCell) holder.itemView).valueTextView.setLoading(fragment.loadingSpan);
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (holder.getItemViewType() == CellType.SHARED_MEDIA) {
            sharedMediaLayoutAttached = false;
        }
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    @Override
    public int getItemViewType(int position) {
        return cells.get(position).getType();
    }

    public int itemPosition(Class<?> classType) {
        for(int i = 0; i < cells.size(); i++) {
            if (cells.get(i).getClass() == classType) { return i; }
        }

        return -1;
    }

    public void setItems(ArrayList<CellData> data) {
        cells = data;
    }

    public void callItemClick(int position, View view, int x, int y) {
        if (cells.get(position) == null || cells.get(position).onClick == null) { return; }
        cells.get(position).onClick.run(view, x, y);
    }


    public void callItemLongClick(int position, View view, int x, int y) {
        if (cells.get(position) == null || cells.get(position).onLongClick == null) { return; }
        cells.get(position).onLongClick.run(view, x, y);
    }

    public boolean hasSharedMedia() {
        return sharedMediaIndex() >= 0;
    }

    public int sharedMediaIndex() {
        for (int cellIndex = 0; cellIndex < cells.size(); cellIndex++) {
            if (cells.get(cellIndex).getType() == CellType.SHARED_MEDIA) { return cellIndex; }
        }

        return -1;
    }

    public  boolean isSharedMediaLayoutAttached() {
        return sharedMediaLayoutAttached;
    }
}