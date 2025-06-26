package org.telegram.ui.ProfileActivityV2.ProfileHeader;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.lerp;
import static org.telegram.ui.Stars.StarsController.findAttribute;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.tl.TL_stars;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.ButtonBounce;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.Point;
import org.telegram.ui.ProfileActivityV2.ProfileActivityV2;
import org.telegram.ui.Stars.StarsController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashSet;

public class ProfileGiftsView extends View implements NotificationCenter.NotificationCenterDelegate {
    public final class Gift {

        public final long id;
        public final TLRPC.Document document;
        public final long documentId;
        public final int color;
        public final String slug;

        public Gift(TL_stars.TL_starGiftUnique gift) {
            id = gift.id;
            document = gift.getDocument();
            documentId = document == null ? 0 : document.id;
            final TL_stars.starGiftAttributeBackdrop backdrop = findAttribute(gift.attributes, TL_stars.starGiftAttributeBackdrop.class);
            color = backdrop.center_color | 0xFF000000;
            slug = gift.slug;
        }

        public Gift(TLRPC.TL_emojiStatusCollectible status) {
            id = status.collectible_id;
            document = null;
            documentId = status.document_id;
            color = status.center_color | 0xFF000000;
            slug = status.slug;
        }

        public boolean equals(Gift b) {
            return b != null && b.id == id;
        }

        public RadialGradient gradient;
        public final Matrix gradientMatrix = new Matrix();
        public Paint gradientPaint;
        public AnimatedEmojiDrawable emojiDrawable;
        public AnimatedFloat animatedFloat;

        public final RectF bounds = new RectF();
        public final ButtonBounce bounce = new ButtonBounce(ProfileGiftsView.this);

        public void copy(Gift b) {
            gradient = b.gradient;
            emojiDrawable = b.emojiDrawable;
            gradientPaint = b.gradientPaint;
            animatedFloat = b.animatedFloat;
        }

        public void draw(
                Canvas canvas,
                float cx, float cy,
                float ascale, float rotate,
                float alpha,
                float gradientAlpha
        ) {
            if (alpha <= 0.0f) return;
            final float gsz = dp(45);
            bounds.set(cx - gsz / 2, cy - gsz / 2, cx + gsz / 2, cy + gsz / 2);
            canvas.save();
            canvas.translate(cx, cy);
            canvas.rotate(rotate);
            final float scale = ascale * bounce.getScale(0.1f);
            canvas.scale(scale, scale);
            if (gradientPaint != null) {
                gradientPaint.setAlpha((int) (0xFF * alpha * gradientAlpha));
                canvas.drawRect(-gsz / 2.0f, -gsz / 2.0f, gsz / 2.0f, gsz / 2.0f, gradientPaint);
            }
            if (emojiDrawable != null) {
                final int sz = dp(24);
                emojiDrawable.setBounds(-sz / 2, -sz / 2, sz / 2, sz / 2);
                emojiDrawable.setAlpha((int) (0xFF * alpha));
                emojiDrawable.draw(canvas);
            }
            canvas.restore();
        }
    }

    public final ArrayList<Gift> oldGifts = new ArrayList<>();
    public final ArrayList<Gift> gifts = new ArrayList<>();
    public final HashSet<Long> giftIds = new HashSet<>();
    public int maxCount;
    private StarsController.GiftsList list;
    private long dialogId;

    private Point[] giftPositions;

    private final int currentAccount;
    private float expandProgress = 0;
    private ValueAnimator presentAnimator;
    private CubicBezierInterpolator interpolator = CubicBezierInterpolator.EASE_IN;

    private ProfileActivityV2.HeaderSizeConfiguration configuration;
    public void setExpandProgress(float progress) {
        if (this.expandProgress != progress) {
            this.expandProgress = progress;
            invalidate();
        }
    }

    public ProfileGiftsView(@NonNull Context context, int currentAccount, long dialogId, ProfileActivityV2.HeaderSizeConfiguration configuration) {
        super(context);
        this.currentAccount = currentAccount;
        this.dialogId = dialogId;
        this.configuration = configuration;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.starUserGiftsLoaded);

        for (Gift gift : gifts) {
            gift.emojiDrawable.addView(this);
        }

        update();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.starUserGiftsLoaded);

        for (Gift gift : gifts) {
            gift.emojiDrawable.removeView(this);
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.starUserGiftsLoaded) {
            if ((long) args[0] == dialogId) {
                update();
            }

            if (presentAnimator == null) {
                presentAnimator = ValueAnimator.ofFloat(0, 1);
                presentAnimator.setDuration(300);
                presentAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                presentAnimator.addUpdateListener(valueAnimator -> {
                    float value = (float) valueAnimator.getAnimatedValue();
                    ProfileGiftsView.this.setAlpha(value);
                });
                presentAnimator.start();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        calculateGiftPositions(w);
    }


    private void calculateGiftPositions(int w) {
        float centerX = w / 2f;
        float topCenter = AndroidUtilities.statusBarHeight + AndroidUtilities.dp(12 + 45);

        giftPositions = new Point[6];
        giftPositions[0] = new Point(
                centerX - AndroidUtilities.dp(90 + 30),
                topCenter
        );

        giftPositions[1] = new Point(
                centerX + AndroidUtilities.dp(90 + 30),
                topCenter
        );

        giftPositions[2] = new Point(
                centerX - AndroidUtilities.dp(90),
                topCenter - AndroidUtilities.dp(45)
        );

        giftPositions[3] = new Point(
                centerX + AndroidUtilities.dp(90),
                topCenter - AndroidUtilities.dp(45)
        );

        giftPositions[4] = new Point(
                centerX - AndroidUtilities.dp(90),
                topCenter + AndroidUtilities.dp(45)
        );

        giftPositions[5] = new Point(
                centerX + AndroidUtilities.dp(90),
                topCenter + AndroidUtilities.dp(45)
        );
    };

    private Point collapsingPosition(Point p, float progress, boolean isTop) {
        Point center = new Point(
            getWidth() / 2f,
            AndroidUtilities.statusBarHeight + AndroidUtilities.dp(12 + 45)
        );

        Point controlPoint = calculatePointFromLine(p, center, AndroidUtilities.dp(30) * (isTop ? 1 : -1));

        return getQuadraticBezierPoint(interpolator.getInterpolation(progress), p, controlPoint, center);
    }

    private Point calculatePointFromLine(Point p1, Point p2, float distance) {
        float centerX = (p1.x + p2.x) / 2.0f;
        float centerY = (p1.y + p2.y) / 2.0f;

        float directionX = p2.x - p1.x;
        float directionY = p2.y - p1.y;

        float perpX = -directionY;
        float perpY = directionX;

        float length = (float) Math.sqrt(perpX * perpX + perpY * perpY);
        if (length == 0) {
            return new Point(p1.x, p1.y);
        }

        float normX = perpX / length;
        float normY = perpY / length;

        float resultX = centerX + normX * distance;
        float resultY = centerY + normY * distance;

        return new Point(resultX, resultY);
    }

    public static Point getQuadraticBezierPoint(float t, Point p0, Point p1, Point p2) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;

        float x = uu * p0.x + 2 * u * t * p1.x + tt * p2.x;
        float y = uu * p0.y + 2 * u * t * p1.y + tt * p2.y;

        return new Point(x, y);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        if (gifts.isEmpty()) return;

        float[] animationOffsets = new float[6];
        animationOffsets[0] = 0.2f;
        animationOffsets[1] = 0.3f;
        animationOffsets[2] = 0.4f;
        animationOffsets[3] = 0.5f;
        animationOffsets[4] = 0.1f;
        animationOffsets[5] = 0f;

        canvas.translate(
            0,
            AndroidUtilities.lerp(0, (configuration.minimizedHeight - configuration.defaultHeight) / 1.25f, expandProgress)
        );
        canvas.save();
        canvas.clipRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < gifts.size(); ++i) {
            float realProgress = Math.min(1f, Math.max(0f, expandProgress * 3 - animationOffsets[i]));

            final Gift gift = gifts.get(i);

            final float alpha = gift.animatedFloat.set(1.0f);
            final float scale = lerp(1.25f, 0.5f, realProgress * alpha);
            final Point position = collapsingPosition(
                    new Point(
                            giftPositions[i % giftPositions.length].x,
                            giftPositions[i % giftPositions.length].y
                    ),
                    realProgress,
                    i % 2 == 0
            );
            gift.draw(
                    canvas,
                    position.x,
                    position.y,
                    scale,
                    0,
                    alpha * (1 - realProgress),
                    1
            );
        }

        canvas.restore();
    }

    public void update() {
        if (!MessagesController.getInstance(currentAccount).enableGiftsInProfile) {
            return;
        }

        maxCount = MessagesController.getInstance(currentAccount).stargiftsPinnedToTopLimit;
        oldGifts.clear();
        oldGifts.addAll(gifts);
        gifts.clear();
        giftIds.clear();

        final TLRPC.EmojiStatus emojiStatus;
        if (dialogId >= 0) {
            final TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(dialogId);
            emojiStatus = user == null ? null : user.emoji_status;
        } else {
            final TLRPC.User chat = MessagesController.getInstance(currentAccount).getUser(-dialogId);
            emojiStatus = chat == null ? null : chat.emoji_status;
        }
        if (emojiStatus instanceof TLRPC.TL_emojiStatusCollectible) {
            giftIds.add(((TLRPC.TL_emojiStatusCollectible) emojiStatus).collectible_id);
        }
        list = StarsController.getInstance(currentAccount).getProfileGiftsList(dialogId);
        if (list != null) {
            for (int i = 0; i < list.gifts.size(); i++) {
                final TL_stars.SavedStarGift savedGift = list.gifts.get(i);
                if (!savedGift.unsaved && savedGift.pinned_to_top && savedGift.gift instanceof TL_stars.TL_starGiftUnique) {
                    final Gift gift = new Gift((TL_stars.TL_starGiftUnique) savedGift.gift);
                    if (!giftIds.contains(gift.id)) {
                        gifts.add(gift);
                        giftIds.add(gift.id);
                    }
                }
            }
        }

        boolean changed = false;
        if (gifts.size() != oldGifts.size()) {
            changed = true;
        } else for (int i = 0; i < gifts.size(); i++) {
            if (!gifts.get(i).equals(oldGifts.get(i))) {
                changed = true;
                break;
            }
        }

        for (int i = 0; i < gifts.size(); i++) {
            final Gift g = gifts.get(i);
            Gift oldGift = null;
            for (int j = 0; j < oldGifts.size(); ++j) {
                if (oldGifts.get(j).id == g.id) {
                    oldGift = oldGifts.get(j);
                    break;
                }
            }

            if (oldGift != null) {
                g.copy(oldGift);
            } else {
                g.gradient = new RadialGradient(0, 0, dp(22.5f), new int[] { g.color, Theme.multAlpha(g.color, 0.0f) }, new float[] { 0, 1 }, Shader.TileMode.CLAMP);
                g.gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                g.gradientPaint.setShader(g.gradient);
                if (g.document != null) {
                    g.emojiDrawable = AnimatedEmojiDrawable.make(currentAccount, AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, g.document);
                } else {
                    g.emojiDrawable = AnimatedEmojiDrawable.make(currentAccount, AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, g.documentId);
                }
                g.animatedFloat = new AnimatedFloat(this, 0, 320, CubicBezierInterpolator.EASE_OUT_QUINT);
                g.animatedFloat.force(0.0f);
                if (isAttachedToWindow()) {
                    g.emojiDrawable.addView(this);
                }
            }
        }

        for (int i = 0; i < oldGifts.size(); i++) {
            final Gift g = oldGifts.get(i);
            Gift newGift = null;
            for (int j = 0; j < gifts.size(); ++j) {
                if (gifts.get(j).id == g.id) {
                    newGift = gifts.get(j);
                    break;
                }
            }
            if (newGift == null) {
                g.emojiDrawable.removeView(this);
                g.emojiDrawable = null;
                g.gradient = null;
            }
        }

        if (changed)
            invalidate();
    }

    private Gift pressedGift;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final Gift hit = getGiftUnder(event.getX(), event.getY());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pressedGift = hit;
            if (pressedGift != null) {
                pressedGift.bounce.setPressed(true);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (pressedGift != hit && pressedGift != null) {
                pressedGift.bounce.setPressed(false);
                pressedGift = null;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (pressedGift != null) {
                onGiftClick(pressedGift);
                pressedGift.bounce.setPressed(false);
                pressedGift = null;
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (pressedGift != null) {
                pressedGift.bounce.setPressed(false);
                pressedGift = null;
            }
        }
        return pressedGift != null;
    }

    private Gift getGiftUnder(float x, float y) {
        for (int i = 0; i < gifts.size(); ++i) {
            if (gifts.get(i).bounds.contains(x, y))
                return gifts.get(i);
        }
        return null;
    }

    public void onGiftClick(Gift gift) {
        Browser.openUrl(getContext(), "https://t.me/nft/" + gift.slug);
    }
}
