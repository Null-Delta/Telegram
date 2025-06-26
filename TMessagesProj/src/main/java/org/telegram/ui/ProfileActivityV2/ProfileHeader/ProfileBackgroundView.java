package org.telegram.ui.ProfileActivityV2.ProfileHeader;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.UserObject;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.PhotoFilterView;
import org.telegram.ui.PeerColorActivity;
import org.telegram.ui.ProfileActivityV2.ProfileActivityDataProvider;
import org.telegram.ui.ProfileActivityV2.ProfileActivityV2;

import java.util.Random;

public class ProfileBackgroundView extends FrameLayout {
    final private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final private ProfileActivityDataProvider dataProvider;
    final private ProfileActivityV2.HeaderSizeConfiguration configuration;
    final private float topMargin = AndroidUtilities.statusBarHeight + AndroidUtilities.dp(12 + 45);

    private int emojiColor;
    private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable emoji = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(this, false, dp(20), AnimatedEmojiDrawable.CACHE_TYPE_ALERT_PREVIEW_STATIC);

    private int numRows = 5;
    private int avatarPatternCount = 7;
    private float avatarPatternAngleSpan = (float) Math.PI * 2.0f / 6f;
    private CubicBezierInterpolator interpolator = CubicBezierInterpolator.EASE_IN;

    public ProfileBackgroundView(
            Context context,
            ProfileActivityV2.HeaderSizeConfiguration configuration,
            ProfileActivityDataProvider dataProvider
    ) {
        super(context);

        this.dataProvider = dataProvider;
        this.configuration = configuration;

        setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));

        setClickable(false);
        setFocusable(false);
    }

    public void updateProfileData() {
        if (dataProvider.peerColor() != null) {
            if (dataProvider.peerColor().patternColor != 0) {
                emojiColor = dataProvider.peerColor().patternColor;
            } else {
                emojiColor = PeerColorActivity.adaptProfileEmojiColor(dataProvider.peerColor().getColor1());
            }
        } else {
            if (AndroidUtilities.computePerceivedBrightness(dataProvider.color(Theme.key_actionBarDefault)) > .8f) {
                emojiColor = dataProvider.color(Theme.key_windowBackgroundWhiteBlueText);
            } else if (AndroidUtilities.computePerceivedBrightness(dataProvider.color(Theme.key_actionBarDefault)) < .2f) {
                emojiColor = Theme.multAlpha(dataProvider.color(Theme.key_actionBarDefaultTitle), .5f);
            } else {
                emojiColor = PeerColorActivity.adaptProfileEmojiColor(dataProvider.color(Theme.key_actionBarDefault));
            }
        }

        long emojiId = 0;

        if (dataProvider.getUser() != null) {
            emojiId = UserObject.getProfileEmojiId(dataProvider.getUser());
        } else if (dataProvider.getChat() != null) {
            emojiId = ChatObject.getProfileEmojiId(dataProvider.getChat());
        }
        boolean animated = true;

        emoji.set(emojiId, animated);
        emoji.setColor(emojiColor);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (dataProvider.peerColor() == null) { return; }

        float offset = (getHeight() - configuration.defaultHeight) / (dataProvider.isMyProfile ? 1.2f : 1.5f);

        LinearGradient linearGradient = new LinearGradient(
                0, offset, 0, getHeight(),
                new int[] {
                        dataProvider.peerColor().getBgColor2(Theme.isCurrentThemeDark()),
                        dataProvider.peerColor().getBgColor1(Theme.isCurrentThemeDark())
                },
                new float[] { 0.1f, 0.9f },
                Shader.TileMode.CLAMP
        );

        int blendedColor = ColorUtils.blendARGB(
                dataProvider.peerColor().getBgColor2(Theme.isCurrentThemeDark()),
                Color.argb(0, 255, 255, 255),
                0.25f
        );

        RadialGradient radialGradient = new RadialGradient(
                (float) getWidth() / 2,
                topMargin + offset,
                (float) getWidth() / 2,
                blendedColor,
                ColorUtils.setAlphaComponent(dataProvider.peerColor().getBgColor2(Theme.isCurrentThemeDark()), 0),
                Shader.TileMode.CLAMP
        );

        Rect bounds = new Rect(0, 0, getWidth(), getHeight());

        backgroundPaint.setXfermode(null);
        backgroundPaint.setShader(linearGradient);
        canvas.drawRect(bounds, backgroundPaint);

        backgroundPaint.setShader(radialGradient);
        canvas.drawRect(bounds, backgroundPaint);

        RadialGradient radialGradient2 = new RadialGradient(
                (float) getWidth() / 2,
                topMargin + offset,
                (float) getWidth() / 2,
                Color.WHITE,
                ColorUtils.setAlphaComponent(Color.WHITE, 0),
                Shader.TileMode.CLAMP
        );

        LinearGradient linearGradient2 = new LinearGradient(
                0, offset, 0, getHeight(),
                new int[] {
                        Color.WHITE,
                        Color.WHITE,
                        ColorUtils.setAlphaComponent(Color.WHITE, 0)
                },
                new float[] { 0.1f, 0.5f, 0.8f },
                Shader.TileMode.CLAMP
        );

        int layerSave = canvas.saveLayer(new RectF(bounds), null, Canvas.ALL_SAVE_FLAG);

        drawEmoji(canvas, offset);
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        backgroundPaint.setShader(radialGradient2);
        canvas.drawRect(bounds, backgroundPaint);

        backgroundPaint.setShader(linearGradient2);
        canvas.drawRect(bounds, backgroundPaint);

        canvas.restoreToCount(layerSave);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        emoji.attach();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        emoji.detach();
    }

    private void drawEmoji(Canvas canvas, float offset) {
        float centerX = getWidth() / 2f;
        float centerY = topMargin + offset;

        float collapseProgress = -offset / (configuration.defaultHeight - configuration.minimizedHeight);
        collapseProgress *= 2f;

        for(int row = 0; row < numRows; row++) {
            for(int i = 0; i < avatarPatternCount - 1; i++) {
                float baseItemDistance = AndroidUtilities.dp(72) + (float) row * AndroidUtilities.dp(32);
                float a = baseItemDistance;

                float collapseEmojiProgress =  interpolator.getInterpolation(Math.max(0, Math.min(1, collapseProgress - 0.1f * row)));
                baseItemDistance = Math.max(0, baseItemDistance - collapseEmojiProgress * AndroidUtilities.dp(a));

                float itemAngle = -(float) Math.PI / 2f + (float) i * avatarPatternAngleSpan;
                if (row % 2 != 0) {
                    itemAngle += avatarPatternAngleSpan * 0.5f;
                }

                int emojiCenterX = (int) (centerX + (float) Math.cos(itemAngle) * baseItemDistance);
                int emojiCenterY = (int) (centerY + (float) Math.sin(itemAngle) * baseItemDistance);
                int size = AndroidUtilities.dp(24 * (1 - collapseEmojiProgress));

                emoji.setAlpha((int) Math.max(0, (128 * Math.min(1, (baseItemDistance - AndroidUtilities.dp(20)) / AndroidUtilities.dp(90)))));
                emoji.setBounds(
                        new Rect(
                            emojiCenterX - size / 2,
                            emojiCenterY - size / 2,
                            emojiCenterX + size / 2,
                            emojiCenterY + size / 2
                        )
                );

                emoji.draw(canvas);
            }
        }
    }
}
