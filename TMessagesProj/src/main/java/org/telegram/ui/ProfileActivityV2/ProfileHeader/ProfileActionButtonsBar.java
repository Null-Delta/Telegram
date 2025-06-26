package org.telegram.ui.ProfileActivityV2.ProfileHeader;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Bitmaps;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.ProfileActivityV2.ProfileActivityV2;

import java.util.ArrayList;

public class ProfileActionButtonsBar extends FrameLayout {
    public static class Button extends View {
        public static class Model {
            public Drawable icon;
            public String text;
            public Utilities.Callback3<View, Integer, Integer> callback;

            public Model(Drawable icon, String text, Utilities.Callback3<View, Integer, Integer> callback) {
                this.icon = icon;
                this.text = text;
                this.callback = callback;
            }
        }


        public int buttonBackgroundColor = Theme.isCurrentThemeDark() ? Color.WHITE : Color.BLACK;
        public float buttonAlphaFactor = 1;
        final private Model model;
        final private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final private Paint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        private boolean isPressed = false;

        private float collapseProgress = 0.0f;
        private RectF bounds = new RectF();

        public Button(@NonNull Context context, Model model) {
            super(context);
            this.model = model;

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            backgroundPaint.setColor(Color.argb(32, 0, 0, 0));
            
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(AndroidUtilities.dp(10));
            textPaint.setFakeBoldText(true);

            setWillNotDraw(false);

            this.setOnTouchListener(new OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int action = motionEvent.getAction();

                    if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_HOVER_ENTER || action == MotionEvent.ACTION_HOVER_MOVE || action == MotionEvent.ACTION_MOVE) {
                        isPressed = true;
                        Button.this.invalidate();
                    } else {
                        isPressed = false;
                        Button.this.invalidate();
                    }

                    if (action == MotionEvent.ACTION_UP) {
                        model.callback.run(ProfileActionButtonsBar.Button.this, (int) motionEvent.getX(), (int)motionEvent.getY());
                    }

                    return true;
                }
            });
        }

        @Override
        public boolean performClick() {
            return super.performClick();
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = AndroidUtilities.dp(
                    AndroidUtilities.lerp(54, 12, collapseProgress)
            );
            setMeasuredDimension(width, height);
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            backgroundPaint.setColor(
                    ColorUtils.setAlphaComponent(buttonBackgroundColor, (int)((isPressed ? 48 : 24) * buttonAlphaFactor))
            );

            bounds.set(0f, 0f, getWidth(), getHeight());
            canvas.drawRoundRect(
                    bounds,
                    AndroidUtilities.dp(10), AndroidUtilities.dp(10),
                    backgroundPaint
            );

            canvas.translate(getWidth() / 2f, getHeight() / 2f);
            canvas.scale(
                    AndroidUtilities.lerp(1, 0.1f, collapseProgress),
                    AndroidUtilities.lerp(1, 0.1f, collapseProgress)
            );
            canvas.translate(-getWidth() / 2f, -getHeight() / 2f - (AndroidUtilities.dp(44) * collapseProgress) / 2f);

            int iconSize = AndroidUtilities.dp(24);

            if (model.icon != null) {
                model.icon.setBounds(
                        getWidth() / 2 - iconSize / 2,
                        AndroidUtilities.dp(6),
                        getWidth() / 2 + iconSize / 2,
                        AndroidUtilities.dp(6) + iconSize
                );
                model.icon.draw(canvas);
            }

            float textWidth = textPaint.measureText(model.text);
            while(textWidth > canvas.getWidth() - AndroidUtilities.dp(2)) {
                textPaint.setTextSize(textPaint.getTextSize() - AndroidUtilities.dp(1));
                textWidth = textPaint.measureText(model.text);
            }

            canvas.drawText(model.text, (float) getWidth() / 2 - textWidth / 2f, AndroidUtilities.dp(54 - 10), textPaint);

            textPaint.setTextSize(AndroidUtilities.dp(10));
            super.onDraw(canvas);
        }

        public void updateCollapseProgress(float collapseProgress) {
            this.collapseProgress = collapseProgress;
            this.setAlpha(Math.min(1f, Math.max(0f, 1 - collapseProgress)));
            requestLayout();
        }
    }

    final private LinearLayout linearLayout;
    final private ArrayList<Button> buttons = new ArrayList<>();
    
    private Bitmap blurBitmap;
    private BitmapShader blurShader;
    private Matrix blurMatrix;
    private final Paint blurPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private LinearGradient gradient;
    private boolean preparingBlur = false;

    private ValueAnimator expandingAnimator;
    private float currentExpandingProgress = 0;
    private float collapseProgress = 0f;

    private Utilities.Callback0Return<View> bluredImage;

    public ProfileActionButtonsBar(@NonNull Context context, Utilities.Callback0Return<View> bluredImage) {
        super(context);

        this.bluredImage = bluredImage;

        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), AndroidUtilities.dp(12));
        addView(
                linearLayout,
                LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER)
        );
        
        setWillNotDraw(false);
        backgroundPaint.setColor(Color.TRANSPARENT);
        
        gradientPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        
        blurMatrix = new Matrix();
        blurPaint.setFilterBitmap(true);
        blurPaint.setAlpha(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = AndroidUtilities.dp(102);
        setMeasuredDimension(width, height);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        if (changed) {
            invalidateBlurredBackground();
            invalidate();
            if (getWidth() > 0 && getHeight() > 0) {
                createGradient();
            }
        }
    }
    
    private void createGradient() {
        int height = getHeight();
        float midPoint = AndroidUtilities.dp(30f) / (float) height;
        gradient = new LinearGradient(
                0, 0, 0, height,
                new int[]{Color.TRANSPARENT, Color.WHITE, Color.WHITE},
                new float[]{0.0f, midPoint, 1.0f},
                Shader.TileMode.CLAMP
        );
        gradientPaint.setShader(gradient);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        createBlurredBackground();

        if (gradient == null && getWidth() > 0 && getHeight() > 0) {
            createGradient();
        }

        bounds.set(0f, 0f, getWidth(), getHeight());

        if (blurBitmap != null && !blurBitmap.isRecycled() && blurShader != null && gradient != null) {
            int layerSave = canvas.saveLayer(bounds, null, Canvas.ALL_SAVE_FLAG);

            canvas.drawRect(bounds, blurPaint);
            canvas.drawRect(bounds, gradientPaint);

            canvas.restoreToCount(layerSave);
        }

        super.onDraw(canvas);
    }
    
    private void createBlurredBackground() {
        if (preparingBlur || getWidth() == 0 || getHeight() == 0) {
            return;
        }
        
        preparingBlur = true;
        
        float downscale = 4;
        float blurRadius = 24;
        
        switch (SharedConfig.getDevicePerformanceClass()) {
            case SharedConfig.PERFORMANCE_CLASS_HIGH:
                downscale *= 1;
                blurRadius /= 1;
                break;
            case SharedConfig.PERFORMANCE_CLASS_AVERAGE:
                downscale *= 2;
                blurRadius /= 2;
                break;
            case SharedConfig.PERFORMANCE_CLASS_LOW:
            default:
                downscale *= 3;
                blurRadius /= 3;
                break;
        }

        this.setVisibility(View.INVISIBLE);
        
        View parentView = (View) this.bluredImage.run();
        if (parentView != null) {
            try {
                int width = (int) (getWidth() / downscale);
                int height = (int) (getHeight() / downscale) + (int) blurRadius;
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);

                int[] location = new int[2];
                int[] parentLocation = new int[2];

                getLocationInWindow(location);
                parentView.getLocationInWindow(parentLocation);

                canvas.translate(0, blurRadius);
                canvas.scale(1.0f / downscale, 1.0f / downscale);

                canvas.translate(
                    -(location[0] - parentLocation[0]),
                    -(location[1] - parentLocation[1])
                );

                parentView.draw(canvas);
                Utilities.stackBlurBitmap(bitmap, (int) blurRadius);

                Bitmap bitmap2 = Bitmap.createBitmap(width - (int) (blurRadius), height - (int) blurRadius, Bitmap.Config.ARGB_8888);
                Canvas canvas2 = new Canvas(bitmap2);
                canvas2.drawBitmap(
                        bitmap,
                        new Rect((int) blurRadius / 2, (int) blurRadius, width - (int) blurRadius / 2, height),
                        new Rect(0, 0, width - (int) (blurRadius), height - (int) blurRadius),
                        new Paint()
                );

                bitmap = bitmap2;

                if (blurBitmap != null && !blurBitmap.isRecycled()) {
                    blurBitmap.recycle();
                }

                blurBitmap = bitmap;
                blurShader = new BitmapShader(blurBitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
                blurPaint.setShader(blurShader);
                updateBlurMatrix();
            } catch (Exception e) {
                invalidateBlurredBackground();
            }
        }

        this.setVisibility(View.VISIBLE);
        preparingBlur = false;
        invalidate();
    }
    
    private void updateBlurMatrix() {
        if (blurShader != null && blurBitmap != null && !blurBitmap.isRecycled()) {
            blurMatrix.reset();
            blurMatrix.setScale(
                (float) getWidth() / blurBitmap.getWidth(),
                (float) getHeight() / blurBitmap.getHeight()
            );

            blurShader.setLocalMatrix(blurMatrix);
        }
    }

    public void updateButtons(ArrayList<Button.Model> buttonModels, boolean isExpanded) {
        linearLayout.removeAllViews();
        buttons.clear();

        for (int modelIndex = 0; modelIndex < buttonModels.size(); modelIndex++) {
            final Button btn = new Button(getContext(), buttonModels.get(modelIndex));
            buttons.add(btn);
            btn.buttonBackgroundColor = Theme.isCurrentThemeDark() && !isExpanded ? Color.WHITE : Color.BLACK;
            btn.updateCollapseProgress(collapseProgress);

            linearLayout.addView(btn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 54, 1, Gravity.BOTTOM));

            if (modelIndex < buttonModels.size() - 1) {
                linearLayout.addView(
                        new Space(getContext()),
                        LayoutHelper.createFrame(6, LayoutHelper.MATCH_PARENT)
                );
            }
        }
    }

    public void updateCollapseProgress(float collapseProgress) {
        this.collapseProgress = collapseProgress;
        for (int buttonIndex = 0; buttonIndex < buttons.size(); buttonIndex++) {
            buttons.get(buttonIndex).updateCollapseProgress(collapseProgress);
        }

        if (collapseProgress >= 1) {
            setClickable(false);
        } else {
            setClickable(true);
        }

        invalidateBlurredBackground();
        invalidate();
    }

    public void invalidateBlurredBackground() {
        if (blurBitmap != null && !blurBitmap.isRecycled()) {
            blurBitmap.recycle();
            blurBitmap = null;
        }
        blurShader = null;
        blurPaint.setShader(null);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        invalidateBlurredBackground();
    }

    public void updateState(boolean isExpanding) {
        if (this.getWidth() == 0) { return; }
        if (expandingAnimator != null) {
            expandingAnimator.cancel();
        }

        expandingAnimator = ValueAnimator.ofFloat(currentExpandingProgress, isExpanding ? 1 : 0);
        expandingAnimator.setDuration(ProfileActivityV2.expandingAnimationDuration);
        expandingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        expandingAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            currentExpandingProgress = value;
            blurPaint.setAlpha(AndroidUtilities.lerp(0, 255, value));

            for(int buttonIndex = 0; buttonIndex < buttons.size(); buttonIndex++) {
                buttons.get(buttonIndex).buttonBackgroundColor =
                        ColorUtils.blendARGB(
                                Theme.isCurrentThemeDark() ? Color.WHITE : Color.BLACK,
                                Color.BLACK,
                                value
                        );
                buttons.get(buttonIndex).buttonAlphaFactor = AndroidUtilities.lerp(1f, 1.5f, value);
            }

            invalidate();
        });
        expandingAnimator.start();
    }
}