package org.telegram.ui.ProfileActivityV2.ProfileHeader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatNotificationsPopupWrapper;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.Forum.ForumUtilities;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.VectorAvatarThumbDrawable;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.Gifts.GiftSheet;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.ProfileActivityV2.AvatarImageView;
import org.telegram.ui.ProfileActivityV2.ProfileActivityDataProvider;
import org.telegram.ui.ProfileActivityV2.ProfileActivityV2;
import org.telegram.ui.ProfileNotificationsActivity;
import org.telegram.ui.ReportBottomSheet;
import org.telegram.ui.Stars.StarsController;
import org.telegram.ui.Stories.ProfileStoriesView;
import org.telegram.ui.Stories.StoriesController;
import org.telegram.ui.Stories.StoryViewer;
import org.telegram.ui.Stories.recorder.StoryRecorder;
import org.telegram.ui.TopicsNotifySettingsFragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class ProfileHeaderView extends FrameLayout {
    private class OverlaysView extends View implements ProfileGalleryView.Callback {

        private final int statusBarHeight = fragment.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
        private int overlayCountVisible = 3;

        private final android.graphics.Rect topOverlayRect = new android.graphics.Rect();
        private final android.graphics.Rect bottomOverlayRect = new Rect();
        private final RectF rect = new RectF();

        private final GradientDrawable topOverlayGradient;
        private final GradientDrawable bottomOverlayGradient;
        private final ValueAnimator animator;
        private final float[] animatorValues = new float[]{0f, 1f};
        private final Paint backgroundPaint;
        private final Paint barPaint;
        private final Paint selectedBarPaint;

        private final GradientDrawable[] pressedOverlayGradient = new GradientDrawable[2];
        private final boolean[] pressedOverlayVisible = new boolean[2];
        private final float[] pressedOverlayAlpha = new float[2];

        private boolean isOverlaysVisible;
        private float currentAnimationValue;
        private float alpha = 0f;
        private float[] alphas = null;
        private long lastTime;
        private float previousSelectedProgress;
        private int previousSelectedPotision = -1;
        private float currentProgress;
        private int selectedPosition;

        private float currentLoadingAnimationProgress;
        private int currentLoadingAnimationDirection = 1;

        public OverlaysView(Context context) {
            super(context);
            setVisibility(GONE);

            barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            barPaint.setColor(0x55ffffff);
            selectedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            selectedBarPaint.setColor(0xffffffff);

            topOverlayGradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0x42000000, 0});
            topOverlayGradient.setShape(GradientDrawable.RECTANGLE);

            bottomOverlayGradient = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0x42000000, 0x42000000, 0});
            bottomOverlayGradient.setShape(GradientDrawable.RECTANGLE);

            for (int i = 0; i < 2; i++) {
                final GradientDrawable.Orientation orientation = i == 0 ? GradientDrawable.Orientation.LEFT_RIGHT : GradientDrawable.Orientation.RIGHT_LEFT;
                pressedOverlayGradient[i] = new GradientDrawable(orientation, new int[]{0x32000000, 0});
                pressedOverlayGradient[i].setShape(GradientDrawable.RECTANGLE);
            }

            backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            backgroundPaint.setColor(Color.BLACK);
            backgroundPaint.setAlpha(66);
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(250);
            animator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
            animator.addUpdateListener(anim -> {
                float value = AndroidUtilities.lerp(animatorValues, currentAnimationValue = anim.getAnimatedFraction());
                setAlphaValue(value, true);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isOverlaysVisible) {
                        setVisibility(GONE);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    setVisibility(VISIBLE);
                }
            });
        }

        public void saveCurrentPageProgress() {
            previousSelectedProgress = currentProgress;
            previousSelectedPotision = selectedPosition;
            currentLoadingAnimationProgress = 0.0f;
            currentLoadingAnimationDirection = 1;
        }

        public void setAlphaValue(float value, boolean self) {
            if (Build.VERSION.SDK_INT > 18) {
                int alpha = (int) (255 * value);
                topOverlayGradient.setAlpha(alpha);
                bottomOverlayGradient.setAlpha(alpha);
                backgroundPaint.setAlpha((int) (66 * value));
                barPaint.setAlpha((int) (0x55 * value));
                selectedBarPaint.setAlpha(alpha);
                this.alpha = value;
            } else {
                setAlpha(value);
            }
            if (!self) {
                currentAnimationValue = value;
            }
            invalidate();
        }

        public boolean isOverlaysVisible() {
            return isOverlaysVisible;
        }

        public void setOverlaysVisible() {
            isOverlaysVisible = true;
            setVisibility(VISIBLE);
        }

        public void setOverlaysVisible(boolean overlaysVisible, float durationFactor) {
            if (overlaysVisible != isOverlaysVisible) {
                isOverlaysVisible = overlaysVisible;
                animator.cancel();
                final float value = AndroidUtilities.lerp(animatorValues, currentAnimationValue);
                if (overlaysVisible) {
                    animator.setDuration((long) ((1f - value) * 250f / durationFactor));
                } else {
                    animator.setDuration((long) (value * 250f / durationFactor));
                }
                animatorValues[0] = value;
                animatorValues[1] = overlaysVisible ? 1f : 0f;
                animator.start();
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            final int actionBarHeight = statusBarHeight + ActionBar.getCurrentActionBarHeight();
            final float k = 0.5f;
            topOverlayRect.set(0, 0, w, (int) (actionBarHeight * k));
            bottomOverlayRect.set(0, (int) (h - AndroidUtilities.dp(72f) * k), w, h);
            topOverlayGradient.setBounds(0, topOverlayRect.bottom, w, actionBarHeight + AndroidUtilities.dp(16f));
            bottomOverlayGradient.setBounds(0, h - AndroidUtilities.dp(72f * 2), w, bottomOverlayRect.top);
            pressedOverlayGradient[0].setBounds(0, 0, w / 5, h);
            pressedOverlayGradient[1].setBounds(w - (w / 5), 0, w, h);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 0; i < 2; i++) {
                if (pressedOverlayAlpha[i] > 0f) {
                    pressedOverlayGradient[i].setAlpha((int) (pressedOverlayAlpha[i] * 255));
                    pressedOverlayGradient[i].draw(canvas);
                }
            }

            topOverlayGradient.draw(canvas);
            bottomOverlayGradient.draw(canvas);
            canvas.drawRect(topOverlayRect, backgroundPaint);
            canvas.drawRect(bottomOverlayRect, backgroundPaint);

            int count = avatarsViewPager.getRealCount();
            selectedPosition = avatarsViewPager.getRealPosition();

            if (alphas == null || alphas.length != count) {
                alphas = new float[count];
                Arrays.fill(alphas, 0.0f);
            }

            boolean invalidate = false;

            long newTime = SystemClock.elapsedRealtime();
            long dt = (newTime - lastTime);
            if (dt < 0 || dt > 20) {
                dt = 17;
            }
            lastTime = newTime;

            if (count > 1 && count <= 20) {
                if (overlayCountVisible == 0) {
                    alpha = 0.0f;
                    overlayCountVisible = 3;
                } else if (overlayCountVisible == 1) {
                    alpha = 0.0f;
                    overlayCountVisible = 2;
                }
                if (overlayCountVisible == 2) {
                    barPaint.setAlpha((int) (0x55 * alpha));
                    selectedBarPaint.setAlpha((int) (0xff * alpha));
                }
                int width = (getMeasuredWidth() - AndroidUtilities.dp(5 * 2) - AndroidUtilities.dp(2 * (count - 1))) / count;
                int y = AndroidUtilities.dp(4) + (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
                for (int a = 0; a < count; a++) {
                    int x = AndroidUtilities.dp(5 + a * 2) + width * a;
                    float progress;
                    int baseAlpha = 0x55;
                    if (a == previousSelectedPotision && Math.abs(previousSelectedProgress - 1.0f) > 0.0001f) {
                        progress = previousSelectedProgress;
                        canvas.save();
                        canvas.clipRect(x + width * progress, y, x + width, y + AndroidUtilities.dp(2));
                        rect.set(x, y, x + width, y + AndroidUtilities.dp(2));
                        barPaint.setAlpha((int) (0x55 * alpha));
                        canvas.drawRoundRect(rect, AndroidUtilities.dp(1), AndroidUtilities.dp(1), barPaint);
                        baseAlpha = 0x50;
                        canvas.restore();
                        invalidate = true;
                    } else if (a == selectedPosition) {
                        if (avatarsViewPager.isCurrentItemVideo()) {
                            progress = currentProgress = avatarsViewPager.getCurrentItemProgress();
                            if (progress <= 0 && avatarsViewPager.isLoadingCurrentVideo() || currentLoadingAnimationProgress > 0.0f) {
                                currentLoadingAnimationProgress += currentLoadingAnimationDirection * dt / 500.0f;
                                if (currentLoadingAnimationProgress > 1.0f) {
                                    currentLoadingAnimationProgress = 1.0f;
                                    currentLoadingAnimationDirection *= -1;
                                } else if (currentLoadingAnimationProgress <= 0) {
                                    currentLoadingAnimationProgress = 0.0f;
                                    currentLoadingAnimationDirection *= -1;
                                }
                            }
                            rect.set(x, y, x + width, y + AndroidUtilities.dp(2));
                            barPaint.setAlpha((int) ((0x55 + 0x30 * currentLoadingAnimationProgress) * alpha));
                            canvas.drawRoundRect(rect, AndroidUtilities.dp(1), AndroidUtilities.dp(1), barPaint);
                            invalidate = true;
                            baseAlpha = 0x50;
                        } else {
                            progress = currentProgress = 1.0f;
                        }
                    } else {
                        progress = 1.0f;
                    }
                    rect.set(x, y, x + width * progress, y + AndroidUtilities.dp(2));

                    if (a != selectedPosition) {
                        if (overlayCountVisible == 3) {
                            barPaint.setAlpha((int) (AndroidUtilities.lerp(baseAlpha, 0xff, CubicBezierInterpolator.EASE_BOTH.getInterpolation(alphas[a])) * alpha));
                        }
                    } else {
                        alphas[a] = 0.75f;
                    }

                    canvas.drawRoundRect(rect, AndroidUtilities.dp(1), AndroidUtilities.dp(1), a == selectedPosition ? selectedBarPaint : barPaint);
                }

                if (overlayCountVisible == 2) {
                    if (alpha < 1.0f) {
                        alpha += dt / 180.0f;
                        if (alpha > 1.0f) {
                            alpha = 1.0f;
                        }
                        invalidate = true;
                    } else {
                        overlayCountVisible = 3;
                    }
                } else if (overlayCountVisible == 3) {
                    for (int i = 0; i < alphas.length; i++) {
                        if (i != selectedPosition && alphas[i] > 0.0f) {
                            alphas[i] -= dt / 500.0f;
                            if (alphas[i] <= 0.0f) {
                                alphas[i] = 0.0f;
                                if (i == previousSelectedPotision) {
                                    previousSelectedPotision = -1;
                                }
                            }
                            invalidate = true;
                        } else if (i == previousSelectedPotision) {
                            previousSelectedPotision = -1;
                        }
                    }
                }
            }

            for (int i = 0; i < 2; i++) {
                if (pressedOverlayVisible[i]) {
                    if (pressedOverlayAlpha[i] < 1f) {
                        pressedOverlayAlpha[i] += dt / 180.0f;
                        if (pressedOverlayAlpha[i] > 1f) {
                            pressedOverlayAlpha[i] = 1f;
                        }
                        invalidate = true;
                    }
                } else {
                    if (pressedOverlayAlpha[i] > 0f) {
                        pressedOverlayAlpha[i] -= dt / 180.0f;
                        if (pressedOverlayAlpha[i] < 0f) {
                            pressedOverlayAlpha[i] = 0f;
                        }
                        invalidate = true;
                    }
                }
            }

            if (invalidate) {
                postInvalidateOnAnimation();
            }
        }

        @Override
        public void onDown(boolean left) {
            pressedOverlayVisible[left ? 0 : 1] = true;
            postInvalidateOnAnimation();
        }

        @Override
        public void onRelease() {
            Arrays.fill(pressedOverlayVisible, false);
            postInvalidateOnAnimation();
        }

        @Override
        public void onPhotosLoaded() {
            updateProfileData(false);
        }

        @Override
        public void onVideoSet() {
            invalidate();
        }
    }

    public final AvatarImageView avatarImage;
    public final AvatarDrawable avatarDrawable;
    public final ProfileGalleryView avatarsViewPager;
    public final ProfileGiftsView giftsView;
    public final FrameLayout avatarExpandAnimationContainer;
    public final FrameLayout avatarScrollAnimationContainer;
    public final FrameLayout avatarsPagerContainer;
    final private RLottieImageView collapseMaskView;
    final private ProfileActionButtonsBar buttonsBar;

    final private ProfileBackgroundView background;

    final private ProfileActivityDataProvider dataProvider;

    final private int currentAccount;

    private ValueAnimator expandingAnimator;
    private ValueAnimator mediaPresenterAnimator;
    public  boolean isExpanded = false;
    private float currentExpandingProgress = 0;
    private float listViewOffset = 0;
    private int avatarCornerRadius;

    final private ProfileActivityV2.HeaderSizeConfiguration configuration;

    final private float avatarTopMargin = AndroidUtilities.statusBarHeight + AndroidUtilities.dp(12);

    private ProfileActivityV2 fragment;
    
    public ProfileStoriesView storyView;
    private OverlaysView overlaysView;

    public ProfileInfoView infoView;

    public TLRPC.FileLocation avatar;
    public TLRPC.FileLocation avatarBig;
    public ImageLocation uploadingImageLocation;
    private ImageLocation prevLoadedImageLocation;
    public boolean hasFallbackPhoto;
    private boolean hasCustomPhoto;
    private ImageReceiver fallbackImage;

    HashSet<Integer> notificationsExceptionTopics = new HashSet<>();

    public ProfileHeaderView(
            @NonNull Context context,
            ProfileActivityV2.HeaderSizeConfiguration configuration,
            ProfileActivityDataProvider dataProvider,
            ProfileActivityV2 fragment
    ) {
        super(context);

        this.configuration = configuration;
        this.dataProvider = dataProvider;
        this.fragment = fragment;

        if (ChatObject.isForum(dataProvider.getChat())) {
            this.avatarCornerRadius = AndroidUtilities.dp(20);
        } else {
            this.avatarCornerRadius = AndroidUtilities.dp(45);
        }

        currentAccount = UserConfig.selectedAccount;
        avatarDrawable = new AvatarDrawable();

        avatarImage = new AvatarImageView(context) {
            @Override
            public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(info);
                if (getImageReceiver().hasNotThumb()) {
                    info.setText(LocaleController.getString(R.string.AccDescrProfilePicture));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, LocaleController.getString(R.string.Open)));
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_LONG_CLICK, LocaleController.getString(R.string.AccDescrOpenInPhotoViewer)));
                    }
                } else {
                    info.setVisibleToUser(false);
                }
            }

            @Override
            protected void dispatchDraw(@NonNull Canvas canvas) {
                super.dispatchDraw(canvas);
                if (animatedEmojiDrawable != null && animatedEmojiDrawable.getImageReceiver() != null) {
                    animatedEmojiDrawable.getImageReceiver().startAnimation();
                }
            }
        };
        avatarImage.getImageReceiver().setAllowDecodeSingleFrame(true);
        avatarImage.setRoundRadius(avatarCornerRadius);
        avatarImage.setPivotX(AndroidUtilities.dp(45));
        avatarImage.setPivotY(0);
        avatarImage.setHasStories(dataProvider.needInsetForStories());

        overlaysView = new OverlaysView(context);
        overlaysView.setOverlaysVisible(false, 1);
        avatarsViewPager = new ProfileGalleryView(
                context,
                dataProvider.getDialogId(),
                fragment.getActionBar(),
                fragment.scrollController.listView,
                avatarImage,
                fragment.getClassGuid(),
                overlaysView
        );
        avatarsViewPager.setPivotX(AndroidUtilities.dp(45));
        avatarsViewPager.setPivotY(0);

        avatarImage.setAvatarsViewPager(avatarsViewPager);
        avatarsViewPager.setVisibility(View.GONE);

        avatarsPagerContainer = new FrameLayout(context) {
            float lastY;

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
//                return infoView.dispatchTouchEvent(ev);
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    lastY = ev.getY();
                }

                ev.setLocation(ev.getX(), lastY);
                return super.dispatchTouchEvent(ev);
            }
        };
        avatarsPagerContainer.setClipChildren(true);
        avatarsPagerContainer.setClipToPadding(false);
        avatarsPagerContainer.setPivotX(AndroidUtilities.dp(45));
        avatarsPagerContainer.setPivotY(0);
        avatarsPagerContainer.addView(avatarsViewPager, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
        avatarsPagerContainer.setEnabled(false);

        avatarExpandAnimationContainer = new FrameLayout(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);

                if (dataProvider.isMyProfile || fragment.isSettings()) { return; }

                int saveCount = canvas.save();

                canvas.clipRect(0, 0, getWidth(), getWidth() * avatarsPagerContainer.getScaleX() + AndroidUtilities.dp(72) * currentExpandingProgress);

                canvas.translate(avatarExpandAnimationContainer.getWidth() / 2f, 0);
                canvas.scale(avatarsPagerContainer.getScaleX(), -avatarsPagerContainer.getScaleX());
                canvas.translate(-avatarExpandAnimationContainer.getWidth() / 2f, 0);

                canvas.translate(0, -getWidth() * 2 + 1);
                avatarImage.draw(canvas);
                avatarsPagerContainer.draw(canvas);
                canvas.translate(0, getWidth() * 2 - 1);

                canvas.translate(avatarExpandAnimationContainer.getWidth() / 2f, 0);
                canvas.scale(1 / avatarsPagerContainer.getScaleX(), -1 / avatarsPagerContainer.getScaleX());
                canvas.translate(-avatarExpandAnimationContainer.getWidth() / 2f, 0);

                canvas.restoreToCount(saveCount);
            }
        };
        avatarExpandAnimationContainer.addView(avatarImage, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
        avatarExpandAnimationContainer.addView(avatarsPagerContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
        avatarExpandAnimationContainer.setClipChildren(false);
        avatarExpandAnimationContainer.setClipToPadding(false);
        avatarExpandAnimationContainer.setPivotX(AndroidUtilities.dp(45));
        avatarExpandAnimationContainer.setPivotY(0);
        avatarExpandAnimationContainer.setBackgroundColor(Color.TRANSPARENT);
        avatarScrollAnimationContainer = new FrameLayout(context);
        avatarScrollAnimationContainer.addView(avatarExpandAnimationContainer, LayoutHelper.createFrame(90, 90, Gravity.CENTER_HORIZONTAL | Gravity.TOP));
        avatarScrollAnimationContainer.setClipChildren(false);
        avatarScrollAnimationContainer.setClipToPadding(false);

        storyView = new ProfileStoriesView(context, currentAccount, dataProvider.getDialogId(), dataProvider.isTopic, avatarExpandAnimationContainer, avatarImage, fragment.getResourceProvider()) {
            @Override
            protected void onTap(StoryViewer.PlaceProvider provider) {
                long did = dataProvider.getDialogId();
                StoriesController storiesController = dataProvider.getMessagesController().getStoriesController();
                if (storiesController.hasStories(did) || storiesController.hasUploadingStories(did) || storiesController.isLastUploadingFailed(did)) {
                    fragment.getOrCreateStoryViewer().open(context, did, provider);
                } else {
                    TLRPC.UserFull userFull = dataProvider.getUserFull();
                    TLRPC.ChatFull chatFull = dataProvider.getChatFull();
                    if (userFull != null && userFull.stories != null && !userFull.stories.stories.isEmpty() && dataProvider.userId != fragment.getUserConfig().clientUserId) {
                        fragment.getOrCreateStoryViewer().open(context, userFull.stories, provider);
                    } else if (chatFull != null && chatFull.stories != null && !chatFull.stories.stories.isEmpty()) {
                        fragment.getOrCreateStoryViewer().open(context, chatFull.stories, provider);
                    } else {
                        fragment.expandAvatar();
                    }
                }
            }

            @Override
            protected void onLongPress() {
                fragment.openAvatar();
            }
        };

        avatarScrollAnimationContainer.addView(storyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        background = new ProfileBackgroundView(context, configuration, dataProvider);
        addView(background, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, configuration.defaultHeight / AndroidUtilities.dp(1), Gravity.TOP | Gravity.LEFT));

        giftsView = new ProfileGiftsView(context, fragment.getCurrentAccount(), dataProvider.getDialogId(), configuration);
        addView(giftsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, configuration.defaultHeight / AndroidUtilities.dp(1), Gravity.TOP | Gravity.LEFT));

        collapseMaskView = new RLottieImageView(context);
        collapseMaskView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        collapseMaskView.setAnimation(R.raw.profile_avatar_collapse, 153, 153);
        addView(
                collapseMaskView,
                LayoutHelper.createFrame(153, 153, Gravity.TOP | Gravity.CENTER_HORIZONTAL)
        );

        addView(avatarScrollAnimationContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, configuration.expandedHeight / AndroidUtilities.dp(1), Gravity.TOP | Gravity.CENTER_HORIZONTAL));

        addView(overlaysView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));

        this.setClipChildren(false);
        this.setClipToPadding(false);

        avatarScrollAnimationContainer.setTranslationY(avatarTopMargin);

        avatarImage.setOnClickListener(view -> {
            fragment.expandAvatar();
        });

        avatarImage.setOnLongClickListener(view -> {
            fragment.openAvatar();
            return true;
        });

        buttonsBar = new ProfileActionButtonsBar(context, () -> this);
        addView(buttonsBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));

        final FrameLayout.LayoutParams buttonsBarParams = (FrameLayout.LayoutParams) buttonsBar.getLayoutParams();
        buttonsBarParams.topMargin = (int) configuration.defaultHeight - AndroidUtilities.dp(102);
        buttonsBar.setLayoutParams(buttonsBarParams);
        setupActionButtons();

        if (dataProvider.isMyProfile || fragment.isSettings()) {
            buttonsBar.setVisibility(View.GONE);
        }

        infoView = new ProfileInfoView(context, dataProvider, configuration, fragment);
        addView(infoView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL));

        updateProfileData(true);
    }

    public void updateMediaPresenting(boolean isPresenting) {
        if (mediaPresenterAnimator != null) {
            mediaPresenterAnimator.cancel();
        }

        mediaPresenterAnimator = ValueAnimator.ofFloat(isPresenting ? 0 : 1, isPresenting ? 1 : 0);
        mediaPresenterAnimator.setDuration(ProfileActivityV2.expandingAnimationDuration);
        mediaPresenterAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        mediaPresenterAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            background.setTranslationY(-configuration.minimizedHeight * value);

        });
        mediaPresenterAnimator.start();
    }

    private void setupActionButtons() {
        ArrayList<ProfileActionButtonsBar.Button.Model> buttons = new ArrayList<>();

        ProfileActionButtonsBar.Button.Model shareButtonModel = new ProfileActionButtonsBar.Button.Model(
                ResourcesCompat.getDrawable(getResources(), R.drawable.share, null),
            LocaleController.getString(R.string.BotShare),
            (view, x, y) -> {
                fragment.shareChat();
            }
        );

        if (dataProvider.userId != 0 && !dataProvider.isMyProfile) {
            TLRPC.User user = dataProvider.getUser();
            TLRPC.UserFull userInfo = dataProvider.getUserFull();

            buttons.add(
                new ProfileActionButtonsBar.Button.Model(
                    ResourcesCompat.getDrawable(getResources(), R.drawable.message, null),
                    LocaleController.getString(R.string.Message),
                    (view, x, y) -> {
                        fragment.startChat();
                    }
                )
            );

            boolean isMuted = fragment.getMessagesController().isDialogMuted(dataProvider.getDialogId(), dataProvider.topicId);
            buttons.add(
                    new ProfileActionButtonsBar.Button.Model(
                            ResourcesCompat.getDrawable(
                                    getResources(),
                                    isMuted ? R.drawable.unmute : R.drawable.mute,
                                    null
                            ),
                            LocaleController.getString(isMuted ? R.string.Unmute : R.string.Mute),
                            this::presentNotificationsMenu
                    )
            );

            if (dataProvider.isBot()) {
                buttons.add(shareButtonModel);
            }

            if (userInfo != null && userInfo.phone_calls_available) {
                buttons.add(
                    new ProfileActionButtonsBar.Button.Model(
                        ResourcesCompat.getDrawable(
                            getResources(),
                            R.drawable.call,
                            null
                        ),
                        LocaleController.getString(R.string.Call),
                        (view, x, y) -> {
                            if (user != null) {
                                VoIPHelper.startCall(user, false, userInfo != null && userInfo.video_calls_available, fragment.getParentActivity(), userInfo, fragment.getAccountInstance());
                            }
                        }
                    )
                );
            }

            if (userInfo != null && userInfo.video_calls_available) {
                buttons.add(
                        new ProfileActionButtonsBar.Button.Model(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        R.drawable.video,
                                        null
                                ),
                                LocaleController.getString(R.string.VideoCall),
                                (view, x, y) -> {
                                    if (user != null) {
                                        VoIPHelper.startCall(user, true, userInfo != null && userInfo.video_calls_available, fragment.getParentActivity(), userInfo, fragment.getAccountInstance());
                                    }
                                }
                        )
                );
            }

            if (!BuildVars.IS_BILLING_UNAVAILABLE && !dataProvider.getMessagesController().premiumPurchaseBlocked() && !dataProvider.isBot()) {
                StarsController.getInstance(currentAccount).loadStarGifts();
                buttons.add(sendGiftButton());
            }

            if (dataProvider.isBot() && !dataProvider.userBlocked()) {
                buttons.add(
                        new ProfileActionButtonsBar.Button.Model(
                                ResourcesCompat.getDrawable(getResources(), R.drawable.block, null),
                                LocaleController.getString(R.string.BlockBot),
                                (view, x, y) -> {
                                    fragment.blockUser();
                                }
                        )
                );
            }
        } else if (dataProvider.chatId != 0){
            TLRPC.Chat chat = dataProvider.getChat();
            TLRPC.ChatFull chatInfo = dataProvider.getChatFull();

            if (chat.left && !chat.kicked) {
                long requestedTime = MessagesController.getNotificationsSettings(currentAccount).getLong("dialog_join_requested_time_" + dataProvider.dialogId, -1);
                if (!(requestedTime > 0 && System.currentTimeMillis() - requestedTime < 1000 * 60 * 2)) {
                    buttons.add(
                        new ProfileActionButtonsBar.Button.Model(
                            ResourcesCompat.getDrawable(getResources(), R.drawable.join, null),
                            chat.megagroup ? LocaleController.getString(R.string.ProfileJoinGroup) : LocaleController.getString(R.string.ProfileJoinChannel),
                            (view, x ,y) -> {
                                fragment.addToChannel();
                            }
                        )
                    );
                }
            }

            if (!chat.left && !chat.kicked && chatInfo != null && (chatInfo.linked_chat_id != 0 || dataProvider.isTopic)) {
                buttons.add(
                        new ProfileActionButtonsBar.Button.Model(
                                ResourcesCompat.getDrawable(getResources(), R.drawable.message, null),
                                LocaleController.getString(R.string.Discussion),
                                (view, x, y) -> {
                                    if (dataProvider.isTopic) {
                                        fragment.openTopic(dataProvider.chatId, dataProvider.topicId);
                                    } else {
                                        fragment.openDiscussion();
                                    }
                                }
                        )
                );
            }

            if (!chat.left && !chat.kicked) {
                boolean isMuted = fragment.getMessagesController().isDialogMuted(dataProvider.getDialogId(), dataProvider.topicId);
                buttons.add(
                        new ProfileActionButtonsBar.Button.Model(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        isMuted ? R.drawable.unmute : R.drawable.mute,
                                        null
                                ),
                                LocaleController.getString(isMuted ? R.string.Unmute : R.string.Mute),
                                this::presentNotificationsMenu
                        )
                );
            }

            if (!BuildVars.IS_BILLING_UNAVAILABLE && !fragment.getMessagesController().premiumPurchaseBlocked() && chatInfo != null && chatInfo.stargifts_available) {
                StarsController.getInstance(currentAccount).loadStarGifts();
                buttons.add(sendGiftButton());
            }

            if (ChatObject.isPublic(chat)) {
                buttons.add(shareButtonModel);
            }


            if (!chat.left && !chat.kicked) {
                if(ChatObject.canManageCalls(chat) && chatInfo != null && chatInfo.call == null) {
                    buttons.add(
                            new ProfileActionButtonsBar.Button.Model(
                                ResourcesCompat.getDrawable(getResources(), R.drawable.live_stream, null),
                                chat.megagroup && !chat.gigagroup ? LocaleController.getString(R.string.StartVoipChat) : LocaleController.getString(R.string.StartVoipChannel),
                            (view, x, y) -> {
                                ChatObject.Call call = fragment.getMessagesController().getGroupCall(dataProvider.chatId, false);
                                if (call == null) {
                                    VoIPHelper.showGroupCallAlert(fragment, chat, null, false, fragment.getAccountInstance());
                                } else {
                                    VoIPHelper.startCall(chat, null, null, false, fragment.getParentActivity(), fragment, fragment.getAccountInstance());
                                }
                            }
                        )
                    );
                }

                StoriesController storiesController = dataProvider.getMessagesController().getStoriesController();
                if (storiesController.canPostStories(dataProvider.getDialogId()) && (chat.creator || chat.admin_rights != null && chat.admin_rights.edit_stories)) {
                    buttons.add(
                            new ProfileActionButtonsBar.Button.Model(
                                    ResourcesCompat.getDrawable(getResources(), R.drawable.story, null),
                                    LocaleController.getString(R.string.Story),
                                    (view, x, y) -> {
                                        StoryRecorder.getInstance(fragment.getParentActivity(), fragment.getCurrentAccount()).open(null);
                                    }
                            )
                    );
                }


                if (!dataProvider.isTopic && !chat.creator) {
                    buttons.add(
                            new ProfileActionButtonsBar.Button.Model(
                                    ResourcesCompat.getDrawable(getResources(), R.drawable.leave, null),
                                    LocaleController.getString(R.string.LeaveChannel),
                                    (view, x, y) -> {
                                        fragment.leaveChat();
                                    }
                            )
                    );
                }
            }

            if (chat.left && !chat.kicked) {
                buttons.add(
                        new ProfileActionButtonsBar.Button.Model(
                                ResourcesCompat.getDrawable(getResources(), R.drawable.report, null),
                                LocaleController.getString(R.string.Report2),
                                (view, x, y) -> {
                                    ReportBottomSheet.openChat(fragment, dataProvider.getDialogId());
                                }
                        )
                );
            }
        }

        while(buttons.size() > 4) {
            buttons.remove(buttons.size() - 1);
        }

        buttonsBar.updateButtons(buttons, isExpanded);
    }

    private ProfileActionButtonsBar.Button.Model sendGiftButton() {
        return new ProfileActionButtonsBar.Button.Model(
                ResourcesCompat.getDrawable(getResources(), R.drawable.gift, null),
                LocaleController.getString(R.string.ActionStarGift),
                (view, x, y) -> {
                    if (UserObject.areGiftsDisabled(dataProvider.getUserFull())) {
                        BaseFragment lastFragment = LaunchActivity.getSafeLastFragment();
                        if (lastFragment != null) {
                            BulletinFactory.of(lastFragment).createSimpleBulletin(R.raw.error, AndroidUtilities.replaceTags(LocaleController.formatString(R.string.UserDisallowedGifts, DialogObject.getShortName(dataProvider.getDialogId())))).show();
                        }
                        return;
                    }
                    if (dataProvider.getChat() != null) {
                        MessagesController.getGlobalMainSettings().edit().putInt("channelgifthint", 3).apply();
                    }
                    fragment.showDialog(new GiftSheet(getContext(), currentAccount, dataProvider.getDialogId(), null, null));
                }
        );
    }

    private void presentNotificationsMenu(View view, int x, int y) {
        ChatNotificationsPopupWrapper chatNotificationsPopupWrapper = new ChatNotificationsPopupWrapper(fragment.getContext(), currentAccount, null, true, true, new ChatNotificationsPopupWrapper.Callback() {
            @Override
            public void toggleSound() {
                SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                boolean enabled = !preferences.getBoolean("sound_enabled_" + NotificationsController.getSharedPrefKey(dataProvider.getDialogId(), dataProvider.topicId), true);
                preferences.edit().putBoolean("sound_enabled_" + NotificationsController.getSharedPrefKey(dataProvider.getDialogId(), dataProvider.topicId), enabled).apply();
                if (BulletinFactory.canShowBulletin(fragment)) {
                    BulletinFactory.createSoundEnabledBulletin(fragment, enabled ? NotificationsController.SETTING_SOUND_ON : NotificationsController.SETTING_SOUND_OFF, fragment.getResourceProvider()).show();
                }
            }

            @Override
            public void muteFor(int timeInSeconds) {
                if (timeInSeconds == 0) {
                    if (fragment.getMessagesController().isDialogMuted(dataProvider.getDialogId(), dataProvider.topicId)) {
                        toggleMute();
                    }
                    if (BulletinFactory.canShowBulletin(fragment)) {
                        BulletinFactory.createMuteBulletin(fragment, NotificationsController.SETTING_MUTE_UNMUTE, timeInSeconds, fragment.getResourceProvider()).show();
                    }
                } else {
                    dataProvider.getNotificationsController().muteUntil(dataProvider.getDialogId(), dataProvider.topicId, timeInSeconds);
                    if (BulletinFactory.canShowBulletin(fragment)) {
                        BulletinFactory.createMuteBulletin(fragment, NotificationsController.SETTING_MUTE_CUSTOM, timeInSeconds, fragment.getResourceProvider()).show();
                    }
                    updateExceptions();
                }
            }

            @Override
            public void showCustomize() {
                if (dataProvider.getDialogId() != 0) {
                    Bundle args = new Bundle();
                    args.putLong("dialog_id", dataProvider.getDialogId());
                    args.putLong("topic_id", dataProvider.topicId);
                    fragment.presentFragment(new ProfileNotificationsActivity(args, fragment.getResourceProvider()));
                }
            }

            @Override
            public void toggleMute() {
                boolean muted = dataProvider.getMessagesController().isDialogMuted(dataProvider.getDialogId(), dataProvider.topicId);
                dataProvider.getNotificationsController().muteDialog(dataProvider.getDialogId(), dataProvider.topicId, !muted);
                if (fragment.fragmentView != null) {
                    BulletinFactory.createMuteBulletin(fragment, !muted, null).show();
                }
                updateExceptions();
            }

            @Override
            public void openExceptions() {
                Bundle bundle = new Bundle();
                bundle.putLong("dialog_id", dataProvider.getDialogId());
                TopicsNotifySettingsFragments notifySettings = new TopicsNotifySettingsFragments(bundle);
                notifySettings.setExceptions(notificationsExceptionTopics);
                fragment.presentFragment(notifySettings);
            }
        }, fragment.getResourceProvider());
        chatNotificationsPopupWrapper.update(dataProvider.getDialogId(), dataProvider.topicId, notificationsExceptionTopics);
        if (AndroidUtilities.isTablet()) {
//            View v = parentLayout.getView();
//            x += v.getX() + v.getPaddingLeft();
//            y += v.getY() + v.getPaddingTop();
        }
        chatNotificationsPopupWrapper.showAsOptions(fragment, view, x, y);

    }

    private void updateExceptions() {
        if (!dataProvider.isTopic && ChatObject.isForum(dataProvider.getChat())) {
            dataProvider.getNotificationsController().loadTopicsNotificationsExceptions(-dataProvider.chatId, (topics) -> {
                ArrayList<Integer> arrayList = new ArrayList<>(topics);
                for (int i = 0; i < arrayList.size(); i++) {
                    if (dataProvider.getMessagesController().getTopicsController().findTopic(dataProvider.chatId, arrayList.get(i)) == null) {
                        arrayList.remove(i);
                        i--;
                    }
                }
                notificationsExceptionTopics.clear();
                notificationsExceptionTopics.addAll(arrayList);
            });
        }

        setupActionButtons();
    }

    public void udpateHeight(float height) {
        avatarScrollAnimationContainer.setPivotX(getWidth() / 2f);
        avatarScrollAnimationContainer.setPivotY(AndroidUtilities.dp(45));

        LayoutParams overlayLayoutParams = (LayoutParams) overlaysView.getLayoutParams();
        overlayLayoutParams.height = (int) height;
        overlaysView.setLayoutParams(overlayLayoutParams);

        float progress = (height - listViewOffset - configuration.minimizedHeight) / (configuration.defaultHeight - configuration.minimizedHeight);

        updateAvatarState(progress);
        infoView.updateTitleState(progress);
        infoView.updateSubtitleState(progress);
        giftsView.setExpandProgress(1 - progress);

        final ViewGroup.LayoutParams params = background.getLayoutParams();
        params.height = (int) height;
        background.setLayoutParams(params);

        buttonsBar.setTranslationY(height - configuration.defaultHeight);
        if (progress < 0.75) {
            buttonsBar.updateCollapseProgress((0.75f - progress) * 2f);
        } else {
            buttonsBar.updateCollapseProgress(0);
        }
        buttonsBar.invalidate();

        updateStoriesViewBounds(false);
        storyView.setExpandCoords(getWidth() - AndroidUtilities.dp(12 + 48 + 12), false, AndroidUtilities.statusBarHeight + AndroidUtilities.dp(16 + 12));
    }

    public void startAnimation(boolean isExpanding) {
        if (isExpanding) {
            expandHeader();
            infoView.expandTitle();
            infoView.expandSubtitle();
        } else {
            collapseHeader();
            infoView.collapseTitle();
            infoView.collapseSubtitle();
        }

        buttonsBar.updateState(isExpanding);

        updateProfileData(false);
    }

    private void expandHeader() {
        if (this.getWidth() == 0) { return; }
        isExpanded = true;

        if (expandingAnimator != null) {
            expandingAnimator.cancel();
        }

        if (avatarsViewPager.hasImages()) {
            avatarsViewPager.setClickable(true);
            avatarsViewPager.setFocusable(true);

            avatarImage.setVisibility(View.GONE);
            avatarsViewPager.setVisibility(View.VISIBLE);
            avatarsViewPager.setCreateThumbFromParent(true);

            overlaysView.setOverlaysVisible(true, 1);
            overlaysView.setOverlaysVisible();
        }

        float maxOffsetY = avatarScrollAnimationContainer.getTop();
        float containerTranslation = avatarScrollAnimationContainer.getTranslationY();
        float containerScale = avatarScrollAnimationContainer.getScaleX();

        expandingAnimator = ValueAnimator.ofFloat(currentExpandingProgress, 1);
        expandingAnimator.setDuration(ProfileActivityV2.expandingAnimationDuration);
        expandingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        expandingAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            currentExpandingProgress = value;
            FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) avatarExpandAnimationContainer.getLayoutParams();
            layout.width = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth(), value);
            layout.height = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth() + AndroidUtilities.dp(72), value);
            avatarExpandAnimationContainer.setLayoutParams(layout);
            avatarExpandAnimationContainer.setTranslationY(AndroidUtilities.lerp(0, -maxOffsetY, value));

            FrameLayout.LayoutParams layout2 = (FrameLayout.LayoutParams) avatarsPagerContainer.getLayoutParams();
            layout2.width = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth(), value);
            layout2.height = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth(), value);
            avatarsPagerContainer.setLayoutParams(layout2);

            FrameLayout.LayoutParams layout3 = (FrameLayout.LayoutParams) avatarImage.getLayoutParams();
            layout3.width = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth(), value);
            layout3.height = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth(), value);
            avatarImage.setLayoutParams(layout3);

            avatarScrollAnimationContainer.setScaleX(AndroidUtilities.lerp(containerScale, 1, value));
            avatarScrollAnimationContainer.setScaleY(AndroidUtilities.lerp(containerScale, 1, value));
            avatarScrollAnimationContainer.setTranslationY(AndroidUtilities.lerp(containerTranslation, 0, value));

            avatarImage.setRoundRadius(AndroidUtilities.lerp(avatarCornerRadius, 0, value));
            avatarsViewPager.setRoundRadius(
                    AndroidUtilities.lerp(avatarCornerRadius, 0, value),
                    AndroidUtilities.lerp(avatarCornerRadius, 0, value)
            );

            setStoryViewExpandProgress(value);

            listViewOffset = (configuration.expandedHeight - configuration.defaultHeight) * value;
        });
        expandingAnimator.start();
    }

    private void collapseHeader() {
        isExpanded = false;

        if (expandingAnimator != null) {
            expandingAnimator.cancel();
        }

        avatarsViewPager.setClickable(false);
        avatarsViewPager.setFocusable(false);

        avatarImage.setVisibility(View.VISIBLE);
        avatarsViewPager.resetCurrentItem();
        avatarsViewPager.setVisibility(View.INVISIBLE);
        avatarsViewPager.setCreateThumbFromParent(false);
        overlaysView.setOverlaysVisible(false, 10);

        expandingAnimator = ValueAnimator.ofFloat(currentExpandingProgress, 0);
        expandingAnimator.setDuration(ProfileActivityV2.expandingAnimationDuration);
        expandingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        expandingAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            currentExpandingProgress = value;
            FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) avatarExpandAnimationContainer.getLayoutParams();
            layout.width = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth(), value);
            layout.height = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth() + + AndroidUtilities.dp(72), value);
            avatarExpandAnimationContainer.setLayoutParams(layout);
            avatarExpandAnimationContainer.setTranslationY(AndroidUtilities.lerp(0, -avatarTopMargin, value));

            FrameLayout.LayoutParams layout2 = (FrameLayout.LayoutParams) avatarsPagerContainer.getLayoutParams();
            layout2.width = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth(), value);
            layout2.height = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth(), value);
            avatarsPagerContainer.setLayoutParams(layout2);

            FrameLayout.LayoutParams layout3 = (FrameLayout.LayoutParams) avatarImage.getLayoutParams();
            layout3.width = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth(), value);
            layout3.height = AndroidUtilities.lerp(AndroidUtilities.dp(90), getWidth(), value);
            avatarImage.setLayoutParams(layout3);

            avatarImage.setRoundRadius(AndroidUtilities.lerp(avatarCornerRadius, 0, value));
            avatarsViewPager.setRoundRadius(
                    AndroidUtilities.lerp(avatarCornerRadius, 0, value),
                    AndroidUtilities.lerp(avatarCornerRadius, 0, value)
            );

            setStoryViewExpandProgress(value);

            listViewOffset = (configuration.expandedHeight - configuration.defaultHeight) * value;
        });
        expandingAnimator.start();
    }

    private void updateAvatarState(float progress) {
        float startCollapseTopOffset = 25.4f;
        float maskAnimationStart = ((configuration.defaultHeight - configuration.minimizedHeight) - avatarTopMargin + AndroidUtilities.dp(startCollapseTopOffset)) / (configuration.defaultHeight - configuration.minimizedHeight);

        float collapsingProgress = (maskAnimationStart - progress) / maskAnimationStart;

        if (!isExpanded && collapsingProgress < 0) {
            avatarScrollAnimationContainer.setTranslationY(avatarTopMargin + AndroidUtilities.lerp(0, configuration.defaultHeight - configuration.minimizedHeight, (progress - 1)));
        }

        if (progress > 1) {
            if (!isExpanded) {
                avatarScrollAnimationContainer.setScaleX(AndroidUtilities.lerp(1f, 1.5f, progress - 1));
                avatarScrollAnimationContainer.setScaleY(AndroidUtilities.lerp(1f, 1.5f, progress - 1));
            } else {
                float maxProgress = AndroidUtilities.dp (44) / (configuration.defaultHeight - configuration.minimizedHeight);

                float scaleFactor = (getWidth() + AndroidUtilities.dp(44)) / (float) (getWidth() + AndroidUtilities.dp(0));
                float scaleFactor2 = (getWidth() + AndroidUtilities.dp(72 + 44)) / (float) (getWidth() + AndroidUtilities.dp(72));

                avatarsPagerContainer.setPivotX(avatarsPagerContainer.getWidth() / 2f);

                avatarImage.setScaleX(AndroidUtilities.lerp(1, scaleFactor, Math.max(0, (progress - 1) / maxProgress)));
                avatarImage.setScaleY(AndroidUtilities.lerp(1, scaleFactor, Math.max(0, (progress - 1) / maxProgress)));
                avatarsPagerContainer.setScaleX(AndroidUtilities.lerp(1, scaleFactor, Math.max(0, (progress - 1) / maxProgress)));
                avatarsPagerContainer.setScaleY(AndroidUtilities.lerp(1, scaleFactor, Math.max(0, (progress - 1) / maxProgress)));
                avatarExpandAnimationContainer.invalidate();
            }
        } else {
            avatarImage.setScaleX(1f);
            avatarImage.setScaleY(1f);
            avatarsPagerContainer.setScaleX(1f);
            avatarsPagerContainer.setScaleY(1f);

        }

        if (collapsingProgress >= 0) {
            collapseMaskView.setAlpha(collapsingProgress == 1f ? 0f : 1f);
            collapseMaskView.setProgress(collapsingProgress);

            avatarScrollAnimationContainer.setTranslationY(
                avatarTopMargin +
                AndroidUtilities.lerp(
                    -(avatarTopMargin - AndroidUtilities.dp(startCollapseTopOffset)),
                    -(avatarTopMargin + AndroidUtilities.dp(90 - startCollapseTopOffset)),
                    collapsingProgress
                )
            );

            avatarScrollAnimationContainer.setScaleX(AndroidUtilities.lerp(1, 0.2f, collapsingProgress));
            avatarScrollAnimationContainer.setScaleY(AndroidUtilities.lerp(1, 0.2f, collapsingProgress));
            avatarImage.setAlpha(AndroidUtilities.lerp(1f, 0f, collapsingProgress));

            if (ChatObject.isForum(dataProvider.getChat())) {
                avatarImage.setRoundRadius(
                        AndroidUtilities.lerp(avatarCornerRadius, AndroidUtilities.dp(45), Math.min(1, collapsingProgress * 4))
                );
            }
        } else {
            collapseMaskView.setAlpha(0f);
            collapseMaskView.setProgress(1);
            avatarImage.setAlpha(1);
            if (progress <= 1) {
                avatarScrollAnimationContainer.setScaleX(1f);
                avatarScrollAnimationContainer.setScaleY(1f);
            }
        }
    }

    public void updateProfileData(boolean reload) {
        infoView.updateTitleData();
        infoView.updateSubtitleData();
        background.updateProfileData();
        giftsView.update();
        updateStoryData();
        setupActionButtons();

        collapseMaskView.setVisibility(dataProvider.isTopic ? View.GONE : View.VISIBLE);

        if (avatarExpandAnimationContainer == null || fragment.getParentActivity() == null) {
            return;
        }

        TLRPC.TL_forumTopic topic = null;
        hasFallbackPhoto = false;
        hasCustomPhoto = false;
        if (dataProvider.userId != 0) {
            TLRPC.User user = dataProvider.getUser();
            if (user == null) { return; }
            TLRPC.FileLocation photoBig = null;
            if (user.photo != null) {
                photoBig = user.photo.photo_big;
            }
            avatarDrawable.setInfo(currentAccount, user);
            final ImageLocation imageLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_BIG);
            final ImageLocation thumbLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL);
            final ImageLocation videoThumbLocation = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_VIDEO_BIG);
            VectorAvatarThumbDrawable vectorAvatarThumbDrawable = null;
            TLRPC.VideoSize vectorAvatar = null;
            if (dataProvider.getUserFull() != null) {
                vectorAvatar = FileLoader.getVectorMarkupVideoSize(user.photo != null && user.photo.personal ? dataProvider.getUserFull().personal_photo : dataProvider.getUserFull().profile_photo);
                if (vectorAvatar != null) {
                    vectorAvatarThumbDrawable = new VectorAvatarThumbDrawable(vectorAvatar, user.premium, VectorAvatarThumbDrawable.TYPE_PROFILE);
                }
            }
            final ImageLocation videoLocation = avatarsViewPager.getCurrentVideoLocation(thumbLocation, imageLocation);
            if (avatar == null) {
                avatarsViewPager.initIfEmpty(vectorAvatarThumbDrawable, imageLocation, thumbLocation, reload);
            }
            if (avatarBig == null) {
                if (vectorAvatar != null) {
                    avatarImage.setImageDrawable(vectorAvatarThumbDrawable);
                } else if (videoThumbLocation != null && !user.photo.personal) {
                    avatarImage.getImageReceiver().setVideoThumbIsSame(true);
                    avatarImage.setImage(videoThumbLocation, "avatar", thumbLocation, "50_50", avatarDrawable, user);
                } else {
                    avatarImage.setImage(videoLocation, ImageLoader.AUTOPLAY_FILTER, thumbLocation, "50_50", avatarDrawable, user);
                }
            }

            if (imageLocation != null && (prevLoadedImageLocation == null || imageLocation.photoId != prevLoadedImageLocation.photoId)) {
                prevLoadedImageLocation = imageLocation;
                fragment.getFileLoader().loadFile(imageLocation, user, null, FileLoader.PRIORITY_LOW, 1);
            }

            if (user.id == fragment.getUserConfig().getClientUserId()) {
                if (UserObject.hasFallbackPhoto(dataProvider.getUserFull())) {
                    hasFallbackPhoto = true;
                    TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(dataProvider.getUserFull().fallback_photo.sizes, 1000);
                    if (smallSize != null) {
                        fallbackImage.setImage(ImageLocation.getForPhoto(smallSize, dataProvider.getUserFull().fallback_photo), "50_50", (Drawable) null, 0, null, UserConfig.getInstance(currentAccount).getCurrentUser(), 0);
                    }
                }
            }
            hasCustomPhoto = user.photo != null && user.photo.personal;
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.isShowingImage(photoBig) && (fragment.getLastStoryViewer() == null || fragment.getLastStoryViewer().transitionViewHolder.view != avatarImage), storyView != null);
        } else if (dataProvider.chatId != 0) {
            TLRPC.Chat chat = dataProvider.getChat();

            if (dataProvider.isTopic) {
                topic = dataProvider.getMessagesController().getTopicsController().findTopic(dataProvider.chatId, dataProvider.topicId);
            }

            Log.i("PHOTO", "" + chat.photo);

            TLRPC.FileLocation photoBig = null;
            if (chat.photo != null && !dataProvider.isTopic) {
                photoBig = chat.photo.photo_big;
            }

            final ImageLocation imageLocation;
            final ImageLocation thumbLocation;
            final ImageLocation videoLocation;
            if (dataProvider.isTopic) {
                imageLocation = null;
                thumbLocation = null;
                videoLocation = null;
                ForumUtilities.setTopicIcon(avatarImage, topic, true, true, fragment.getResourceProvider());
            } else if (ChatObject.isMonoForum(fragment.currentChat)) {
                TLRPC.Chat channel = dataProvider.getMessagesController().getMonoForumLinkedChat(fragment.currentChat.id);
                avatarDrawable.setInfo(currentAccount, channel);
                imageLocation = ImageLocation.getForUserOrChat(channel, ImageLocation.TYPE_BIG);
                thumbLocation = ImageLocation.getForUserOrChat(channel, ImageLocation.TYPE_SMALL);
                videoLocation = avatarsViewPager.getCurrentVideoLocation(thumbLocation, imageLocation);
            } else {
                avatarDrawable.setInfo(currentAccount, chat);
                imageLocation = ImageLocation.getForUserOrChat(chat, ImageLocation.TYPE_BIG);
                thumbLocation = ImageLocation.getForUserOrChat(chat, ImageLocation.TYPE_SMALL);
                videoLocation = avatarsViewPager.getCurrentVideoLocation(thumbLocation, imageLocation);
            }

            String filter;
            if (videoLocation != null && videoLocation.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                filter = ImageLoader.AUTOPLAY_FILTER;
            } else {
                filter = null;
            }
            if (avatarBig == null && !dataProvider.isTopic) {
                avatarImage.setImage(videoLocation, filter, thumbLocation, "50_50", avatarDrawable, chat);
            }
            if (imageLocation != null && (prevLoadedImageLocation == null || imageLocation.photoId != prevLoadedImageLocation.photoId)) {
                prevLoadedImageLocation = imageLocation;
                fragment.getFileLoader().loadFile(imageLocation, chat, null, FileLoader.PRIORITY_LOW, 1);
            }
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.isShowingImage(photoBig) && (fragment.getLastStoryViewer() == null || fragment.getLastStoryViewer().transitionViewHolder.view != avatarImage), storyView != null);
        }
    }

    public void updateStoryData() {
        if (storyView != null) {
            TLRPC.UserFull userFull = dataProvider.getUserFull();
            TLRPC.ChatFull chatFull = dataProvider.getChatFull();
            
            if (userFull != null) {
                storyView.setStories(userFull.stories);
            } else if (chatFull != null) {
                storyView.setStories(chatFull.stories);
            }
            
            avatarImage.setHasStories(dataProvider.needInsetForStories());
        }
    }
    
    public void updateStoriesViewBounds(boolean animated) {
        if (storyView == null || fragment.getActionBar() == null) {
            return;
        }
        
        float atop = fragment.getActionBar().getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
        float aleft = 0;
        float aright = fragment.getActionBar().getWidth();

        if (fragment.getActionBar().getBackButton() != null) {
            aleft = Math.max(aleft, fragment.getActionBar().getBackButton().getRight());
        }
        if (fragment.getActionBar().menu != null) {
            for (int i = 0; i < fragment.getActionBar().menu.getChildCount(); ++i) {
                View child = fragment.getActionBar().menu.getChildAt(i);
                if (child.getAlpha() <= 0 || child.getVisibility() != View.VISIBLE) {
                    continue;
                }
                int left = fragment.getActionBar().menu.getLeft() + (int) child.getX();
                if (left < aright) {
                    aright = AndroidUtilities.lerp(aright, left, child.getAlpha());
                }
            }
        }
        
        storyView.setBounds(aleft, aright, atop + (fragment.getActionBar().getHeight() - atop) / 2f, !animated);
    }
    
    private void setStoryViewExpandProgress(float progress) {
        if (storyView != null) {
            storyView.setExpandProgress(progress);
        }
    }

    public void onDestroy() {
        if (avatarsViewPager != null) {
            avatarsViewPager.onDestroy();
        }
        if (expandingAnimator != null) {
            expandingAnimator.cancel();
            expandingAnimator = null;
        }

        if (storyView != null) {
            storyView = null;
        }
    }
}
