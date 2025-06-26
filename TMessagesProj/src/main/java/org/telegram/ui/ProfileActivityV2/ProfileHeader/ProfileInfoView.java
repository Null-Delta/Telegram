package org.telegram.ui.ProfileActivityV2.ProfileHeader;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.exoplayer2.util.Log;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SvgHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CrossfadeDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.MessagePrivateSeenView;
import org.telegram.ui.Components.Premium.PremiumPreviewBottomSheet;
import org.telegram.ui.Components.ScamDrawable;
import org.telegram.ui.ProfileActivityV2.ProfileActivityDataProvider;
import org.telegram.ui.ProfileActivityV2.ProfileActivityV2;
import org.telegram.tgnet.TLRPC;

public class ProfileInfoView extends FrameLayout {
    public FrameLayout titleViewContainer;
    private FrameLayout subtitleViewContainer;
    public SimpleTextView titleView;
    public SimpleTextView subtitleView;
    public AudioPlayerAlert.ClippingTextViewSwitcher mediaCounterTextView;

    public AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable emojiStatusDrawable;
    public AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable botVerificationDrawable;
    public Drawable lockIconDrawable;
    public Drawable premiumStarDrawable;
    private CrossfadeDrawable premiumCrossfadeDrawable;
    public Drawable verifiedCheckDrawable;
    private CrossfadeDrawable verifiedCrossfadeDrawable;
    public Drawable verifiedDrawable;
    public ScamDrawable scamDrawable;
    private String nameTextViewRightDrawableContentDescription = null;
    private String nameTextViewRightDrawable2ContentDescription = null;

    private ProfileActivityDataProvider dataProvider;
    private ProfileActivityV2.HeaderSizeConfiguration configuration;

    private ValueAnimator titleExpandingAnimator;
    private ValueAnimator subtitleExpandingAnimator;

    private ProfileActivityV2 fragment;
    private float currentExpandingProgress;
    private boolean isExpanded;
    final private float avatarTopMargin = AndroidUtilities.statusBarHeight + AndroidUtilities.dp(12);

    public int onlineCount = -1;
    private boolean profileStatusIsButton = false;

    private float maxTitleWidth = 0;
    private float maxTitleExpandedWidth = 0;
    private float titleContentWidth = 0;

    public Long emojiStatusGiftId;
    private boolean preloadedChannelEmojiStatuses;

    private ProfileActivityV2.ShowDrawable showStatusButton;
    public ProfileActivityV2.ShowDrawable getShowStatusButton() {
        if (showStatusButton == null) {
            showStatusButton = new ProfileActivityV2.ShowDrawable(LocaleController.getString(R.string.StatusHiddenShow));
            showStatusButton.setAlpha(255);
//            showStatusButton.setAlpha((int) (0xFF * Math.min(1f, extraHeight / AndroidUtilities.dp(88f))));
//            showStatusButton.setBackgroundColor(ColorUtils.blendARGB(Theme.multAlpha(Theme.adaptHSV(actionBarBackgroundColor, +0.18f, -0.1f), 0.5f), 0x23ffffff, currentExpandAnimatorValue));
        }
        return showStatusButton;
    }

    public ProfileInfoView(@NonNull Context context, ProfileActivityDataProvider dataProvider, ProfileActivityV2.HeaderSizeConfiguration configuration, ProfileActivityV2 fragment) {
        super(context);

        this.dataProvider = dataProvider;
        this.configuration = configuration;
        this.fragment = fragment;

        titleView = new SimpleTextView(context) {
            @Override
            public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(info);
                if (isFocusable() && (nameTextViewRightDrawableContentDescription != null || nameTextViewRightDrawable2ContentDescription != null)) {
                    StringBuilder s = new StringBuilder(getText());
                    if (nameTextViewRightDrawable2ContentDescription != null) {
                        if (s.length() > 0) s.append(", ");
                        s.append(nameTextViewRightDrawable2ContentDescription);
                    }
                    if (nameTextViewRightDrawableContentDescription != null) {
                        if (s.length() > 0) s.append(", ");
                        s.append(nameTextViewRightDrawableContentDescription);
                    }
                    info.setText(s);
                }
            }
            @Override
            protected void onDraw(Canvas canvas) {
//                final int wasRightDrawableX = getRightDrawableX();
                super.onDraw(canvas);
//                if (wasRightDrawableX != getRightDrawableX()) {
//                    updateCollectibleHint();
//                }
            }
        };

        titleViewContainer = new FrameLayout(context);
        titleViewContainer.setClipChildren(false);
        titleViewContainer.setPivotX(0);
        titleViewContainer.setPivotY(0);

        titleView.setScrollNonFitText(true);
        titleView.setTextColor(Theme.getColor(Theme.key_profile_title));
        titleView.setPadding(0, AndroidUtilities.dp(6), 0, AndroidUtilities.dp(12));
        titleView.setTextSize(22);
        titleView.setGravity(Gravity.LEFT);
        titleView.setTypeface(AndroidUtilities.bold());
        titleView.setLeftDrawableTopPadding(-AndroidUtilities.dp(1.3f));
        titleView.setFocusable(true);
        titleView.setEllipsizeByGradient(true);
        titleView.setRightDrawableInside(true);

        titleViewContainer.addView(
                titleView,
                LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT)
        );

        addView(
                titleViewContainer,
                LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL)
        );


        subtitleView = new LinkSpanDrawable.ClickableSmallTextView(context) {

            @Override
            public void setAlpha(float alpha) {
                super.setAlpha(alpha);
//                checkPhotoDescriptionAlpha();
            }

            @Override
            public void setTextColor(int color) {
                super.setTextColor(color);
                if (showStatusButton != null) {
                    showStatusButton.setTextColor(Theme.multAlpha(Theme.adaptHSV(color, -.02f, +.15f), 1.4f));
                }
            }
        };

        subtitleView.setEllipsizeByGradient(true);
        subtitleView.setTextColor(fragment.applyPeerColor(fragment.getThemedColor(Theme.key_avatar_subtitleInProfileBlue), true, null));
        subtitleView.setTextSize(14);
        subtitleView.setGravity(Gravity.LEFT);
        subtitleView.setAlpha(1.0f);
        subtitleView.setPivotX(0);
        subtitleView.setPivotY(0);
        subtitleView.setPadding(0, AndroidUtilities.dp(2), 0, AndroidUtilities.dp(2));
        subtitleView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        subtitleView.setRightDrawableInside(true);

        subtitleViewContainer = new FrameLayout(context);
        subtitleViewContainer.setClipChildren(false);
        subtitleViewContainer.setPivotX(0);
        subtitleViewContainer.setPivotY(0);

        subtitleViewContainer.addView(
                subtitleView,
                LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT)
        );

        addView(
                subtitleViewContainer,
                LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL)
        );


        mediaCounterTextView = new AudioPlayerAlert.ClippingTextViewSwitcher(context) {
            @Override
            protected TextView createTextView() {
                TextView textView = new TextView(context);
                textView.setTextColor(fragment.getThemedColor(Theme.key_player_actionBarSubtitle));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, AndroidUtilities.dp(14));
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setGravity(Gravity.LEFT);
                return textView;
            }
        };
        mediaCounterTextView.setAlpha(0.0f);

        subtitleViewContainer.addView(
                mediaCounterTextView,
                LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT)
        );


        setClipChildren(false);
    }

    public void onDestroy() {
        if (titleExpandingAnimator != null) {
            titleExpandingAnimator.cancel();
            titleExpandingAnimator = null;
        }

        if (subtitleExpandingAnimator != null) {
            subtitleExpandingAnimator.cancel();
            subtitleExpandingAnimator = null;
        }
    }

    public void updateTitleData() {
        if (titleView == null) {
            return;
        }

        if (dataProvider.getUser() != null) {
            TLRPC.User user = dataProvider.getUser();

            CharSequence newString = UserObject.getUserName(user);

            if (user.id != fragment.getUserConfig().getClientUserId() && !MessagesController.isSupportUser(user) && user.phone != null && user.phone.length() != 0 && dataProvider.getContactsController().contactsDict.get(user.id) == null &&
                    (dataProvider.getContactsController().contactsDict.size() != 0 || !dataProvider.getContactsController().isLoadingContacts())) {
                titleView.setText(PhoneFormat.getInstance().format("+" + user.phone));
            } else {
                titleView.setText(newString);
            }

            Drawable leftIcon = dataProvider.getCurrentEncryptedChat() != null ? getLockIconDrawable() : null;
            titleView.setRightDrawableOutside(true);
            if (user.scam || user.fake) {
                titleView.setRightDrawable2(getScamDrawable(user.scam ? 0 : 1));
                nameTextViewRightDrawable2ContentDescription = LocaleController.getString(R.string.ScamMessage);
            } else if (user.verified) {
                titleView.setRightDrawable2(getVerifiedCrossfadeDrawable());
                nameTextViewRightDrawable2ContentDescription = LocaleController.getString(R.string.AccDescrVerified);
            } else if (dataProvider.getMessagesController().isDialogMuted(dataProvider.dialogId != 0 ? dataProvider.dialogId : dataProvider.userId, dataProvider.topicId)) {
                titleView.setRightDrawable2(fragment.getThemedDrawable(Theme.key_drawable_muteIconDrawable));
                nameTextViewRightDrawable2ContentDescription = LocaleController.getString(R.string.NotificationsMuted);
            } else {
                titleView.setRightDrawable2(null);
                nameTextViewRightDrawable2ContentDescription = null;
            }
            if (!dataProvider.premiumFeaturesBlocked() && !MessagesController.isSupportUser(user) && DialogObject.getEmojiStatusDocumentId(user.emoji_status) != 0) {
                titleView.setRightDrawable(getEmojiStatusDrawable(user.emoji_status, false));
                nameTextViewRightDrawableContentDescription = LocaleController.getString(R.string.AccDescrPremium);
            } else if (dataProvider.isPremiumUser()) {
                titleView.setRightDrawable(getEmojiStatusDrawable(null, false));
                nameTextViewRightDrawableContentDescription = LocaleController.getString(R.string.AccDescrPremium);
            } else {
                titleView.setRightDrawable(null);
                nameTextViewRightDrawableContentDescription = null;
            }

            if (leftIcon == null && dataProvider.getCurrentEncryptedChat() == null && user.bot_verification_icon != 0) {
                titleView.setLeftDrawableOutside(true);
                leftIcon = getBotVerificationDrawable(user.bot_verification_icon, false);
            } else {
                titleView.setLeftDrawableOutside(false);
            }
            titleView.setLeftDrawable(leftIcon);
            if (user.self && dataProvider.isPremiumUser()) {
                titleView.setRightDrawableOnClick(v -> {
                    fragment.showStatusSelect();
                });
            }
            if (!user.self && dataProvider.isPremiumUser()) {
                final SimpleTextView textView = titleView;
                titleView.setRightDrawableOnClick(v -> {
                    if (user.emoji_status instanceof TLRPC.TL_emojiStatusCollectible) {
                        TLRPC.TL_emojiStatusCollectible status = (TLRPC.TL_emojiStatusCollectible) user.emoji_status;
                        if (status != null) {
                            Browser.openUrl(getContext(), "https://" + fragment.getMessagesController().linkPrefix + "/nft/" + status.slug);
                        }
                        return;
                    }
                    PremiumPreviewBottomSheet premiumPreviewBottomSheet = new PremiumPreviewBottomSheet(fragment, fragment.getCurrentAccount(), user, fragment.getResourceProvider());
                    int[] coords = new int[2];
                    float scale = titleView.getScaleX();
                    if(Math.abs(scale - 1f) < 0.03f) {
                        scale = titleViewContainer.getScaleX();
                    }
                    textView.getLocationOnScreen(coords);
                    premiumPreviewBottomSheet.startEnterFromX = textView.rightDrawableX;
                    premiumPreviewBottomSheet.startEnterFromY = textView.rightDrawableY;
                    premiumPreviewBottomSheet.startEnterFromScale = scale;
                    premiumPreviewBottomSheet.startEnterFromX1 = coords[0];
                    premiumPreviewBottomSheet.startEnterFromY1 = coords[1];
                    premiumPreviewBottomSheet.startEnterFromView = textView;
                    if (textView.getRightDrawable() == emojiStatusDrawable && emojiStatusDrawable != null && emojiStatusDrawable.getDrawable() instanceof AnimatedEmojiDrawable) {
                        premiumPreviewBottomSheet.startEnterFromScale *= 0.98f;
                        TLRPC.Document document = ((AnimatedEmojiDrawable) emojiStatusDrawable.getDrawable()).getDocument();
                        if (document != null) {
                            BackupImageView icon = new BackupImageView(getContext());
                            String filter = "160_160";
                            ImageLocation mediaLocation;
                            String mediaFilter;
                            SvgHelper.SvgDrawable thumbDrawable = DocumentObject.getSvgThumb(document.thumbs, Theme.key_windowBackgroundWhiteGrayIcon, 0.2f);
                            TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(document.thumbs, 90);
                            if ("video/webm".equals(document.mime_type)) {
                                mediaLocation = ImageLocation.getForDocument(document);
                                mediaFilter = filter + "_" + ImageLoader.AUTOPLAY_FILTER;
                                if (thumbDrawable != null) {
                                    thumbDrawable.overrideWidthAndHeight(512, 512);
                                }
                            } else {
                                if (thumbDrawable != null && MessageObject.isAnimatedStickerDocument(document, false)) {
                                    thumbDrawable.overrideWidthAndHeight(512, 512);
                                }
                                mediaLocation = ImageLocation.getForDocument(document);
                                mediaFilter = filter;
                            }
                            icon.setLayerNum(7);
                            icon.setRoundRadius(AndroidUtilities.dp(4));
                            icon.setImage(mediaLocation, mediaFilter, ImageLocation.getForDocument(thumb, document), "140_140", thumbDrawable, document);
                            premiumPreviewBottomSheet.statusStickerSet = MessageObject.getInputStickerSet(document);
                            premiumPreviewBottomSheet.overrideTitleIcon = icon;
                            premiumPreviewBottomSheet.isEmojiStatus = true;
                        }
                    }
                    fragment.showDialog(premiumPreviewBottomSheet);
                });
            }
        } else if (dataProvider.getChat() != null) {
            TLRPC.Chat chat = dataProvider.getChat();
            TLRPC.TL_forumTopic topic = null;

            if (dataProvider.isTopic) {
                topic = dataProvider.findTopic();
            }

            if (dataProvider.isTopic) {
                CharSequence title = topic == null ? "" : topic.title;
                try {
                    title = Emoji.replaceEmoji(title, titleView.getPaint().getFontMetricsInt(), false);
                } catch (Exception ignore) {
                }
                titleView.setText(title);
            } else if (ChatObject.isMonoForum(chat)) {
                CharSequence title = getString(R.string.ChatMessageSuggestions);
                titleView.setText(title);
            } else if (chat.title != null) {
                CharSequence title = chat.title;
                try {
                    title = Emoji.replaceEmoji(title, titleView.getPaint().getFontMetricsInt(), false);
                } catch (Exception ignore) {
                }
                titleView.setText(title);
            }
            titleView.setLeftDrawableOutside(false);
            titleView.setLeftDrawable(null);
            titleView.setRightDrawableOutside(true);

            if (chat.scam || chat.fake) {
                titleView.setRightDrawable2(getScamDrawable(chat.scam ? 0 : 1));
            } else if (chat.verified) {
                titleView.setRightDrawable2(getVerifiedCrossfadeDrawable());
            } else if (dataProvider.isDialogMuted()) {
                titleView.setRightDrawable2(fragment.getThemedDrawable(Theme.key_drawable_muteIconDrawable));
            } else {
                titleView.setRightDrawable2(null);
            }
            if (DialogObject.getEmojiStatusDocumentId(chat.emoji_status) != 0) {
                titleView.setRightDrawable(getEmojiStatusDrawable(chat.emoji_status, false));
                titleView.setRightDrawableOutside(true);
            } else {
                titleView.setRightDrawable(null);
            }

            if (chat.bot_verification_icon != 0) {
                titleView.setLeftDrawableOutside(true);
                titleView.setLeftDrawable(getBotVerificationDrawable(chat.bot_verification_icon, false));
            } else {
                titleView.setLeftDrawable(null);
            }

            if (DialogObject.getEmojiStatusDocumentId(chat.emoji_status) != 0) {
                titleView.setRightDrawable(getEmojiStatusDrawable(chat.emoji_status, true));
//                titleView.setRightDrawableOutside(true);
                nameTextViewRightDrawableContentDescription = null;
                if (ChatObject.canChangeChatInfo(chat)) {
                    titleView.setRightDrawableOnClick(v -> {
                        fragment.showStatusSelect();
                    });
                    if (preloadedChannelEmojiStatuses) {
                        preloadedChannelEmojiStatuses = true;
                        fragment.getMediaDataController().loadRestrictedStatusEmojis();
                    }
                } else if (chat.emoji_status instanceof TLRPC.TL_emojiStatusCollectible) {
                    final String slug = ((TLRPC.TL_emojiStatusCollectible) chat.emoji_status).slug;
                    titleView.setRightDrawableOnClick(v -> {
                        Browser.openUrl(getContext(), "https://" + fragment.getMessagesController().linkPrefix + "/nft/" + slug);
                    });
                }
            }
        }
    }

    public void updateSubtitleData() {
        if (subtitleView == null) {
            return;
        }

        String onlineTextOverride;
        int currentConnectionState = fragment.getConnectionsManager().getConnectionState();
        if (currentConnectionState == ConnectionsManager.ConnectionStateWaitingForNetwork) {
            onlineTextOverride = LocaleController.getString(R.string.WaitingForNetwork);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateConnecting) {
            onlineTextOverride = LocaleController.getString(R.string.Connecting);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateUpdating) {
            onlineTextOverride = LocaleController.getString(R.string.Updating);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateConnectingToProxy) {
            onlineTextOverride = LocaleController.getString(R.string.ConnectingToProxy);
        } else {
            onlineTextOverride = null;
        }

        TLRPC.TL_forumTopic topic = null;
        boolean shortStatus;

        if (dataProvider.userId != 0) {
            TLRPC.User user = dataProvider.getUser();
            if (user == null) { return; }

            shortStatus = user.photo != null && user.photo.personal;

            CharSequence newString = UserObject.getUserName(user);
            String newString2;
            boolean hiddenStatusButton = false;
            if (user.id == fragment.getUserConfig().getClientUserId()) {
                if (UserObject.hasFallbackPhoto(dataProvider.getUserFull())) {
                    newString2 = "";
                } else {
                    newString2 = LocaleController.getString(R.string.Online);
                }
            } else if (user.id == UserObject.VERIFY) {
                newString2 = LocaleController.getString(R.string.VerifyCodesNotifications);
            } else if (user.id == 333000 || user.id == 777000 || user.id == 42777) {
                newString2 = LocaleController.getString(R.string.ServiceNotifications);
            } else if (MessagesController.isSupportUser(user)) {
                newString2 = LocaleController.getString(R.string.SupportStatus);
            } else if (dataProvider.isBot()) {
                if (user.bot_active_users != 0) {
                    newString2 = LocaleController.formatPluralStringComma("BotUsers", user.bot_active_users, ',');
                } else {
                    newString2 = LocaleController.getString(R.string.Bot);
                }
            } else {
                boolean isOnline = false;
                boolean[] input = new boolean[1];
                input[0] =  isOnline;
                newString2 = LocaleController.formatUserStatus(fragment.getCurrentAccount(), user, input, shortStatus ? new boolean[1] : null);
                hiddenStatusButton = user != null && !isOnline && !fragment.getUserConfig().isPremium() && user.status != null && (user.status instanceof TLRPC.TL_userStatusRecently || user.status instanceof TLRPC.TL_userStatusLastMonth || user.status instanceof TLRPC.TL_userStatusLastWeek) && user.status.by_me;

                boolean mediaHeaderVisible = false;

                if (subtitleView != null && !mediaHeaderVisible) {
                    int key = isOnline && dataProvider.peerColor() == null ? Theme.key_profile_status : Theme.key_avatar_subtitleInProfileBlue;
                    subtitleView.setTag(key);
                    if (!isExpanded) {
                        subtitleView.setTextColor(fragment.applyPeerColor(fragment.getThemedColor(key), true, null));
                    }
                }
            }

            if (onlineTextOverride != null) {
                subtitleView.setText(onlineTextOverride);
            } else {
                subtitleView.setText(newString2);
            }
            subtitleView.setDrawablePadding(dp(9));
            subtitleView.setRightDrawableInside(true);
            subtitleView.setRightDrawable(hiddenStatusButton ? getShowStatusButton() : null);
            subtitleView.setRightDrawableOnClick(hiddenStatusButton ? v -> {
                MessagePrivateSeenView.showSheet(getContext(), fragment.getCurrentAccount(), dataProvider.getDialogId(), true, null, () -> {
                    dataProvider.getMessagesController().reloadUser(dataProvider.getDialogId());
                }, fragment.getResourceProvider());
            } : null);

            if (dataProvider.userId == UserConfig.getInstance(fragment.getCurrentAccount()).clientUserId) {
                subtitleView.setText(LocaleController.getString(R.string.Online));
            }
        } else if (dataProvider.chatId != 0) {
            TLRPC.Chat chat = dataProvider.getChat();
            TLRPC.ChatFull chatInfo = dataProvider.getChatFull();

            if (dataProvider.isTopic) {
                topic = dataProvider.getMessagesController().getTopicsController().findTopic(dataProvider.chatId, dataProvider.topicId);
            }

            CharSequence statusString;
            CharSequence profileStatusString;

            if (ChatObject.isChannel(chat)) {
                if (!dataProvider.isTopic && (chatInfo == null || !fragment.currentChat.megagroup && (chatInfo.participants_count == 0 || ChatObject.hasAdminRights(fragment.currentChat) || chatInfo.can_view_participants))) {
                    if (fragment.currentChat.megagroup) {
                        statusString = profileStatusString = LocaleController.getString(R.string.Loading).toLowerCase();
                    } else {
                        if (ChatObject.isPublic(chat)) {
                            statusString = profileStatusString = LocaleController.getString(R.string.ChannelPublic).toLowerCase();
                        } else {
                            statusString = profileStatusString = LocaleController.getString(R.string.ChannelPrivate).toLowerCase();
                        }
                    }
                } else {
                    if (dataProvider.isTopic) {
                        int count = 0;
                        if (topic != null) {
                            count = topic.totalMessagesCount - 1;
                        }
                        if (count > 0) {
                            statusString = LocaleController.formatPluralString("messages", count, count);
                        } else {
                            statusString = LocaleController.formatString("TopicProfileStatus", R.string.TopicProfileStatus, chat.title);
                        }
                        SpannableString arrowString = new SpannableString(">");
                        arrowString.setSpan(new ColoredImageSpan(R.drawable.arrow_newchat), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        profileStatusString = new SpannableStringBuilder(chat.title).append(' ').append(arrowString);
                        profileStatusIsButton = true;
                    } else if (fragment.currentChat.megagroup) {
                        if (onlineCount > 1 && chatInfo.participants_count != 0) {
                            statusString = String.format("%s, %s", LocaleController.formatPluralString("Members", chatInfo.participants_count), LocaleController.formatPluralString("OnlineCount", Math.min(onlineCount, chatInfo.participants_count)));
                            profileStatusString = String.format("%s, %s", LocaleController.formatPluralStringComma("Members", chatInfo.participants_count), LocaleController.formatPluralStringComma("OnlineCount", Math.min(onlineCount, chatInfo.participants_count)));
                        } else {
                            if (chatInfo.participants_count == 0) {
                                if (chat.has_geo) {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaLocation).toLowerCase();
                                } else if (ChatObject.isPublic(chat)) {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaPublic).toLowerCase();
                                } else {
                                    statusString = profileStatusString = LocaleController.getString(R.string.MegaPrivate).toLowerCase();
                                }
                            } else {
                                statusString = LocaleController.formatPluralString("Members", chatInfo.participants_count);
                                profileStatusString = LocaleController.formatPluralStringComma("Members", chatInfo.participants_count);
                            }
                        }
                    } else {
                        int[] result = new int[1];
                        if (fragment.currentChat.megagroup) {
                            statusString = LocaleController.formatPluralString("Members", chatInfo.participants_count);
                            profileStatusString = LocaleController.formatPluralStringComma("Members", chatInfo.participants_count);
                        } else {
                            statusString = LocaleController.formatPluralString("Subscribers", chatInfo.participants_count);
                            profileStatusString = LocaleController.formatPluralStringComma("Subscribers", chatInfo.participants_count);
                        }
                    }
                }
            } else {
                if (ChatObject.isKickedFromChat(chat)) {
                    statusString = profileStatusString = LocaleController.getString(R.string.YouWereKicked);
                } else if (ChatObject.isLeftFromChat(chat)) {
                    statusString = profileStatusString = LocaleController.getString(R.string.YouLeft);
                } else {
                    int count = chat.participants_count;
                    if (dataProvider.getChatFull() != null && dataProvider.getChatFull().participants != null) {
                        count = dataProvider.getChatFull().participants.participants.size();
                    }
                    if (count != 0 && onlineCount > 1) {
                        statusString = profileStatusString = String.format("%s, %s", LocaleController.formatPluralString("Members", count), LocaleController.formatPluralString("OnlineCount", onlineCount));
                    } else {
                        statusString = profileStatusString = LocaleController.formatPluralString("Members", count);
                    }
                }
            }

            if (onlineTextOverride != null) {
                subtitleView.setText(onlineTextOverride);
            } else {
                if (ChatObject.isChannel(fragment.currentChat) && chatInfo != null && chatInfo.participants_count != 0 && (fragment.currentChat.megagroup || fragment.currentChat.broadcast)) {
                    int[] result = new int[1];
                    boolean ignoreShort = AndroidUtilities.isAccessibilityScreenReaderEnabled();
                    String shortNumber = ignoreShort ? String.valueOf(result[0] = chatInfo.participants_count) : LocaleController.formatShortNumber(chatInfo.participants_count, result);
                    if (fragment.currentChat.megagroup) {
                        if (chatInfo.participants_count == 0) {
                            if (chat.has_geo) {
                                subtitleView.setText(LocaleController.getString(R.string.MegaLocation).toLowerCase());
                            } else if (ChatObject.isPublic(chat)) {
                                subtitleView.setText(LocaleController.getString(R.string.MegaPublic).toLowerCase());
                            } else {
                                subtitleView.setText(LocaleController.getString(R.string.MegaPrivate).toLowerCase());
                            }
                        } else {
                            subtitleView.setText(LocaleController.formatPluralString("Members", result[0]).replace(String.format("%d", result[0]), shortNumber));
                        }
                    } else {
                        subtitleView.setText(LocaleController.formatPluralString("Subscribers", result[0]).replace(String.format("%d", result[0]), shortNumber));
                    }
                } else {
                    subtitleView.setText(statusString);
                }
            }
                if (dataProvider.isTopic) {
                    if (profileStatusIsButton) {
                        subtitleView.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(2), AndroidUtilities.dp(4), AndroidUtilities.dp(2));
                        subtitleView.setOnClickListener(e -> fragment.goToForum());
                    } else {
                        subtitleView.setPadding(0, AndroidUtilities.dp(2), 0, AndroidUtilities.dp(2));
                        subtitleView.setOnClickListener(null);
                        subtitleView.setClickable(false);
                    }
                }
        }
    }

    public void expandTitle() {
        if (this.getWidth() == 0) { return; }
        if (titleExpandingAnimator != null) {
            titleExpandingAnimator.cancel();
        }

        isExpanded = true;
        float yOffset = dataProvider.isMyProfile || fragment.isSettings() ? AndroidUtilities.dp(-12) : 0;

        titleExpandingAnimator = ValueAnimator.ofFloat(currentExpandingProgress, 1);
        titleExpandingAnimator.setDuration(ProfileActivityV2.expandingAnimationDuration);
        titleExpandingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        titleExpandingAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            titleViewContainer.setTranslationX(AndroidUtilities.lerp(0, -(titleViewContainer.getLeft() - AndroidUtilities.dp(20)), value));
            titleViewContainer.setTranslationY(AndroidUtilities.lerp(0, configuration.expandedHeight - configuration.defaultHeight + yOffset, value));
            titleViewContainer.setScaleX(AndroidUtilities.lerp(1f, 1.2f, value));
            titleViewContainer.setScaleY(AndroidUtilities.lerp(1f, 1.2f, value));

            FrameLayout.LayoutParams titleParams = (FrameLayout.LayoutParams) titleViewContainer.getLayoutParams();
            titleParams.width = (int) AndroidUtilities.lerp(titleContentWidth, Math.min(titleContentWidth, maxTitleExpandedWidth), value);
            titleViewContainer.setLayoutParams(titleParams);

            if (verifiedCrossfadeDrawable != null) {
                verifiedCrossfadeDrawable.setProgress(value);
            }
            if (premiumCrossfadeDrawable != null) {
                premiumCrossfadeDrawable.setProgress(value);
            }

            currentExpandingProgress = value;
        });
        titleExpandingAnimator.start();
    }

    public void expandSubtitle() {
        if (this.getWidth() == 0) { return; }
        if (subtitleExpandingAnimator != null) {
            subtitleExpandingAnimator.cancel();
        }

        float yOffset = dataProvider.isMyProfile || fragment.isSettings() ? AndroidUtilities.dp(-12) : 0;

        subtitleExpandingAnimator = ValueAnimator.ofFloat(currentExpandingProgress, 1);
        subtitleExpandingAnimator.setDuration(ProfileActivityV2.expandingAnimationDuration);
        subtitleExpandingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        subtitleExpandingAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            subtitleView.setTranslationX(AndroidUtilities.lerp(0, -(subtitleViewContainer.getLeft() - AndroidUtilities.dp(20)), value));
            subtitleView.setTranslationY(AndroidUtilities.lerp(0, configuration.expandedHeight - configuration.defaultHeight + AndroidUtilities.dp(8) + yOffset, value));
            subtitleView.setScaleX(AndroidUtilities.lerp(1f, 1.2f, value));
            subtitleView.setScaleY(AndroidUtilities.lerp(1f, 1.2f, value));
        });
        subtitleExpandingAnimator.start();
    }

    public void collapseTitle() {
        if (this.getWidth() == 0) { return; }
        if (titleExpandingAnimator != null) {
            titleExpandingAnimator.cancel();
        }

        isExpanded = false;

        float yOffset = dataProvider.isMyProfile || fragment.isSettings() ? AndroidUtilities.dp(-12) : 0;

        titleExpandingAnimator = ValueAnimator.ofFloat(currentExpandingProgress, 0);
        titleExpandingAnimator.setDuration(ProfileActivityV2.expandingAnimationDuration);
        titleExpandingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        titleExpandingAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            titleViewContainer.setTranslationX(AndroidUtilities.lerp(0, -(titleViewContainer.getLeft() - AndroidUtilities.dp(20)), value));
            titleViewContainer.setTranslationY(AndroidUtilities.lerp(0, configuration.expandedHeight - configuration.defaultHeight + yOffset, value));
            titleViewContainer.setScaleX(AndroidUtilities.lerp(1f, 1.2f, value));
            titleViewContainer.setScaleY(AndroidUtilities.lerp(1f, 1.2f, value));

            FrameLayout.LayoutParams titleParams = (FrameLayout.LayoutParams) titleViewContainer.getLayoutParams();
            titleParams.width = (int) AndroidUtilities.lerp(titleContentWidth, Math.min(titleContentWidth, maxTitleExpandedWidth), value);
            titleViewContainer.setLayoutParams(titleParams);

            if (verifiedCrossfadeDrawable != null) {
                verifiedCrossfadeDrawable.setProgress(value);
            }
            if (premiumCrossfadeDrawable != null) {
                premiumCrossfadeDrawable.setProgress(value);
            }
            currentExpandingProgress = value;
        });
        titleExpandingAnimator.start();
    }

    public void collapseSubtitle() {
        if (this.getWidth() == 0) { return; }
        if (subtitleExpandingAnimator != null) {
            subtitleExpandingAnimator.cancel();
        }

        float yOffset = dataProvider.isMyProfile || fragment.isSettings() ? AndroidUtilities.dp(-12) : 0;

        subtitleExpandingAnimator = ValueAnimator.ofFloat(currentExpandingProgress, 0);
        subtitleExpandingAnimator.setDuration(ProfileActivityV2.expandingAnimationDuration);
        subtitleExpandingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        subtitleExpandingAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            subtitleView.setTranslationX(AndroidUtilities.lerp(0, -(subtitleViewContainer.getLeft() - AndroidUtilities.dp(20)), value));
            subtitleView.setTranslationY(AndroidUtilities.lerp(0, configuration.expandedHeight - configuration.defaultHeight +  + AndroidUtilities.dp(8) + yOffset, value));
            subtitleView.setScaleX(AndroidUtilities.lerp(1f, 1.2f, value));
            subtitleView.setScaleY(AndroidUtilities.lerp(1f, 1.2f, value));
        });
        subtitleExpandingAnimator.start();
    }

    public void updateTitleWidth() {
        titleContentWidth = titleView.getTextWidth();
        if (titleView.getRightDrawable() != null) { titleContentWidth += AndroidUtilities.dp(4); }
        if (titleView.getRightDrawable2() != null) { titleContentWidth += AndroidUtilities.dp(4); }
        if (titleView.getLeftDrawable() != null) {
            titleContentWidth += AndroidUtilities.dp(4);
            titleContentWidth += titleView.getLeftDrawable().getIntrinsicWidth();
        }

        titleContentWidth = Math.min(AndroidUtilities.displaySize.x - AndroidUtilities.dp(40), titleContentWidth);

        maxTitleWidth = AndroidUtilities.displaySize.x - AndroidUtilities.dp(40 * 3 + 24);

        maxTitleExpandedWidth = Math.min(titleContentWidth, (AndroidUtilities.displaySize.x - AndroidUtilities.dp(40)) / 1.2f);

        if (!isExpanded) {
            FrameLayout.LayoutParams titleParams = (FrameLayout.LayoutParams) titleViewContainer.getLayoutParams();
            titleParams.width = (int) (titleContentWidth);
            titleParams.topMargin =(int) (avatarTopMargin + AndroidUtilities.dp(90 + 4));
            titleViewContainer.setLayoutParams(titleParams);
        }
    }
    public void updateTitleState(float progress) {
        updateTitleWidth();

        if (progress < 0.8) {
            float finishProgress = AndroidUtilities.statusBarHeight / ((configuration.defaultHeight - configuration.minimizedHeight) * 0.8f);
            float titleProgress = (0.8f - progress) / (1 - finishProgress);

            titleView.setScaleX(AndroidUtilities.lerp(1f, 0.9f, Math.min(titleProgress, 1)));
            titleView.setScaleY(AndroidUtilities.lerp(1f, 0.9f, Math.min(titleProgress, 1)));
            setTranslationX(
                    AndroidUtilities.lerp(
                            0,
                            AndroidUtilities.dp(48 + 18) - titleViewContainer.getLeft() - titleViewContainer.getWidth() * (0.1f / 2f),
                            Math.min(titleProgress, 1)
                    )
            );

            setTranslationY(
                    AndroidUtilities.lerp(
                            (0.8f - 1) * (configuration.defaultHeight - configuration.minimizedHeight),
                            AndroidUtilities.statusBarHeight - titleViewContainer.getTop() - AndroidUtilities.dp(2),
                            Math.min(titleProgress, 1)
                    )
            );

            FrameLayout.LayoutParams titleParams2 = (FrameLayout.LayoutParams) titleViewContainer.getLayoutParams();
            titleParams2.width = (int) AndroidUtilities.lerp(titleContentWidth, Math.min(titleContentWidth, maxTitleWidth), Math.min(titleProgress, 1));
            titleViewContainer.setLayoutParams(titleParams2);
        } else {
            setTranslationX(0);
            setTranslationY((progress - 1) * (configuration.defaultHeight - configuration.minimizedHeight));
        }
    }

    public void updateMediaCounter() {
        mediaCounterTextView.invalidate();
        float mediaCounterWidth = mediaCounterTextView.getTextView().getPaint().measureText(mediaCounterTextView.getTextView().getText().toString());
        FrameLayout.LayoutParams mediaCounterParams = (FrameLayout.LayoutParams) mediaCounterTextView.getLayoutParams();
        mediaCounterParams.width = (int) (mediaCounterWidth);
        mediaCounterTextView.setLayoutParams(mediaCounterParams);
    }

    public void updateSubtitleState(float progress) {
        if (subtitleView != null) {
            int statusColor;
            if (subtitleView.getTag() instanceof Integer) {
                statusColor = fragment.applyPeerColor(fragment.getThemedColor((Integer) subtitleView.getTag()), true, null);
            } else {
                statusColor = fragment.applyPeerColor(fragment.getThemedColor(Theme.key_avatar_subtitleInProfileBlue), true, null);
            }
            subtitleView.setTextColor(ColorUtils.blendARGB(statusColor, 0xB3FFFFFF, isExpanded ? 1 : 0));
        }

        float width = subtitleView.getTextWidth();
        if (subtitleView.getRightDrawable() != null) { width += AndroidUtilities.dp(4); }
        if (subtitleView.getRightDrawable2() != null) { width += AndroidUtilities.dp(4); }
        if (subtitleView.getLeftDrawable() != null) {
            width += AndroidUtilities.dp(4);
            width += subtitleView.getLeftDrawable().getIntrinsicWidth();
        }

        if (dataProvider.isTopic && profileStatusIsButton) {
            width += AndroidUtilities.dp(9);
        }

        FrameLayout.LayoutParams subtitleParams = (FrameLayout.LayoutParams) subtitleViewContainer.getLayoutParams();
        subtitleParams.width = (int) (width);
        subtitleParams.topMargin =(int) (avatarTopMargin + AndroidUtilities.dp(90 + 4)) + titleView.getHeight() - AndroidUtilities.dp(12);
        subtitleViewContainer.setLayoutParams(subtitleParams);

        updateMediaCounter();

        if (progress < 0.8) {
            float finishProgress = AndroidUtilities.statusBarHeight / ((configuration.defaultHeight - configuration.minimizedHeight) * 0.8f);
            float titleProgress = (0.8f - progress) / (1 - finishProgress);

            subtitleViewContainer.setTranslationX(
                    AndroidUtilities.lerp(
                        0,
                        -(subtitleViewContainer.getLeft() - titleViewContainer.getLeft()) + titleViewContainer.getWidth() * (0.1f / 2f),
                        Math.min(titleProgress, 1)
                    )
            );

            subtitleViewContainer.setTranslationY(
                    AndroidUtilities.lerp(0,  -AndroidUtilities.dp(2), Math.min(titleProgress, 1))
            );
        } else {
            subtitleViewContainer.setTranslationX(0);
        }
    }

    private Drawable getLockIconDrawable() {
        if (lockIconDrawable == null) {
            lockIconDrawable = Theme.chat_lockIconDrawable.getConstantState().newDrawable().mutate();
        }
        return lockIconDrawable;
    }

    private Drawable getEmojiStatusDrawable(TLRPC.EmojiStatus emojiStatus, boolean animated) {
        if (emojiStatusDrawable == null) {
            emojiStatusDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(titleView, AndroidUtilities.dp(24), AnimatedEmojiDrawable.CACHE_TYPE_EMOJI_STATUS);
            if (fragment.fragmentViewAttached) {
                emojiStatusDrawable.attach();
            }
        }
        if (emojiStatus instanceof TLRPC.TL_emojiStatus) {
            final TLRPC.TL_emojiStatus status = (TLRPC.TL_emojiStatus) emojiStatus;
            if ((status.flags & 1) == 0 || status.until > (int) (System.currentTimeMillis() / 1000)) {
                emojiStatusDrawable.set(status.document_id, animated);
                emojiStatusDrawable.setParticles(false, animated);
            } else {
                emojiStatusDrawable.set(getPremiumCrossfadeDrawable(), animated);
                emojiStatusDrawable.setParticles(false, animated);
            }
        } else if (emojiStatus instanceof TLRPC.TL_emojiStatusCollectible) {
            final TLRPC.TL_emojiStatusCollectible status = (TLRPC.TL_emojiStatusCollectible) emojiStatus;
            if ((status.flags & 1) == 0 || status.until > (int) (System.currentTimeMillis() / 1000)) {
                emojiStatusGiftId = status.collectible_id;
                emojiStatusDrawable.set(status.document_id, animated);
                emojiStatusDrawable.setParticles(true, animated);
            } else {
                emojiStatusDrawable.set(getPremiumCrossfadeDrawable(), animated);
                emojiStatusDrawable.setParticles(false, animated);
            }
        } else {
            emojiStatusDrawable.set(getPremiumCrossfadeDrawable(), animated);
            emojiStatusDrawable.setParticles(false, animated);
        }
        updateEmojiStatusDrawableColor();
        return emojiStatusDrawable;
    }

    public Drawable getPremiumCrossfadeDrawable() {
        if (premiumCrossfadeDrawable == null) {
            premiumStarDrawable = ContextCompat.getDrawable(fragment.getParentActivity(), R.drawable.msg_premium_liststar).mutate();
            int color = fragment.getThemedColor(Theme.key_profile_verifiedBackground);
            premiumStarDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            premiumCrossfadeDrawable = new CrossfadeDrawable(premiumStarDrawable, ContextCompat.getDrawable(fragment.getParentActivity(), R.drawable.msg_premium_prolfilestar).mutate());
        }
        return premiumCrossfadeDrawable;
    }

    private Drawable getVerifiedCrossfadeDrawable() {
        if (verifiedCrossfadeDrawable == null) {
            verifiedDrawable = Theme.profile_verifiedDrawable.getConstantState().newDrawable().mutate();
            verifiedCheckDrawable = Theme.profile_verifiedCheckDrawable.getConstantState().newDrawable().mutate();
            verifiedCrossfadeDrawable = new CrossfadeDrawable(
                    new CombinedDrawable(verifiedDrawable, verifiedCheckDrawable),
                    ContextCompat.getDrawable(fragment.getParentActivity(), R.drawable.verified_profile)
            );
        }

        return verifiedCrossfadeDrawable;
    }

    private Drawable getBotVerificationDrawable(long icon, boolean animated) {
        if (botVerificationDrawable == null) {
            botVerificationDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(titleView, AndroidUtilities.dp(17), AnimatedEmojiDrawable.CACHE_TYPE_EMOJI_STATUS);
            botVerificationDrawable.offset(0, dp(1));
            if (fragment.fragmentViewAttached) {
                botVerificationDrawable.attach();
            }
        }
        if (icon != 0) {
            botVerificationDrawable.set(icon, animated);
        } else {
            botVerificationDrawable.set((Drawable) null, animated);
        }
        updateEmojiStatusDrawableColor();
        return botVerificationDrawable;
    }

    private Drawable getScamDrawable(int type) {
        if (scamDrawable == null) {
            scamDrawable = new ScamDrawable(11, type);
            scamDrawable.setColor(fragment.getThemedColor(Theme.key_avatar_subtitleInProfileBlue));
        }
        return scamDrawable;
    }

    private float lastEmojiStatusProgress;
    public void updateEmojiStatusDrawableColor() {
        updateEmojiStatusDrawableColor(lastEmojiStatusProgress);
    }
    private void updateEmojiStatusDrawableColor(float progress) {
        float mediaHeaderAnimationProgress = 0;
        final int fromColor;
        fromColor = AndroidUtilities.getOffsetColor(fragment.getThemedColor(Theme.key_profile_verifiedBackground), fragment.getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress, 1.0f);
        final int color = ColorUtils.blendARGB(ColorUtils.blendARGB(fromColor, 0xffffffff, progress), fragment.getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress);
        if (emojiStatusDrawable != null) {
            emojiStatusDrawable.setColor(color);
        }
        if (botVerificationDrawable != null) {
            botVerificationDrawable.setColor(ColorUtils.blendARGB(ColorUtils.blendARGB(fromColor, 0x99ffffff, progress), fragment.getThemedColor(Theme.key_player_actionBarTitle), mediaHeaderAnimationProgress));
        }

        lastEmojiStatusProgress = progress;
    }
}
