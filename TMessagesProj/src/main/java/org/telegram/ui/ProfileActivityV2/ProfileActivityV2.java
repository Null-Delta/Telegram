package org.telegram.ui.ProfileActivityV2;

import static androidx.core.content.ContextCompat.getSystemService;
import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.Stars.StarsIntroActivity.formatStarsAmountShort;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Property;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScrollerCustom;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BillingController;
import org.telegram.messenger.BirthdayController;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.TopicsController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.tgnet.tl.TL_bots;
import org.telegram.tgnet.tl.TL_fragment;
import org.telegram.tgnet.tl.TL_stars;
import org.telegram.ui.AccountFrozenAlert;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.OKLCH;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.AutoDeleteMessagesActivity;
import org.telegram.ui.BasePermissionsActivity;
import org.telegram.ui.Business.OpeningHoursActivity;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.SettingsSuggestionCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChangeUsernameActivity;
import org.telegram.ui.ChannelMonetizationLayout;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ChatEditActivity;
import org.telegram.ui.ChatRightsEditActivity;
import org.telegram.ui.ChatUsersActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedEmojiSpan;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.AutoDeletePopupWrapper;
import org.telegram.ui.Components.BackButtonMenu;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ButtonBounce;
import org.telegram.ui.Components.ChatActivityInterface;
import org.telegram.ui.Components.ChatNotificationsPopupWrapper;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.Forum.ForumUtilities;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MediaActivity;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.Premium.PremiumGradient;
import org.telegram.ui.Components.Premium.boosts.UserSelectorBottomSheet;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.SharedMediaLayout;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.TimerDrawable;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.ContactAddActivity;
import org.telegram.ui.DataSettingsActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.FiltersSetupActivity;
import org.telegram.ui.FragmentUsernameBottomSheet;
import org.telegram.ui.Gifts.GiftSheet;
import org.telegram.ui.GroupCreateActivity;
import org.telegram.ui.IdenticonActivity;
import org.telegram.ui.LanguageSelectActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LiteModeSettingsActivity;
import org.telegram.ui.LocationActivity;
import org.telegram.ui.LogoutActivity;
import org.telegram.ui.MemberRequestsActivity;
import org.telegram.ui.NotificationsSettingsActivity;
import org.telegram.ui.PeerColorActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.PinchToZoomHelper;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.PrivacySettingsActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.ProfileActivityV2.ProfileHeader.ProfileHeaderView;
import org.telegram.ui.ProfileBirthdayEffect;
import org.telegram.ui.ProfileNotificationsActivity;
import org.telegram.ui.QrActivity;
import org.telegram.ui.ReportBottomSheet;
import org.telegram.ui.RestrictedLanguagesSelectActivity;
import org.telegram.ui.SelectAnimatedEmojiDialog;
import org.telegram.ui.SessionsActivity;
import org.telegram.ui.Stars.BotStarsActivity;
import org.telegram.ui.Stars.BotStarsController;
import org.telegram.ui.Stars.StarGiftSheet;
import org.telegram.ui.Stars.StarsController;
import org.telegram.ui.Stars.StarsIntroActivity;
import org.telegram.ui.StatisticActivity;
import org.telegram.ui.StickersActivity;
import org.telegram.ui.Stories.StoriesController;
import org.telegram.ui.Stories.recorder.StoryRecorder;
import org.telegram.ui.ThemeActivity;
import org.telegram.ui.TopicCreateFragment;
import org.telegram.ui.TopicsFragment;
import org.telegram.ui.TopicsNotifySettingsFragments;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.AffiliateProgramFragment;
import org.telegram.ui.bots.BotWebViewAttachedSheet;
import org.telegram.ui.bots.ChannelAffiliateProgramsFragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import kotlin.jvm.internal.Lambda;

public class ProfileActivityV2 extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, SharedMediaLayout.Delegate, SharedMediaLayout.SharedMediaPreloaderDelegate, DialogsActivity.DialogsActivityDelegate, ImageUpdater.ImageUpdaterDelegate {

    public static long expandingAnimationDuration = 300;

    public static class ShowDrawable extends Drawable implements SimpleTextView.PressableDrawable {

        public final AnimatedTextView.AnimatedTextDrawable textDrawable;
        public final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        public ShowDrawable(String string) {
            textDrawable = new AnimatedTextView.AnimatedTextDrawable();
            textDrawable.setCallback(new Callback() {
                @Override
                public void invalidateDrawable(@NonNull Drawable who) {
                    if (view != null) {
                        view.invalidate();
                    }
                }
                @Override
                public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {}
                @Override
                public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {}
            });
            textDrawable.setText(string);
            textDrawable.setTextSize(dp(11));
            textDrawable.setGravity(Gravity.CENTER);
            backgroundPaint.setColor(0x1f000000);
        }

        private int textColor;
        public void setBackgroundColor(int backgroundColor) {
            if (backgroundPaint.getColor() != backgroundColor) {
                backgroundPaint.setColor(backgroundColor);
                invalidateSelf();
            }
        }
        public void setTextColor(int textColor) {
            if (this.textColor != textColor) {
                this.textColor = textColor;
                invalidateSelf();
            }
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            final float alpha = this.alpha * this.alpha2;
            if (alpha <= 0) return;
            AndroidUtilities.rectTmp.set(getBounds());
            canvas.save();
            final float s = bounce.getScale(0.1f);
            canvas.scale(s, s, AndroidUtilities.rectTmp.centerX(), AndroidUtilities.rectTmp.centerY());
            final int wasAlpha = backgroundPaint.getAlpha();
            backgroundPaint.setAlpha((int) (wasAlpha * alpha));
            canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(20), dp(20), backgroundPaint);
            backgroundPaint.setAlpha(wasAlpha);
            textDrawable.setTextColor(textColor);
            textDrawable.setAlpha((int) (0xFF * alpha));
            textDrawable.setBounds((int) AndroidUtilities.rectTmp.left, (int) AndroidUtilities.rectTmp.top, (int) AndroidUtilities.rectTmp.right, (int) AndroidUtilities.rectTmp.bottom);
            textDrawable.draw(canvas);
            canvas.restore();
        }

        private float alpha = 1f, alpha2 = 1f;
        @Override
        public void setAlpha(int alpha) {
            this.alpha = alpha / 255f;
            invalidateSelf();
        }

        public void setAlpha2(float alpha) {
            this.alpha2 = alpha;
            invalidateSelf();
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getIntrinsicWidth() {
            return (int) (textDrawable.getAnimateToWidth() + dp(11));
        }

        @Override
        public int getIntrinsicHeight() {
            return dp(17.33f);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }

        private boolean pressed;
        private final ButtonBounce bounce = new ButtonBounce(null) {
            @Override
            public void invalidate() {
                invalidateSelf();
            }
        };

        @Override
        public void setPressed(boolean pressed) {
            bounce.setPressed(pressed);
            this.pressed = pressed;
        }

        @Override
        public boolean isPressed() {
            return pressed;
        }

        private View view;
        public void setView(View view) {
            this.view = view;
        }
    }

    @Override
    public boolean didSelectDialogs(DialogsActivity fragment, ArrayList<MessagesStorage.TopicKey> dids, CharSequence message, boolean param, boolean notify, int scheduleDate, TopicsFragment topicsFragment) {
        long did = dids.get(0).dialogId;
        Bundle args = new Bundle();
        args.putBoolean("scrollToTopOnResume", true);
        if (DialogObject.isEncryptedDialog(did)) {
            args.putInt("enc_id", DialogObject.getEncryptedChatId(did));
        } else if (DialogObject.isUserDialog(did)) {
            args.putLong("user_id", did);
        } else if (DialogObject.isChatDialog(did)) {
            args.putLong("chat_id", -did);
        }
        if (!getMessagesController().checkCanOpenChat(args, fragment)) {
            return false;
        }

        getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
        presentFragment(new ChatActivity(args), true);
        removeSelfFromStack();
        TLRPC.User user = dataProvider.getUser();
        getSendMessagesHelper().sendMessage(SendMessagesHelper.SendMessageParams.of(user, did, null, null, null, null, notify, scheduleDate));
        if (!TextUtils.isEmpty(message)) {
            AccountInstance accountInstance = AccountInstance.getInstance(currentAccount);
            SendMessagesHelper.prepareSendingText(accountInstance, message.toString(), did, notify, scheduleDate, 0);
        }
        return true;
    }

    private class NestedFrameLayout extends SizeNotifierFrameLayout implements NestedScrollingParent3 {

        final private NestedScrollingParentHelper nestedScrollingParentHelper;

        public NestedFrameLayout(Context context) {
            super(context);
            nestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        }

        @Override
        public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
            try {
                if (target == scrollController.listView && listAdapter.isSharedMediaLayoutAttached()) {
                    RecyclerListView innerListView = sharedMediaLayout.getCurrentListView();
                    int top = sharedMediaLayout.getTop();
                    if (top == 0) {
                        consumed[1] = dyUnconsumed;
                        innerListView.scrollBy(0, dyUnconsumed);
                    }
                }
            } catch (Throwable e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> {
                    try {
                        RecyclerListView innerListView = sharedMediaLayout.getCurrentListView();
                        if (innerListView != null && innerListView.getAdapter() != null) {
                            innerListView.getAdapter().notifyDataSetChanged();
                        }
                    } catch (Throwable ignored) {

                    }
                });
            }
        }

        @Override
        public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
            if (target == scrollController.listView && listAdapter.hasSharedMedia() && listAdapter.isSharedMediaLayoutAttached()) {
                boolean searchVisible = actionBar.isSearchFieldVisible();
                int t = sharedMediaLayout.getTop();
                if (dy < 0) {
                    boolean scrolledInner = false;
                    if (t <= 0) {
                        RecyclerListView innerListView = sharedMediaLayout.getCurrentListView();
                        if (innerListView != null) {
                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) innerListView.getLayoutManager();
                            int pos = linearLayoutManager.findFirstVisibleItemPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                RecyclerView.ViewHolder holder = innerListView.findViewHolderForAdapterPosition(pos);
                                int top = holder != null ? holder.itemView.getTop() : -1;
                                int paddingTop = innerListView.getPaddingTop();
                                if (top != paddingTop || pos != 0) {
                                    consumed[1] = pos != 0 ? dy : Math.max(dy, (top - paddingTop));
                                    innerListView.scrollBy(0, dy);
                                    scrolledInner = true;
                                }
                            }
                        }
                    }
                    if (searchVisible) {
                        if (!scrolledInner && t < 0) {
                            consumed[1] = dy - Math.max(t, dy);
                        } else {
                            consumed[1] = dy;
                        }
                    }
                } else {
                    if (searchVisible) {
                        RecyclerListView innerListView = sharedMediaLayout.getCurrentListView();
                        consumed[1] = dy;
                        if (t > 0) {
                            consumed[1] -= dy;
                        }
                        if (innerListView != null && consumed[1] > 0) {
                            innerListView.scrollBy(0, consumed[1]);
                        }
                    }
                }
            }
        }

        @Override
        public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
            return listAdapter.hasSharedMedia() && axes == ViewCompat.SCROLL_AXIS_VERTICAL;
        }

        @Override
        public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
            nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        }

        @Override
        public void onStopNestedScroll(@NonNull View target, int type) {
            nestedScrollingParentHelper.onStopNestedScroll(target);
        }

        @Override
        public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {

        }

        @Override
        protected void drawList(Canvas blurCanvas, boolean top, ArrayList<IViewWithInvalidateCallback> views) {
            super.drawList(blurCanvas, top, views);
            blurCanvas.save();
            blurCanvas.translate(0, scrollController.listView.getY());
            sharedMediaLayout.drawListForBlur(blurCanvas, views);
            blurCanvas.restore();
        }
    }

    private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider() {

        @Override
        public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index, boolean needPreview, boolean closing) {
            if (fileLocation == null) {
                return null;
            }

            TLRPC.FileLocation photoBig = null;
            if (dataProvider.userId != 0) {
                TLRPC.User user = dataProvider.getUser();
                if (user != null && user.photo != null && user.photo.photo_big != null) {
                    photoBig = user.photo.photo_big;
                }
            } else if (dataProvider.chatId != 0) {
                TLRPC.Chat chat = dataProvider.getChat();
                if (chat != null && chat.photo != null && chat.photo.photo_big != null) {
                    photoBig = chat.photo.photo_big;
                }
            }

            if (photoBig != null && photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                int[] coords = new int[2];
                profileHeaderView.avatarImage.getLocationInWindow(coords);
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1] - (Build.VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight);
                object.parentView = profileHeaderView.avatarImage;
                object.imageReceiver = profileHeaderView.avatarImage.getImageReceiver();
                if (dataProvider.userId != 0) {
                    object.dialogId = dataProvider.userId;
                } else if (dataProvider.chatId != 0) {
                    object.dialogId = -dataProvider.chatId;
                }
                object.thumb = object.imageReceiver.getBitmapSafe();
                object.size = -1;
                object.radius = profileHeaderView.avatarImage.getImageReceiver().getRoundRadius(true);
                object.scale = profileHeaderView.avatarExpandAnimationContainer.getScaleX();
                object.canEdit = dataProvider.userId == getUserConfig().clientUserId;
                return object;
            }
            return null;
        }

        @Override
        public void willHidePhotoViewer() {
            profileHeaderView.avatarImage.getImageReceiver().setVisible(true, true);
        }

        @Override
        public void openPhotoForEdit(String file, String thumb, boolean isVideo) {
            imageUpdater.openPhotoForEdit(file, thumb, 0, isVideo);
        }
    };

    public static class HeaderSizeConfiguration {
        public float minimizedHeight;
        public float defaultHeight;
        public float expandedHeight;

        public HeaderSizeConfiguration(float minimizedHeight, float defaultHeight, float expandedHeight) {
            this.minimizedHeight = minimizedHeight;
            this.defaultHeight = defaultHeight;
            this.expandedHeight = expandedHeight;
        }
    }

    private ProfileActivityListAdapter listAdapter;
    private Theme.ResourcesProvider resourcesProvider;
    public ProfileScrollController scrollController;
    private ProfileHeaderView profileHeaderView;
    public SharedMediaLayout sharedMediaLayout;
    private SharedMediaLayout.SharedMediaPreloader sharedMediaPreloader;
    PinchToZoomHelper pinchToZoomHelper;

    private long mergeDialogId;
    AutoDeletePopupWrapper autoDeletePopupWrapper;

    public ProfileActivityDataProvider dataProvider;
    public boolean fragmentViewAttached = false;
    private boolean savedScrollToSharedMedia = false;

    private boolean openSimilar;
    public boolean openCommonChats;
    public boolean openGifts;
    private boolean openedGifts;
    private String vcardPhone;

    public TLRPC.Chat currentChat;
    private ArrayList<Integer> sortedUsers;

    private ProfileChannelCell.ChannelMessageFetcher profileChannelMessageFetcher;

    ActionBarMenuItem actionBarMoreItem;
    ActionBarMenuItem actionBarEditItem;

    TimerDrawable autoDeleteItemDrawable;
    private ActionBarMenuSubItem autoDeleteItem;

    private ImageView ttlIconView;

    private boolean fragmentOpened = false;
    private boolean creatingChat;
    boolean hasVoiceChatItem = false;
    private boolean isInLandscapeMode = false;
    private boolean mediaHeaderVisible = false;
    private boolean editItemVisible;
    private boolean isFragmentPhoneNumber = false;
    boolean canSearchMembers = false;
    private TL_account.TL_password currentPassword;
    private UndoView undoView;

    private ActionBarMenuSubItem setUsernameItem;
    private ActionBarMenuSubItem linkItem;
    private ImageUpdater imageUpdater;

    private ActionBarMenuSubItem editColorItem;

    public ProfileBirthdayEffect.BirthdayEffectFetcher birthdayFetcher;
    public boolean createdBirthdayFetcher;

    private final ArrayList<TLRPC.ChatParticipant> visibleChatParticipants = new ArrayList<>();
    private final ArrayList<Integer> visibleSortedUsers = new ArrayList<>();
    private int usersForceShowingIn = 0;

    public long getDialogId() {
        return dataProvider.getDialogId();
    }

    public boolean isChat() {
        return dataProvider.chatId != 0;
    }

    public TLRPC.UserFull getUserInfo() {
        return dataProvider.getUserFull();
    }

    public boolean isMyProfile() {
        return dataProvider.isMyProfile;
    }

    public  boolean isSaved() {
        return dataProvider.saved;
    }
    public void prepareBlurBitmap() {
//        if (blurredView == null) {
//            return;
//        }
//        int w = (int) (fragmentView.getMeasuredWidth() / 6.0f);
//        int h = (int) (fragmentView.getMeasuredHeight() / 6.0f);
//        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        canvas.scale(1.0f / 6.0f, 1.0f / 6.0f);
//        fragmentView.draw(canvas);
//        Utilities.stackBlurBitmap(bitmap, Math.max(7, Math.max(w, h) / 180));
//        blurredView.setBackground(new BitmapDrawable(bitmap));
//        blurredView.setAlpha(0.0f);
//        blurredView.setVisibility(View.VISIBLE);
    }

    private View scrimView = null;
    private Paint scrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
        @Override
        public void setAlpha(int a) {
            super.setAlpha(a);
            fragmentView.invalidate();
        }
    };

    public ProfileActivityV2(Bundle args) {
        this(args, null);
    }

    public ProfileActivityV2(Bundle args, SharedMediaLayout.SharedMediaPreloader preloader) {
        super(args);
        this.sharedMediaPreloader = preloader;
        dataProvider = new ProfileActivityDataProvider(
            arguments,
            this::getMessagesController,
            this::getContactsController,
            this::getNotificationsController,
            this::getThemedColor
        );
    }

    public static void sendLogs(Activity activity, boolean last) {
        if (activity == null) {
            return;
        }
        AlertDialog progressDialog = new AlertDialog(activity, AlertDialog.ALERT_TYPE_SPINNER);
        progressDialog.setCanCancel(false);
        progressDialog.show();
        Utilities.globalQueue.postRunnable(() -> {
            try {
                File dir = AndroidUtilities.getLogsDir();
                if (dir == null) {
                    AndroidUtilities.runOnUIThread(progressDialog::dismiss);
                    return;
                }

                File zipFile = new File(dir, "logs.zip");
                if (zipFile.exists()) {
                    zipFile.delete();
                }

                ArrayList<File> files = new ArrayList<>();

                File[] logFiles = dir.listFiles();
                for (File f : logFiles) {
                    files.add(f);
                }

                File filesDir = ApplicationLoader.getFilesDirFixed();
                filesDir = new File(filesDir, "malformed_database/");
                if (filesDir.exists() && filesDir.isDirectory()) {
                    File[] malformedDatabaseFiles = filesDir.listFiles();
                    for (File file : malformedDatabaseFiles) {
                        files.add(file);
                    }
                }

                boolean[] finished = new boolean[1];
                long currentDate = System.currentTimeMillis();

                BufferedInputStream origin = null;
                ZipOutputStream out = null;
                try {
                    FileOutputStream dest = new FileOutputStream(zipFile);
                    out = new ZipOutputStream(new BufferedOutputStream(dest));
                    byte[] data = new byte[1024 * 64];

                    for (int i = 0; i < files.size(); i++) {
                        File file = files.get(i);
                        if (!file.getName().contains("cache4") && (last || file.getName().contains("_mtproto")) && (currentDate - file.lastModified()) > 24 * 60 * 60 * 1000) {
                            continue;
                        }
                        if (!file.exists()) {
                            continue;
                        }
                        FileInputStream fi = new FileInputStream(file);
                        origin = new BufferedInputStream(fi, data.length);

                        ZipEntry entry = new ZipEntry(file.getName());
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, data.length)) != -1) {
                            out.write(data, 0, count);
                        }
                        origin.close();
                        origin = null;
                    }
                    finished[0] = true;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (origin != null) {
                        origin.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }

                AndroidUtilities.runOnUIThread(() -> {
                    try {
                        progressDialog.dismiss();
                    } catch (Exception ignore) {

                    }
                    if (finished[0]) {
                        Uri uri;
                        if (Build.VERSION.SDK_INT >= 24) {
                            uri = FileProvider.getUriForFile(activity, ApplicationLoader.getApplicationId() + ".provider", zipFile);
                        } else {
                            uri = Uri.fromFile(zipFile);
                        }

                        Intent i = new Intent(Intent.ACTION_SEND);
                        if (Build.VERSION.SDK_INT >= 24) {
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, "");
                        i.putExtra(Intent.EXTRA_SUBJECT, "Logs from " + LocaleController.getInstance().getFormatterStats().format(System.currentTimeMillis()));
                        i.putExtra(Intent.EXTRA_STREAM, uri);
                        if (activity != null) {
                            try {
                                activity.startActivityForResult(Intent.createChooser(i, "Select email application."), 500);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        }
                    } else {
                        if (activity != null) {
                            Toast.makeText(activity, getString(R.string.ErrorOccurred), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static ProfileActivity of(long dialogId) {
        Bundle bundle = new Bundle();
        if (dialogId >= 0) {
            bundle.putLong("user_id", dialogId);
        } else {
            bundle.putLong("chat_id", -dialogId);
        }
        return new ProfileActivity(bundle);
    }

    public long getTopicId() {
        return dataProvider.topicId;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (sharedMediaLayout != null) {
            sharedMediaLayout.onConfigurationChanged(newConfig);
        }
        invalidateIsInLandscapeMode();

        if (profileHeaderView.isExpanded) {
            scrollController.collapse();
            scrollController.reset();
            profileHeaderView.udpateHeight(scrollController.configuration.defaultHeight);
            profileHeaderView.invalidate();
        } else {
            AndroidUtilities.runOnUIThread(() -> {
                profileHeaderView.updateProfileData(false);
                profileHeaderView.udpateHeight(scrollController.currentHeaderHeight());
            }, 50);
        }
        
        if (scrollController.layoutManager != null && savedScrollToSharedMedia) {
            AndroidUtilities.runOnUIThread(() -> {
                scrollController.collapse();
                profileHeaderView.udpateHeight(scrollController.configuration.minimizedHeight);
                profileHeaderView.invalidate();
                scrollController.layoutManager.scrollToPositionWithOffset(listAdapter.sharedMediaIndex(), -scrollController.listView.getPaddingTop());
            }, 200);
        }
        
        fixLayout();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharedMediaLayout != null) {
            sharedMediaLayout.onResume();
        }
        invalidateIsInLandscapeMode();
        if (listAdapter != null) {
            // saveScrollPosition();
//            firstLayout = true;
//            updateCells();
            listAdapter.notifyDataSetChanged();
        }
//        if (!parentLayout.isInPreviewMode() && blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
//            blurredView.setVisibility(View.GONE);
//            blurredView.setBackground(null);
//        }

        if (imageUpdater != null) {
            imageUpdater.onResume();
            setParentActivityTitle(getString(R.string.Settings));
        }

        profileHeaderView.updateProfileData(false);
        fixLayout();
        if (profileHeaderView.infoView.titleView != null) {
            setParentActivityTitle(profileHeaderView.infoView.titleView.getText());
        }
        if (dataProvider.userId != 0) {
            final TLRPC.User user = dataProvider.getUser();
            if (user != null && user.photo == null) {
//                if (extraHeight >= AndroidUtilities.dp(88f)) {
//                    expandAnimator.cancel();
//                    expandAnimatorValues[0] = 1f;
//                    expandAnimatorValues[1] = 0f;
//                    setAvatarExpandProgress(1f);
//                    avatarsViewPager.setVisibility(View.GONE);
//                    extraHeight = AndroidUtilities.dp(88f);
//                    allowPullingDown = false;
//                    layoutManager.scrollToPositionWithOffset(0, AndroidUtilities.dp(88) - listView.getPaddingTop());
//                }
            }
        }
//        if (flagSecure != null) {
//            flagSecure.attach();
//        }
        updateItemsUsername();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (undoView != null) {
            undoView.hide(true, 0);
        }
        if (imageUpdater != null) {
            imageUpdater.onPause();
        }
//        if (flagSecure != null) {
//            flagSecure.detach();
//        }
        if (sharedMediaLayout != null) {
            sharedMediaLayout.onPause();
        }
    }

    @Override
    public void onBecomeFullyHidden() {
        if (undoView != null) {
            undoView.hide(true, 0);
        }
        super.onBecomeFullyHidden();
//        fullyVisible = false;
    }

    public UndoView getUndoView() {
        return undoView;
    }

    public boolean isSettings() {
        return imageUpdater != null && !dataProvider.isMyProfile;
    }

    private void updateItemsUsername() {
        if (!dataProvider.isMyProfile || setUsernameItem == null || linkItem == null) return;
        TLRPC.User user = dataProvider.getUser();
        if (user == null) {
            return;
        }
        final boolean hasUsername = UserObject.getPublicUsername(user) != null;
        setUsernameItem.setIcon(hasUsername ? R.drawable.menu_username_change : R.drawable.menu_username_set);
        setUsernameItem.setText(hasUsername ? getString(R.string.ProfileUsernameEdit) : getString(R.string.ProfileUsernameSet));
        linkItem.setVisibility(UserObject.getPublicUsername(user) != null ? View.VISIBLE : View.GONE);
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
//                    checkListViewScroll();
//                    needLayout(true);
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }

    @Override
    public boolean onFragmentCreate() {
        openSimilar = arguments.getBoolean("similar", false);
        vcardPhone = PhoneFormat.stripExceptNumbers(arguments.getString("vcard_phone"));
        openGifts = arguments.getBoolean("open_gifts", false);
        openCommonChats = arguments.getBoolean("open_common", false);

        if (dataProvider.getUser() != null) {
            getMessagesController().loadFullUser(getMessagesController().getUser(dataProvider.getUser().id), classGuid, true);

            getNotificationCenter().addObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.newSuggestionsAvailable);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatCreated);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.blockedUsersDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.botInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.userInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.privacyRulesUpdated);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.reloadInterface);

            if (UserObject.isUserSelf(dataProvider.getUser())) {
                imageUpdater = new ImageUpdater(true, ImageUpdater.FOR_TYPE_USER, true);
                imageUpdater.setOpenWithFrontfaceCamera(true);
                imageUpdater.parentFragment = this;
                imageUpdater.setDelegate(this);
                getMediaDataController().checkFeaturedStickers();
                getMessagesController().loadSuggestedFilters();
                getMessagesController().loadUserInfo(getUserConfig().getCurrentUser(), true, classGuid);

                TL_account.getPassword req = new TL_account.getPassword();
                getConnectionsManager().sendRequest(req, (response, error) -> {
                    if (response instanceof TL_account.TL_password) {
                        currentPassword = (TL_account.TL_password) response;
                    }
                });
            }

        } else if (dataProvider.getChat() != null) {
            currentChat = getMessagesController().getChat(dataProvider.getChat().id);
            if (currentChat == null) {
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                getMessagesStorage().getStorageQueue().postRunnable(() -> {
                    currentChat = getMessagesStorage().getChat(dataProvider.getChat().id);
                    countDownLatch.countDown();
                });
                try {
                    countDownLatch.await();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                if (currentChat != null) {
                    getMessagesController().putChat(currentChat, true);
                } else {
                    return false;
                }
            }

            getNotificationCenter().addObserver(this, NotificationCenter.chatInfoDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.chatOnlineCountDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.groupCallUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.channelRightsUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.chatWasBoostedByUser);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.uploadStoryEnd);

            sortedUsers = new ArrayList<>();
            if (ChatObject.isChannel(currentChat)) {
                getMessagesController().loadFullChat(dataProvider.getChat().id, classGuid, true);
            }
        } else {
            return false;
        }
        if (sharedMediaPreloader == null) {
            sharedMediaPreloader = new SharedMediaLayout.SharedMediaPreloader(this);
        }
        sharedMediaPreloader.addDelegate(this);

        getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().addObserver(this, NotificationCenter.didReceiveNewMessages);
        getNotificationCenter().addObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().addObserver(this, NotificationCenter.topicsDidLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.updateSearchSettings);
        getNotificationCenter().addObserver(this, NotificationCenter.reloadDialogPhotos);
        getNotificationCenter().addObserver(this, NotificationCenter.storiesUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.storiesReadUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.userIsPremiumBlockedUpadted);
        getNotificationCenter().addObserver(this, NotificationCenter.currentUserPremiumStatusChanged);
        getNotificationCenter().addObserver(this, NotificationCenter.starBalanceUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.botStarsUpdated);
        getNotificationCenter().addObserver(this, NotificationCenter.botStarsTransactionsLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.dialogDeleted);
        getNotificationCenter().addObserver(this, NotificationCenter.channelRecommendationsLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.starUserGiftsLoaded);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);

        updateOnlineCount(false);

        if (listAdapter != null) {
            updateCells();
            listAdapter.notifyDataSetChanged();
        }

        return true;
    }

    @Override
    public View createView(Context context) {
        Theme.createProfileResources(context);
        Theme.createChatResources(context, false);
        initializeResourcesProvider();

        fragmentView = new NestedFrameLayout(context) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (pinchToZoomHelper.isInOverlayMode()) {
                    return pinchToZoomHelper.onTouchEvent(ev);
                }
                if (sharedMediaLayout != null && sharedMediaLayout.isInFastScroll() && sharedMediaLayout.isPinnedToTop()) {
                    return sharedMediaLayout.dispatchFastScrollEvent(ev);
                }
                if (sharedMediaLayout != null && sharedMediaLayout.checkPinchToZoom(ev)) {
                    return true;
                }

                return super.dispatchTouchEvent(ev);
            }

            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                return super.onTouchEvent(ev);
            }

            @Override
            public boolean hasOverlappingRendering() {
                return false;
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int height = MeasureSpec.getSize(heightMeasureSpec);
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                fragmentViewAttached = true;
                if (profileHeaderView.infoView.emojiStatusDrawable != null) {
                    profileHeaderView.infoView.emojiStatusDrawable.attach();
                }

                if (profileHeaderView.infoView.botVerificationDrawable != null) {
                    profileHeaderView.infoView.botVerificationDrawable.attach();
                }
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                fragmentViewAttached = false;
                if (profileHeaderView.infoView.emojiStatusDrawable != null) {
                    profileHeaderView.infoView.emojiStatusDrawable.detach();
                }
                if (profileHeaderView.infoView.botVerificationDrawable != null) {
                    profileHeaderView.infoView.botVerificationDrawable.detach();
                }
            }
        };
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        pinchToZoomHelper = new PinchToZoomHelper(frameLayout, frameLayout) {
            @Override
            protected void invalidateViews() {
                super.invalidateViews();
                fragmentView.invalidate();
                for (int i = 0; i < profileHeaderView.avatarsViewPager.getChildCount(); i++) {
                    profileHeaderView.avatarsViewPager.getChildAt(i).invalidate();
                }
            }

            @Override
            protected boolean zoomEnabled(View child, ImageReceiver receiver) {
                if (!super.zoomEnabled(child, receiver)) {
                    return false;
                }
                return scrollController.listView.getScrollState() != RecyclerView.SCROLL_STATE_DRAGGING;
            }
        };

        pinchToZoomHelper.setCallback(new PinchToZoomHelper.Callback() {
            @Override
            public void onZoomStarted(MessageObject messageObject) {
                scrollController.listView.cancelClickRunnables(true);
                if (sharedMediaLayout != null && sharedMediaLayout.getCurrentListView() != null) {
                    sharedMediaLayout.getCurrentListView().cancelClickRunnables(true);
                }
            }
        });


        HeaderSizeConfiguration headerConfiguration = new HeaderSizeConfiguration(
            AndroidUtilities.statusBarHeight + ActionBar.getCurrentActionBarHeight(),
            AndroidUtilities.statusBarHeight + dp(232 + 12 + (dataProvider.isMyProfile || isSettings() ? -72 : 0)),
                AndroidUtilities.displaySize.x + dp(dataProvider.isMyProfile || isSettings() ? 0 : 72)
        );

        setupSharedMediaLayout(context);

        scrollController = new ProfileScrollController(context, headerConfiguration, dataProvider, getNotificationCenter());
        scrollController.setOnScrollListener(
            new ProfileScrollController.ProfileScrollListener() {
                @Override
                public void onHeaderHeightChanged(float height) {
                    profileHeaderView.udpateHeight(height);
                    updateSharedMediaPinnedState();
                }

                @Override
                public void onHeaderExpanded() {
                    BotWebViewVibrationEffect.IMPACT_LIGHT.vibrate();
                    profileHeaderView.startAnimation(true);
                }

                @Override
                public void onHeaderCollapsed() {
                    BotWebViewVibrationEffect.IMPACT_LIGHT.vibrate();
                    profileHeaderView.startAnimation(false);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    updateSharedMediaPinnedState();
                    super.onScrolled(recyclerView, dx, dy);
                }
            }
        );

        listAdapter = new ProfileActivityListAdapter(
                new ProfileActivityListCellFactory(
                        context,
                        resourcesProvider,
                        scrollController.listView,
                        this,
                        sharedMediaLayout
                ),
                new ProfileActivityListCellConfigurator(getMessagesController(), listAdapter, () -> profileChannelMessageFetcher),
                this
        );

        scrollController.listView.setAdapter(listAdapter);
        updateCells();

        profileHeaderView = new ProfileHeaderView(context, headerConfiguration, dataProvider, this);

        frameLayout.addView(scrollController.listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        
        int headerHeight = (int)(headerConfiguration.expandedHeight + dp(44));
        frameLayout.addView(profileHeaderView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, headerHeight, Gravity.TOP | Gravity.LEFT));
        frameLayout.addView(actionBar);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) scrollController.listView.getLayoutParams();
        layoutParams.topMargin = AndroidUtilities.statusBarHeight + ActionBar.getCurrentActionBarHeight();
        scrollController.listView.setLayoutParams(layoutParams);

        fragmentView.post(() -> profileHeaderView.udpateHeight(headerConfiguration.defaultHeight));

        profileHeaderView.avatarsViewPager.setPinchToZoomHelper(pinchToZoomHelper);

        ActionBarMenu menu = actionBar.createMenu();
        if (dataProvider.isMyProfile) {
            actionBarEditItem = menu.addItem(ActionBarItems.edit_profile, R.drawable.group_edit);
        } else {
            actionBarEditItem = menu.addItem(ActionBarItems.edit_channel, R.drawable.group_edit);
        }

        actionBarMoreItem = menu.addItem(0, R.drawable.ic_ab_other);

        setupActionBarMenuOnItemListener();
        createActionBarMenu(false);

        undoView = new UndoView(context, null, false, resourcesProvider);
        frameLayout.addView(undoView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));


        scrollController.listView.setOnItemClickListener((view, position, x, y) -> {
            listAdapter.callItemClick(position, view, (int) x, (int) y);
        });

        scrollController.listView.setOnItemLongClickListener((view, position, x, y) -> {
            listAdapter.callItemLongClick(position, view, (int) x, (int) y);
            return true;
        });

        return fragmentView;
    }

    public void expandAvatar() {
        scrollController.expand();
    }

    private AnimatorSet headerAnimatorSet;
    private AnimatorSet headerShadowAnimatorSet;
    private float mediaHeaderAnimationProgress;
    private Property<ActionBar, Float> ACTIONBAR_HEADER_PROGRESS = new AnimationProperties.FloatProperty<ActionBar>("avatarAnimationProgress") {
        @Override
        public void setValue(ActionBar object, float value) {
            mediaHeaderAnimationProgress = value;
            if (profileHeaderView.storyView != null) {
                profileHeaderView.storyView.setActionBarActionMode(value);
            }
            if (profileHeaderView.giftsView != null) {
//                profileHeaderView.giftsView.setActionBarActionMode(value);
            }
//            topView.invalidate();

            int color1 = getThemedColor(Theme.key_profile_title);
            int color2 = getThemedColor(Theme.key_player_actionBarTitle);
            int c = AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f);
            profileHeaderView.infoView.titleView.setTextColor(c);
            if (profileHeaderView.infoView.lockIconDrawable != null) {
                profileHeaderView.infoView.lockIconDrawable.setColorFilter(c, PorterDuff.Mode.MULTIPLY);
            }
            if (profileHeaderView.infoView.scamDrawable != null) {
                color1 = getThemedColor(Theme.key_avatar_subtitleInProfileBlue);
                profileHeaderView.infoView.scamDrawable.setColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f));
            }

            color1 = dataProvider.peerColor() != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon);
            color2 = getThemedColor(Theme.key_actionBarActionModeDefaultIcon);
            actionBar.setItemsColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), false);

            color1 = dataProvider.peerColor() != null ? Theme.ACTION_BAR_WHITE_SELECTOR_COLOR : dataProvider.peerColor() != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue);
            color2 = getThemedColor(Theme.key_actionBarActionModeDefaultSelector);
            actionBar.setItemsBackgroundColor(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), false);

//            topView.invalidate();
            actionBarMoreItem.setIconColor(dataProvider.peerColor() != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon));
            actionBarEditItem.setIconColor(dataProvider.peerColor() != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon));

            if (profileHeaderView.infoView.verifiedDrawable != null) {
                color1 = getThemedColor(Theme.key_profile_verifiedBackground);
                color2 = getThemedColor(Theme.key_player_actionBarTitle);
                profileHeaderView.infoView.verifiedDrawable.setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }

            if (profileHeaderView.infoView.verifiedCheckDrawable != null) {
                color1 = getThemedColor(Theme.key_profile_verifiedCheck);
                color2 = getThemedColor(Theme.key_windowBackgroundWhite);
                profileHeaderView.infoView.verifiedCheckDrawable.setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }

            if (profileHeaderView.infoView.premiumStarDrawable != null) {
                color1 = getThemedColor(Theme.key_profile_verifiedBackground);
                color2 = getThemedColor(Theme.key_player_actionBarTitle);
                profileHeaderView.infoView.premiumStarDrawable.setColorFilter(AndroidUtilities.getOffsetColor(color1, color2, value, 1.0f), PorterDuff.Mode.MULTIPLY);
            }

            profileHeaderView.infoView.updateEmojiStatusDrawableColor();

//            if (avatarsViewPagerIndicatorView.getSecondaryMenuItem() != null && (videoCallItemVisible || editItemVisible || callItemVisible)) {
//                needLayoutText(Math.min(1f, extraHeight / AndroidUtilities.dp(88f)));
//            }
        }

        @Override
        public Float get(ActionBar object) {
            return mediaHeaderAnimationProgress;
        }
    };

    private void setMediaHeaderVisible(boolean visible) {
        if (mediaHeaderVisible == visible) {
            return;
        }
        mediaHeaderVisible = visible;
        if (headerAnimatorSet != null) {
            headerAnimatorSet.cancel();
        }
        if (headerShadowAnimatorSet != null) {
            headerShadowAnimatorSet.cancel();
        }
        ActionBarMenuItem mediaSearchItem = sharedMediaLayout.getSearchItem();
        ImageView mediaOptionsItem = sharedMediaLayout.getSearchOptionsItem();
        TextView saveItem = sharedMediaLayout.getSaveItem();
        if (!mediaHeaderVisible) {
            if (editItemVisible) {
                actionBarEditItem.setVisibility(View.VISIBLE);
            }
            actionBarMoreItem.setVisibility(View.VISIBLE);
            if (mediaOptionsItem != null) {
                mediaOptionsItem.setVisibility(View.GONE);
            }
            if (saveItem != null) {
                saveItem.setVisibility(View.GONE);
            }
        } else {
            if (sharedMediaLayout.isSearchItemVisible()) {
                mediaSearchItem.setVisibility(View.VISIBLE);
            }
            if (mediaOptionsItem != null) {
                mediaOptionsItem.setVisibility(View.VISIBLE);
            }
            if (sharedMediaLayout.isOptionsItemVisible()) {
                sharedMediaLayout.photoVideoOptionsItem.setVisibility(View.VISIBLE);
                sharedMediaLayout.animateSearchToOptions(true, false);
            } else {
                sharedMediaLayout.photoVideoOptionsItem.setVisibility(View.INVISIBLE);
                sharedMediaLayout.animateSearchToOptions(false, false);
            }
        }
        profileHeaderView.updateStoriesViewBounds(false);

        if (actionBar != null) {
            actionBar.createMenu().requestLayout();
        }

        ArrayList<Animator> animators = new ArrayList<>();

        animators.add(ObjectAnimator.ofFloat(actionBarMoreItem, View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(actionBarEditItem, View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(actionBarMoreItem, View.TRANSLATION_Y, visible ? -dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(actionBarEditItem, View.TRANSLATION_Y, visible ? -dp(10) : 0.0f));
        animators.add(ObjectAnimator.ofFloat(mediaSearchItem, View.ALPHA, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(mediaSearchItem, View.TRANSLATION_Y, visible ? 0.0f : dp(10)));
        animators.add(ObjectAnimator.ofFloat(sharedMediaLayout.photoVideoOptionsItem, View.ALPHA, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(sharedMediaLayout.photoVideoOptionsItem, View.TRANSLATION_Y, visible ? 0.0f : dp(10)));
        animators.add(ObjectAnimator.ofFloat(actionBar, ACTIONBAR_HEADER_PROGRESS, visible ? 1.0f : 0.0f));
        animators.add(ObjectAnimator.ofFloat(profileHeaderView.infoView.subtitleView, View.ALPHA, visible ? 0.0f : 1.0f));
        animators.add(ObjectAnimator.ofFloat(profileHeaderView.infoView.mediaCounterTextView, View.ALPHA, visible ? 1.0f : 0.0f));

//        if (visible) {
//            animators.add(ObjectAnimator.ofFloat(this, HEADER_SHADOW, 0.0f));
//        }
        if (profileHeaderView.storyView != null || profileHeaderView.giftsView != null) {
            ValueAnimator va = ValueAnimator.ofFloat(0, 1);
            va.addUpdateListener(a -> profileHeaderView.updateStoriesViewBounds(true));
            animators.add(va);
        }

        headerAnimatorSet = new AnimatorSet();
        headerAnimatorSet.playTogether(animators);
        headerAnimatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
        headerAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (headerAnimatorSet != null) {
                    if (mediaHeaderVisible) {
                        if (editItemVisible) {
                            actionBarEditItem.setVisibility(View.GONE);
                        }
                        actionBarMoreItem.setVisibility(View.GONE);
                    } else {
                        if (sharedMediaLayout.isSearchItemVisible()) {
                            mediaSearchItem.setVisibility(View.VISIBLE);
                        }

                        sharedMediaLayout.photoVideoOptionsItem.setVisibility(View.INVISIBLE);

                        headerShadowAnimatorSet = new AnimatorSet();
//                        headerShadowAnimatorSet.playTogether(ObjectAnimator.ofFloat(ProfileActivityV2.this, HEADER_SHADOW, 1.0f));
                        headerShadowAnimatorSet.setDuration(100);
                        headerShadowAnimatorSet.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                headerShadowAnimatorSet = null;
                            }
                        });
                        headerShadowAnimatorSet.start();
                    }
                }
                profileHeaderView.updateStoriesViewBounds(false);
                headerAnimatorSet = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                headerAnimatorSet = null;
            }
        });
        headerAnimatorSet.setDuration(150);
        headerAnimatorSet.start();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
    }

    private void updateSharedMediaPinnedState() {
        if (sharedMediaLayout != null && listAdapter != null && listAdapter.hasSharedMedia() && listAdapter.isSharedMediaLayoutAttached()) {
            boolean isPinnedToTop = sharedMediaLayout.getTop() <= 0;

            if (isPinnedToTop != sharedMediaLayout.isPinnedToTop()) {
                savedScrollToSharedMedia = isPinnedToTop;
                createActionBarMenu(false);
                setMediaHeaderVisible(isPinnedToTop);
                profileHeaderView.updateMediaPresenting(isPinnedToTop);
            }

            sharedMediaLayout.setPinnedToTop(isPinnedToTop);
        }
    }

    private void setupSharedMediaLayout(Context context) {
        final ArrayList<Integer> users = dataProvider.getChatFull() != null && dataProvider.getChatFull().participants != null && dataProvider.getChatFull().participants.participants.size() > 5 ? sortedUsers : null;
        int initialTab = -1;
        if (openCommonChats) {
            initialTab = SharedMediaLayout.TAB_COMMON_GROUPS;
        } else if (openGifts && (dataProvider.getUserFull() != null && dataProvider.getUserFull().stargifts_count > 0 || dataProvider.getChatFull() != null && dataProvider.getChatFull().stargifts_count > 0)) {
            initialTab = SharedMediaLayout.TAB_GIFTS;
            openedGifts = true;
        } else if (openSimilar) {
            initialTab = SharedMediaLayout.TAB_RECOMMENDED_CHANNELS;
        } else if (users != null) {
            initialTab = SharedMediaLayout.TAB_GROUPUSERS;
        }

        sharedMediaLayout = new SharedMediaLayout(context, dataProvider.getDialogId(), sharedMediaPreloader, dataProvider.getUserFull() != null ? dataProvider.getUserFull().common_chats_count : 0, new ArrayList<>(), dataProvider.getChatFull(), dataProvider.getUserFull(), initialTab, this, this, SharedMediaLayout.VIEW_TYPE_PROFILE_ACTIVITY, resourcesProvider) {
            @Override
            protected void onSelectedTabChanged() {
                updateSelectedMediaTabText();
            }
            @Override
            protected boolean includeSavedDialogs() {
                long dialogId = 0;
                if (dataProvider.getDialog() != null) {
                    dialogId = dataProvider.getDialog().id;
                }

                return dialogId == getUserConfig().getClientUserId() && !dataProvider.saved;
            }
            @Override
            protected boolean isSelf() {
                return dataProvider.isMyProfile;
            }

            @Override
            protected boolean isStoriesView() {
                return dataProvider.isMyProfile;
            }

            @Override
            protected void onSearchStateChanged(boolean expanded) {
                AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);
                scrollController.listView.stopScroll();
                actionBarEditItem.setVisibility(expanded || !editItemVisible ? GONE : INVISIBLE);
                actionBarMoreItem.setVisibility(expanded ? GONE : INVISIBLE);
                profileHeaderView.updateStoriesViewBounds(false);
                profileHeaderView.infoView.setVisibility(expanded ? GONE : VISIBLE);
            }

            @Override
            protected boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, View view) {
                return ProfileActivityV2.this.onMemberClick(participant, isLong, view);
            }

            @Override
            protected void drawBackgroundWithBlur(Canvas canvas, float y, Rect rectTmp2, Paint backgroundPaint) {
//                contentView.drawBlurRect(canvas, listView.getY() + getY() + y, rectTmp2, backgroundPaint, true);
            }

            @Override
            protected void invalidateBlur() {
//                if (contentView != null) {
//                    contentView.invalidateBlur();
//                }
            }

            @Override
            protected int getInitialTab() {
                return TAB_STORIES;
            }

            @Override
            protected void showActionMode(boolean show) {
                super.showActionMode(show);
//                if (myProfile) {
//                    disableScroll(show);
//
//                    int a = getSelectedTab() - SharedMediaLayout.TAB_STORIES;
//                    if (a < 0 || a > 1) return;
//                    bottomButtonContainer[a]
//                            .animate()
//                            .translationY(show || a == 0 && MessagesController.getInstance(currentAccount).storiesEnabled() ? 0 : dp(72))
//                            .setDuration(320)
//                            .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
//                            .setUpdateListener(anm -> updateBottomButtonY())
//                            .start();
//                }
            }
            @Override
            protected void onTabProgress(float progress) {
                super.onTabProgress(progress);
//                if (myProfile) {
//                    int width = sharedMediaLayout == null ? AndroidUtilities.displaySize.x : sharedMediaLayout.getMeasuredWidth();
//                    if (bottomButtonContainer[0] != null)
//                        bottomButtonContainer[0].setTranslationX((SharedMediaLayout.TAB_STORIES - progress) * width);
//                    if (bottomButtonContainer[1] != null)
//                        bottomButtonContainer[1].setTranslationX((SharedMediaLayout.TAB_ARCHIVED_STORIES - progress) * width);
//                    updateBottomButtonY();
//                }
            }

            @Override
            protected void onActionModeSelectedUpdate(SparseArray<MessageObject> messageObjects) {
                super.onActionModeSelectedUpdate(messageObjects);
//                if (myProfile) {
//                    final int count = messageObjects.size();
//                    int a = getSelectedTab() - SharedMediaLayout.TAB_STORIES;
//                    if (a < 0 || a > 1) return;
//                    if (a == 0) {
//                        bottomButton[a].setText(count > 0 || !MessagesController.getInstance(currentAccount).storiesEnabled() ? formatPluralString("ArchiveStories", count) : bottomButtonPostText, true);
//                    }
//                    bottomButton[a].setCount(count, true);
//                }
            }

            @Override
            public void openStoryRecorder() {
                StoryRecorder.getInstance(getParentActivity(), currentAccount)
                        .selectedPeerId(dataProvider.getDialogId())
                        .canChangePeer(false)
                        .closeToWhenSent(new StoryRecorder.ClosingViewProvider() {
                            @Override
                            public void preLayout(long dialogId, Runnable runnable) {
                                profileHeaderView.avatarImage.setHasStories(dataProvider.needInsetForStories());
                                if (dialogId == dataProvider.getDialogId()) {
                                    scrollController.collapse();
                                }
                                AndroidUtilities.runOnUIThread(runnable, 30);
                            }

                            @Override
                            public StoryRecorder.SourceView getView(long dialogId) {
                                if (dialogId != dataProvider.getDialogId()) {
                                    return null;
                                }
                                profileHeaderView.updateStoriesViewBounds(true);
//                                updateAvatarRoundRadius();
                                return StoryRecorder.SourceView.fromAvatarImage(profileHeaderView.avatarImage, ChatObject.isForum(currentChat));
                            }
                        })
                        .open(StoryRecorder.SourceView.fromAvatarImage(profileHeaderView.avatarImage, ChatObject.isForum(currentChat)), true);
            }

            @Override
            public void updateTabs(boolean animated) {
                super.updateTabs(animated);
                if (openGifts && !openedGifts && scrollSlidingTextTabStrip.hasTab(TAB_GIFTS)) {
                    openedGifts = true;
                    scrollToPage(TAB_GIFTS);
                }
            }
        };

        sharedMediaLayout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
    }

    @Override
    public ActionBar createActionBar(Context context) {
        initializeResourcesProvider();

        ActionBar actionBar = new ActionBar(context, resourcesProvider);
        actionBar.setForceSkipTouches(true);
        actionBar.setBackgroundColor(Color.TRANSPARENT);
        actionBar.setItemsBackgroundColor(dataProvider.peerColor() != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), false);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarDefaultIcon), false);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarDefaultIcon), true);
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setCastShadows(false);
        actionBar.setAddToContainer(false);
        actionBar.setClipContent(true);
        actionBar.setOccupyStatusBar(Build.VERSION.SDK_INT >= 21 && !AndroidUtilities.isTablet() && !inBubbleMode);
        ImageView backButton = actionBar.getBackButton();
        backButton.setOnLongClickListener(e -> {
            ActionBarPopupWindow menu = BackButtonMenu.show(this, backButton, dataProvider.getDialogId(), dataProvider.topicId, resourcesProvider);
            if (menu != null) {
                menu.setOnDismissListener(() -> dimBehindView(false));
                dimBehindView(backButton, 0.3f);
                if (undoView != null) {
                    undoView.hide(true, 1);
                }
                return true;
            } else {
                return false;
            }
        });
        return actionBar;
    }

    private void dimBehindView(View view, float value) {
        scrimView = view;
        dimBehindView(value);
    }

    private void dimBehindView(boolean enable) {
        dimBehindView(enable ? 0.2f : 0);
    }

    private AnimatorSet scrimAnimatorSet = null;

    private void dimBehindView(float value) {
        boolean enable = value > 0;
        fragmentView.invalidate();
        if (scrimAnimatorSet != null) {
            scrimAnimatorSet.cancel();
        }
        scrimAnimatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<>();
        ValueAnimator scrimPaintAlphaAnimator;
        if (enable) {
            animators.add(scrimPaintAlphaAnimator = ValueAnimator.ofFloat(0, value));
        } else {
            animators.add(scrimPaintAlphaAnimator = ValueAnimator.ofFloat(scrimPaint.getAlpha() / 255f, 0));
        }
        scrimPaintAlphaAnimator.addUpdateListener(a -> {
            scrimPaint.setAlpha((int) (255 * (float) a.getAnimatedValue()));
        });
        scrimAnimatorSet.playTogether(animators);
        scrimAnimatorSet.setDuration(enable ? 150 : 220);
        if (!enable) {
            scrimAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scrimView = null;
                    fragmentView.invalidate();
                }
            });
        }
        scrimAnimatorSet.start();
    }

    private static class ActionBarItems {
        static int edit_info = 0;
        static int add_photo = 1;
        static int edit_color = 2;
        static int set_username = 3;
        static int copy_link_profile = 4;
        static int block_contact = 5;
        static int add_shortcut = 6;
        static int share = 7;
        static int share_contact = 8;
        static int bot_privacy = 9;
        static int report = 10;
        static int edit_contact = 11;
        static int delete_contact = 12;
        static int gift_premium = 13;
        static int start_secret_chat = 14;
        static int call_item = 15;
        static int can_view_revenue = 16;
        static int statistics = 17;
        static int search_members = 18;
        static int leave_group = 19;
        static int delete_topic = 20;
        static int channel_stories = 21;
        static int view_discussion = 22;
        static int set_as_main = 23;
        static int gallery_menu_save = 24;
        static int delete_avatar = 25;
        static int edit_avatar = 26;
        static int logout = 27;
        static int add_contact = 28;
        static int add_member = 29;
        static int video_call_item = 30;
        static int edit_channel = 31;
        static int edit_profile = 32;
        static int invite_to_group = 33;
    }

    private void setupActionBarMenuOnItemListener() {
        boolean disableProfileAnimation = false;
        int playProfileAnimation = 1;

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(final int id) {
                if (getParentActivity() == null) {
                    return;
                }
                if (id == -1) {
                    finishFragment();
                } else if (id == ActionBarItems.block_contact) {
                    blockUser();
                } else if (id == ActionBarItems.add_contact) {
                    TLRPC.User user = getMessagesController().getUser(dataProvider.userId);
                    Bundle args = new Bundle();
                    args.putLong("user_id", user.id);
                    args.putBoolean("addContact", true);
                    openAddToContact(user, args);
                } else if (id == ActionBarItems.share_contact) {
                    Bundle args = new Bundle();
                    args.putBoolean("onlySelect", true);
                    args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_FORWARD);
                    args.putString("selectAlertString", getString(R.string.SendContactToText));
                    args.putString("selectAlertStringGroup", getString(R.string.SendContactToGroupText));
                    DialogsActivity fragment = new DialogsActivity(args);
                    fragment.setDelegate(ProfileActivityV2.this);
                    presentFragment(fragment);
                } else if (id == ActionBarItems.edit_contact) {
                    Bundle args = new Bundle();
                    args.putLong("user_id", dataProvider.userId);
                    presentFragment(new ContactAddActivity(args, resourcesProvider));
                } else if (id == ActionBarItems.delete_contact) {
                    final TLRPC.User user = getMessagesController().getUser(dataProvider.userId);
                    if (user == null || getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                    builder.setTitle(getString(R.string.DeleteContact));
                    builder.setMessage(getString(R.string.AreYouSureDeleteContact));
                    builder.setPositiveButton(getString(R.string.Delete), (dialogInterface, i) -> {
                        ArrayList<TLRPC.User> arrayList = new ArrayList<>();
                        arrayList.add(user);
                        getContactsController().deleteContact(arrayList, true);
                        user.contact = false;
                        updateListAnimated(false);
                    });
                    builder.setNegativeButton(getString(R.string.Cancel), null);
                    AlertDialog dialog = builder.create();
                    showDialog(dialog);
                    TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(getThemedColor(Theme.key_text_RedBold));
                    }
                } else if (id == ActionBarItems.leave_group) {
                    leaveChatPressed();
                } else if (id == ActionBarItems.delete_topic) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(LocaleController.getPluralString("DeleteTopics", 1));
                    TLRPC.TL_forumTopic topic = MessagesController.getInstance(currentAccount).getTopicsController().findTopic(dataProvider.chatId, dataProvider.topicId);
                    builder.setMessage(formatString("DeleteSelectedTopic", R.string.DeleteSelectedTopic, topic == null ? "topic" : topic.title));
                    builder.setPositiveButton(getString(R.string.Delete), (dialog, which) -> {
                        ArrayList<Integer> topicIds = new ArrayList<>();
                        topicIds.add((int) dataProvider.topicId);
                        getMessagesController().getTopicsController().deleteTopics(dataProvider.chatId, topicIds);
//                        playProfileAnimation = 0;
                        if (parentLayout != null && parentLayout.getFragmentStack() != null) {
                            for (int i = 0; i < parentLayout.getFragmentStack().size(); ++i) {
                                BaseFragment fragment = parentLayout.getFragmentStack().get(i);
                                if (fragment instanceof ChatActivity && ((ChatActivity) fragment).getTopicId() == dataProvider.topicId) {
                                    fragment.removeSelfFromStack();
                                }
                            }
                        }
                        finishFragment();

                        Context context = getContext();
                        if (context != null) {
                            BulletinFactory.of(Bulletin.BulletinWindow.make(context), resourcesProvider).createSimpleBulletin(R.raw.ic_delete, LocaleController.getPluralString("TopicsDeleted", 1)).show();
                        }
                        dialog.dismiss();
                    });
                    builder.setNegativeButton(getString(R.string.Cancel), (dialog, which) -> dialog.dismiss());
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
                    }
                } else if (id == ActionBarItems.report) {
                    ReportBottomSheet.openChat(ProfileActivityV2.this, dataProvider.getDialogId());
                } else if (id == ActionBarItems.edit_channel) {
                    if (dataProvider.isTopic) {
                        Bundle args = new Bundle();
                        args.putLong("chat_id", dataProvider.chatId);
                        TopicCreateFragment fragment = TopicCreateFragment.create(dataProvider.chatId, dataProvider.topicId);
                        presentFragment(fragment);
                    } else {
                        Bundle args = new Bundle();
                        if (dataProvider.chatId != 0) {
                            args.putLong("chat_id", dataProvider.chatId);
                        } else if (dataProvider.isBot()) {
                            args.putLong("user_id", dataProvider.userId);
                        }
                        ChatEditActivity fragment = new ChatEditActivity(args);
                        if (dataProvider.getChatFull() != null) {
                            fragment.setInfo(dataProvider.getChatFull());
                        } else {
                            fragment.setInfo(dataProvider.getUserFull());
                        }
                        presentFragment(fragment);
                    }
                } else if (id == ActionBarItems.edit_profile) {
                    presentFragment(new UserInfoActivity());
                } else if (id == ActionBarItems.invite_to_group) {
                    final TLRPC.User user = getMessagesController().getUser(dataProvider.userId);
                    if (user == null) {
                        return;
                    }
                    Bundle args = new Bundle();
                    args.putBoolean("onlySelect", true);
                    args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_ADD_USERS_TO);
                    args.putBoolean("resetDelegate", false);
                    args.putBoolean("closeFragment", false);
//                    args.putString("addToGroupAlertString", LocaleController.formatString("AddToTheGroupAlertText", R.string.AddToTheGroupAlertText, UserObject.getUserName(user), "%1$s"));
                    DialogsActivity fragment = new DialogsActivity(args);
                    fragment.setDelegate((fragment1, dids, message, param, notify, scheduleDate, topicsFragment) -> {
                        long did = dids.get(0).dialogId;

                        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-did);
                        if (chat != null && (chat.creator || chat.admin_rights != null && chat.admin_rights.add_admins)) {
                            getMessagesController().checkIsInChat(false, chat, user, (isInChatAlready, rightsAdmin, currentRank) -> AndroidUtilities.runOnUIThread(() -> {
                                ChatRightsEditActivity editRightsActivity = new ChatRightsEditActivity(dataProvider.userId, -did, rightsAdmin, null, null, currentRank, ChatRightsEditActivity.TYPE_ADD_BOT, true, !isInChatAlready, null);
                                editRightsActivity.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
                                    @Override
                                    public void didSetRights(int rights, TLRPC.TL_chatAdminRights rightsAdmin, TLRPC.TL_chatBannedRights rightsBanned, String rank) {
//                                        disableProfileAnimation = true;
                                        fragment.removeSelfFromStack();
                                        getNotificationCenter().removeObserver(ProfileActivityV2.this, NotificationCenter.closeChats);
                                        getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                                    }

                                    @Override
                                    public void didChangeOwner(TLRPC.User user) {
                                    }
                                });
                                presentFragment(editRightsActivity);
                            }));
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                            builder.setTitle(getString(R.string.AddBot));
                            String chatName = chat == null ? "" : chat.title;
                            builder.setMessage(AndroidUtilities.replaceTags(formatString("AddMembersAlertNamesText", R.string.AddMembersAlertNamesText, UserObject.getUserName(user), chatName)));
                            builder.setNegativeButton(getString(R.string.Cancel), null);
                            builder.setPositiveButton(getString(R.string.AddBot), (di, i) -> {
//                                disableProfileAnimation = true;

                                Bundle args1 = new Bundle();
                                args1.putBoolean("scrollToTopOnResume", true);
                                args1.putLong("chat_id", -did);
                                if (!getMessagesController().checkCanOpenChat(args1, fragment1)) {
                                    return;
                                }
                                ChatActivity chatActivity = new ChatActivity(args1);
                                getNotificationCenter().removeObserver(ProfileActivityV2.this, NotificationCenter.closeChats);
                                getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                                getMessagesController().addUserToChat(-did, user, 0, null, chatActivity, true, null, null);
                                presentFragment(chatActivity, true);
                            });
                            showDialog(builder.create());
                        }
                        return true;
                    });
                    presentFragment(fragment);
                } else if (id == ActionBarItems.share) {
                    shareChat();
                } else if (id == ActionBarItems.add_shortcut) {
                    try {
                        long did;
                        if (dataProvider.currentEncryptedChat != null) {
                            did = DialogObject.makeEncryptedDialogId(dataProvider.currentEncryptedChat.id);
                        } else if (dataProvider.userId != 0) {
                            did = dataProvider.userId;
                        } else if (dataProvider.chatId != 0) {
                            did = -dataProvider.chatId;
                        } else {
                            return;
                        }
                        getMediaDataController().installShortcut(did, MediaDataController.SHORTCUT_TYPE_USER_OR_CHAT);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                } else if (id == ActionBarItems.call_item || id == ActionBarItems.video_call_item) {
                    if (dataProvider.userId != 0) {
                        TLRPC.User user = getMessagesController().getUser(dataProvider.userId);
                        if (user != null) {
                            VoIPHelper.startCall(user, id == ActionBarItems.video_call_item, dataProvider.getUserFull() != null && dataProvider.getUserFull().video_calls_available, getParentActivity(), dataProvider.getUserFull(), getAccountInstance());
                        }
                    } else if (dataProvider.chatId != 0) {
                        ChatObject.Call call = getMessagesController().getGroupCall(dataProvider.chatId, false);
                        if (call == null) {
                            VoIPHelper.showGroupCallAlert(ProfileActivityV2.this, currentChat, null, false, getAccountInstance());
                        } else {
                            VoIPHelper.startCall(currentChat, null, null, false, getParentActivity(), ProfileActivityV2.this, getAccountInstance());
                        }
                    }
                } else if (id == ActionBarItems.search_members) {
                    Bundle args = new Bundle();
                    args.putLong("chat_id", dataProvider.chatId);
                    args.putInt("type", ChatUsersActivity.TYPE_USERS);
                    args.putBoolean("open_search", true);
                    ChatUsersActivity fragment = new ChatUsersActivity(args);
                    fragment.setInfo(dataProvider.getChatFull());
                    presentFragment(fragment);
                } else if (id == ActionBarItems.add_member) {
                    openAddMember();
                } else if (id == ActionBarItems.statistics) {
                    TLRPC.Chat chat = dataProvider.getChat();
                    presentFragment(StatisticActivity.create(chat, false));
                } else if (id == ActionBarItems.view_discussion) {
                    openDiscussion();
                } else if (id == ActionBarItems.gift_premium) {
                    if (dataProvider.getUserFull() != null && UserObject.areGiftsDisabled(dataProvider.getUserFull())) {
                        BaseFragment lastFragment = LaunchActivity.getSafeLastFragment();
                        if (lastFragment != null) {
                            BulletinFactory.of(lastFragment).createSimpleBulletin(R.raw.error, AndroidUtilities.replaceTags(formatString(R.string.UserDisallowedGifts, DialogObject.getShortName(dataProvider.getDialogId())))).show();
                        }
                        return;
                    }
                    if (currentChat != null) {
                        MessagesController.getGlobalMainSettings().edit().putInt("channelgifthint", 3).apply();
                    }
                    showDialog(new GiftSheet(getContext(), currentAccount, dataProvider.getDialogId(), null, null));
                } else if (id == ActionBarItems.channel_stories) {
                    Bundle args = new Bundle();
                    args.putInt("type", MediaActivity.TYPE_ARCHIVED_CHANNEL_STORIES);
                    args.putLong("dialog_id", -dataProvider.chatId);
                    MediaActivity fragment = new MediaActivity(args, null);
                    fragment.setChatInfo(dataProvider.getChatFull());
                    presentFragment(fragment);
                } else if (id == ActionBarItems.start_secret_chat) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                    builder.setTitle(getString(R.string.AreYouSureSecretChatTitle));
                    builder.setMessage(getString(R.string.AreYouSureSecretChat));
                    builder.setPositiveButton(getString(R.string.Start), (dialogInterface, i) -> {
                        if (MessagesController.getInstance(currentAccount).isFrozen()) {
                            AccountFrozenAlert.show(currentAccount);
                            return;
                        }
                        creatingChat = true;
                        getSecretChatHelper().startSecretChat(getParentActivity(), getMessagesController().getUser(dataProvider.userId));
                    });
                    builder.setNegativeButton(getString(R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (id == ActionBarItems.bot_privacy) {
                    BotWebViewAttachedSheet.openPrivacy(currentAccount, dataProvider.userId);
                } else if (id == ActionBarItems.gallery_menu_save) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= 23 && (Build.VERSION.SDK_INT <= 28 || BuildVars.NO_SCOPED_STORAGE) && getParentActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        getParentActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
                        return;
                    }
                    ImageLocation location = profileHeaderView.avatarsViewPager.getImageLocation(profileHeaderView.avatarsViewPager.getRealPosition());
                    if (location == null) {
                        return;
                    }
                    final boolean isVideo = location.imageType == FileLoader.IMAGE_TYPE_ANIMATION;
                    File f = FileLoader.getInstance(currentAccount).getPathToAttach(location.location, isVideo ? "mp4" : null, true);
                    if (isVideo && !f.exists()) {
                        f = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_IMAGE), FileLoader.getAttachFileName(location.location, "mp4"));
                    }
                    if (f.exists()) {
                        MediaController.saveFile(f.toString(), getParentActivity(), 0, null, null, uri -> {
                            if (getParentActivity() == null) {
                                return;
                            }
                            BulletinFactory.createSaveToGalleryBulletin(ProfileActivityV2.this, isVideo, null).show();
                        });
                    }
                } else if (id == ActionBarItems.edit_info) {
                    presentFragment(new UserInfoActivity());
                } else if (id == ActionBarItems.edit_color) {
                    if (!getUserConfig().isPremium()) {
                        showDialog(new PremiumFeatureBottomSheet(ProfileActivityV2.this, PremiumPreviewFragment.PREMIUM_FEATURE_NAME_COLOR, true));
                        return;
                    }
                    presentFragment(new PeerColorActivity(0).startOnProfile().setOnApplied(ProfileActivityV2.this));
                } else if (id == ActionBarItems.copy_link_profile) {
                    TLRPC.User user = getMessagesController().getUser(dataProvider.userId);
                    AndroidUtilities.addToClipboard(getMessagesController().linkPrefix + "/" + UserObject.getPublicUsername(user));
                } else if (id == ActionBarItems.set_username) {
                    presentFragment(new ChangeUsernameActivity());
                } else if (id == ActionBarItems.logout) {
                    presentFragment(new LogoutActivity());
                } else if (id == ActionBarItems.set_as_main) {
                    int position = profileHeaderView.avatarsViewPager.getRealPosition();
                    TLRPC.Photo photo = profileHeaderView.avatarsViewPager.getPhoto(position);
                    if (photo == null) {
                        return;
                    }
                    profileHeaderView.avatarsViewPager.startMovePhotoToBegin(position);

                    TLRPC.TL_photos_updateProfilePhoto req = new TLRPC.TL_photos_updateProfilePhoto();
                    req.id = new TLRPC.TL_inputPhoto();
                    req.id.id = photo.id;
                    req.id.access_hash = photo.access_hash;
                    req.id.file_reference = photo.file_reference;
                    UserConfig userConfig = getUserConfig();
                    getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                        profileHeaderView.avatarsViewPager.finishSettingMainPhoto();
                        if (response instanceof TLRPC.TL_photos_photo) {
                            TLRPC.TL_photos_photo photos_photo = (TLRPC.TL_photos_photo) response;
                            getMessagesController().putUsers(photos_photo.users, false);
                            TLRPC.User user = getMessagesController().getUser(userConfig.clientUserId);
                            if (photos_photo.photo instanceof TLRPC.TL_photo) {
                                profileHeaderView.avatarsViewPager.replaceFirstPhoto(photo, photos_photo.photo);
                                if (user != null) {
                                    user.photo.photo_id = photos_photo.photo.id;
                                    userConfig.setCurrentUser(user);
                                    userConfig.saveConfig(true);
                                }
                            }
                        }
                    }));
                    undoView.showWithAction(dataProvider.userId, UndoView.ACTION_PROFILE_PHOTO_CHANGED, photo.video_sizes.isEmpty() ? null : 1);
                    TLRPC.User user = getMessagesController().getUser(userConfig.clientUserId);

                    TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 800);
                    if (user != null) {
                        TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 90);
                        user.photo.photo_id = photo.id;
                        user.photo.photo_small = smallSize.location;
                        user.photo.photo_big = bigSize.location;
                        userConfig.setCurrentUser(user);
                        userConfig.saveConfig(true);
                        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
                        profileHeaderView.updateProfileData(true);
                    }
                    profileHeaderView.avatarsViewPager.commitMoveToBegin();
                } else if (id == ActionBarItems.edit_avatar) {
                    if (MessagesController.getInstance(currentAccount).isFrozen()) {
                        AccountFrozenAlert.show(currentAccount);
                        return;
                    }
                    int position = profileHeaderView.avatarsViewPager.getRealPosition();
                    ImageLocation location = profileHeaderView.avatarsViewPager.getImageLocation(position);
                    if (location == null) {
                        return;
                    }

                    File f = FileLoader.getInstance(currentAccount).getPathToAttach(PhotoViewer.getFileLocation(location), PhotoViewer.getFileLocationExt(location), true);
                    boolean isVideo = location.imageType == FileLoader.IMAGE_TYPE_ANIMATION;
                    String thumb;
                    if (isVideo) {
                        ImageLocation imageLocation = profileHeaderView.avatarsViewPager.getRealImageLocation(position);
                        thumb = FileLoader.getInstance(currentAccount).getPathToAttach(PhotoViewer.getFileLocation(imageLocation), PhotoViewer.getFileLocationExt(imageLocation), true).getAbsolutePath();
                    } else {
                        thumb = null;
                    }
                    imageUpdater.openPhotoForEdit(f.getAbsolutePath(), thumb, 0, isVideo);
                } else if (id == ActionBarItems.delete_avatar) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                    ImageLocation location = profileHeaderView.avatarsViewPager.getImageLocation(profileHeaderView.avatarsViewPager.getRealPosition());
                    if (location == null) {
                        return;
                    }
                    if (location.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                        builder.setTitle(getString(R.string.AreYouSureDeleteVideoTitle));
                        builder.setMessage(getString(R.string.AreYouSureDeleteVideo));
                    } else {
                        builder.setTitle(getString(R.string.AreYouSureDeletePhotoTitle));
                        builder.setMessage(getString(R.string.AreYouSureDeletePhoto));
                    }
                    builder.setPositiveButton(getString(R.string.Delete), (dialogInterface, i) -> {
                        int position = profileHeaderView.avatarsViewPager.getRealPosition();
                        TLRPC.Photo photo = profileHeaderView.avatarsViewPager.getPhoto(position);
                        TLRPC.UserFull userFull = dataProvider.getUserFull();
                        if (profileHeaderView.avatar != null && position == 0) {
                            imageUpdater.cancel();
                            if (avatarUploadingRequest != 0) {
                                getConnectionsManager().cancelRequest(avatarUploadingRequest, true);
                            }
                            profileHeaderView.avatar = null;
                            profileHeaderView.avatarBig = null;
                            profileHeaderView.avatarsViewPager.scrolledByUser = true;
                            profileHeaderView.avatarsViewPager.removeUploadingImage(profileHeaderView.uploadingImageLocation);
                            profileHeaderView.avatarsViewPager.setCreateThumbFromParent(false);
                            profileHeaderView.updateProfileData(true);
//                            showAvatarProgress(false, true);
                            getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                            getUserConfig().saveConfig(true);
                            return;
                        }
                        if (profileHeaderView.hasFallbackPhoto && photo != null && userFull != null && userFull.fallback_photo != null && userFull.fallback_photo.id == photo.id) {
                            userFull.fallback_photo = null;
                            userFull.flags &= ~4194304;
                            getMessagesStorage().updateUserInfo(userFull, true);
                            profileHeaderView.updateProfileData(false);
                        }
                        if (profileHeaderView.avatarsViewPager.getRealCount() == 1) {
//                            setForegroundImage(true);
                        }
                        if (photo == null || profileHeaderView.avatarsViewPager.getRealPosition() == 0) {
                            TLRPC.Photo nextPhoto = profileHeaderView.avatarsViewPager.getPhoto(1);
                            if (nextPhoto != null) {
                                getUserConfig().getCurrentUser().photo =new TLRPC.TL_userProfilePhoto();
                                TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(nextPhoto.sizes, 90);
                                TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(nextPhoto.sizes, 1000);
                                if (smallSize != null && bigSize != null) {
                                    getUserConfig().getCurrentUser().photo.photo_small = smallSize.location;
                                    getUserConfig().getCurrentUser().photo.photo_big = bigSize.location;
                                }
                            } else {
                                getUserConfig().getCurrentUser().photo = new TLRPC.TL_userProfilePhotoEmpty();
                            }
                            getMessagesController().deleteUserPhoto(null);
                        } else {
                            TLRPC.TL_inputPhoto inputPhoto = new TLRPC.TL_inputPhoto();
                            inputPhoto.id = photo.id;
                            inputPhoto.access_hash = photo.access_hash;
                            inputPhoto.file_reference = photo.file_reference;
                            if (inputPhoto.file_reference == null) {
                                inputPhoto.file_reference = new byte[0];
                            }
                            getMessagesController().deleteUserPhoto(inputPhoto);
                            getMessagesStorage().clearUserPhoto(dataProvider.userId, photo.id);
                        }
                        if (profileHeaderView.avatarsViewPager.removePhotoAtIndex(position) || profileHeaderView.avatarsViewPager.getRealCount() <= 0) {
                            profileHeaderView.avatarsViewPager.setVisibility(View.GONE);
                            profileHeaderView.avatarImage.setForegroundAlpha(1f);
//                            avatarContainer.setVisibility(View.VISIBLE);
//                            doNotSetForeground = true;
                            final View view = scrollController.layoutManager.findViewByPosition(0);
                            if (view != null) {
                                scrollController.listView.smoothScrollBy(0, view.getTop() - dp(88), CubicBezierInterpolator.EASE_OUT_QUINT);
                            }
                        }
                    });
                    builder.setNegativeButton(getString(R.string.Cancel), null);
                    AlertDialog alertDialog = builder.create();
                    showDialog(alertDialog);
                    TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(getThemedColor(Theme.key_text_RedBold));
                    }
                } else if (id == ActionBarItems.add_photo) {
                    onWriteButtonClick();
                }
            }
        });
    }

    public void blockUser() {
        TLRPC.User user = dataProvider.getUser();
        if (user == null) {
            return;
        }
        if (!dataProvider.isBot() || MessagesController.isSupportUser(user)) {
            if (dataProvider.userBlocked()) {
                getMessagesController().unblockPeer(dataProvider.userId);
                if (BulletinFactory.canShowBulletin(ProfileActivityV2.this)) {
                    BulletinFactory.createBanBulletin(ProfileActivityV2.this, false).show();
                }
            } else {
                if (dataProvider.reportSpam) {
                    AlertsCreator.showBlockReportSpamAlert(ProfileActivityV2.this, dataProvider.userId, user, null, dataProvider.currentEncryptedChat, false, null, param -> {
                        if (param == 1) {
                            getNotificationCenter().removeObserver(ProfileActivityV2.this, NotificationCenter.closeChats);
                            getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
//                                        playProfileAnimation = 0;
                            finishFragment();
                        } else {
                            getNotificationCenter().postNotificationName(NotificationCenter.peerSettingsDidLoad, dataProvider.userId);
                        }
                    }, resourcesProvider);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
                    builder.setTitle(getString(R.string.BlockUser));
                    builder.setMessage(AndroidUtilities.replaceTags(formatString("AreYouSureBlockContact2", R.string.AreYouSureBlockContact2, ContactsController.formatName(user.first_name, user.last_name))));
                    builder.setPositiveButton(getString(R.string.BlockContact), (dialogInterface, i) -> {
                        getMessagesController().blockPeer(dataProvider.userId);
                        if (BulletinFactory.canShowBulletin(ProfileActivityV2.this)) {
                            BulletinFactory.createBanBulletin(ProfileActivityV2.this, true).show();
                        }
                    });
                    builder.setNegativeButton(getString(R.string.Cancel), null);
                    AlertDialog dialog = builder.create();
                    showDialog(dialog);
                    TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(getThemedColor(Theme.key_text_RedBold));
                    }
                }
            }
        } else {
            if (!dataProvider.userBlocked()) {
                AlertsCreator.createClearOrDeleteDialogAlert(ProfileActivityV2.this, false, currentChat, user, dataProvider.currentEncryptedChat != null, true, true, (param) -> {
                    if (getParentLayout() != null) {
                        List<BaseFragment> fragmentStack = getParentLayout().getFragmentStack();
                        BaseFragment prevFragment = fragmentStack == null || fragmentStack.size() < 2 ? null : fragmentStack.get(fragmentStack.size() - 2);
                        if (prevFragment instanceof ChatActivity) {
                            getParentLayout().removeFragmentFromStack(fragmentStack.size() - 2);
                        }
                    }
                    finishFragment();
                    getNotificationCenter().postNotificationName(NotificationCenter.needDeleteDialog, dataProvider.dialogId, user, currentChat, param);
                }, getResourceProvider());
            } else {
                getMessagesController().unblockPeer(dataProvider.userId, ()-> getSendMessagesHelper().sendMessage(SendMessagesHelper.SendMessageParams.of("/start", dataProvider.userId, null, null, null, false, null, null, null, true, 0, null, false)));
                finishFragment();
            }
        }
    }
    private void onWriteButtonClick() {
        if (dataProvider.userId != 0) {
            if (imageUpdater != null) {
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
                if (user == null) {
                    user = UserConfig.getInstance(currentAccount).getCurrentUser();
                }
                if (user == null) {
                    return;
                }
                imageUpdater.openMenu(user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty), () -> {
                    MessagesController.getInstance(currentAccount).deleteUserPhoto(null);
//                    cameraDrawable.setCurrentFrame(0);
//                    cellCameraDrawable.setCurrentFrame(0);
                }, dialog -> {
                    if (!imageUpdater.isUploadingImage()) {
//                        cameraDrawable.setCustomEndFrame(86);
//                        cellCameraDrawable.setCustomEndFrame(86);
//                        writeButton.playAnimation();
//                        if (setAvatarCell != null) {
//                            setAvatarCell.getImageView().playAnimation();
//                        }
                    } else {
//                        cameraDrawable.setCurrentFrame(0, false);
//                        cellCameraDrawable.setCurrentFrame(0, false);
                    }
                }, 0);
//                cameraDrawable.setCurrentFrame(0);
//                cameraDrawable.setCustomEndFrame(43);
//                cellCameraDrawable.setCurrentFrame(0);
//                cellCameraDrawable.setCustomEndFrame(43);
//                writeButton.playAnimation();
//                if (setAvatarCell != null) {
//                    setAvatarCell.getImageView().playAnimation();
//                }
            } else {
                if (parentLayout != null && parentLayout.getFragmentStack() != null && parentLayout.getFragmentStack().size() >= 2 && parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2) instanceof ChatActivity) {
                    finishFragment();
                } else {
                    TLRPC.User user = dataProvider.getUser();
                    if (user == null || user instanceof TLRPC.TL_userEmpty) {
                        return;
                    }
                    Bundle args = new Bundle();
                    args.putLong("user_id", dataProvider.userId);
                    if (!getMessagesController().checkCanOpenChat(args, ProfileActivityV2.this)) {
                        return;
                    }
                    boolean removeFragment = arguments.getBoolean("removeFragmentOnChatOpen", true);
                    if (!AndroidUtilities.isTablet() && removeFragment) {
                        getNotificationCenter().removeObserver(ProfileActivityV2.this, NotificationCenter.closeChats);
                        getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                    }
                    int distance = getArguments().getInt("nearby_distance", -1);
                    if (distance >= 0) {
                        args.putInt("nearby_distance", distance);
                    }
                    ChatActivity chatActivity = new ChatActivity(args);
                    chatActivity.setPreloadedSticker(getMediaDataController().getGreetingsSticker(), false);
                    presentFragment(chatActivity, removeFragment);
                    if (AndroidUtilities.isTablet()) {
                        finishFragment();
                    }
                }
            }
        } else {
            openDiscussion();
        }
    }

    public void leaveChat() {
        boolean isForum = ChatObject.isForum(currentChat);
        AlertsCreator.createClearOrDeleteDialogAlert(ProfileActivityV2.this, false, currentChat, null, false, isForum, !isForum, (param) -> {
            getNotificationCenter().removeObserver(ProfileActivityV2.this, NotificationCenter.closeChats);
            getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
            finishFragment();
            getNotificationCenter().postNotificationName(NotificationCenter.needDeleteDialog, -currentChat.id, null, currentChat, param);
        }, resourcesProvider);
    }

    public void shareChat() {
        try {
            String text = null;
            if (dataProvider.userId != 0) {
                TLRPC.User user = dataProvider.getUser();
                if (user == null) {
                    return;
                }
                if (dataProvider.botInfo != null && dataProvider.getUserFull() != null && !TextUtils.isEmpty(dataProvider.getUserFull().about)) {
                    text = String.format("%s https://" + getMessagesController().linkPrefix + "/%s", dataProvider.getUserFull().about, UserObject.getPublicUsername(user));
                } else {
                    text = String.format("https://" + getMessagesController().linkPrefix + "/%s", UserObject.getPublicUsername(user));
                }
            } else if (dataProvider.chatId != 0) {
                TLRPC.Chat chat = getMessagesController().getChat(dataProvider.chatId);
                if (chat == null) {
                    return;
                }
                if (dataProvider.getChatFull() != null && !TextUtils.isEmpty(dataProvider.getChatFull().about)) {
                    text = String.format("%s\nhttps://" + getMessagesController().linkPrefix + "/%s", dataProvider.getChatFull().about, ChatObject.getPublicUsername(chat));
                } else {
                    text = String.format("https://" + getMessagesController().linkPrefix + "/%s", ChatObject.getPublicUsername(chat));
                }
            }
            if (TextUtils.isEmpty(text)) {
                return;
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.BotShare)), 500);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void createActionBarMenu(boolean animated) {
        if (actionBar == null || actionBarMoreItem == null) {
            return;
        }
        Context context = actionBar.getContext();
        actionBarMoreItem.removeAllSubItems();
        ActionBarMenuSubItem animatingItem = null;

        canSearchMembers = false;
        boolean selfUser = false;
        hasVoiceChatItem = false;

        long userId = dataProvider.userId;
        long chatId = dataProvider.chatId;

        if (userId != 0) {
            TLRPC.User user = dataProvider.getUser();
            TLRPC.UserFull userInfo = dataProvider.getUserFull();
            if (user == null) {
                return;
            }
            if (UserObject.isUserSelf(user)) {
                editItemVisible = dataProvider.isMyProfile;
                actionBarMoreItem.addSubItem(ActionBarItems.edit_info, R.drawable.msg_edit, getString(R.string.EditInfo));
                if (imageUpdater != null) {
                    actionBarMoreItem.addSubItem(ActionBarItems.add_photo, R.drawable.msg_addphoto, getString(R.string.AddPhoto));
                }
                editColorItem = actionBarMoreItem.addSubItem(ActionBarItems.edit_color, R.drawable.menu_profile_colors, getString(R.string.ProfileColorEdit));
                updateEditColorIcon();
                if (dataProvider.isMyProfile) {
                    setUsernameItem = actionBarMoreItem.addSubItem(ActionBarItems.set_username, R.drawable.menu_username_change, getString(R.string.ProfileUsernameEdit));
                    linkItem = actionBarMoreItem.addSubItem(ActionBarItems.copy_link_profile, R.drawable.msg_link2, getString(R.string.ProfileCopyLink));
                    updateItemsUsername();
                }
                selfUser = true;
            } else {
                if (user.bot && user.bot_can_edit) {
                    editItemVisible = true;
                }

                if (dataProvider.isBot() || getContactsController().contactsDict.get(userId) == null) {
                    if (MessagesController.isSupportUser(user)) {
                        if (dataProvider.userBlocked()) {
                            actionBarMoreItem.addSubItem(ActionBarItems.block_contact, R.drawable.msg_block, getString(R.string.Unblock));
                        }
                        actionBarMoreItem.addSubItem(ActionBarItems.add_shortcut, R.drawable.msg_home, getString(R.string.AddShortcut));
                    } else if (dataProvider.getDialogId() != UserObject.VERIFY) {
                        if (dataProvider.currentEncryptedChat == null) {
                            createAutoDeleteItem(context);
                        }
                        actionBarMoreItem.addSubItem(ActionBarItems.add_shortcut, R.drawable.msg_home, getString(R.string.AddShortcut));
                        if (dataProvider.isBot()) {
                            actionBarMoreItem.addSubItem(ActionBarItems.share, R.drawable.msg_share, getString(R.string.BotShare));
                        } else {
                            actionBarMoreItem.addSubItem(ActionBarItems.add_contact, R.drawable.msg_addcontact, getString(R.string.AddContact));
                        }
                        if (!TextUtils.isEmpty(user.phone)) {
                            actionBarMoreItem.addSubItem(ActionBarItems.share_contact, R.drawable.msg_share, getString(R.string.ShareContact));
                        }
                        if (dataProvider.isBot()) {
                            actionBarMoreItem.addSubItem(ActionBarItems.bot_privacy, R.drawable.menu_privacy_policy, getString(R.string.BotPrivacyPolicy));
                            if (dataProvider.hasPrivacyCommand()) {
                                actionBarMoreItem.showSubItem(ActionBarItems.bot_privacy);
                            } else {
                                actionBarMoreItem.hideSubItem(ActionBarItems.bot_privacy);
                            }
                            actionBarMoreItem.addSubItem(ActionBarItems.report, R.drawable.msg_report, getString(R.string.ReportBot)).setColors(getThemedColor(Theme.key_text_RedRegular), getThemedColor(Theme.key_text_RedRegular));
                            if (!dataProvider.userBlocked()) {
                                actionBarMoreItem.addSubItem(ActionBarItems.block_contact, R.drawable.msg_block2, getString(R.string.DeleteAndBlock)).setColors(getThemedColor(Theme.key_text_RedRegular), getThemedColor(Theme.key_text_RedRegular));
                            } else {
                                actionBarMoreItem.addSubItem(ActionBarItems.block_contact, R.drawable.msg_retry, getString(R.string.BotRestart));
                            }
                        } else {
                            actionBarMoreItem.addSubItem(ActionBarItems.block_contact, !dataProvider.userBlocked() ? R.drawable.msg_block : R.drawable.msg_block, !dataProvider.userBlocked() ? getString(R.string.BlockContact) : getString(R.string.Unblock));
                        }
                    }
                } else {
                    if (dataProvider.currentEncryptedChat == null) {
                        createAutoDeleteItem(context);
                    }
                    if (!TextUtils.isEmpty(user.phone)) {
                        actionBarMoreItem.addSubItem(ActionBarItems.share_contact, R.drawable.msg_share, getString(R.string.ShareContact));
                    }
                    actionBarMoreItem.addSubItem(ActionBarItems.block_contact, !dataProvider.userBlocked() ? R.drawable.msg_block : R.drawable.msg_block, !dataProvider.userBlocked() ? getString(R.string.BlockContact) : getString(R.string.Unblock));
                    actionBarMoreItem.addSubItem(ActionBarItems.edit_contact, R.drawable.msg_edit, getString(R.string.EditContact));
                    actionBarMoreItem.addSubItem(ActionBarItems.delete_contact, R.drawable.msg_delete, getString(R.string.DeleteContact));
                }
                if (!UserObject.isDeleted(user) && !dataProvider.isBot() && dataProvider.currentEncryptedChat == null && !dataProvider.userBlocked() && userId != 333000 && userId != 777000 && userId != 42777) {
                    if (!BuildVars.IS_BILLING_UNAVAILABLE && !user.self && !user.bot && !MessagesController.isSupportUser(user) && !getMessagesController().premiumPurchaseBlocked()) {
                        StarsController.getInstance(currentAccount).loadStarGifts();
                        actionBarMoreItem.addSubItem(ActionBarItems.gift_premium, R.drawable.msg_gift_premium, getString(R.string.ProfileSendAGift));
                    }
                    actionBarMoreItem.addSubItem(ActionBarItems.start_secret_chat, R.drawable.msg_secret, getString(R.string.StartEncryptedChat));
                    actionBarMoreItem.setSubItemShown(ActionBarItems.start_secret_chat, DialogObject.isEmpty(getMessagesController().isUserContactBlocked(userId)));
                }
                if (!dataProvider.isBot() && getContactsController().contactsDict.get(userId) != null) {
                    actionBarMoreItem.addSubItem(ActionBarItems.add_shortcut, R.drawable.msg_home, getString(R.string.AddShortcut));
                }
            }
        } else if (chatId != 0) {
            TLRPC.Chat chat = dataProvider.getChat();
            TLRPC.ChatFull chatInfo = dataProvider.getChatFull();
            hasVoiceChatItem = false;

            if (dataProvider.topicId == 0 && ChatObject.canChangeChatInfo(chat)) {
                createAutoDeleteItem(context);
            }
            if (ChatObject.isChannel(chat)) {
                if (dataProvider.isTopic) {
                    if (ChatObject.canManageTopic(currentAccount, chat, dataProvider.topicId)) {
                        editItemVisible = true;
                    }
                } else {
                    if (ChatObject.hasAdminRights(chat) || chat.megagroup && ChatObject.canChangeChatInfo(chat)) {
                        editItemVisible = true;
                    }
                }
                if (chatInfo != null) {
                    if (ChatObject.canManageCalls(chat) && chatInfo.call == null) {
                        actionBarMoreItem.addSubItem(ActionBarItems.call_item, R.drawable.msg_voicechat, chat.megagroup && !chat.gigagroup ? getString(R.string.StartVoipChat) : getString(R.string.StartVoipChannel));
                        hasVoiceChatItem = true;
                    }
                    if ((chatInfo.can_view_stats || chatInfo.can_view_revenue || chatInfo.can_view_stars_revenue || getMessagesController().getStoriesController().canPostStories(dataProvider.getDialogId())) && dataProvider.topicId == 0) {
                        actionBarMoreItem.addSubItem(ActionBarItems.statistics, R.drawable.msg_stats, getString(R.string.Statistics));
                    }
                    ChatObject.Call call = getMessagesController().getGroupCall(chatId, false);
                }
                if (chat.megagroup) {
                    if (chatInfo == null || !chatInfo.participants_hidden || ChatObject.hasAdminRights(chat)) {
                        canSearchMembers = true;
                        actionBarMoreItem.addSubItem(ActionBarItems.search_members, R.drawable.msg_search, getString(R.string.SearchMembers));
                    }
                    if (!chat.creator && !chat.left && !chat.kicked && !dataProvider.isTopic) {
                        actionBarMoreItem.addSubItem(ActionBarItems.leave_group, R.drawable.msg_leave, getString(R.string.LeaveMegaMenu));
                    }
                    if (dataProvider.isTopic && ChatObject.canDeleteTopic(currentAccount, chat, dataProvider.topicId)) {
                        actionBarMoreItem.addSubItem(ActionBarItems.delete_topic, R.drawable.msg_delete, LocaleController.getPluralString("DeleteTopics", 1));
                    }
                } else {
                    if (chat.creator || chat.admin_rights != null && chat.admin_rights.edit_stories) {
                        actionBarMoreItem.addSubItem(ActionBarItems.channel_stories, R.drawable.msg_archive, getString(R.string.OpenChannelArchiveStories));
                    }
                    if (ChatObject.isPublic(chat)) {
                        actionBarMoreItem.addSubItem(ActionBarItems.share, R.drawable.msg_share, getString(R.string.BotShare));
                    }
                    if (!BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked()) {
                        StarsController.getInstance(currentAccount).loadStarGifts();
                        actionBarMoreItem.addSubItem(ActionBarItems.gift_premium, R.drawable.msg_gift_premium, getString(R.string.ProfileSendAGiftToChannel));
                        actionBarMoreItem.setSubItemShown(ActionBarItems.gift_premium, chatInfo != null && chatInfo.stargifts_available);
                    }
                    if (chatInfo != null && chatInfo.linked_chat_id != 0) {
                        actionBarMoreItem.addSubItem(ActionBarItems.view_discussion, R.drawable.msg_discussion, getString(R.string.ViewDiscussion));
                    }
                    if (!currentChat.creator && !currentChat.left && !currentChat.kicked) {
                        actionBarMoreItem.addSubItem(ActionBarItems.leave_group, R.drawable.msg_leave, getString(R.string.LeaveChannelMenu));
                    }
                }
            } else {
                if (chatInfo != null) {
                    if (ChatObject.canManageCalls(chat) && chatInfo.call == null) {
                        actionBarMoreItem.addSubItem(ActionBarItems.call_item, R.drawable.msg_voicechat, getString(R.string.StartVoipChat));
                        hasVoiceChatItem = true;
                    }
                    ChatObject.Call call = getMessagesController().getGroupCall(chatId, false);
                }
                if (ChatObject.canChangeChatInfo(chat)) {
                    editItemVisible = true;
                }
                if (!ChatObject.isKickedFromChat(chat) && !ChatObject.isLeftFromChat(chat)) {
                    if (chatInfo == null || !chatInfo.participants_hidden || ChatObject.hasAdminRights(chat)) {
                        canSearchMembers = true;
                        actionBarMoreItem.addSubItem(ActionBarItems.search_members, R.drawable.msg_search, getString(R.string.SearchMembers));
                    }
                }
                actionBarMoreItem.addSubItem(ActionBarItems.leave_group, R.drawable.msg_leave, getString(R.string.DeleteAndExit));
            }
            if (dataProvider.topicId == 0) {
                actionBarMoreItem.addSubItem(ActionBarItems.add_shortcut, R.drawable.msg_home, getString(R.string.AddShortcut));
            }
        }

        if (imageUpdater != null) {
            actionBarMoreItem.addSubItem(ActionBarItems.set_as_main, R.drawable.msg_openprofile, getString(R.string.SetAsMain));
            actionBarMoreItem.addSubItem(ActionBarItems.gallery_menu_save, R.drawable.msg_gallery, getString(R.string.SaveToGallery));
            //otherItem.addSubItem(edit_avatar, R.drawable.photo_paint, LocaleController.getString(R.string.EditPhoto));
            actionBarMoreItem.addSubItem(ActionBarItems.delete_avatar, R.drawable.msg_delete, getString(R.string.Delete));
        } else {
            actionBarMoreItem.addSubItem(ActionBarItems.gallery_menu_save, R.drawable.msg_gallery, getString(R.string.SaveToGallery));
        }
        if (getMessagesController().isChatNoForwards(currentChat)) {
            actionBarMoreItem.hideSubItem(ActionBarItems.gallery_menu_save);
        }

        if (selfUser && !dataProvider.isMyProfile) {
            actionBarMoreItem.addSubItem(ActionBarItems.logout, R.drawable.msg_leave, getString(R.string.LogOut));
        }
        if (!profileHeaderView.isExpanded) {
            actionBarMoreItem.hideSubItem(ActionBarItems.gallery_menu_save);
            actionBarMoreItem.hideSubItem(ActionBarItems.set_as_main);
            actionBarMoreItem.showSubItem(ActionBarItems.add_photo);
            actionBarMoreItem.hideSubItem(ActionBarItems.edit_avatar);
            actionBarMoreItem.hideSubItem(ActionBarItems.delete_avatar);
        }

        if (!mediaHeaderVisible) {
            if (editItemVisible) {
                if (actionBarEditItem.getVisibility() != View.VISIBLE) {
                    actionBarEditItem.setVisibility(View.VISIBLE);
                    if (animated) {
                        actionBarEditItem.setAlpha(0);
                        actionBarEditItem.animate().alpha(1f).setDuration(150).start();
                    }
                }
            } else {
                if (actionBarEditItem.getVisibility() != View.GONE) {
                    actionBarEditItem.setVisibility(View.GONE);
                }
            }
        }
        if (sharedMediaLayout != null) {
            sharedMediaLayout.getSearchItem().requestLayout();
        }
        profileHeaderView.updateStoriesViewBounds(false);
    }

    private void createAutoDeleteItem(Context context) {
        autoDeletePopupWrapper = new AutoDeletePopupWrapper(context, actionBarMoreItem.getPopupLayout().getSwipeBack(), new AutoDeletePopupWrapper.Callback() {

            @Override
            public void dismiss() {
                actionBarMoreItem.toggleSubMenu();
            }

            @Override
            public void setAutoDeleteHistory(int time, int action) {
                ProfileActivityV2.this.setAutoDeleteHistory(time, action);
            }

            @Override
            public void showGlobalAutoDeleteScreen() {
                presentFragment(new AutoDeleteMessagesActivity());
                dismiss();
            }
        }, false, 0, resourcesProvider);
        if (dataProvider.dialogId > 0 || dataProvider.userId > 0) {
            int linkColor = getThemedColor(Theme.key_windowBackgroundWhiteBlueText);
            autoDeletePopupWrapper.allowExtendedHint(linkColor);
        }
        int ttl = 0;
        if (dataProvider.getUserFull() != null || dataProvider.getChatFull() != null) {
            ttl = dataProvider.getUserFull() != null ? dataProvider.getUserFull().ttl_period : dataProvider.getChatFull().ttl_period;
        }
        autoDeleteItemDrawable = TimerDrawable.getTtlIcon(ttl);
        autoDeleteItem = actionBarMoreItem.addSwipeBackItem(0, autoDeleteItemDrawable, getString(R.string.AutoDeletePopupTitle), autoDeletePopupWrapper.windowLayout);
        actionBarMoreItem.addColoredGap();
        updateAutoDeleteItem();
    }

    private void updateAutoDeleteItem() {
        if (autoDeleteItem == null || autoDeletePopupWrapper == null) {
            return;
        }
        int ttl = 0;
        if (dataProvider.getUserFull() != null || dataProvider.getChatFull() != null) {
            ttl = dataProvider.getUserFull() != null ? dataProvider.getUserFull().ttl_period : dataProvider.getChatFull().ttl_period;
        }
        autoDeleteItemDrawable.setTime(ttl);
        autoDeletePopupWrapper.updateItems(ttl);
    }

    private void setAutoDeleteHistory(int time, int action) {
        long did = dataProvider.getDialogId();
        getMessagesController().setDialogHistoryTTL(did, time);
        if (dataProvider.getUserFull() != null || dataProvider.getChatFull() != null) {
            undoView.showWithAction(did, action, getMessagesController().getUser(did), dataProvider.getUserFull() != null ? dataProvider.getUserFull().ttl_period : dataProvider.getChatFull().ttl_period, null, null);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateListAnimated(boolean updateOnlineCount) {
        if (updateOnlineCount) {
            updateOnlineCount(true);
        }
        updateCells();
        if(listAdapter != null && !scrollController.listView.isComputingLayout()) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private void invalidateIsInLandscapeMode() {
        final Point size = new Point();
        final Display display = getParentActivity().getWindowManager().getDefaultDisplay();
        display.getSize(size);
        isInLandscapeMode = size.x > size.y;
    }

    public void startChat() {
        TLRPC.User user = getMessagesController().getUser(dataProvider.userId);
        if (user == null || user instanceof TLRPC.TL_userEmpty) {
            return;
        }
        Bundle args = new Bundle();
        args.putLong("user_id", dataProvider.userId);
        if (!getMessagesController().checkCanOpenChat(args, ProfileActivityV2.this)) {
            return;
        }
        boolean removeFragment = arguments.getBoolean("removeFragmentOnChatOpen", true);
        if (!AndroidUtilities.isTablet() && removeFragment) {
            getNotificationCenter().removeObserver(ProfileActivityV2.this, NotificationCenter.closeChats);
            getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
        }
        int distance = getArguments().getInt("nearby_distance", -1);
        if (distance >= 0) {
            args.putInt("nearby_distance", distance);
        }
        ChatActivity chatActivity = new ChatActivity(args);
        chatActivity.setPreloadedSticker(getMediaDataController().getGreetingsSticker(), false);
        presentFragment(chatActivity, removeFragment);
        if (AndroidUtilities.isTablet()) {
            finishFragment();
        }
    }
    public void openDiscussion() {
        if (dataProvider.getChatFull() == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putLong("chat_id", dataProvider.getChatFull().linked_chat_id == 0 ? dataProvider.getChatFull().id : dataProvider.getChatFull().linked_chat_id);
//        if (!getMessagesController().checkCanOpenChat(args, ProfileActivityV2.this)) {
//            return;
//        }
        presentFragment(new ChatActivity(args));
    }

    public void openTopic(long chatId, long topicId) {
        int currentAccount = getCurrentAccount();
        TopicsController topicsController = MessagesController.getInstance(currentAccount).getTopicsController();

        TLRPC.TL_forumTopic topic = topicsController.findTopic(chatId, topicId);

        if (topic != null) {
            ForumUtilities.openTopic(this, chatId, topic, 0);
        } else {
            topicsController.loadTopic(chatId, topicId, () -> {
                TLRPC.TL_forumTopic loadedTopic = topicsController.findTopic(chatId, topicId);
                if (loadedTopic != null) {
                    ForumUtilities.openTopic(this, chatId, loadedTopic, 0);
                }
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openAddToContact(TLRPC.User user, Bundle args) {
        ContactAddActivity contactAddActivity = new ContactAddActivity(args, resourcesProvider);
        contactAddActivity.setDelegate(() -> {
//            int currentAddToContactsRow = addToContactsRow;
//            if (currentAddToContactsRow >= 0) {
//                if (!listAdapter.hasSharedMedia()) {
//                    updateCells();
//                    listAdapter.notifyDataSetChanged();
//                } else {
//                    updateListAnimated(false);
//                }
//            }

            updateListAnimated(false);

            if (!listAdapter.hasSharedMedia()) {
                if (isInLandscapeMode || AndroidUtilities.isTablet()) {
                    scrollController.listView.setPadding(0, dp(88), 0, 0);
//                    expandAnimator.cancel();
//                    expandAnimatorValues[0] = 1f;
//                    expandAnimatorValues[1] = 0f;
//                    setAvatarExpandProgress(1f);
//                    extraHeight = AndroidUtilities.dp(88);
                } else {
                    final int actionBarHeight = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
                    int ws = View.MeasureSpec.makeMeasureSpec(scrollController.listView.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
                    int hs = View.MeasureSpec.makeMeasureSpec(scrollController.listView.getMeasuredHeight(), View.MeasureSpec.UNSPECIFIED);
                    int contentHeight = 0;
                    for (int i = 0; i < listAdapter.getItemCount(); i++) {
                        RecyclerView.ViewHolder holder = listAdapter.createViewHolder(null, listAdapter.getItemViewType(i));
                        listAdapter.onBindViewHolder(holder, i);
                        holder.itemView.measure(ws, hs);
                        contentHeight += holder.itemView.getMeasuredHeight();
                    }
                    int paddingBottom = Math.max(0, fragmentView.getMeasuredHeight() - (contentHeight + dp(88) + actionBarHeight));
                    scrollController.listView.setPadding(0, scrollController.listView.getPaddingTop(), 0, paddingBottom);
                }
            }
            undoView.showWithAction(dataProvider.dialogId, UndoView.ACTION_CONTACT_ADDED, user);
        });
        presentFragment(contactAddActivity);
    }

    private void updateOnlineCount(boolean notify) {
        if (profileHeaderView == null || profileHeaderView.infoView == null) { return; }
        profileHeaderView.infoView.onlineCount = 0;
        int currentTime = getConnectionsManager().getCurrentTime();
        sortedUsers.clear();
        if (dataProvider.getChatFull() instanceof TLRPC.TL_chatFull || dataProvider.getChatFull() instanceof TLRPC.TL_channelFull && dataProvider.getChatFull().participants_count <= 200 && dataProvider.getChatFull().participants != null) {
            final ArrayList<Integer> sortNum = new ArrayList<>();
            for (int a = 0; a < dataProvider.getChatFull().participants.participants.size(); a++) {
                TLRPC.ChatParticipant participant = dataProvider.getChatFull().participants.participants.get(a);
                TLRPC.User user = getMessagesController().getUser(participant.user_id);
                if (user != null && user.status != null && (user.status.expires > currentTime || user.id == getUserConfig().getClientUserId()) && user.status.expires > 10000) {
                    profileHeaderView.infoView.onlineCount++;
                }
                sortedUsers.add(a);
                int sort = Integer.MIN_VALUE;
                if (user != null) {
                    if (user.bot) {
                        sort = -110;
                    } else if (user.self) {
                        sort = currentTime + 50000;
                    } else if (user.status != null) {
                        sort = user.status.expires;
                    }
                }
                sortNum.add(sort);
            }

            try {
                Collections.sort(sortedUsers, Comparator.comparingInt(hs -> sortNum.get((int) hs)).reversed());
            } catch (Exception e) {
                FileLog.e(e);
            }

            if (notify && listAdapter != null) {
                listAdapter.notifyDataSetChanged();
//                AndroidUtilities.updateVisibleRows(scrollController.listView);
            }
            if (sharedMediaLayout != null && listAdapter.hasSharedMedia() && (sortedUsers.size() > 5 || usersForceShowingIn == 2) && usersForceShowingIn != 1) {
                sharedMediaLayout.setChatUsers(sortedUsers, dataProvider.getChatFull());
            }
        } else if (dataProvider.getChatFull() instanceof TLRPC.TL_channelFull && dataProvider.getChatFull().participants_count > 200) {
            profileHeaderView.infoView.onlineCount = dataProvider.getChatFull().online_count;
        }
    }

    private void openAddMember() {
        Bundle args = new Bundle();
        args.putBoolean("addToGroup", true);
        args.putLong("chatId", currentChat.id);
        GroupCreateActivity fragment = new GroupCreateActivity(args);
        fragment.setInfo(dataProvider.getChatFull());
        if (dataProvider.getChatFull() != null && dataProvider.getChatFull().participants != null) {
            LongSparseArray<TLObject> users = new LongSparseArray<>();
            for (int a = 0; a < dataProvider.getChatFull().participants.participants.size(); a++) {
                users.put(dataProvider.getChatFull().participants.participants.get(a).user_id, null);
            }
            fragment.setIgnoreUsers(users);
        }
        fragment.setDelegate2((users, fwdCount) -> {
            HashSet<Long> currentParticipants = new HashSet<>();
            ArrayList<TLRPC.User> addedUsers = new ArrayList<>();
            if (dataProvider.getChatFull() != null && dataProvider.getChatFull().participants != null && dataProvider.getChatFull().participants.participants != null) {
                for (int i = 0; i < dataProvider.getChatFull().participants.participants.size(); i++) {
                    currentParticipants.add(dataProvider.getChatFull().participants.participants.get(i).user_id);
                }
            }
            getMessagesController().addUsersToChat(currentChat, ProfileActivityV2.this, users, fwdCount, user -> {
                addedUsers.add(user);
            }, restrictedUser -> {
                for (int i = 0; i < dataProvider.getChatFull().participants.participants.size(); i++) {
                    if (dataProvider.getChatFull().participants.participants.get(i).user_id == restrictedUser.id) {
                        dataProvider.getChatFull().participants.participants.remove(i);
                        updateListAnimated(true);
                        break;
                    }
                }
            }, () -> {
                int N = addedUsers.size();
                int[] finished = new int[1];
                for (int a = 0; a < N; a++) {
                    TLRPC.User user = addedUsers.get(a);
                    if (!currentParticipants.contains(user.id)) {
                        if (dataProvider.getChatFull().participants == null) {
                            dataProvider.getChatFull().participants = new TLRPC.TL_chatParticipants();
                        }
                        if (ChatObject.isChannel(currentChat)) {
                            TLRPC.TL_chatChannelParticipant channelParticipant1 = new TLRPC.TL_chatChannelParticipant();
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipant();
                            channelParticipant1.channelParticipant.inviter_id = getUserConfig().getClientUserId();
                            channelParticipant1.channelParticipant.peer = new TLRPC.TL_peerUser();
                            channelParticipant1.channelParticipant.peer.user_id = user.id;
                            channelParticipant1.channelParticipant.date = getConnectionsManager().getCurrentTime();
                            channelParticipant1.user_id = user.id;
                            dataProvider.getChatFull().participants.participants.add(channelParticipant1);
                        } else {
                            TLRPC.ChatParticipant participant = new TLRPC.TL_chatParticipant();
                            participant.user_id = user.id;
                            participant.inviter_id = getAccountInstance().getUserConfig().clientUserId;
                            dataProvider.getChatFull().participants.participants.add(participant);
                        }
                        dataProvider.getChatFull().participants_count++;
                        getMessagesController().putUser(user, false);
                    }
                }
                updateListAnimated(true);
            });

        });
        presentFragment(fragment);
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        scrollController.finish();

        if (profileHeaderView != null) {
            profileHeaderView.onDestroy();
        }

        if (sharedMediaPreloader != null) {
            sharedMediaPreloader.onDestroy(this);
        }
        if (sharedMediaPreloader != null) {
            sharedMediaPreloader.removeDelegate(this);
        }

        if (imageUpdater != null) {
            imageUpdater.clear();
        }

        if (pinchToZoomHelper != null) {
            pinchToZoomHelper.clear();
        }

        if (birthdayFetcher != null && createdBirthdayFetcher) {
            birthdayFetcher.detach(true);
            birthdayFetcher = null;
        }

        getNotificationCenter().removeObserver(this, NotificationCenter.updateInterfaces);
        getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
        getNotificationCenter().removeObserver(this, NotificationCenter.didReceiveNewMessages);
        getNotificationCenter().removeObserver(this, NotificationCenter.topicsDidLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.updateSearchSettings);
        getNotificationCenter().removeObserver(this, NotificationCenter.reloadDialogPhotos);
        getNotificationCenter().removeObserver(this, NotificationCenter.storiesUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.storiesReadUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.userIsPremiumBlockedUpadted);
        getNotificationCenter().removeObserver(this, NotificationCenter.currentUserPremiumStatusChanged);
        getNotificationCenter().removeObserver(this, NotificationCenter.starBalanceUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.botStarsUpdated);
        getNotificationCenter().removeObserver(this, NotificationCenter.botStarsTransactionsLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.dialogDeleted);
        getNotificationCenter().removeObserver(this, NotificationCenter.channelRecommendationsLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.starUserGiftsLoaded);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);

        if (dataProvider.userId != 0) {
            getNotificationCenter().removeObserver(this, NotificationCenter.newSuggestionsAvailable);
            getNotificationCenter().removeObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.encryptedChatCreated);
            getNotificationCenter().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.blockedUsersDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.botInfoDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.userInfoDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.privacyRulesUpdated);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.reloadInterface);
            getMessagesController().cancelLoadFullUser(dataProvider.userId);
        } else if (dataProvider.chatId != 0) {
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.uploadStoryEnd);
            getNotificationCenter().removeObserver(this, NotificationCenter.chatWasBoostedByUser);
            getNotificationCenter().removeObserver(this, NotificationCenter.chatInfoDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.chatOnlineCountDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.groupCallUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.channelRightsUpdated);
        }
    }

    private void initializeResourcesProvider() {
        BaseFragment lastFragment = parentLayout.getLastFragment();
        if (lastFragment instanceof ChatActivity && ((ChatActivity) lastFragment).themeDelegate != null && ((ChatActivity) lastFragment).themeDelegate.getCurrentTheme() != null) {
            resourcesProvider = lastFragment.getResourceProvider();
        }
    }

    public void setPlayProfileAnimation(int type) {
        //TODO: implement Profile Animation
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setUserInfo(
            TLRPC.UserFull value,
            ProfileChannelCell.ChannelMessageFetcher channelMessageFetcher,
            ProfileBirthdayEffect.BirthdayEffectFetcher birthdayAssetsFetcher
    ) {
        dataProvider.setUserFull(value);
        if (profileHeaderView != null && profileHeaderView.storyView != null) {
            profileHeaderView.storyView.setStories(dataProvider.getUserFull().stories);
        }
        if (profileHeaderView != null && profileHeaderView.giftsView != null) {
            profileHeaderView.giftsView.update();
        }
        if (profileHeaderView != null && profileHeaderView.avatarImage != null) {
            profileHeaderView.avatarImage.setHasStories(dataProvider.needInsetForStories());
            profileHeaderView.updateProfileData(true);
        }
        if (sharedMediaLayout != null) {
            sharedMediaLayout.setUserInfo(dataProvider.getUserFull());
        }
        if (profileChannelMessageFetcher == null) {
            profileChannelMessageFetcher = channelMessageFetcher;
        }
        if (profileChannelMessageFetcher == null) {
            profileChannelMessageFetcher = new ProfileChannelCell.ChannelMessageFetcher(currentAccount);
        }
        profileChannelMessageFetcher.subscribe(() -> {
            if (listAdapter == null) { return; }
            updateCells();
            listAdapter.notifyDataSetChanged();
        });
        profileChannelMessageFetcher.fetch(dataProvider.getUserFull());
        if (birthdayFetcher == null) {
            birthdayFetcher = birthdayAssetsFetcher;
        }
        if (birthdayFetcher == null) {
            birthdayFetcher = ProfileBirthdayEffect.BirthdayEffectFetcher.of(currentAccount, dataProvider.getUserFull(), birthdayFetcher);
            createdBirthdayFetcher = birthdayFetcher != null;
        }
        if (birthdayFetcher != null) {
            birthdayFetcher.subscribe(this::createBirthdayEffect);
        }
        if (actionBarMoreItem != null) {
            actionBarMoreItem.setSubItemShown(ActionBarItems.start_secret_chat, DialogObject.isEmpty(getMessagesController().isUserContactBlocked(dataProvider.userId)));
            if (dataProvider.hasPrivacyCommand()) {
                actionBarMoreItem.showSubItem(ActionBarItems.bot_privacy);
            } else {
                actionBarMoreItem.hideSubItem(ActionBarItems.bot_privacy);
            }
        }
    }

    public void setChatInfo(TLRPC.ChatFull value) {
        dataProvider.setChatFull(value);
        if (dataProvider.getChatFull() != null && dataProvider.getChatFull().migrated_from_chat_id != 0 && mergeDialogId == 0) {
            mergeDialogId = -dataProvider.getChatFull().migrated_from_chat_id;
            getMediaDataController().getMediaCounts(mergeDialogId, dataProvider.topicId, classGuid);
        }
        if (sharedMediaLayout != null) {
            sharedMediaLayout.setChatInfo(dataProvider.getChatFull());
        }
        if (profileHeaderView != null && profileHeaderView.avatarsViewPager != null && !dataProvider.isTopic) {
            profileHeaderView.avatarsViewPager.setChatInfo(dataProvider.getChatFull());
        }
        if (profileHeaderView != null && profileHeaderView.storyView != null && dataProvider.getChatFull() != null) {
            profileHeaderView.storyView.setStories(dataProvider.getChatFull().stories);
        }
        if (profileHeaderView != null && profileHeaderView.giftsView != null) {
            profileHeaderView.giftsView.update();
        }
        if (profileHeaderView != null && profileHeaderView.avatarImage != null) {
            profileHeaderView.avatarImage.setHasStories(dataProvider.needInsetForStories());
        }
        fetchUsersFromChannelInfo();
        if (dataProvider.chatId != 0 && actionBarMoreItem != null) {
            actionBarMoreItem.setSubItemShown(ActionBarItems.gift_premium, !BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked() && dataProvider.getChatFull() != null && dataProvider.getChatFull().stargifts_available);
        }
    }

    private ProfileBirthdayEffect birthdayEffect;
    private void createBirthdayEffect() {
//        if (fragmentView == null || !fullyVisible || birthdayFetcher == null || getContext() == null)
//            return;
//
//        if (birthdayEffect != null) {
//            birthdayEffect.updateFetcher(birthdayFetcher);
//            birthdayEffect.invalidate();
//            return;
//        }
//
//        birthdayEffect = new ProfileBirthdayEffect(this, birthdayFetcher);
//        ((FrameLayout) fragmentView).addView(birthdayEffect, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL_HORIZONTAL | Gravity.TOP));
    }

    private void fetchUsersFromChannelInfo() {
        if (currentChat == null || !currentChat.megagroup) {
            return;
        }
//        if (chatInfo instanceof TLRPC.TL_channelFull && chatInfo.participants != null) {
//            for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
//                TLRPC.ChatParticipant chatParticipant = chatInfo.participants.participants.get(a);
//                participantsMap.put(chatParticipant.user_id, chatParticipant);
//            }
//        }
    }

    public void openAvatar() {
        if (scrollController.listView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
            return;
        }
        if (dataProvider.userId != 0) {
            TLRPC.User user = dataProvider.getUser();
            if (user.photo != null && user.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity(ProfileActivityV2.this);
                if (user.photo.dc_id != 0) {
                    user.photo.photo_big.dc_id = user.photo.dc_id;
                }
                PhotoViewer.getInstance().openPhoto(user.photo.photo_big, provider);
            }
        } else if (dataProvider.chatId != 0) {
            TLRPC.Chat chat = dataProvider.getChat();
            if (chat.photo != null && chat.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity(ProfileActivityV2.this);
                if (chat.photo.dc_id != 0) {
                    chat.photo.photo_big.dc_id = chat.photo.dc_id;
                }
                ImageLocation videoLocation;
                if (dataProvider.getChatFull() != null && (dataProvider.getChatFull().chat_photo instanceof TLRPC.TL_photo) && !dataProvider.getChatFull().chat_photo.video_sizes.isEmpty()) {
                    videoLocation = ImageLocation.getForPhoto(dataProvider.getChatFull().chat_photo.video_sizes.get(0), dataProvider.getChatFull().chat_photo);
                } else {
                    videoLocation = null;
                }
                PhotoViewer.getInstance().openPhotoWithVideo(chat.photo.photo_big, videoLocation, provider);
            }
        }
    }

    private void updateCells() {
        ArrayList<ProfileActivityListAdapter.CellData> items = new ArrayList();

        ProfileActivityListAdapter.TextCellData addContactCellData = null;

        visibleChatParticipants.clear();
        visibleSortedUsers.clear();

        final TLRPC.UserFull userInfo = dataProvider.getUserFull();
        final TLRPC.ChatFull chatInfo = dataProvider.getChatFull();

        boolean hasMedia = false;
        if (sharedMediaPreloader != null) {
            int[] lastMediaCount = sharedMediaPreloader.getLastMediaCount();
            for (int a = 0; a < lastMediaCount.length; a++) {
                if (lastMediaCount[a] > 0) {
                    hasMedia = true;
                    break;
                }
            }
            if (!hasMedia) {
                hasMedia = sharedMediaPreloader.hasSavedMessages;
            }
            if (!hasMedia) {
                hasMedia = sharedMediaPreloader.hasPreviews;
            }
        }
        if (!hasMedia && userInfo != null) {
            hasMedia = userInfo.stories_pinned_available;
        }
        if (!hasMedia && userInfo != null && userInfo.bot_info != null) {
            hasMedia = userInfo.bot_info.has_preview_medias;
        }
        if (!hasMedia && (userInfo != null && userInfo.stargifts_count > 0 || chatInfo != null && chatInfo.stargifts_count > 0)) {
            hasMedia = true;
        }
        if (!hasMedia && chatInfo != null) {
            hasMedia = chatInfo.stories_pinned_available;
        }
        if (!hasMedia) {
            if (dataProvider.chatId != 0 && MessagesController.ChannelRecommendations.hasRecommendations(currentAccount, -dataProvider.chatId)) {
                hasMedia = true;
            } else if (dataProvider.isBot() && dataProvider.userId != 0 && MessagesController.ChannelRecommendations.hasRecommendations(currentAccount, dataProvider.userId)) {
                hasMedia = true;
            }
        }

        if (dataProvider.getUser() != null) {
                if (dataProvider.getUserFull() != null && (dataProvider.getUserFull().flags2 & 64) != 0 && (profileChannelMessageFetcher == null || !profileChannelMessageFetcher.loaded || profileChannelMessageFetcher.messageObject != null)) {
                    final TLRPC.Chat channel = getMessagesController().getChat(dataProvider.getUserFull().personal_channel_id);
                    if (channel != null && (ChatObject.isPublic(channel) || !ChatObject.isNotInChat(channel)) && (profileChannelMessageFetcher != null && profileChannelMessageFetcher.messageObject != null)) {
                        items.add(
                                new ProfileActivityListAdapter.PersonalChannelCellData(
                                        dataProvider.getUserFull().personal_channel_id,
                                        profileChannelMessageFetcher == null ? null : profileChannelMessageFetcher.messageObject,
                                        (view, x, y) -> {
                                            if (userInfo == null) return;
                                            Bundle args = new Bundle();
                                            args.putLong("chat_id", userInfo.personal_channel_id);
                                            presentFragment(new ChatActivity(args));
                                        }
                                )
                        );
                        items.add(new ProfileActivityListAdapter.SectionDividerCellData());
                    }
                }

                if (dataProvider.getUser().phone != null) {
                    String text;
                    if (!TextUtils.isEmpty(vcardPhone)) {
                        text = PhoneFormat.getInstance().format("+" + vcardPhone);
                    } else if (!TextUtils.isEmpty(dataProvider.getUser().phone)) {
                        text = PhoneFormat.getInstance().format("+" + dataProvider.getUser().phone);
                    } else {
                        text = getString(R.string.PhoneHidden);
                    }

                    isFragmentPhoneNumber = text != null && text.matches("888\\d{8}");

                    items.add(
                            new ProfileActivityListAdapter.TitleSubtitleCellData(
                                    getString(isSettings() ? R.string.TapToChangePhone : R.string.Phone),
                                    text,
                                    false,
                                    0,
                                    (x, y, view) -> {
                                        if (isSettings()) {
                                            presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
                                        } else {
                                            processPhoneClick(x, y, view);
                                        }
                                    },
                                    this::processPhoneClick,
                                    null
                            )
                    );
                }

                if (dataProvider.getUserFull() != null && dataProvider.getUserFull().about != null) {
                    items.add(
                            new ProfileActivityListAdapter.AboutLinkCellData(
                                    dataProvider.getUserFull().about,
                                    getString(R.string.UserBio),
                                    true,
                                    (view, x, y) -> {
                                        ((AboutLinkCell) view).onClick();
                                    },
                                    this::processAbout
                            )
                    );
                }

                if (dataProvider.getUser().username != null) {
                    items.add(userNameData());
                }

                if (isSettings()) {
                    items.add(new ProfileActivityListAdapter.SectionDividerCellData());
                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.ChatSettings),
                                    null,
                                    R.drawable.input_message,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC))
                            )
                    );

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.PrivacySettings),
                                    null,
                                    R.drawable.msg_secret,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> presentFragment(new PrivacySettingsActivity().setCurrentPassword(currentPassword))
                            )
                    );

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.NotificationsAndSounds),
                                    null,
                                    R.drawable.msg_notifications,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> presentFragment(new NotificationsSettingsActivity())
                            )
                    );

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.DataSettings),
                                    null,
                                    R.drawable.msg2_data,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> presentFragment(new DataSettingsActivity())
                            )
                    );

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.PowerUsage),
                                    null,
                                    R.drawable.msg2_battery,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> presentFragment(new LiteModeSettingsActivity())
                            )
                    );

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.Filters),
                                    null,
                                    R.drawable.msg2_folder,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> presentFragment(new FiltersSetupActivity())
                            )
                    );

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.Devices),
                                    null,
                                    R.drawable.msg2_devices,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> presentFragment(new SessionsActivity(0))
                            )
                    );

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.Language),
                                    LocaleController.getCurrentLanguageName(),
                                    R.drawable.msg2_language,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> presentFragment(new LanguageSelectActivity())
                            )
                    );

                    if (getMessagesController().starsPurchaseAvailable() || !getMessagesController().premiumFeaturesBlocked()) {
                        items.add(new ProfileActivityListAdapter.SectionDividerCellData());
                    }


                    if (!getMessagesController().premiumFeaturesBlocked()) {
                        items.add(
                                new ProfileActivityListAdapter.TextCellData(
                                        getString(R.string.TelegramPremium),
                                        null,
                                        new AnimatedEmojiDrawable.WrapSizeDrawable(PremiumGradient.getInstance().premiumStarMenuDrawable, dp(24), dp(24)),
                                        Theme.key_windowBackgroundWhiteBlackText,
                                        Theme.key_windowBackgroundWhiteGrayIcon,
                                        false,
                                        () -> presentFragment(new PremiumPreviewFragment("settings"))
                                )
                        );
                    }
                    if (getMessagesController().starsPurchaseAvailable()) {
                        StarsController c = StarsController.getInstance(currentAccount);
                        long balance = c.getBalance().amount;

                        items.add(
                                new ProfileActivityListAdapter.TextCellData(
                                        getString(R.string.MenuTelegramStars),
                                        c.balanceAvailable() && balance > 0 ? LocaleController.formatNumber((int) balance, ',') : "",
                                        new AnimatedEmojiDrawable.WrapSizeDrawable(PremiumGradient.getInstance().goldenStarMenuDrawable, dp(24), dp(24)),
                                        Theme.key_windowBackgroundWhiteBlackText,
                                        Theme.key_windowBackgroundWhiteGrayIcon,
                                        false,
                                        () -> presentFragment(new StarsIntroActivity())
                                )
                        );
                    }
                    if (!getMessagesController().premiumFeaturesBlocked()) {
                        items.add(
                                new ProfileActivityListAdapter.TextCellData(
                                        getString(R.string.TelegramBusiness),
                                       "",
                                        R.drawable.menu_shop,
                                        Theme.key_windowBackgroundWhiteBlackText,
                                        Theme.key_windowBackgroundWhiteGrayIcon,
                                        false,
                                        () -> presentFragment(new PremiumPreviewFragment(PremiumPreviewFragment.FEATURES_BUSINESS, "settings"))
                                )
                        );
                    }
                    if (!getMessagesController().premiumPurchaseBlocked()) {
                        items.add(
                                new ProfileActivityListAdapter.TextCellData(
                                        getString(R.string.SendAGift),
                                        "",
                                        R.drawable.menu_gift,
                                        Theme.key_windowBackgroundWhiteBlackText,
                                        Theme.key_windowBackgroundWhiteGrayIcon,
                                        false,
                                        () -> UserSelectorBottomSheet.open(0, BirthdayController.getInstance(currentAccount).getState())
                                )
                        );
                    }

                    items.add(new ProfileActivityListAdapter.SectionDividerCellData());

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.AskAQuestion),
                                    null,
                                    R.drawable.msg2_ask_question,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> showDialog(AlertsCreator.createSupportAlert(ProfileActivityV2.this, resourcesProvider))
                            )
                    );

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.TelegramFAQ),
                                    null,
                                    R.drawable.msg2_help,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> {
                                        Browser.openUrl(getParentActivity(), getString(R.string.TelegramFaqUrl));
                                        return true;
                                    }
                            )
                    );

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.PrivacyPolicy),
                                    null,
                                    R.drawable.msg2_policy,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> {
                                        Browser.openUrl(getParentActivity(), getString(R.string.PrivacyPolicyUrl));
                                        return true;
                                    }
                            )
                    );

                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        int code = pInfo.versionCode / 10;
                        String abi = "";
                        switch (pInfo.versionCode % 10) {
                            case 1:
                            case 2:
                                abi = "store bundled " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                                break;
                            default:
                            case 9:
                                if (ApplicationLoader.isStandaloneBuild()) {
                                    abi = "direct " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                                } else {
                                    abi = "universal " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                                }
                                break;
                        }
                        items.add(
                                new ProfileActivityListAdapter.ShadowTextCellData(
                                        formatString("TelegramVersion", R.string.TelegramVersion, String.format(Locale.US, "v%s (%d) %s", pInfo.versionName, code, abi)),
                                        Gravity.CENTER_HORIZONTAL
                                )
                        );
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                } else {
                    if (dataProvider.getUserFull() != null && dataProvider.getUserFull().birthday != null) {
                        final boolean today = BirthdayController.isToday(dataProvider.getUserFull());
                        final boolean withYear = (dataProvider.getUserFull().birthday.flags & 1) != 0;
                        final int age = withYear ? Period.between(LocalDate.of(dataProvider.getUserFull().birthday.year, dataProvider.getUserFull().birthday.month, dataProvider.getUserFull().birthday.day), LocalDate.now()).getYears() : -1;

                        String text = UserInfoActivity.birthdayString(dataProvider.getUserFull().birthday);
                        if (withYear) {
                            text = LocaleController.formatPluralString(today ? "ProfileBirthdayTodayValueYear" : "ProfileBirthdayValueYear", age, text);
                        } else {
                            text = formatString(today ? R.string.ProfileBirthdayTodayValue : R.string.ProfileBirthdayValue, text);
                        }

                        items.add(
                                new ProfileActivityListAdapter.TitleSubtitleCellData(
                                        getString(R.string.ProfileBirthday),
                                        text,
                                        false,
                                        0,
                                        null,
                                        null,
                                        null
                                )
                        );
                    }

                    if (dataProvider.getUserFull() != null && dataProvider.getUserFull().business_work_hours != null) {
                        ProfileActivityListAdapter.HoursCellData cellData = new ProfileActivityListAdapter.HoursCellData(
                                dataProvider.getUserFull().business_work_hours,
                                null,
                                null
                        );

                        cellData.onClick = (view, x, y) -> {
                            cellData.isExpanded = !cellData.isExpanded;
                            view.requestLayout();
                            listAdapter.notifyItemChanged(listAdapter.itemPosition(ProfileActivityListAdapter.HoursCellData.class));
                        };
                        cellData.onLongClick = this::processBusinessHours;

                        items.add(cellData);
                    }


                    if (dataProvider.getUserFull() != null && dataProvider.getUserFull().business_location != null) {
                        items.add(
                                new ProfileActivityListAdapter.LocationCellData(
                                        dataProvider.getUserFull() == null ? null : dataProvider.getUserFull().business_location,
                                        false,
                                        (view, x, y) -> openLocation(false),
                                        this::processUserLocation
                                )
                        );
                    }

                    if (dataProvider.isBot() && dataProvider.getUser() != null && dataProvider.getUser().bot_has_main_app) {
                        items.add(new ProfileActivityListAdapter.AppButtonCellData());

                        SpannableStringBuilder sb = new SpannableStringBuilder();
                        final TLRPC.User user = dataProvider.getUser();
                        final boolean botOwner = user != null && user.bot && user.bot_can_edit;
                        TL_bots.botVerification bot_verification = userInfo != null ? userInfo.bot_verification : chatInfo != null ? chatInfo.bot_verification : null;

                        sb.append(AndroidUtilities.replaceSingleTag(getString(botOwner ? R.string.ProfileBotOpenAppInfoOwner : R.string.ProfileBotOpenAppInfo), () -> {
                            Browser.openUrl(getContext(), getString(botOwner ? R.string.ProfileBotOpenAppInfoOwnerLink : R.string.ProfileBotOpenAppInfoLink));
                        }));

                        TextPaint paint = new TextPaint();

                        if (bot_verification != null) {
                            sb.append("\n\n\n");
                        }
                        if (bot_verification != null) {
                            sb.append("x");
                            sb.setSpan(new AnimatedEmojiSpan(bot_verification.icon, paint.getFontMetricsInt()), sb.length() - 1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            sb.append(" ");
                            SpannableString description = new SpannableString(bot_verification.description);
                            try {
                                AndroidUtilities.addLinksSafe(description, Linkify.WEB_URLS, false, false);
                                URLSpan[] spans = description.getSpans(0, description.length(), URLSpan.class);
                                for (int i = 0; i < spans.length; ++i) {
                                    URLSpan span = spans[i];
                                    int start = description.getSpanStart(span);
                                    int end = description.getSpanEnd(span);
                                    final String url = span.getURL();

                                    description.removeSpan(span);
                                    description.setSpan(new URLSpan(url) {
                                        @Override
                                        public void onClick(View widget) {
                                            Browser.openUrl(getContext(), url);
                                        }
                                        @Override
                                        public void updateDrawState(@NonNull TextPaint ds) {
                                            ds.setUnderlineText(true);
                                        }
                                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            sb.append(description);
                        }
                        items.add(
                                new ProfileActivityListAdapter.ShadowTextCellData(
                                        sb,
                                        Gravity.LEFT
                                )
                        );
                    }

                    if (!dataProvider.isMyProfile && dataProvider.showAddToContacts && dataProvider.getUser() != null && !dataProvider.getUser().contact && !dataProvider.getUser().bot && !UserObject.isService(dataProvider.getUser().id)) {
                        items.add(new ProfileActivityListAdapter.SectionDividerCellData());


                        items.add(
                                new ProfileActivityListAdapter.TextCellData(
                                        getString(R.string.AddToContacts),
                                        null,
                                        R.drawable.msg_contact_add,
                                        Theme.key_windowBackgroundWhiteBlueButton,
                                        Theme.key_windowBackgroundWhiteBlueIcon,
                                        false,
                                        () -> {
                                            TLRPC.User user = getMessagesController().getUser(dataProvider.userId);
                                            Bundle args = new Bundle();
                                            args.putLong("user_id", user.id);
                                            args.putBoolean("addContact", true);
                                            openAddToContact(user, args);
                                            return true;
                                        }
                                )
                        );
                    }
                }
        } else if (dataProvider.getChat() != null) {
            if (chatInfo != null && (!TextUtils.isEmpty(chatInfo.about) || chatInfo.location instanceof TLRPC.TL_channelLocation) || ChatObject.isPublic(currentChat)) {
                if (LocaleController.isRTL && ChatObject.isChannel(currentChat) && chatInfo != null && !currentChat.megagroup && chatInfo.linked_chat_id != 0) {
//                    emptyRow = rowCount++;
                }

                if (chatInfo != null) {
                    if (!TextUtils.isEmpty(chatInfo.about)) {
                        items.add(
                            new ProfileActivityListAdapter.AboutLinkCellData(
                                chatInfo.about,
                                getString(R.string.DescriptionPlaceholder),
                                true,
                                (view, x, y) -> {
                                    ((AboutLinkCell) view).onClick();
                                },
                                this::processAbout
                            )
                        );
                    }
                    if (chatInfo.location instanceof TLRPC.TL_channelLocation) {
                        TLRPC.TL_channelLocation location = (TLRPC.TL_channelLocation) chatInfo.location;
                        items.add(
                            new ProfileActivityListAdapter.TitleSubtitleCellData(
                                    getString(R.string.AttachLocation),
                                    location.address,
                                    true,
                                    0,
                                    null,
                                    null,
                                    null
                            )
                        );
                    }
                }
                if (ChatObject.isPublic(currentChat)) {
                    items.add(userNameData());
                }
            }

            if (!isSettings()) {
                items.add(
                        new ProfileActivityListAdapter.SectionDividerCellData()
                );
            }
//            notificationsRow = rowCount++;
//            infoSectionRow = rowCount++;

            if (ChatObject.isChannel(currentChat) && !currentChat.megagroup) {
                if (chatInfo != null && (currentChat.creator || chatInfo.can_view_participants)) {
                    String subscribersText;
                    String subscribersValue = null;

                    if (chatInfo != null) {
                        if (ChatObject.isChannel(currentChat) && !currentChat.megagroup) {
                            subscribersText = getString(R.string.ChannelSubscribers);
                            subscribersValue = LocaleController.formatNumber(chatInfo.participants_count, ',');
                        } else {
                            subscribersText = getString(R.string.ChannelMembers);
                            subscribersValue = LocaleController.formatNumber(chatInfo.participants_count, ',');
                        }
                    } else {
                        if (ChatObject.isChannel(currentChat) && !currentChat.megagroup) {
                            subscribersText = getString(R.string.ChannelSubscribers);
                        } else {
                            subscribersText = getString(R.string.ChannelMembers);
                        }
                    }

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                subscribersText,
                                subscribersValue,
                                R.drawable.msg_groups,
                                Theme.key_windowBackgroundWhiteBlackText,
                                Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> {
                                    Bundle args = new Bundle();
                                    args.putLong("chat_id", dataProvider.chatId);
                                    args.putInt("type", ChatUsersActivity.TYPE_USERS);
                                    ChatUsersActivity fragment = new ChatUsersActivity(args);
                                    fragment.setInfo(chatInfo);
                                    return presentFragment(fragment);
                                }
                            )
                    );

                    if (chatInfo.requests_pending > 0) {
                        items.add(
                                new ProfileActivityListAdapter.TextCellData(
                                        getString(R.string.SubscribeRequests),
                                        String.format("%d", chatInfo.requests_pending),
                                        R.drawable.msg_requests,
                                        Theme.key_windowBackgroundWhiteBlackText,
                                        Theme.key_windowBackgroundWhiteGrayIcon,
                                        false,
                                        () -> {
                                            MemberRequestsActivity activity = new MemberRequestsActivity(dataProvider.chatId);
                                            return presentFragment(activity);
                                        }
                                )
                        );
                    }

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.ChannelAdministrators),
                                    String.format("%d", chatInfo.admins_count),
                                    R.drawable.msg_admins,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> {
                                        Bundle args = new Bundle();
                                        args.putLong("chat_id", dataProvider.chatId);
                                        args.putInt("type", ChatUsersActivity.TYPE_ADMIN);
                                        ChatUsersActivity fragment = new ChatUsersActivity(args);
                                        fragment.setInfo(chatInfo);
                                        return presentFragment(fragment);
                                    }
                            )
                    );

                    if ((chatInfo.banned_count != 0 || chatInfo.kicked_count != 0)) {
                        items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                getString(R.string.ChannelBlacklist),
                                String.format("%d", Math.max(chatInfo.banned_count, chatInfo.kicked_count)),
                                R.drawable.msg_user_remove,
                                Theme.key_windowBackgroundWhiteBlackText,
                                Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> {
                                    Bundle args = new Bundle();
                                    args.putLong("chat_id", dataProvider.chatId);
                                    args.putInt("type", ChatUsersActivity.TYPE_BANNED);
                                    ChatUsersActivity fragment = new ChatUsersActivity(args);
                                    fragment.setInfo(chatInfo);
                                    return presentFragment(fragment);
                                }
                            )
                        );
                    }

                    if (
                            chatInfo.can_view_stars_revenue && (
                                BotStarsController.getInstance(currentAccount).getBotStarsBalance(-dataProvider.chatId).amount > 0 ||
                                    BotStarsController.getInstance(currentAccount).hasTransactions(-dataProvider.chatId)
                            ) ||
                                chatInfo.can_view_revenue &&
                                    BotStarsController.getInstance(currentAccount).getTONBalance(-dataProvider.chatId) > 0
                    ) {
                        items.add(channelBalanceData());
                    }

                    items.add(
                            new ProfileActivityListAdapter.TextCellData(
                                    getString(R.string.ChannelAdminSettings),
                                    null,
                                    R.drawable.msg_customize,
                                    Theme.key_windowBackgroundWhiteBlackText,
                                    Theme.key_windowBackgroundWhiteGrayIcon,
                                    false,
                                    () -> actionBarEditItem.performClick()
                            )
                    );

                    items.add(new ProfileActivityListAdapter.SectionDividerCellData());
                }
            } else {
                if (
                        chatInfo != null &&
                        chatInfo.can_view_stars_revenue && (
                            BotStarsController.getInstance(currentAccount).getBotStarsBalance(-dataProvider.chatId).amount > 0 ||
                            BotStarsController.getInstance(currentAccount).hasTransactions(-dataProvider.chatId)
                        ) ||
                            chatInfo != null &&
                            chatInfo.can_view_revenue &&
                            BotStarsController.getInstance(currentAccount).getTONBalance(-dataProvider.chatId) > 0
                ) {
                    items.add(channelBalanceData());
                    items.add(new ProfileActivityListAdapter.SectionDividerCellData());
                }
            }

            if (ChatObject.isChannel(currentChat)) {
                if (!dataProvider.isTopic && chatInfo != null && currentChat.megagroup && chatInfo.participants != null && chatInfo.participants.participants != null && !chatInfo.participants.participants.isEmpty()) {
                    if (!ChatObject.isNotInChat(currentChat) && ChatObject.canAddUsers(currentChat) && chatInfo.participants_count < getMessagesController().maxMegagroupCount) {
                        addContactCellData = addMemberData();
                        items.add(addContactCellData);
                    }
                    int count = chatInfo.participants.participants.size();
                    if ((count <= 5 || !hasMedia || usersForceShowingIn == 1) && usersForceShowingIn != 2) {
                        visibleChatParticipants.addAll(chatInfo.participants.participants);
                        if (sortedUsers != null) {
                            visibleSortedUsers.addAll(sortedUsers);
                        }

                        items.addAll(practicantsData());
                        items.add(new ProfileActivityListAdapter.SectionDividerCellData());
                        
                        if (addContactCellData != null) {
                            addContactCellData.needDivider = true;
                        }

                        usersForceShowingIn = 1;
                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.setChatUsers(null, null);
                        }
                    } else {
                        if (sharedMediaLayout != null) {
                            if (!sortedUsers.isEmpty()) {
                                usersForceShowingIn = 2;
                            }
                            sharedMediaLayout.setChatUsers(sortedUsers, chatInfo);
                        }
                    }
                } else {
                    if (!ChatObject.isNotInChat(currentChat) && ChatObject.canAddUsers(currentChat) && chatInfo != null && chatInfo.participants_hidden) {
                        addContactCellData = addMemberData();
                        items.add(addContactCellData);
                        items.add(new ProfileActivityListAdapter.SectionDividerCellData());
                    }
                    if (sharedMediaLayout != null) {
                        sharedMediaLayout.updateAdapters();
                    }
                }

            } else if (chatInfo != null) {
                if (!dataProvider.isTopic && chatInfo.participants != null && chatInfo.participants.participants != null && !(chatInfo.participants instanceof TLRPC.TL_chatParticipantsForbidden)) {
                    if (ChatObject.canAddUsers(currentChat) || currentChat.default_banned_rights == null || !currentChat.default_banned_rights.invite_users) {
                        addContactCellData = addMemberData();
                        items.add(addContactCellData);
                    }
                    int count = chatInfo.participants.participants.size();
                    if (count <= 5 || !hasMedia) {
                        visibleChatParticipants.addAll(chatInfo.participants.participants);
                        if (sortedUsers != null) {
                            visibleSortedUsers.addAll(sortedUsers);
                        }

                        items.addAll(practicantsData());
                        items.add(new ProfileActivityListAdapter.SectionDividerCellData());

                        if (addContactCellData != null) {
                            addContactCellData.needDivider = true;
                        }

                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.setChatUsers(null, null);
                        }
                    } else {
                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.setChatUsers(sortedUsers, chatInfo);
                        }
                    }
                } else {
                    if (!ChatObject.isNotInChat(currentChat) && ChatObject.canAddUsers(currentChat) && chatInfo.participants_hidden) {
                        addContactCellData = addMemberData();
                        items.add(addContactCellData);
                        items.add(new ProfileActivityListAdapter.SectionDividerCellData());
                    }
                    if (sharedMediaLayout != null) {
                        sharedMediaLayout.updateAdapters();
                    }
                }
            }
        }

        if (items.get(items.size() - 1).getClass() != ProfileActivityListAdapter.SectionDividerCellData.class) {
            items.add(new ProfileActivityListAdapter.SectionDividerCellData());
        }

        if (!isSettings() && (hasMedia || (dataProvider.getUser() != null && dataProvider.getUser().bot && dataProvider.getUser().bot_can_edit) || dataProvider.getUserFull() != null && dataProvider.getUserFull().common_chats_count != 0 || dataProvider.isMyProfile)) {
            items.add(new ProfileActivityListAdapter.SharedMediaCellData());
        } else {
            items.add(new ProfileActivityListAdapter.BottomSpacerCellData());
        }
        listAdapter.setItems(items);
    }

    private ArrayList<ProfileActivityListAdapter.UserCellData> practicantsData() {
        ArrayList<ProfileActivityListAdapter.UserCellData> list = new ArrayList<>();

        for(int practicantIndex = 0; practicantIndex < dataProvider.getChatFull().participants.participants.size(); practicantIndex++) {
            TLRPC.ChatParticipant part;

            try {
                if (!visibleSortedUsers.isEmpty()) {
                    part = visibleChatParticipants.get(visibleSortedUsers.get(practicantIndex));
                } else {
                    part = visibleChatParticipants.get(practicantIndex);
                }
            } catch (Exception e) {
                part = null;
                FileLog.e(e);
            }

            if (part != null) {
                String role;
                if (part instanceof TLRPC.TL_chatChannelParticipant) {
                    TLRPC.ChannelParticipant channelParticipant = ((TLRPC.TL_chatChannelParticipant) part).channelParticipant;
                    if (!TextUtils.isEmpty(channelParticipant.rank)) {
                        role = channelParticipant.rank;
                    } else {
                        if (channelParticipant instanceof TLRPC.TL_channelParticipantCreator) {
                            role = getString(R.string.ChannelCreator);
                        } else if (channelParticipant instanceof TLRPC.TL_channelParticipantAdmin) {
                            role = getString(R.string.ChannelAdmin);
                        } else {
                            role = null;
                        }
                    }
                } else {
                    if (part instanceof TLRPC.TL_chatParticipantCreator) {
                        role = getString(R.string.ChannelCreator);
                    } else if (part instanceof TLRPC.TL_chatParticipantAdmin) {
                        role = getString(R.string.ChannelAdmin);
                    } else {
                        role = null;
                    }
                }

                list.add(
                    new ProfileActivityListAdapter.UserCellData(
                        getMessagesController().getUser(part.user_id),
                        part,
                        role,
                        practicantIndex != dataProvider.getChatFull().participants.participants.size() - 1,
                            (view, x, y, parti) -> {
                                onMemberClick(parti, false, view);
                            }
                    )
                );
            }
        }

        return list;
    }

    private ProfileActivityListAdapter.TextCellData channelBalanceData() {
        final TL_stars.StarsAmount stars_balance = BotStarsController.getInstance(currentAccount).getBotStarsBalance(-dataProvider.chatId);
        final long ton_balance = BotStarsController.getInstance(currentAccount).getTONBalance(-dataProvider.chatId);
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        if (ton_balance > 0) {
            if (ton_balance / 1_000_000_000.0 > 1000.0) {
                ssb.append("TON ").append(AndroidUtilities.formatWholeNumber((int) (ton_balance / 1_000_000_000.0), 0));
            } else {
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                symbols.setDecimalSeparator('.');
                DecimalFormat formatterTON = new DecimalFormat("#.##", symbols);
                formatterTON.setMinimumFractionDigits(2);
                formatterTON.setMaximumFractionDigits(3);
                formatterTON.setGroupingUsed(false);
                ssb.append("TON ").append(formatterTON.format(ton_balance / 1_000_000_000.0));
            }
        }
        if (stars_balance.amount > 0) {
            if (ssb.length() > 0) ssb.append(" ");
            ssb.append("XTR ").append(formatStarsAmountShort(stars_balance));
        }

        return new ProfileActivityListAdapter.TextCellData(
                getString(R.string.ChannelStars),
                ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), new TextPaint()),
                R.drawable.menu_feature_paid,
                Theme.key_windowBackgroundWhiteBlackText,
                Theme.key_windowBackgroundWhiteGrayIcon,
                false,
                () -> {
                    Bundle args = new Bundle();
                    args.putLong("chat_id", dataProvider.chatId);
                    args.putBoolean("start_from_monetization", true);
                    return presentFragment(new StatisticActivity(args));
                }
        );
    }

    private ProfileActivityListAdapter.TextCellData addMemberData() {
        return new ProfileActivityListAdapter.TextCellData(
                getString(R.string.AddMember),
                null,
                R.drawable.msg_contact_add,
                Theme.key_windowBackgroundWhiteBlueButton,
                Theme.key_windowBackgroundWhiteBlueIcon,
                false,
                () -> {
                    openAddMember();
                    return true;
                }
        );
    }

    private ProfileActivityListAdapter.TitleSubtitleCellData userNameData() {
        String username = null;
        CharSequence text;
        CharSequence value;
        boolean containsQr = false;

        ArrayList<TLRPC.TL_username> usernames = new ArrayList<>();
        if (dataProvider.userId != 0) {
            final TLRPC.User user = dataProvider.getUser();
            if (user != null) {
                usernames.addAll(user.usernames);
            }
            TLRPC.TL_username usernameObj = null;
            if (user != null && !TextUtils.isEmpty(user.username)) {
                usernameObj = DialogObject.findUsername(user.username, usernames);
                username = user.username;
            }
            usernames = user == null ? new ArrayList<>() : new ArrayList<>(user.usernames);
            if (TextUtils.isEmpty(username) && usernames != null) {
                for (int i = 0; i < usernames.size(); ++i) {
                    TLRPC.TL_username u = usernames.get(i);
                    if (u != null && u.active && !TextUtils.isEmpty(u.username)) {
                        usernameObj = u;
                        username = u.username;
                        break;
                    }
                }
            }
            value = getString(R.string.Username);
            if (username != null) {
                text = "@" + username;
                if (usernameObj != null && !usernameObj.editable) {
                    text = new SpannableString(text);
                    ((SpannableString) text).setSpan(makeUsernameLinkSpan(usernameObj), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                text = "—";
            }
            containsQr = true;
        } else if (currentChat != null) {
            TLRPC.Chat chat = dataProvider.getChat();
            username = ChatObject.getPublicUsername(chat);
            if (chat != null) {
                usernames.addAll(chat.usernames);
            }
            if (ChatObject.isPublic(chat)) {
                containsQr = true;
                text = getMessagesController().linkPrefix + "/" + username + (dataProvider.topicId != 0 ? "/" + dataProvider.topicId : "");
                value = getString(R.string.InviteLink);
            } else {
                text = getMessagesController().linkPrefix + "/c/" + dataProvider.chatId + (dataProvider.topicId != 0 ? "/" + dataProvider.topicId : "");
                value = getString(R.string.InviteLinkPrivate);
            }
        } else {
            text = "";
            value = "";
            usernames = new ArrayList<>();
        }

        return new ProfileActivityListAdapter.TitleSubtitleCellData(
                alsoUsernamesString(username, usernames, value).toString(),
                text.toString(),
                true,
                containsQr ? R.drawable.msg_qrcode : 0,
                (view, x, y) -> {
                    processUsernameClick(view, x, y);
                },
                (view, x, y) -> {
                    processUsernameClick(view, x, y);
                },
                () -> {
                    Bundle args = new Bundle();
                    args.putLong("chat_id", dataProvider.chatId);
                    args.putLong("user_id", dataProvider.userId);
                    presentFragment(new QrActivity(args));
                }
        );
    }

    private CharSequence alsoUsernamesString(String originalUsername, ArrayList<TLRPC.TL_username> alsoUsernames, CharSequence fallback) {
        if (alsoUsernames == null) {
            return fallback;
        }
        alsoUsernames = new ArrayList<>(alsoUsernames);
        for (int i = 0; i < alsoUsernames.size(); ++i) {
            if (
                    !alsoUsernames.get(i).active ||
                            originalUsername != null && originalUsername.equals(alsoUsernames.get(i).username)
            ) {
                alsoUsernames.remove(i--);
            }
        }
        if (alsoUsernames.size() > 0) {
            SpannableStringBuilder usernames = new SpannableStringBuilder();
            for (int i = 0; i < alsoUsernames.size(); ++i) {
                TLRPC.TL_username usernameObj = alsoUsernames.get(i);
                final String usernameRaw = usernameObj.username;
                SpannableString username = new SpannableString("@" + usernameRaw);
                username.setSpan(makeUsernameLinkSpan(usernameObj), 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                username.setSpan(new ForegroundColorSpan(getThemedColor(Theme.key_chat_messageLinkIn)), 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                usernames.append(username);
                if (i < alsoUsernames.size() - 1) {
                    usernames.append(", ");
                }
            }
            String string = getString(R.string.UsernameAlso);
            SpannableStringBuilder finalString = new SpannableStringBuilder(string);
            final String toFind = "%1$s";
            int index = string.indexOf(toFind);
            if (index >= 0) {
                finalString.replace(index, index + toFind.length(), usernames);
            }
            return finalString;
        } else {
            return fallback;
        }
    }

    private final HashMap<TLRPC.TL_username, ClickableSpan> usernameSpans = new HashMap<TLRPC.TL_username, ClickableSpan>();
    public CharacterStyle loadingSpan;
    public ClickableSpan makeUsernameLinkSpan(TLRPC.TL_username usernameObj) {
        ClickableSpan span = usernameSpans.get(usernameObj);
        if (span != null) return span;

        final String usernameRaw = usernameObj.username;
        span = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                if (!usernameObj.editable) {
                    if (loadingSpan == this) return;
                    setLoadingSpan(this);
                    TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                    TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                    input.username = usernameObj.username;
                    req.collectible = input;
                    int reqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        setLoadingSpan(null);
                        if (res instanceof TL_fragment.TL_collectibleInfo) {
                            TLObject obj;
                            if (dataProvider.userId != 0) {
                                obj = dataProvider.getUser();
                            } else {
                                obj = dataProvider.getChat();
                            }
                            if (getContext() == null) {
                                return;
                            }
                            FragmentUsernameBottomSheet.open(getContext(), FragmentUsernameBottomSheet.TYPE_USERNAME, usernameObj.username, obj, (TL_fragment.TL_collectibleInfo) res, getResourceProvider());
                        } else {
                            BulletinFactory.showError(err);
                        }
                    }));
                    getConnectionsManager().bindRequestToGuid(reqId, getClassGuid());
                } else {
                    setLoadingSpan(null);
                    String urlFinal = getMessagesController().linkPrefix + "/" + usernameRaw;
                    if (currentChat == null || !currentChat.noforwards) {
                        AndroidUtilities.addToClipboard(urlFinal);
                        undoView.showWithAction(0, UndoView.ACTION_USERNAME_COPIED, null);
                    }
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setColor(ds.linkColor);
            }
        };
        usernameSpans.put(usernameObj, span);
        return span;
    }

    public void setLoadingSpan(CharacterStyle span) {
        if (loadingSpan == span) return;
        loadingSpan = span;
        AndroidUtilities.forEachViews(scrollController.listView, view -> {
            if (view instanceof TextDetailCell) {
                ((TextDetailCell) view).textView.setLoading(loadingSpan);
                ((TextDetailCell) view).valueTextView.setLoading(loadingSpan);
            }
        });
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        RecyclerListView listView = scrollController.listView;
        if (resourcesProvider != null) {
            return null;
        }
        ThemeDescription.ThemeDescriptionDelegate themeDelegate = () -> {
            if (listView != null) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof UserCell) {
                        ((UserCell) child).update(0);
                    }
                }
            }
            if (!profileHeaderView.isExpanded) {
                if (profileHeaderView.infoView.subtitleView != null) {
                    final Object onlineTextViewTag = profileHeaderView.infoView.subtitleView.getTag();
                    for (int i = 0; i < 2; i++) {
                        if (onlineTextViewTag instanceof Integer) {
                            profileHeaderView.infoView.subtitleView.setTextColor(getThemedColor((Integer) onlineTextViewTag));
                        } else {
                            profileHeaderView.infoView.subtitleView.setTextColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue));
                        }
                    }
                }
                if (profileHeaderView.infoView.lockIconDrawable != null) {
                    profileHeaderView.infoView.lockIconDrawable.setColorFilter(getThemedColor(Theme.key_chat_lockIcon), PorterDuff.Mode.MULTIPLY);
                }
                if (profileHeaderView.infoView.scamDrawable != null) {
                    profileHeaderView.infoView.scamDrawable.setColor(getThemedColor(Theme.key_avatar_subtitleInProfileBlue));
                }
                if (profileHeaderView.infoView.titleView != null) {
                    profileHeaderView.infoView.titleView.setTextColor(getThemedColor(Theme.key_profile_title));
                }
                if (actionBar != null) {
                    actionBar.setItemsColor(dataProvider.peerColor() != null ? Color.WHITE : getThemedColor(Theme.key_actionBarDefaultIcon), false);
                    actionBar.setItemsBackgroundColor(dataProvider.peerColor() != null ? 0x20ffffff : getThemedColor(Theme.key_avatar_actionBarSelectorBlue), false);
                }
            }
            profileHeaderView.infoView.updateEmojiStatusDrawableColor();
        };
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        if (sharedMediaLayout != null) {
            arrayList.addAll(sharedMediaLayout.getThemeDescriptions());
        }

        arrayList.add(new ThemeDescription(scrollController.listView, 0, null, null, null, null, Theme.key_windowBackgroundWhite));
//        arrayList.add(new ThemeDescription(searchListView, 0, null, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(scrollController.listView, 0, null, null, null, null, Theme.key_windowBackgroundGray));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM | ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_actionBarDefaultSubmenuItemIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_actionBarDefaultIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_actionBarSelectorBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_chat_lockIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_subtitleInProfileBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundActionBarBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_profile_title));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_profile_status));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_subtitleInProfileBlue));

        if (profileHeaderView.infoView.mediaCounterTextView != null) {
            arrayList.add(new ThemeDescription(profileHeaderView.infoView.mediaCounterTextView.getTextView(), ThemeDescription.FLAG_TEXTCOLOR, null, null, null, themeDelegate, Theme.key_player_actionBarSubtitle));
            arrayList.add(new ThemeDescription(profileHeaderView.infoView.mediaCounterTextView.getNextTextView(), ThemeDescription.FLAG_TEXTCOLOR, null, null, null, themeDelegate, Theme.key_player_actionBarSubtitle));
        }

//        arrayList.add(new ThemeDescription(topView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        arrayList.add(new ThemeDescription(profileHeaderView.avatarImage, 0, null, null, Theme.avatarDrawables, null, Theme.key_avatar_text));
        arrayList.add(new ThemeDescription(profileHeaderView.avatarImage, 0, null, null, new Drawable[]{ profileHeaderView.avatarDrawable }, null, Theme.key_avatar_backgroundInProfileBlue));
//
//        arrayList.add(new ThemeDescription(writeButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_profile_actionIcon));
//        arrayList.add(new ThemeDescription(writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_profile_actionBackground));
//        arrayList.add(new ThemeDescription(writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_profile_actionPressedBackground));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGreenText2));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_text_RedRegular));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueText2));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueButton));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueIcon));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextDetailCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextDetailCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"detailTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_LINKCOLOR, new Class[]{SettingsSuggestionCell.class}, new String[]{"detailTextView"}, null, null, null, Theme.key_windowBackgroundWhiteLinkText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"yesButton"}, null, null, null, Theme.key_featuredStickers_buttonText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[]{SettingsSuggestionCell.class}, new String[]{"yesButton"}, null, null, null, Theme.key_featuredStickers_addButton));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{SettingsSuggestionCell.class}, new String[]{"yesButton"}, null, null, null, Theme.key_featuredStickers_addButtonPressed));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{SettingsSuggestionCell.class}, new String[]{"noButton"}, null, null, null, Theme.key_featuredStickers_buttonText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[]{SettingsSuggestionCell.class}, new String[]{"noButton"}, null, null, null, Theme.key_featuredStickers_addButton));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{SettingsSuggestionCell.class}, new String[]{"noButton"}, null, null, null, Theme.key_featuredStickers_addButtonPressed));

        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{UserCell.class}, new String[]{"adminTextView"}, null, null, null, Theme.key_profile_creatorIcon));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"statusColor"}, null, null, themeDelegate, Theme.key_windowBackgroundWhiteGrayText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"statusOnlineColor"}, null, null, themeDelegate, Theme.key_windowBackgroundWhiteBlueText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{UserCell.class}, null, Theme.avatarDrawables, null, Theme.key_avatar_text));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundRed));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundOrange));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundViolet));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundGreen));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundCyan));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, themeDelegate, Theme.key_avatar_backgroundPink));

        arrayList.add(new ThemeDescription(undoView, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_undo_background));
        arrayList.add(new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"undoImageView"}, null, null, null, Theme.key_undo_cancelColor));
        arrayList.add(new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"undoTextView"}, null, null, null, Theme.key_undo_cancelColor));
        arrayList.add(new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"infoTextView"}, null, null, null, Theme.key_undo_infoColor));
        arrayList.add(new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"textPaint"}, null, null, null, Theme.key_undo_infoColor));
        arrayList.add(new ThemeDescription(undoView, 0, new Class[]{UndoView.class}, new String[]{"progressPaint"}, null, null, null, Theme.key_undo_infoColor));
        arrayList.add(new ThemeDescription(undoView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{UndoView.class}, new String[]{"leftImageView"}, null, null, null, Theme.key_undo_infoColor));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{AboutLinkCell.class}, Theme.profile_aboutTextPaint, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_LINKCOLOR, new Class[]{AboutLinkCell.class}, Theme.profile_aboutTextPaint, null, null, Theme.key_windowBackgroundWhiteLinkText));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{AboutLinkCell.class}, Theme.linkSelectionPaint, null, null, Theme.key_windowBackgroundWhiteLinkSelection));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        arrayList.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        arrayList.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

//        arrayList.add(new ThemeDescription(searchListView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));
//
//        arrayList.add(new ThemeDescription(searchListView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_graySectionText));
//        arrayList.add(new ThemeDescription(searchListView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection));
//
//        arrayList.add(new ThemeDescription(searchListView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
//        arrayList.add(new ThemeDescription(searchListView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
//        arrayList.add(new ThemeDescription(searchListView, 0, new Class[]{SettingsSearchCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));

        boolean mediaHeaderVisible = false;
        if (mediaHeaderVisible) {
            arrayList.add(new ThemeDescription(profileHeaderView.infoView.titleView, 0, null, null, new Drawable[]{profileHeaderView.infoView.verifiedCheckDrawable}, null, Theme.key_player_actionBarTitle));
            arrayList.add(new ThemeDescription(profileHeaderView.infoView.titleView, 0, null, null, new Drawable[]{profileHeaderView.infoView.verifiedDrawable}, null, Theme.key_windowBackgroundWhite));
        } else {
            arrayList.add(new ThemeDescription(profileHeaderView.infoView.titleView, 0, null, null, new Drawable[]{profileHeaderView.infoView.verifiedCheckDrawable}, null, Theme.key_profile_verifiedCheck));
            arrayList.add(new ThemeDescription(profileHeaderView.infoView.titleView, 0, null, null, new Drawable[]{profileHeaderView.infoView.verifiedDrawable}, null, Theme.key_profile_verifiedBackground));
        }

        return arrayList;
    }

    public void goToForum() {
        if (getParentLayout() != null && getParentLayout().getFragmentStack() != null) {
            for (int i = 0; i < getParentLayout().getFragmentStack().size(); ++i) {
                BaseFragment fragment = getParentLayout().getFragmentStack().get(i);
                if (fragment instanceof DialogsActivity) {
                    if (((DialogsActivity) fragment).rightSlidingDialogContainer != null) {
                        BaseFragment previewFragment = ((DialogsActivity) fragment).rightSlidingDialogContainer.getFragment();
                        if (previewFragment instanceof TopicsFragment && ((TopicsFragment) previewFragment).getDialogId() == dataProvider.getDialogId()) {
                            ((DialogsActivity) fragment).rightSlidingDialogContainer.finishPreview();
                        }
                    }
                } else if (fragment instanceof ChatActivity) {
                    if (((ChatActivity) fragment).getDialogId() == dataProvider.getDialogId()) {
                        getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                } else if (fragment instanceof TopicsFragment) {
                    if (((TopicsFragment) fragment).getDialogId() == dataProvider.getDialogId()) {
                        getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                } else if (fragment instanceof ProfileActivityV2) {
                    if (fragment != this && ((ProfileActivityV2) fragment).dataProvider.getDialogId() == dataProvider.getDialogId() && ((ProfileActivityV2) fragment).dataProvider.isTopic) {
                        getParentLayout().removeFragmentFromStack(fragment);
                        i--;
                    }
                }
            }
        }

        Bundle args = new Bundle();
        args.putLong("chat_id", dataProvider.chatId);
        presentFragment(TopicsFragment.getTopicsOrChat(this, args));
    }

    @Override
    public void scrollToSharedMedia() {
        if (listAdapter.hasSharedMedia()) {
            scrollController.scrollToSharedMedia(false, listAdapter.sharedMediaIndex());
        }
    }

    public void scrollToSharedMedia(boolean animated) {
        if (listAdapter.hasSharedMedia()) {
            if (animated) {
                LinearSmoothScrollerCustom linearSmoothScroller = new LinearSmoothScrollerCustom(getContext(), LinearSmoothScrollerCustom.POSITION_TOP, .6f);
                linearSmoothScroller.setTargetPosition(listAdapter.sharedMediaIndex());
                linearSmoothScroller.setOffset(-scrollController.listView.getPaddingTop());
                scrollController.layoutManager.startSmoothScroll(linearSmoothScroller);
            } else {
                scrollController.layoutManager.scrollToPositionWithOffset(listAdapter.sharedMediaIndex(), -scrollController.listView.getPaddingTop());
            }
        }
    }

    @Override
    public TLRPC.Chat getCurrentChat() {
        return currentChat;
    }

    @Override
    public boolean isFragmentOpened() {
        return fragmentOpened;
    }

    @Override
    public RecyclerListView getListView() {
        return scrollController.listView;
    }

    @Override
    public boolean canSearchMembers() {
        return canSearchMembers;
    }

    @Override
    public void updateSelectedMediaTabText() {
        if (profileHeaderView == null) { return; }

        AudioPlayerAlert.ClippingTextViewSwitcher mediaCounterTextView = profileHeaderView.infoView.mediaCounterTextView;

        if (sharedMediaLayout == null || mediaCounterTextView == null) {
            return;
        }
        int id = sharedMediaLayout.getClosestTab();
        int[] mediaCount = sharedMediaPreloader.getLastMediaCount();
        if (id == SharedMediaLayout.TAB_PHOTOVIDEO) {
            if (mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY] <= 0 && mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY] <= 0) {
                if (mediaCount[MediaDataController.MEDIA_PHOTOVIDEO] <= 0) {
                    mediaCounterTextView.setText(getString(R.string.SharedMedia));
                } else {
                    mediaCounterTextView.setText(LocaleController.formatPluralString("Media", mediaCount[MediaDataController.MEDIA_PHOTOVIDEO]));
                }
            } else if (sharedMediaLayout.getPhotosVideosTypeFilter() == SharedMediaLayout.FILTER_PHOTOS_ONLY || mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY] <= 0) {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Photos", mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY]));
            } else if (sharedMediaLayout.getPhotosVideosTypeFilter() == SharedMediaLayout.FILTER_VIDEOS_ONLY || mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY] <= 0) {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Videos", mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY]));
            } else {
                String str = String.format("%s, %s", LocaleController.formatPluralString("Photos", mediaCount[MediaDataController.MEDIA_PHOTOS_ONLY]), LocaleController.formatPluralString("Videos", mediaCount[MediaDataController.MEDIA_VIDEOS_ONLY]));
                mediaCounterTextView.setText(str);
            }
        } else if (id == SharedMediaLayout.TAB_FILES) {
            if (mediaCount[MediaDataController.MEDIA_FILE] <= 0) {
                mediaCounterTextView.setText(getString(R.string.Files));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Files", mediaCount[MediaDataController.MEDIA_FILE]));
            }
        } else if (id == SharedMediaLayout.TAB_VOICE) {
            if (mediaCount[MediaDataController.MEDIA_AUDIO] <= 0) {
                mediaCounterTextView.setText(getString(R.string.Voice));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Voice", mediaCount[MediaDataController.MEDIA_AUDIO]));
            }
        } else if (id == SharedMediaLayout.TAB_LINKS) {
            if (mediaCount[MediaDataController.MEDIA_URL] <= 0) {
                mediaCounterTextView.setText(getString(R.string.SharedLinks));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("Links", mediaCount[MediaDataController.MEDIA_URL]));
            }
        } else if (id == SharedMediaLayout.TAB_AUDIO) {
            if (mediaCount[MediaDataController.MEDIA_MUSIC] <= 0) {
                mediaCounterTextView.setText(getString(R.string.Music));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("MusicFiles", mediaCount[MediaDataController.MEDIA_MUSIC]));
            }
        } else if (id == SharedMediaLayout.TAB_GIF) {
            if (mediaCount[MediaDataController.MEDIA_GIF] <= 0) {
                mediaCounterTextView.setText(getString(R.string.AccDescrGIFs));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("GIFs", mediaCount[MediaDataController.MEDIA_GIF]));
            }
        } else if (id == SharedMediaLayout.TAB_COMMON_GROUPS) {
            mediaCounterTextView.setText(LocaleController.formatPluralString("CommonGroups", dataProvider.getUserFull().common_chats_count));
        } else if (id == SharedMediaLayout.TAB_GROUPUSERS) {
            mediaCounterTextView.setText(profileHeaderView.infoView.subtitleView.getText());
        } else if (id == SharedMediaLayout.TAB_STORIES) {
            if (dataProvider.isBot()) {
                mediaCounterTextView.setText(sharedMediaLayout.getBotPreviewsSubtitle(false));
            } else {
                mediaCounterTextView.setText(LocaleController.formatPluralString("ProfileStoriesCount", sharedMediaLayout.getStoriesCount(id)));
            }
        } else if (id == SharedMediaLayout.TAB_BOT_PREVIEWS) {
            mediaCounterTextView.setText(sharedMediaLayout.getBotPreviewsSubtitle(true));
        } else if (id == SharedMediaLayout.TAB_ARCHIVED_STORIES) {
            mediaCounterTextView.setText(LocaleController.formatPluralString("ProfileStoriesArchiveCount", sharedMediaLayout.getStoriesCount(id)));
        } else if (id == SharedMediaLayout.TAB_RECOMMENDED_CHANNELS) {
            final MessagesController.ChannelRecommendations rec = MessagesController.getInstance(currentAccount).getChannelRecommendations(getDialogId());
            mediaCounterTextView.setText(LocaleController.formatPluralString(dataProvider.isBot() ? "Bots" : "Channels", rec == null ? 0 : rec.chats.size() + rec.more));
        } else if (id == SharedMediaLayout.TAB_SAVED_MESSAGES) {
            int messagesCount = getMessagesController().getSavedMessagesController().getMessagesCount(getDialogId());
            mediaCounterTextView.setText(LocaleController.formatPluralString("SavedMessagesCount", Math.max(1, messagesCount)));
        } else if (id == SharedMediaLayout.TAB_GIFTS) {
            mediaCounterTextView.setText(LocaleController.formatPluralStringComma("ProfileGiftsCount", sharedMediaLayout.giftsContainer == null ? 0 : sharedMediaLayout.giftsContainer.getGiftsCount()));
        }

        profileHeaderView.infoView.updateMediaCounter();
    }

    @Override
    public void mediaCountUpdated() {
        if (sharedMediaLayout != null && sharedMediaPreloader != null) {
            sharedMediaLayout.setNewMediaCounts(sharedMediaPreloader.getLastMediaCount());
        }
        updateSharedMediaRows();
        updateSelectedMediaTabText();

        if (dataProvider.getUserFull() != null) {
            resumeDelayedFragmentAnimation();
        }
    }

    private void updateSharedMediaRows() {
        if (listAdapter == null) {
            return;
        }

        updateCells();
    }

    public void openUrl(String url, Browser.Progress progress) {
        if (url.startsWith("@")) {
            getMessagesController().openByUserName(url.substring(1), ProfileActivityV2.this, 0, progress);
        } else if (url.startsWith("#") || url.startsWith("$")) {
            DialogsActivity fragment = new DialogsActivity(null);
            fragment.setSearchString(url);
            presentFragment(fragment);
        } else if (url.startsWith("/")) {
            if (parentLayout.getFragmentStack().size() > 1) {
                BaseFragment previousFragment = parentLayout.getFragmentStack().get(parentLayout.getFragmentStack().size() - 2);
                if (previousFragment instanceof ChatActivity) {
                    finishFragment();
                    ((ChatActivity) previousFragment).getChatActivityEnterView().setCommand(null, url, false, false);
                }
            }
        }
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        Rect rect = new Rect();
        if (profileHeaderView.avatarsViewPager != null && profileHeaderView.avatarsViewPager.getVisibility() == View.VISIBLE && profileHeaderView.avatarsViewPager.getRealCount() > 1) {
            profileHeaderView.avatarsViewPager.getHitRect(rect);
            if (event != null && rect.contains((int) event.getX(), (int) event.getY() - actionBar.getMeasuredHeight())) {
                return false;
            }
        }

        if (!listAdapter.hasSharedMedia()) {
            return true;
        }
        if (!sharedMediaLayout.isSwipeBackEnabled()) {
            return false;
        }
        sharedMediaLayout.getHitRect(rect);
        if (event != null && !rect.contains((int) event.getX(), (int) event.getY() - actionBar.getMeasuredHeight())) {
            return true;
        }
        return sharedMediaLayout.isCurrentTabFirst();
    }

    private void updateTtlIcon() {
        if (ttlIconView == null) {
            return;
        }
        boolean visible = false;
        if (dataProvider.currentEncryptedChat == null) {
            if (dataProvider.getUserFull() != null && dataProvider.getUserFull().ttl_period > 0) {
                visible = true;
            } else if (dataProvider.getChatFull() != null && ChatObject.canUserDoAdminAction(currentChat, ChatObject.ACTION_DELETE_MESSAGES) && dataProvider.getChatFull().ttl_period > 0) {
                visible = true;
            }
        }
        AndroidUtilities.updateViewVisibilityAnimated(ttlIconView, visible, 0.8f, fragmentOpened);
    }

    private void leaveChatPressed() {
        boolean isForum = ChatObject.isForum(currentChat);
        AlertsCreator.createClearOrDeleteDialogAlert(ProfileActivityV2.this, false, currentChat, null, false, isForum, !isForum, (param) -> {
//            playProfileAnimation = 0;
            getNotificationCenter().removeObserver(ProfileActivityV2.this, NotificationCenter.closeChats);
            getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
            finishFragment();
            getNotificationCenter().postNotificationName(NotificationCenter.needDeleteDialog, -currentChat.id, null, currentChat, param);
        }, resourcesProvider);
    }

    private final SparseIntArray adaptedColors = new SparseIntArray();

    public int applyPeerColor(int color, boolean actionBar, Boolean online) {
        if (!actionBar && isSettings()) return color;
        if (dataProvider.peerColor() != null) {
            if (!actionBar) {
                int index = adaptedColors.indexOfKey(color);
                if (index < 0) {
                    final int baseColor = Theme.adaptHSV(dataProvider.peerColor().getBgColor1(Theme.isCurrentThemeDark()), Theme.isCurrentThemeDark() ? 0 : +.05f, Theme.isCurrentThemeDark() ? -.1f : -.04f);
                    int adapted = OKLCH.adapt(color, baseColor);
                    adaptedColors.put(color, adapted);
                    return adapted;
                } else {
                    return adaptedColors.valueAt(index);
                }
            }
            final int baseColor = getThemedColor(actionBar ? Theme.key_actionBarDefault : Theme.key_windowBackgroundWhiteBlueIcon);
            final int storyColor = ColorUtils.blendARGB(dataProvider.peerColor().getStoryColor1(Theme.isCurrentThemeDark()), dataProvider.peerColor().getStoryColor2(Theme.isCurrentThemeDark()), .5f);
            int accentColor = actionBar ? storyColor : dataProvider.peerColor().getBgColor1(Theme.isCurrentThemeDark());
            if (!Theme.hasHue(baseColor)) {
                return online != null && !online ? Theme.adaptHSV(Theme.multAlpha(storyColor, .7f), -.2f, +.2f) : storyColor;
            }
            return Theme.changeColorAccent(baseColor, accentColor, color, Theme.isCurrentThemeDark(), online != null && !online ? Theme.multAlpha(storyColor, .7f) : storyColor);
        }
        return color;
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            if (!backward) {
//                if (playProfileAnimation != 0 && allowProfileAnimation) {
//                    if (playProfileAnimation == 1) {
//                        currentExpandAnimatorValue = 0f;
//                    }
//                    openAnimationInProgress = false;
//                    checkListViewScroll();
//                    if (recreateMenuAfterAnimation) {
//                        createActionBarMenu(true);
//                    }
//                }
                if (!fragmentOpened) {
                    fragmentOpened = true;
                    fragmentView.requestLayout();
                }
            }
//            getNotificationCenter().onAnimationFinish(transitionIndex);

//            if (blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
//                blurredView.setVisibility(View.GONE);
//                blurredView.setBackground(null);
//            }
        }
//        transitionAnimationInProress = false;
//        checkPhotoDescriptionAlpha();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, int account, final Object... args) {
        if (id == NotificationCenter.uploadStoryEnd || id == NotificationCenter.chatWasBoostedByUser) {
        } else if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            boolean infoChanged = (mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0 || (mask & MessagesController.UPDATE_MASK_EMOJI_STATUS) != 0;
            if (dataProvider.userId != 0) {
                if (infoChanged) {
                    profileHeaderView.updateProfileData(true);
                }
                if ((mask & MessagesController.UPDATE_MASK_PHONE) != 0) {
                    if (scrollController.listView != null) {
//                        RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForPosition(phoneRow);
//                        if (holder != null) {
//                            listAdapter.onBindViewHolder(holder, phoneRow);
//                        }
                    }
                }
            } else if (dataProvider.chatId != 0) {
                if ((mask & MessagesController.UPDATE_MASK_CHAT) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_CHAT_MEMBERS) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0 || (mask & MessagesController.UPDATE_MASK_EMOJI_STATUS) != 0) {
                    if ((mask & MessagesController.UPDATE_MASK_CHAT) != 0) {
                        updateListAnimated(true);
                    } else {
                        updateOnlineCount(true);
                    }
                    profileHeaderView.updateProfileData(true);
                }
                if (infoChanged) {
                    if (scrollController.listView != null) {
                        int count = scrollController.listView.getChildCount();
                        for (int a = 0; a < count; a++) {
                            View child = scrollController.listView.getChildAt(a);
                            if (child instanceof UserCell) {
                                ((UserCell) child).update(mask);
                            }
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.chatOnlineCountDidLoad) {
            Long chatId = (Long) args[0];
            if (dataProvider.getChatFull() == null || currentChat == null || currentChat.id != chatId) {
                return;
            }
            dataProvider.getChatFull().online_count = (Integer) args[1];
            updateOnlineCount(true);
            profileHeaderView.updateProfileData(false);
        } else if (id == NotificationCenter.contactsDidLoad || id == NotificationCenter.channelRightsUpdated) {
            createActionBarMenu(true);
        } else if (id == NotificationCenter.encryptedChatCreated) {
            if (creatingChat) {
                AndroidUtilities.runOnUIThread(() -> {
                    getNotificationCenter().removeObserver(ProfileActivityV2.this, NotificationCenter.closeChats);
                    getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                    TLRPC.EncryptedChat encryptedChat = (TLRPC.EncryptedChat) args[0];
                    Bundle args2 = new Bundle();
                    args2.putInt("enc_id", encryptedChat.id);
                    presentFragment(new ChatActivity(args2), true);
                });
            }
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            TLRPC.EncryptedChat chat = (TLRPC.EncryptedChat) args[0];
            if (dataProvider.currentEncryptedChat != null && chat.id == dataProvider.currentEncryptedChat.id) {
                dataProvider.currentEncryptedChat = chat;
                updateListAnimated(false);
//                if (flagSecure != null) {
//                    flagSecure.invalidate();
//                }
            }
        } else if (id == NotificationCenter.blockedUsersDidLoad) {
            createActionBarMenu(true);
            updateListAnimated(false);
        } else if (id == NotificationCenter.groupCallUpdated) {
            Long chatId = (Long) args[0];
            if (currentChat != null && chatId == currentChat.id && ChatObject.canManageCalls(currentChat)) {
                TLRPC.ChatFull chatFull = MessagesController.getInstance(currentAccount).getChatFull(chatId);
                if (chatFull != null) {
                    if (dataProvider.getChatFull() != null) {
                        chatFull.participants = dataProvider.getChatFull().participants;
                    }
                    dataProvider.setChatFull(chatFull);
                }
                if (sharedMediaLayout != null) {
                    sharedMediaLayout.setChatInfo(dataProvider.getChatFull());
                }
                if (dataProvider.getChatFull() != null && (dataProvider.getChatFull().call == null && !hasVoiceChatItem || dataProvider.getChatFull().call != null && hasVoiceChatItem)) {
                    createActionBarMenu(false);
                }
                if (profileHeaderView.storyView != null && dataProvider.getChatFull() != null) {
                    profileHeaderView.storyView.setStories(dataProvider.getChatFull().stories);
                }
                if (profileHeaderView.giftsView != null) {
                    profileHeaderView.giftsView.update();
                }
                if (profileHeaderView.avatarImage != null) {
                    profileHeaderView.avatarImage.setHasStories(dataProvider.needInsetForStories());
                }
                if (chatId != 0) {
                    actionBarMoreItem.setSubItemShown(ActionBarItems.gift_premium, !BuildVars.IS_BILLING_UNAVAILABLE && !getMessagesController().premiumPurchaseBlocked() && dataProvider.getChatFull() != null && dataProvider.getChatFull().stargifts_available);
                }
            }
        } else if (id == NotificationCenter.chatInfoDidLoad) {
            final TLRPC.ChatFull chatFull = (TLRPC.ChatFull) args[0];
            if (chatFull.id == dataProvider.chatId) {
                final boolean byChannelUsers = (Boolean) args[2];
                if (dataProvider.getChatFull() instanceof TLRPC.TL_channelFull) {
                    if (chatFull.participants == null) {
                        chatFull.participants = dataProvider.getChatFull().participants;
                    }
                }
                final boolean loadChannelParticipants = dataProvider.getChatFull() == null && chatFull instanceof TLRPC.TL_channelFull;
                dataProvider.setChatFull(chatFull);
                if (mergeDialogId == 0 && dataProvider.getChatFull().migrated_from_chat_id != 0) {
                    mergeDialogId = -dataProvider.getChatFull().migrated_from_chat_id;
                    getMediaDataController().getMediaCount(mergeDialogId, dataProvider.topicId, MediaDataController.MEDIA_PHOTOVIDEO, classGuid, true);
                }
                fetchUsersFromChannelInfo();
                if (profileHeaderView.avatarsViewPager != null && !dataProvider.isTopic) {
                    profileHeaderView.avatarsViewPager.setChatInfo(dataProvider.getChatFull());
                }
                updateListAnimated(true);
                TLRPC.Chat newChat = getMessagesController().getChat(dataProvider.chatId);
                if (newChat != null) {
                    currentChat = newChat;
                    createActionBarMenu(true);
                }
//                if (flagSecure != null) {
//                    flagSecure.invalidate();
//                }
                if (currentChat.megagroup && (loadChannelParticipants || !byChannelUsers)) {
//                    getChannelParticipants(true);
                }

                updateAutoDeleteItem();
                updateTtlIcon();
                if (profileHeaderView.storyView != null && dataProvider.getChatFull() != null) {
                    profileHeaderView.storyView.setStories(dataProvider.getChatFull().stories);
                }
                if (profileHeaderView.giftsView != null) {
                    profileHeaderView.giftsView.update();
                }
                if (profileHeaderView.avatarImage != null) {
                    profileHeaderView.avatarImage.setHasStories(dataProvider.needInsetForStories());
                }
                if (sharedMediaLayout != null) {
                    sharedMediaLayout.setChatInfo(dataProvider.getChatFull());
                }
            }
        } else if (id == NotificationCenter.closeChats) {
            removeSelfFromStack(true);
        } else if (id == NotificationCenter.botInfoDidLoad) {
            final TL_bots.BotInfo info = (TL_bots.BotInfo) args[0];
            if (info.user_id == dataProvider.userId) {
                dataProvider.botInfo = info;
                updateListAnimated(false);
            }
        } else if (id == NotificationCenter.userInfoDidLoad) {
            final long uid = (Long) args[0];
            if (uid == dataProvider.userId) {
                dataProvider.setUserFull((TLRPC.UserFull) args[1]);
                if (profileHeaderView.storyView != null) {
                    profileHeaderView.storyView.setStories(dataProvider.getUserFull().stories);
                }
                if (profileHeaderView.giftsView != null) {
                    profileHeaderView.giftsView.update();
                }
                if (profileHeaderView.avatarImage != null) {
                    profileHeaderView.avatarImage.setHasStories(dataProvider.needInsetForStories());
                }
                if (sharedMediaLayout != null) {
                    sharedMediaLayout.setUserInfo(dataProvider.getUserFull());
                }
//                if (imageUpdater != null) {
//                    if (listAdapter != null && !TextUtils.equals(dataProvider.getUserFull().about, currentBio)) {
//                        listAdapter.notifyItemChanged(bioRow);
//                    }
//                } else {
//                    if (!openAnimationInProgress && !callItemVisible) {
//                        createActionBarMenu(true);
//                    } else {
//                        recreateMenuAfterAnimation = true;
//                    }
//                    updateListAnimated(false);
//                    if (sharedMediaLayout != null) {
//                        sharedMediaLayout.setCommonGroupsCount(userInfo.common_chats_count);
//                        updateSelectedMediaTabText();
//                        if (sharedMediaPreloader == null || sharedMediaPreloader.isMediaWasLoaded()) {
//                            resumeDelayedFragmentAnimation();
//                            needLayout(true);
//                        }
//                    }
//                }
                updateAutoDeleteItem();
                updateTtlIcon();

                updateCells();
                listAdapter.notifyDataSetChanged();
                if (profileChannelMessageFetcher == null && !isSettings()) {
                    profileChannelMessageFetcher = new ProfileChannelCell.ChannelMessageFetcher(currentAccount);
                    profileChannelMessageFetcher.subscribe(() -> updateListAnimated(false));
                    profileChannelMessageFetcher.fetch(dataProvider.getUserFull());
                }
                if (!isSettings()) {
                    ProfileBirthdayEffect.BirthdayEffectFetcher oldFetcher = birthdayFetcher;
                    birthdayFetcher = ProfileBirthdayEffect.BirthdayEffectFetcher.of(currentAccount, dataProvider.getUserFull(), birthdayFetcher);
                    createdBirthdayFetcher = birthdayFetcher != oldFetcher;
                    if (birthdayFetcher != null) {
                        birthdayFetcher.subscribe(this::createBirthdayEffect);
                    }
                }
                if (actionBarMoreItem != null) {
                    if (dataProvider.hasPrivacyCommand()) {
                        actionBarMoreItem.showSubItem(ActionBarItems.bot_privacy);
                    } else {
                        actionBarMoreItem.hideSubItem(ActionBarItems.bot_privacy);
                    }
                }
            }
        } else if (id == NotificationCenter.privacyRulesUpdated) {
        } else if (id == NotificationCenter.didReceiveNewMessages) {
            final boolean scheduled = (Boolean) args[2];
            if (scheduled) {
                return;
            }
            final long did = dataProvider.getDialogId();
            if (did == (Long) args[0]) {
                boolean enc = DialogObject.isEncryptedDialog(did);
                ArrayList<MessageObject> arr = (ArrayList<MessageObject>) args[1];
                for (int a = 0; a < arr.size(); a++) {
                    MessageObject obj = arr.get(a);
                    if (dataProvider.currentEncryptedChat != null && obj.messageOwner.action instanceof TLRPC.TL_messageEncryptedAction && obj.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL) {
                        TLRPC.TL_decryptedMessageActionSetMessageTTL action = (TLRPC.TL_decryptedMessageActionSetMessageTTL) obj.messageOwner.action.encryptedAction;
                        if (listAdapter != null) {
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.emojiLoaded) {
            if (scrollController.listView != null) {
                scrollController.listView.invalidateViews();
            }
        } else if (id == NotificationCenter.reloadInterface) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.newSuggestionsAvailable) {
//            final int prevRow1 = passwordSuggestionRow;
//            final int prevRow2 = phoneSuggestionRow;
//            final int prevRow3 = graceSuggestionRow;
            updateCells();
            listAdapter.notifyDataSetChanged();
//            if (prevRow1 != passwordSuggestionRow || prevRow2 != phoneSuggestionRow || prevRow3 != graceSuggestionRow) {
//                listAdapter.notifyDataSetChanged();
//            }
        } else if (id == NotificationCenter.topicsDidLoaded) {
            if (dataProvider.isTopic) {
                profileHeaderView.updateProfileData(false);
            }
        } else if (id == NotificationCenter.updateSearchSettings) {
//            if (searchAdapter != null) {
//                searchAdapter.searchArray = searchAdapter.onCreateSearchArray();
//                searchAdapter.recentSearches.clear();
//                searchAdapter.updateSearchArray();
//                searchAdapter.search(searchAdapter.lastSearchString);
//            }
        } else if (id == NotificationCenter.reloadDialogPhotos) {
            profileHeaderView.updateProfileData(false);
        } else if (id == NotificationCenter.storiesUpdated || id == NotificationCenter.storiesReadUpdated) {
            if (profileHeaderView.avatarImage != null) {
                profileHeaderView.avatarImage.setHasStories(dataProvider.needInsetForStories());
//                updateAvatarRoundRadius();
            }
            if (profileHeaderView.storyView != null) {
                if (dataProvider.getUserFull() != null) {
                    profileHeaderView.storyView.setStories(dataProvider.getUserFull().stories);
                } else if (dataProvider.getChatFull() != null) {
                    profileHeaderView.storyView.setStories(dataProvider.getChatFull().stories);
                }
            }
        } else if (id == NotificationCenter.userIsPremiumBlockedUpadted) {
            if (actionBarMoreItem != null) {
                actionBarMoreItem.setSubItemShown(ActionBarItems.start_secret_chat, DialogObject.isEmpty(getMessagesController().isUserContactBlocked(dataProvider.userId)));
            }
            updateEditColorIcon();
        } else if (id == NotificationCenter.currentUserPremiumStatusChanged) {
            updateEditColorIcon();
        } else if (id == NotificationCenter.starBalanceUpdated) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.botStarsUpdated) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.botStarsTransactionsLoaded) {
            updateListAnimated(false);
        } else if (id == NotificationCenter.dialogDeleted) {
            final long dialogId = (long) args[0];
            if (dataProvider.getDialogId() == dialogId) {
                if (parentLayout != null && parentLayout.getLastFragment() == this) {
                    finishFragment();
                } else {
                    removeSelfFromStack();
                }
            }
        } else if (id == NotificationCenter.channelRecommendationsLoaded) {
            final long dialogId = (long) args[0];
            if (!listAdapter.hasSharedMedia() && dialogId == dataProvider.getDialogId()) {
                updateCells();
                updateSelectedMediaTabText();
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
            }
        } else if (id == NotificationCenter.starUserGiftsLoaded) {
            final long dialogId = (long) args[0];
            if (dialogId == dataProvider.getDialogId() && !isSettings()) {
                if (!listAdapter.hasSharedMedia()) {
                    updateCells();
                    updateSelectedMediaTabText();
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    AndroidUtilities.runOnUIThread(() -> {
                        if (sharedMediaLayout != null) {
                            sharedMediaLayout.updateTabs(true);
                            sharedMediaLayout.updateAdapters();
                        }
                    });
                } else if (sharedMediaLayout != null) {
                    sharedMediaLayout.updateTabs(true);
                }
            }
        }
    }

    private void updateEditColorIcon() {
        if (getContext() == null || editColorItem == null) return;
        if (getUserConfig().isPremium()) {
            editColorItem.setIcon(R.drawable.menu_profile_colors);
        } else {
            Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.menu_profile_colors_locked);
            icon.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarDefaultSubmenuItemIcon), PorterDuff.Mode.SRC_IN));
            Drawable lockIcon = ContextCompat.getDrawable(getContext(), R.drawable.msg_gallery_locked2);
            lockIcon.setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(Color.WHITE, Color.BLACK, 0.5f), PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(icon, lockIcon, dp(1), -dp(1)) {
                @Override
                public void setColorFilter(ColorFilter colorFilter) {}
            };
            editColorItem.setIcon(combinedDrawable);
        }
    }

    public void getEmojiStatusLocation(Rect rect) {
        float scale = profileHeaderView.infoView.titleView.getScaleX();
        if (Math.abs(scale - 1.0f) < 0.03f) {
            scale = profileHeaderView.infoView.titleViewContainer.getScaleX();
        }

        if (profileHeaderView.infoView.titleView == null) {
            return;
        }
        if (profileHeaderView.infoView.titleView.getRightDrawable() == null) {
            rect.set(profileHeaderView.infoView.titleView.getWidth() - 1, profileHeaderView.infoView.titleView.getHeight() / 2 - 1, profileHeaderView.infoView.titleView.getWidth() + 1, profileHeaderView.infoView.titleView.getHeight() / 2 + 1);
            return;
        }
        Rect bounds = profileHeaderView.infoView.titleView.getRightDrawable().getBounds();
        rect.set(bounds);
        rect.offset((int) (rect.centerX() * (scale - 1f)), 0);
        rect.offset((int) profileHeaderView.infoView.titleView.getX(), (int) profileHeaderView.infoView.titleView.getY());
    }

    private SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow selectAnimatedEmojiDialog;
    public void showStatusSelect() {
        if (selectAnimatedEmojiDialog != null) {
            return;
        }
        final SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow[] popup = new SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow[1];
        int xoff, yoff;
        getEmojiStatusLocation(AndroidUtilities.rectTmp2);
        int topMarginDp = profileHeaderView.infoView.titleViewContainer.getScaleX() < 1.5f ? 16 : 32;
        int[] location = new int[2];
        profileHeaderView.infoView.titleView.getLocationInWindow(location);
        yoff = location[1] + (AndroidUtilities.rectTmp2.centerY()) - dp(topMarginDp + 1);
        int popupWidth = (int) Math.min(dp(340 - 16), AndroidUtilities.displaySize.x * .95f);
        int ecenter = AndroidUtilities.rectTmp2.centerX();
        xoff = MathUtils.clamp(ecenter - popupWidth / 2, 0, AndroidUtilities.displaySize.x - popupWidth);
        ecenter -= xoff - location[0];
        SelectAnimatedEmojiDialog popupLayout = new SelectAnimatedEmojiDialog(this, getContext(), true, Math.max(0, ecenter), currentChat == null ? SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS : SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS_CHANNEL, true, resourcesProvider, topMarginDp) {
            @Override
            protected boolean willApplyEmoji(View view, Long documentId, TLRPC.Document document, TL_stars.TL_starGiftUnique gift, Integer until) {
                if (gift != null) {
                    final TL_stars.SavedStarGift savedStarGift = StarsController.getInstance(currentAccount).findUserStarGift(gift.id);
                    return savedStarGift == null || MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) >= 2;
                }
                return true;
            }

            @Override
            public long getDialogId() {
                return dataProvider.getDialogId();
            }

            @Override
            protected void onEmojiSelected(View emojiView, Long documentId, TLRPC.Document document, TL_stars.TL_starGiftUnique gift, Integer until) {
                final TLRPC.EmojiStatus emojiStatus;
                if (gift != null) {
                    final TL_stars.SavedStarGift savedStarGift = StarsController.getInstance(currentAccount).findUserStarGift(gift.id);
                    if (savedStarGift != null && MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) < 2) {
                        MessagesController.getGlobalMainSettings().edit().putInt("statusgiftpage", MessagesController.getGlobalMainSettings().getInt("statusgiftpage", 0) + 1).apply();
                        new StarGiftSheet(getContext(), currentAccount, UserConfig.getInstance(currentAccount).getClientUserId(), resourcesProvider)
                                .set(savedStarGift, null)
                                .setupWearPage()
                                .show();
                        if (popup[0] != null) {
                            selectAnimatedEmojiDialog = null;
                            popup[0].dismiss();
                        }
                        return;
                    }
                    final TLRPC.TL_inputEmojiStatusCollectible status = new TLRPC.TL_inputEmojiStatusCollectible();
                    status.collectible_id = gift.id;
                    if (until != null) {
                        status.flags |= 1;
                        status.until = until;
                    }
                    emojiStatus = status;
                } else if (documentId == null) {
                    emojiStatus = new TLRPC.TL_emojiStatusEmpty();
                } else {
                    final TLRPC.TL_emojiStatus status = new TLRPC.TL_emojiStatus();
                    status.document_id = documentId;
                    if (until != null) {
                        status.flags |= 1;
                        status.until = until;
                    }
                    emojiStatus = status;
                }
                profileHeaderView.infoView.emojiStatusGiftId = gift != null ? gift.id : null;
                getMessagesController().updateEmojiStatus(currentChat == null ? 0 : -currentChat.id, emojiStatus, gift);
                for (int a = 0; a < 2; ++a) {
                    if (profileHeaderView.infoView.emojiStatusDrawable != null) {
                        if (documentId == null && currentChat == null) {
                            profileHeaderView.infoView.emojiStatusDrawable.set(profileHeaderView.infoView.getPremiumCrossfadeDrawable(), true);
                        } else if (documentId != null) {
                            profileHeaderView.infoView.emojiStatusDrawable.set(documentId, true);
                        } else {
                            profileHeaderView.infoView.emojiStatusDrawable.set((Drawable) null, true);
                        }
                        profileHeaderView.infoView.emojiStatusDrawable.setParticles(gift != null, true);
                    }
                }
//                if (documentId != null) {
//                    animatedStatusView.animateChange(ReactionsLayoutInBubble.VisibleReaction.fromCustomEmoji(documentId));
//                }
                profileHeaderView.infoView.updateEmojiStatusDrawableColor();
//                updateEmojiStatusEffectPosition();
                if (popup[0] != null) {
                    selectAnimatedEmojiDialog = null;
                    popup[0].dismiss();
                }
            }
        };
        TLRPC.User user = getMessagesController().getUser(dataProvider.userId);
        if (user != null) {
            popupLayout.setExpireDateHint(DialogObject.getEmojiStatusUntil(user.emoji_status));
        }
        if (profileHeaderView.infoView.emojiStatusGiftId != null) {
            popupLayout.setSelected(profileHeaderView.infoView.emojiStatusGiftId);
        } else {
            popupLayout.setSelected(profileHeaderView.infoView.emojiStatusDrawable != null && profileHeaderView.infoView.emojiStatusDrawable.getDrawable() instanceof AnimatedEmojiDrawable ? ((AnimatedEmojiDrawable) profileHeaderView.infoView.emojiStatusDrawable.getDrawable()).getDocumentId() : null);
        }
        popupLayout.setSaveState(3);
        popupLayout.setScrimDrawable(profileHeaderView.infoView.emojiStatusDrawable, profileHeaderView.infoView.titleViewContainer);
        popup[0] = selectAnimatedEmojiDialog = new SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT) {
            @Override
            public void dismiss() {
                super.dismiss();
                selectAnimatedEmojiDialog = null;
            }
        };
        int[] loc = new int[2];
        if (profileHeaderView.infoView.titleView != null) {
            profileHeaderView.infoView.titleView.getLocationOnScreen(loc);
        }
        popup[0].showAsDropDown(fragmentView, xoff, yoff, Gravity.TOP | Gravity.LEFT);
        popup[0].dimBehind();
    }

    int avatarUploadingRequest;
    @Override
    public void didUploadPhoto(TLRPC.InputFile photo, TLRPC.InputFile video, double videoStartTimestamp, String videoPath, TLRPC.PhotoSize bigSize, TLRPC.PhotoSize smallSize, boolean isVideo, TLRPC.VideoSize emojiMarkup) {
        AndroidUtilities.runOnUIThread(() -> {
            if (photo != null || video != null || emojiMarkup != null) {
                if (profileHeaderView.avatar == null) {
                    return;
                }
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                if (photo != null) {
                    req.file = photo;
                    req.flags |= 1;
                }
                if (video != null) {
                    req.video = video;
                    req.flags |= 2;
                    req.video_start_ts = videoStartTimestamp;
                    req.flags |= 4;
                }
                if (emojiMarkup != null) {
                    req.video_emoji_markup = emojiMarkup;
                    req.flags |= 16;
                }
                avatarUploadingRequest = getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                    if (error == null) {
                        TLRPC.User user = getMessagesController().getUser(getUserConfig().getClientUserId());
                        if (user == null) {
                            user = getUserConfig().getCurrentUser();
                            if (user == null) {
                                return;
                            }
                            getMessagesController().putUser(user, false);
                        } else {
                            getUserConfig().setCurrentUser(user);
                        }

                        TLRPC.TL_photos_photo photos_photo = (TLRPC.TL_photos_photo) response;
                        ArrayList<TLRPC.PhotoSize> sizes = photos_photo.photo.sizes;
                        TLRPC.PhotoSize small = FileLoader.getClosestPhotoSizeWithSize(sizes, 150);
                        TLRPC.PhotoSize big = FileLoader.getClosestPhotoSizeWithSize(sizes, 800);
                        TLRPC.VideoSize videoSize = photos_photo.photo.video_sizes.isEmpty() ? null : FileLoader.getClosestVideoSizeWithSize(photos_photo.photo.video_sizes, 1000);
                        user.photo = new TLRPC.TL_userProfilePhoto();
                        user.photo.photo_id = photos_photo.photo.id;
                        if (small != null) {
                            user.photo.photo_small = small.location;
                        }
                        if (big != null) {
                            user.photo.photo_big = big.location;
                        }

                        if (small != null && profileHeaderView.avatar != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(small, true);
                            File src = FileLoader.getInstance(currentAccount).getPathToAttach(profileHeaderView.avatar, true);
                            src.renameTo(destFile);
                            String oldKey = profileHeaderView.avatar.volume_id + "_" + profileHeaderView.avatar.local_id + "@50_50";
                            String newKey = small.location.volume_id + "_" + small.location.local_id + "@50_50";
                            ImageLoader.getInstance().replaceImageInCache(oldKey, newKey, ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL), false);
                        }

                        if (videoSize != null && videoPath != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(videoSize, "mp4", true);
                            File src = new File(videoPath);
                            src.renameTo(destFile);
                        } else if (big != null && profileHeaderView.avatarBig != null) {
                            File destFile = FileLoader.getInstance(currentAccount).getPathToAttach(big, true);
                            File src = FileLoader.getInstance(currentAccount).getPathToAttach(profileHeaderView.avatarBig, true);
                            src.renameTo(destFile);
                        }
                        getMessagesController().getDialogPhotos(user.id).addPhotoAtStart(((TLRPC.TL_photos_photo) response).photo);
                        ArrayList<TLRPC.User> users = new ArrayList<>();
                        users.add(user);
                        getMessagesStorage().putUsersAndChats(users, null, false, true);
                        TLRPC.UserFull userFull = getMessagesController().getUserFull(dataProvider.userId);
                        if (userFull != null) {
                            userFull.profile_photo = photos_photo.photo;
                            getMessagesStorage().updateUserInfo(userFull, false);
                        }
                    }

                    profileHeaderView.avatar = null;
                    profileHeaderView.avatarBig = null;
                    profileHeaderView.avatarsViewPager.scrolledByUser = true;
                    profileHeaderView.avatarsViewPager.removeUploadingImage(profileHeaderView.uploadingImageLocation);
                    profileHeaderView.avatarsViewPager.setCreateThumbFromParent(false);
                    profileHeaderView.updateProfileData(true);
//                    showAvatarProgress(false, true);
                    getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                    getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                    getUserConfig().saveConfig(true);

                }));
            } else {
                profileHeaderView.avatar = smallSize.location;
                profileHeaderView.avatarBig = bigSize.location;
                profileHeaderView.avatarImage.setImage(ImageLocation.getForLocal(profileHeaderView.avatar), "50_50", profileHeaderView.avatarDrawable, null);
//                if (setAvatarRow != -1) {
//                    updateRowsIds();
//                    if (listAdapter != null) {
//                        listAdapter.notifyDataSetChanged();
//                    }
//                    needLayout(true);
//                }
                profileHeaderView.avatarsViewPager.addUploadingImage(profileHeaderView.uploadingImageLocation = ImageLocation.getForLocal(profileHeaderView.avatarBig), ImageLocation.getForLocal(profileHeaderView.avatar));
//                showAvatarProgress(true, false);
            }
            actionBar.createMenu().requestLayout();
        });
    }

    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, View view) {
        return onMemberClick(participant, isLong, false, view);
    }

    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, boolean resultOnly) {
        return onMemberClick(participant, isLong, resultOnly, null);
    }

    @Override
    public boolean onMemberClick(TLRPC.ChatParticipant participant, boolean isLong, boolean resultOnly, View view) {
        if (getParentActivity() == null) {
            return false;
        }
        if (isLong) {
            TLRPC.User user = getMessagesController().getUser(participant.user_id);
            if (user == null || participant.user_id == getUserConfig().getClientUserId()) {
                return false;
            }
            long selectedUser = participant.user_id;
            boolean allowKick;
            boolean canEditAdmin;
            boolean canRestrict;
            boolean editingAdmin;
            final TLRPC.ChannelParticipant channelParticipant;

            if (ChatObject.isChannel(currentChat)) {
                channelParticipant = ((TLRPC.TL_chatChannelParticipant) participant).channelParticipant;
                TLRPC.User u = getMessagesController().getUser(participant.user_id);
                canEditAdmin = ChatObject.canAddAdmins(currentChat);
                if (canEditAdmin && (channelParticipant instanceof TLRPC.TL_channelParticipantCreator || channelParticipant instanceof TLRPC.TL_channelParticipantAdmin && !channelParticipant.can_edit)) {
                    canEditAdmin = false;
                }
                allowKick = canRestrict = ChatObject.canBlockUsers(currentChat) && (!(channelParticipant instanceof TLRPC.TL_channelParticipantAdmin || channelParticipant instanceof TLRPC.TL_channelParticipantCreator) || channelParticipant.can_edit);
                if (currentChat.gigagroup) {
                    canRestrict = false;
                }
                editingAdmin = channelParticipant instanceof TLRPC.TL_channelParticipantAdmin;
            } else {
                channelParticipant = null;
                allowKick = currentChat.creator || participant instanceof TLRPC.TL_chatParticipant && (ChatObject.canBlockUsers(currentChat) || participant.inviter_id == getUserConfig().getClientUserId());
                canEditAdmin = currentChat.creator;
                canRestrict = currentChat.creator;
                editingAdmin = participant instanceof TLRPC.TL_chatParticipantAdmin;
            }

            boolean result = (canEditAdmin || canRestrict || allowKick);
            if (resultOnly || !result) {
                return result;
            }

            Utilities.Callback<Integer> openRightsEdit = action -> {
                if (channelParticipant != null) {
                    openRightsEdit(action, user, participant, channelParticipant.admin_rights, channelParticipant.banned_rights, channelParticipant.rank, editingAdmin);
                } else {
                    openRightsEdit(action, user, participant, null, null, "", editingAdmin);
                }
            };

            ItemOptions.makeOptions(this, view)
                    .setScrimViewBackground(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundWhite)))
                    .addIf(canEditAdmin, R.drawable.msg_admins, editingAdmin ? getString(R.string.EditAdminRights) : getString(R.string.SetAsAdmin), () -> openRightsEdit.run(0))
                    .addIf(canRestrict, R.drawable.msg_permissions, getString(R.string.ChangePermissions), () -> {
                        if (channelParticipant instanceof TLRPC.TL_channelParticipantAdmin || participant instanceof TLRPC.TL_chatParticipantAdmin) {
                            showDialog(
                                    new AlertDialog.Builder(getParentActivity(), resourcesProvider)
                                            .setTitle(getString(R.string.AppName))
                                            .setMessage(formatString("AdminWillBeRemoved", R.string.AdminWillBeRemoved, ContactsController.formatName(user.first_name, user.last_name)))
                                            .setPositiveButton(getString(R.string.OK), (dialog, which) -> openRightsEdit.run(1))
                                            .setNegativeButton(getString(R.string.Cancel), null)
                                            .create()
                            );
                        } else {
                            openRightsEdit.run(1);
                        }
                    })
                    .addIf(allowKick, R.drawable.msg_remove, getString(R.string.KickFromGroup), true, () -> {
                        kickUser(selectedUser, participant);
                    })
                    .setMinWidth(190)
                    .show();
        } else {
            if (participant.user_id == getUserConfig().getClientUserId()) {
                return false;
            }
            Bundle args = new Bundle();
            args.putLong("user_id", participant.user_id);
            args.putBoolean("preload_messages", true);
            presentFragment(new ProfileActivityV2(args));
        }
        return true;
    }

    private void kickUser(long uid, TLRPC.ChatParticipant participant) {
        if (uid != 0) {
            TLRPC.User user = getMessagesController().getUser(uid);
            getMessagesController().deleteParticipantFromChat(dataProvider.chatId, user);
            if (currentChat != null && user != null && BulletinFactory.canShowBulletin(this)) {
                BulletinFactory.createRemoveFromChatBulletin(this, user, currentChat.title).show();
            }
            if (dataProvider.getChatFull().participants.participants.remove(participant)) {
                updateListAnimated(true);
            }
        } else {
            getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
            if (AndroidUtilities.isTablet()) {
                getNotificationCenter().postNotificationName(NotificationCenter.closeChats, -dataProvider.chatId);
            } else {
                getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
            }
            getMessagesController().deleteParticipantFromChat(dataProvider.chatId, getMessagesController().getUser(getUserConfig().getClientUserId()));
            finishFragment();
        }
    }

    public void addToChannel() {
        getMessagesController().addUserToChat(currentChat.id, getUserConfig().getCurrentUser(), 0, null, ProfileActivityV2.this, true, () -> {
            updateCells();
            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }
        }, err -> {
            if (err != null && "INVITE_REQUEST_SENT".equals(err.text)) {
                SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                preferences.edit().putLong("dialog_join_requested_time_" + dataProvider.dialogId, System.currentTimeMillis()).commit();
                JoinGroupAlert.showBulletin(ProfileActivityV2.this.getContext(), ProfileActivityV2.this, ChatObject.isChannel(currentChat) && !currentChat.megagroup);
                updateCells();
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
                return false;
            }
            return true;
        });
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeSearchByActiveAction);
    }

    private void openRightsEdit(int action, TLRPC.User user, TLRPC.ChatParticipant participant, TLRPC.TL_chatAdminRights adminRights, TLRPC.TL_chatBannedRights bannedRights, String rank, boolean editingAdmin) {
        boolean[] needShowBulletin = new boolean[1];
        ChatRightsEditActivity fragment = new ChatRightsEditActivity(user.id, dataProvider.chatId, adminRights, currentChat.default_banned_rights, bannedRights, rank, action, true, false, null) {
            @Override
            public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
                if (!isOpen && backward && needShowBulletin[0] && BulletinFactory.canShowBulletin(ProfileActivityV2.this)) {
                    BulletinFactory.createPromoteToAdminBulletin(ProfileActivityV2.this, user.first_name).show();
                }
            }
        };
        fragment.setDelegate(new ChatRightsEditActivity.ChatRightsEditActivityDelegate() {
            @Override
            public void didSetRights(int rights, TLRPC.TL_chatAdminRights rightsAdmin, TLRPC.TL_chatBannedRights rightsBanned, String rank) {
                TLRPC.ChatFull chatInfo = dataProvider.getChatFull();

                if (action == 0) {
                    if (participant instanceof TLRPC.TL_chatChannelParticipant) {
                        TLRPC.TL_chatChannelParticipant channelParticipant1 = ((TLRPC.TL_chatChannelParticipant) participant);
                        if (rights == 1) {
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipantAdmin();
                            channelParticipant1.channelParticipant.flags |= 4;
                        } else {
                            channelParticipant1.channelParticipant = new TLRPC.TL_channelParticipant();
                        }
                        channelParticipant1.channelParticipant.inviter_id = getUserConfig().getClientUserId();
                        channelParticipant1.channelParticipant.peer = new TLRPC.TL_peerUser();
                        channelParticipant1.channelParticipant.peer.user_id = participant.user_id;
                        channelParticipant1.channelParticipant.date = participant.date;
                        channelParticipant1.channelParticipant.banned_rights = rightsBanned;
                        channelParticipant1.channelParticipant.admin_rights = rightsAdmin;
                        channelParticipant1.channelParticipant.rank = rank;
                    } else if (participant != null) {
                        TLRPC.ChatParticipant newParticipant;
                        if (rights == 1) {
                            newParticipant = new TLRPC.TL_chatParticipantAdmin();
                        } else {
                            newParticipant = new TLRPC.TL_chatParticipant();
                        }
                        newParticipant.user_id = participant.user_id;
                        newParticipant.date = participant.date;
                        newParticipant.inviter_id = participant.inviter_id;
                        int index = chatInfo.participants.participants.indexOf(participant);
                        if (index >= 0) {
                            chatInfo.participants.participants.set(index, newParticipant);
                        }
                    }
                    if (rights == 1 && !editingAdmin) {
                        needShowBulletin[0] = true;
                    }
                } else if (action == 1) {
                    if (rights == 0) {
                        if (currentChat.megagroup && chatInfo != null && chatInfo.participants != null) {
                            boolean changed = false;
                            for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                                TLRPC.ChannelParticipant p = ((TLRPC.TL_chatChannelParticipant) chatInfo.participants.participants.get(a)).channelParticipant;
                                if (MessageObject.getPeerId(p.peer) == participant.user_id) {
                                    chatInfo.participants_count--;
                                    chatInfo.participants.participants.remove(a);
                                    changed = true;
                                    break;
                                }
                            }
                            if (chatInfo != null && chatInfo.participants != null) {
                                for (int a = 0; a < chatInfo.participants.participants.size(); a++) {
                                    TLRPC.ChatParticipant p = chatInfo.participants.participants.get(a);
                                    if (p.user_id == participant.user_id) {
                                        chatInfo.participants.participants.remove(a);
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            if (changed) {
                                updateOnlineCount(true);
                                updateCells();
                                listAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void didChangeOwner(TLRPC.User user) {
                undoView.showWithAction(-dataProvider.chatId, currentChat.megagroup ? UndoView.ACTION_OWNER_TRANSFERED_GROUP : UndoView.ACTION_OWNER_TRANSFERED_CHANNEL, user);
            }
        });
        presentFragment(fragment);
    }

    private boolean processUsernameClick(final View view, final float x, final float y) {
        final String username;
        final TLRPC.TL_username usernameObj;
        if (dataProvider.userId != 0) {
            final TLRPC.User user = getMessagesController().getUser(dataProvider.userId);
            String username1 = UserObject.getPublicUsername(user);
            if (user == null || username1 == null) {
                return false;
            }
            username = username1;
            usernameObj = DialogObject.findUsername(username, user);
        } else if (dataProvider.chatId != 0) {
            final TLRPC.Chat chat = getMessagesController().getChat(dataProvider.chatId);
            if (chat == null || dataProvider.topicId == 0 && !ChatObject.isPublic(chat)) {
                return false;
            }
            username = ChatObject.getPublicUsername(chat);
            usernameObj = DialogObject.findUsername(username, chat);
        } else {
            return false;
        }
        if (dataProvider.userId == 0) {
            TLRPC.Chat chat = getMessagesController().getChat(dataProvider.chatId);
            String link;
            if (ChatObject.isPublic(chat)) {
                link = "https://" + getMessagesController().linkPrefix + "/" + ChatObject.getPublicUsername(chat) + (dataProvider.topicId != 0 ? "/" + dataProvider.topicId : "");
            } else {
                link = "https://" + getMessagesController().linkPrefix + "/c/" + chat.id + (dataProvider.topicId != 0 ? "/" + dataProvider.topicId : "");
            }
            ShareAlert shareAlert = new ShareAlert(getParentActivity(), null, link, false, link, false) {
                @Override
                protected void onSend(LongSparseArray<TLRPC.Dialog> dids, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                    if (!showToast) return;
                    AndroidUtilities.runOnUIThread(() -> {
                        BulletinFactory.createInviteSentBulletin(getParentActivity(), (NestedFrameLayout) fragmentView, dids.size(), dids.size() == 1 ? dids.valueAt(0).id : 0, count, getThemedColor(Theme.key_undo_background), getThemedColor(Theme.key_undo_infoColor)).show();
                    }, 250);
                }
            };
            showDialog(shareAlert);
            if (usernameObj != null && !usernameObj.editable) {
                TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                input.username = usernameObj.username;
                req.collectible = input;
                int reqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                    if (res instanceof TL_fragment.TL_collectibleInfo) {
                        TL_fragment.TL_collectibleInfo info = (TL_fragment.TL_collectibleInfo) res;
                        TLObject obj;
                        if (dataProvider.userId != 0) {
                            obj = getMessagesController().getUser(dataProvider.userId);
                        } else {
                            obj = getMessagesController().getChat(dataProvider.chatId);
                        }
                        final String usernameStr = "@" + usernameObj.username;
                        final String date = LocaleController.getInstance().getFormatterBoostExpired().format(new Date(info.purchase_date * 1000L));
                        final String cryptoAmount = BillingController.getInstance().formatCurrency(info.crypto_amount, info.crypto_currency);
                        final String amount = BillingController.getInstance().formatCurrency(info.amount, info.currency);
                        BulletinFactory.of(shareAlert.bulletinContainer2, resourcesProvider)
                                .createImageBulletin(
                                        R.drawable.filled_username,
                                        AndroidUtilities.withLearnMore(AndroidUtilities.replaceTags(formatString(R.string.FragmentChannelUsername, usernameStr, date, cryptoAmount, TextUtils.isEmpty(amount) ? "" : "("+amount+")")), () -> {
                                            Bulletin.hideVisible();
                                            Browser.openUrl(getContext(), info.url);
                                        })
                                )
                                .setOnClickListener(v -> {
                                    Bulletin.hideVisible();
                                    Browser.openUrl(getContext(), info.url);
                                })
                                .show(false);
                    } else {
                        BulletinFactory.showError(err);
                    }
                }));
                getConnectionsManager().bindRequestToGuid(reqId, getClassGuid());
            }
        } else {
//            if (editRow(view, position)) return true;

            if (usernameObj != null && !usernameObj.editable) {
                TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                input.username = usernameObj.username;
                req.collectible = input;
                int reqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                    if (res instanceof TL_fragment.TL_collectibleInfo) {
                        TLObject obj;
                        if (dataProvider.userId != 0) {
                            obj = dataProvider.getUser();
                        } else {
                            obj = dataProvider.getChat();
                        }
                        FragmentUsernameBottomSheet.open(getContext(), FragmentUsernameBottomSheet.TYPE_USERNAME, usernameObj.username, obj, (TL_fragment.TL_collectibleInfo) res, getResourceProvider());
                    } else {
                        BulletinFactory.showError(err);
                    }
                }));
                getConnectionsManager().bindRequestToGuid(reqId, getClassGuid());
                return true;
            }

            try {
                ClipboardManager clipboard = (ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                String text = "@" + username;
                BulletinFactory.of(this).createCopyBulletin(getString(R.string.UsernameCopied), resourcesProvider).show();
                ClipData clip = ClipData.newPlainText("label", text);
                clipboard.setPrimaryClip(clip);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        return true;
    }

    private boolean processPhoneClick(final View view, final float x, final float y) {
        final int PHONE_OPTION_CALL = 0,
                PHONE_OPTION_COPY = 1,
                PHONE_OPTION_TELEGRAM_CALL = 2,
                PHONE_OPTION_TELEGRAM_VIDEO_CALL = 3;

//            if (editRow(view, position)) return true;
            final TLRPC.User user = dataProvider.getUser();
            final TLRPC.UserFull userInfo = dataProvider.getUserFull();

            if (user == null || user.phone == null || user.phone.length() == 0 || getParentActivity() == null) {
                return false;
            }

            if (!isSettings() && user.phone.startsWith("888")) {
                TL_fragment.TL_inputCollectiblePhone input = new TL_fragment.TL_inputCollectiblePhone();
                final String phone = input.phone = user.phone;
                TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                req.collectible = input;
                int reqId = getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                    if (res instanceof TL_fragment.TL_collectibleInfo) {
                        FragmentUsernameBottomSheet.open(getContext(), FragmentUsernameBottomSheet.TYPE_PHONE, phone, user, (TL_fragment.TL_collectibleInfo) res, getResourceProvider());
                    } else {
                        BulletinFactory.showError(err);
                    }
                }));
                getConnectionsManager().bindRequestToGuid(reqId, getClassGuid());
                return true;
            }

            ArrayList<CharSequence> items = new ArrayList<>();
            ArrayList<Integer> actions = new ArrayList<>();
            List<Integer> icons = new ArrayList<>();
            if (!isSettings()) {
                if (userInfo != null && userInfo.phone_calls_available) {
                    icons.add(R.drawable.msg_calls);
                    items.add(getString(R.string.CallViaTelegram));
                    actions.add(PHONE_OPTION_TELEGRAM_CALL);
                    if (Build.VERSION.SDK_INT >= 18 && userInfo.video_calls_available) {
                        icons.add(R.drawable.msg_videocall);
                        items.add(getString(R.string.VideoCallViaTelegram));
                        actions.add(PHONE_OPTION_TELEGRAM_VIDEO_CALL);
                    }
                }
                if (!isFragmentPhoneNumber) {
                    icons.add(R.drawable.msg_calls_regular);
                    items.add(getString(R.string.Call));
                    actions.add(PHONE_OPTION_CALL);
                }
            }
            icons.add(R.drawable.msg_copy);
            items.add(getString(R.string.Copy));
            actions.add(PHONE_OPTION_COPY);

            AtomicReference<ActionBarPopupWindow> popupWindowRef = new AtomicReference<>();
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), R.drawable.popup_fixed_alert, resourcesProvider) {
                Path path = new Path();

                @Override
                protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                    canvas.save();
                    path.rewind();
                    AndroidUtilities.rectTmp.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                    path.addRoundRect(AndroidUtilities.rectTmp, dp(6), dp(6), Path.Direction.CW);
                    canvas.clipPath(path);
                    boolean draw = super.drawChild(canvas, child, drawingTime);
                    canvas.restore();
                    return draw;
                }
            };
            popupLayout.setFitItems(true);

            for (int i = 0; i < icons.size(); i++) {
                int action = actions.get(i);
                ActionBarMenuItem.addItem(popupLayout, icons.get(i), items.get(i), false, resourcesProvider).setOnClickListener(v -> {
                    popupWindowRef.get().dismiss();
                    switch (action) {
                        case PHONE_OPTION_CALL:
                            try {
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + user.phone));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getParentActivity().startActivityForResult(intent, 500);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            break;
                        case PHONE_OPTION_COPY:
                            try {
                                ClipboardManager clipboard = (ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label", "+" + user.phone);
                                clipboard.setPrimaryClip(clip);
                                if (AndroidUtilities.shouldShowClipboardToast()) {
                                    BulletinFactory.of(this).createCopyBulletin(getString(R.string.PhoneCopied)).show();
                                }
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            break;
                        case PHONE_OPTION_TELEGRAM_CALL:
                        case PHONE_OPTION_TELEGRAM_VIDEO_CALL:
                            if (getParentActivity() == null) {
                                return;
                            }
                            VoIPHelper.startCall(user, action == PHONE_OPTION_TELEGRAM_VIDEO_CALL, userInfo != null && userInfo.video_calls_available, getParentActivity(), userInfo, getAccountInstance());
                            break;
                    }
                });
            }
            if (isFragmentPhoneNumber) {
                FrameLayout gap = new FrameLayout(getContext());
                gap.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuSeparator, resourcesProvider));
                popupLayout.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));

                TextView fragmentInfoView = new TextView(getContext());
                fragmentInfoView.setPadding(dp(13), dp(8), dp(13), dp(8));
                fragmentInfoView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                fragmentInfoView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem, resourcesProvider));
                fragmentInfoView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText, resourcesProvider));
                fragmentInfoView.setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector, resourcesProvider), 0,6));

                SpannableStringBuilder spanned = new SpannableStringBuilder(AndroidUtilities.replaceTags(getString(R.string.AnonymousNumberNotice)));

                int startIndex = TextUtils.indexOf(spanned, '*');
                int lastIndex = TextUtils.lastIndexOf(spanned, '*');
                if (startIndex != -1 && lastIndex != -1 && startIndex != lastIndex) {
                    spanned.replace(lastIndex, lastIndex + 1, "");
                    spanned.replace(startIndex, startIndex + 1, "");
                    spanned.setSpan(new TypefaceSpan(AndroidUtilities.bold()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spanned.setSpan(new ForegroundColorSpan(fragmentInfoView.getLinkTextColors().getDefaultColor()), startIndex, lastIndex - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                fragmentInfoView.setText(spanned);
                fragmentInfoView.setOnClickListener(v -> {
                    try {
                        v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://fragment.com")));
                    } catch (ActivityNotFoundException e) {
                        FileLog.e(e);
                    }
                });

                gap.setTag(R.id.fit_width_tag, 1);
                fragmentInfoView.setTag(R.id.fit_width_tag, 1);
                popupLayout.addView(fragmentInfoView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
            }

            ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
            popupWindow.setPauseNotifications(true);
            popupWindow.setDismissAnimationDuration(220);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setClippingEnabled(true);
            popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
            popupWindow.setFocusable(true);
            popupLayout.measure(View.MeasureSpec.makeMeasureSpec(dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(dp(1000), View.MeasureSpec.AT_MOST));
            popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
            popupWindow.getContentView().setFocusableInTouchMode(true);
            popupWindowRef.set(popupWindow);

            float px = x, py = y;
            View v = view;
            while (v != getFragmentView() && v != null) {
                px += v.getX();
                py += v.getY();
                v = (View) v.getParent();
            }
            if (AndroidUtilities.isTablet()) {
                View pv = parentLayout.getView();
                if (pv != null) {
                    px += pv.getX() + pv.getPaddingLeft();
                    py += pv.getY() + pv.getPaddingTop();
                }
            }
            px -= popupLayout.getMeasuredWidth() / 2f;
            popupWindow.showAtLocation(getFragmentView(), 0, (int) px, (int) py);
            popupWindow.dimBehind();
            return true;
    }

    private void openLocation(boolean inMapsApp) {
        TLRPC.UserFull userInfo = dataProvider.getUserFull();

        if (userInfo == null || userInfo.business_location == null) return;
        if (userInfo.business_location.geo_point != null && !inMapsApp) {
            LocationActivity fragment = new LocationActivity(3) {
                @Override
                protected boolean disablePermissionCheck() {
                    return true;
                }
            };
            fragment.setResourceProvider(resourcesProvider);
            TLRPC.TL_message message = new TLRPC.TL_message();
            message.local_id = -1;
            message.peer_id = getMessagesController().getPeer(getDialogId());
            TLRPC.TL_messageMediaGeo media = new TLRPC.TL_messageMediaGeo();
            media.geo = userInfo.business_location.geo_point;
            media.address = userInfo.business_location.address;
            message.media = media;
            fragment.setSharingAllowed(false);
            fragment.setMessageObject(new MessageObject(UserConfig.selectedAccount, message, false, false));
            presentFragment(fragment);
        } else {
            String domain;
            if (BuildVars.isHuaweiStoreApp()) {
                domain = "mapapp://navigation";
            } else {
                domain = "http://maps.google.com/maps";
            }
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, domain + "?q=" + userInfo.business_location.address )));
                getParentActivity().startActivity(intent);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

    }

    private boolean processUserLocation(final View view, final float x, final float y) {
        TLRPC.UserFull userInfo = dataProvider.getUserFull();
        if (userInfo.business_location == null) return false;

        String finalText = userInfo.business_location.address;
        return showCopyMenu(view, (int) x, (int) y, finalText, false);
    }

    private boolean processBusinessHours(final View view, final float x, final float y) {
        TLRPC.UserFull userInfo = dataProvider.getUserFull();
        if (userInfo.business_work_hours == null) return false;

        String finalText = OpeningHoursActivity.toString(currentAccount, userInfo.user, userInfo.business_work_hours);
        return showCopyMenu(view, (int) x, (int) y, finalText, false);
    }

    private boolean processAbout(final View view, final float x, final float y) {
        String text;
        if(dataProvider.getChatFull() != null){
            text = dataProvider.getChatFull().about;
        } else if (dataProvider.getUserFull() != null){
            text = dataProvider.getUserFull().about;
        } else {
            text = "";
        }

        if (text.isEmpty()) return false;

        return showCopyMenu(view, (int) x, (int) y, text, true);
    }

    private boolean showCopyMenu(View view, int x, int y, String finalText, boolean withTranslate) {
        final boolean translateButtonEnabled = MessagesController.getInstance(currentAccount).getTranslateController().isContextTranslateEnabled();

        final String[] fromLanguage = {"und"};
        final String toLang = LocaleController.getInstance().getCurrentLocale().getLanguage();
        final boolean[] useTranslation = {withTranslate};

        Runnable showMenu = () -> {
            if (getParentActivity() == null) {
                return;
            }
            CharSequence[] items = useTranslation[0] ? new CharSequence[]{getString(R.string.Copy), getString(R.string.TranslateMessage)} : new CharSequence[]{getString(R.string.Copy)};
            int[] icons = useTranslation[0] ? new int[] {R.drawable.msg_copy, R.drawable.msg_translate} : new int[] {R.drawable.msg_copy};

            AtomicReference<ActionBarPopupWindow> popupWindowRef = new AtomicReference<>();
            ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), R.drawable.popup_fixed_alert, resourcesProvider) {
                Path path = new Path();

                @Override
                protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                    canvas.save();
                    path.rewind();
                    AndroidUtilities.rectTmp.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                    path.addRoundRect(AndroidUtilities.rectTmp, dp(6), dp(6), Path.Direction.CW);
                    canvas.clipPath(path);
                    boolean draw = super.drawChild(canvas, child, drawingTime);
                    canvas.restore();
                    return draw;
                }
            };
            popupLayout.setFitItems(true);

            for (int i = 0; i < icons.length; i++) {
                int j = i;
                ActionBarMenuItem.addItem(popupLayout, icons[i], items[i], false, resourcesProvider).setOnClickListener(v -> {
                    popupWindowRef.get().dismiss();
                    try {
                        if (j == 0) {
                            AndroidUtilities.addToClipboard(finalText);
                            BulletinFactory.of(this).createCopyBulletin(getString(R.string.TextCopied)).show();
                        } else if (j == 1) {
                            TranslateAlert2.showAlert(fragmentView.getContext(), this, currentAccount, fromLanguage[0], toLang, finalText, null, false, span -> {
                                if (span != null) {
                                    openUrl(span.getURL(), null);
                                    return true;
                                }
                                return false;
                            }, null);
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                });
            }

            ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
            popupWindow.setPauseNotifications(true);
            popupWindow.setDismissAnimationDuration(220);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setClippingEnabled(true);
            popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
            popupWindow.setFocusable(true);
            popupLayout.measure(View.MeasureSpec.makeMeasureSpec(dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(dp(1000), View.MeasureSpec.AT_MOST));
            popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
            popupWindow.getContentView().setFocusableInTouchMode(true);
            popupWindowRef.set(popupWindow);

            float px = x, py = y;
            View v = view;
            while (v != null && v != getFragmentView()) {
                px += v.getX();
                py += v.getY();
                v = (View) v.getParent();
            }
            if (AndroidUtilities.isTablet()) {
                View pv = parentLayout.getView();
                if (pv != null) {
                    px += pv.getX() + pv.getPaddingLeft();
                    py += pv.getY() + pv.getPaddingTop();
                }
            }
            px -= popupLayout.getMeasuredWidth() / 2f;
            popupWindow.showAtLocation(getFragmentView(), 0, (int) px, (int) py);
            popupWindow.dimBehind();
        };
        if (useTranslation[0]) {
            if (LanguageDetector.hasSupport()) {
                LanguageDetector.detectLanguage(finalText, (fromLang) -> {
                    fromLanguage[0] = fromLang;
                    useTranslation[0] = fromLang != null && (!fromLang.equals(toLang) || fromLang.equals("und")) && (
                            translateButtonEnabled && !RestrictedLanguagesSelectActivity.getRestrictedLanguages().contains(fromLang) ||
                                    (currentChat != null && (currentChat.has_link || ChatObject.isPublic(currentChat))) && ("uk".equals(fromLang) || "ru".equals(fromLang)));
                    showMenu.run();
                }, (error) -> {
                    FileLog.e("mlkit: failed to detect language in selection", error);
                    showMenu.run();
                });
            } else {
                showMenu.run();
            }
        } else {
            showMenu.run();
        }
        return true;
    }
}
