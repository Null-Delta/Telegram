package org.telegram.ui.ProfileActivityV2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScrollerCustom;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SharedMediaLayout;

public class ProfileScrollController extends RecyclerView.OnScrollListener {
    public abstract static class ProfileScrollListener {
        public abstract void onHeaderHeightChanged(float height);
        public abstract void onHeaderExpanded();
        public abstract void onHeaderCollapsed();
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {}
    }

    public RecyclerListView listView;
    public LinearLayoutManager layoutManager;
    public ProfileActivityV2.HeaderSizeConfiguration configuration;
    private ProfileActivityDataProvider dataProvider;

    private ValueAnimator expandingAnimator;
    private boolean isExpanded = false;
    private float currentExpandingProgress = 0;
    private float additionalScrollTouchOffset = 0;
    private boolean wasActionDown = false;
    private boolean canExpand() {
        return listView.getContext().getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE &&
                dataProvider.hasPhoto() &&
                !dataProvider.isTopic;
    }

    private ProfileScrollListener listener;

    public float currentHeaderHeight() {
        if (overscrollOffset() <= 0) {
            return configuration.minimizedHeight;
        }

        return Math.max(configuration.minimizedHeight, overscrollOffset() + listView.getTranslationY() + configuration.minimizedHeight);
    }

    public float overscrollOffset() {
        View view = layoutManager.findViewByPosition(0);
        if (view == null) { return 0; }

        return view.getTop();
    }

    public ProfileScrollController(
            Context context,
            ProfileActivityV2.HeaderSizeConfiguration configuration,
            ProfileActivityDataProvider dataProvider,
            NotificationCenter notificationCenter
    ) {
        this.configuration = configuration;
        this.dataProvider = dataProvider;

        layoutManager = new LinearLayoutManager(context) {
            @Override
            public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
                Log.i("SCROLL", "" + dy);

                float k = overscrollOffset() > configuration.defaultHeight - configuration.minimizedHeight && dy < 0 ? 0.5f : 1f;

                if (overscrollOffset() <= 0 || overscrollOffset() >= listView.getPaddingTop()) {
                    listener.onHeaderHeightChanged(currentHeaderHeight());
                } else {
                    listener.onHeaderHeightChanged(Math.min(Math.max(currentHeaderHeight() - dy * k, configuration.minimizedHeight), configuration.expandedHeight + AndroidUtilities.dp(44)));
                }

                updateListView();

                return super.scrollVerticallyBy((int)(dy * k), recycler, state);
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.mIgnoreTopPadding = false;

        this.listView = new RecyclerListView(context) {
            @Override
            public boolean onTouchEvent(MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_DOWN || (e.getAction() == MotionEvent.ACTION_MOVE && !wasActionDown)) {
                    if ( e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_MOVE) {
                        wasActionDown = true;
                    } else if (e.getAction() == MotionEvent.ACTION_UP) {
                        wasActionDown = false;
                    }

                    additionalScrollTouchOffset = listView.getTranslationY();
                }

                e.setLocation(e.getAxisValue(0), e.getAxisValue(1) - additionalScrollTouchOffset + listView.getTranslationY());
                return super.onTouchEvent(e);
            }
        };

        listView.setVerticalScrollBarEnabled(false);
        listView.setClipToPadding(false);
        listView.setHideIfEmpty(false);

        listView.setLayoutManager(layoutManager);
        listView.setOnScrollListener(this);

        listView.setOverScrollMode(RecyclerListView.OVER_SCROLL_NEVER);
        listView.setPadding(
            0,
            (int)(configuration.defaultHeight - configuration.minimizedHeight) + AndroidUtilities.dp(44),
            0,
            0
        );

        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator() {

            int animationIndex = -1;

            @Override
            protected void onAllAnimationsDone() {
                super.onAllAnimationsDone();
                AndroidUtilities.runOnUIThread(() -> {
                    notificationCenter.onAnimationFinish(animationIndex);
                });
            }

            @Override
            public void runPendingAnimations() {
                boolean removalsPending = !mPendingRemovals.isEmpty();
                boolean movesPending = !mPendingMoves.isEmpty();
                boolean changesPending = !mPendingChanges.isEmpty();
                boolean additionsPending = !mPendingAdditions.isEmpty();
                if (removalsPending || movesPending || additionsPending || changesPending) {
                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1f);
                    valueAnimator.addUpdateListener(valueAnimator1 -> listView.invalidate());
                    valueAnimator.setDuration(getMoveDuration());
                    valueAnimator.start();
                    animationIndex = notificationCenter.setAnimationInProgress(animationIndex, null);
                }
                super.runPendingAnimations();
            }

            @Override
            protected long getAddAnimationDelay(long removeDuration, long moveDuration, long changeDuration) {
                return 0;
            }

            @Override
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                super.onMoveAnimationUpdate(holder);
//                updateBottomButtonY();
            }
        };
        listView.setItemAnimator(defaultItemAnimator);
        defaultItemAnimator.setMoveDelay(0);
        defaultItemAnimator.setMoveDuration(320);
        defaultItemAnimator.setRemoveDuration(320);
        defaultItemAnimator.setAddDuration(320);
        defaultItemAnimator.setSupportsChangeAnimations(false);
        defaultItemAnimator.setDelayAnimations(false);
        defaultItemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);

        layoutManager.scrollToPositionWithOffset(
            0,
            (int)(-listView.getPaddingTop() + configuration.defaultHeight - configuration.minimizedHeight)
        );

        listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            hookToNearestPoint();
        }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        updateListView();
        if (listener != null) {
            listener.onScrolled(recyclerView, dx, dy);
        }
    }

    public void setOnScrollListener(ProfileScrollListener listener) {
        this.listener = listener;
    }

    public void finish() {
        if (expandingAnimator != null) {
            expandingAnimator.cancel();
            expandingAnimator = null;
        }
    }

    public void expand() {
        if (!canExpand()) { return; }
        additionalScrollTouchOffset = 0;
        listener.onHeaderExpanded();
        updateState(true);
    }

    public void collapse() {
        additionalScrollTouchOffset = 0;
        listener.onHeaderCollapsed();
        updateState(false);
    }

    public void reset() {
        additionalScrollTouchOffset = 0;
        listView.setTranslationY(0);
    }

    private void updateListView() {
        if (
            overscrollOffset() > configuration.defaultHeight - configuration.minimizedHeight + AndroidUtilities.dp(22) &&
            !isExpanded && listView.getScrollState() != RecyclerView.SCROLL_STATE_SETTLING && canExpand()
        ) {
            listener.onHeaderExpanded();
            updateState(true);
        } else if (overscrollOffset() < configuration.defaultHeight - configuration.minimizedHeight && isExpanded) {
            listener.onHeaderCollapsed();
            updateState(false);
        }
    }

    private void updateState(boolean toExpanded) {
        if (expandingAnimator != null) {
            expandingAnimator.cancel();
        }

        expandingAnimator = ValueAnimator.ofFloat(currentExpandingProgress, toExpanded ? 1 : 0);
        expandingAnimator.setDuration(ProfileActivityV2.expandingAnimationDuration);
        expandingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        expandingAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            currentExpandingProgress = value;
            listView.setTranslationY(value * (configuration.expandedHeight - configuration.defaultHeight));
            listener.onHeaderHeightChanged(currentHeaderHeight());
        });
        expandingAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isExpanded = toExpanded;
            }
        });
        expandingAnimator.start();
    }

    private void hookToNearestPoint() {
        if (overscrollOffset() <= 0) { return; }

        if (overscrollOffset() < (configuration.defaultHeight - configuration.minimizedHeight) / 2) {
            listView.smoothScrollBy(0,  (int) overscrollOffset());
        } else if (overscrollOffset() > (configuration.defaultHeight - configuration.minimizedHeight) / 2) {
            listView.smoothScrollBy(0,  (int) (overscrollOffset() - (configuration.defaultHeight - configuration.minimizedHeight)));
        }
    }

    public void scrollToSharedMedia(boolean animated, int position) {
        if (animated) {
            LinearSmoothScrollerCustom linearSmoothScroller = new LinearSmoothScrollerCustom(listView.getContext(), LinearSmoothScrollerCustom.POSITION_TOP, .6f);
            linearSmoothScroller.setTargetPosition(position);
            linearSmoothScroller.setOffset(-listView.getPaddingTop());
            layoutManager.startSmoothScroll(linearSmoothScroller);
        } else {
            layoutManager.scrollToPositionWithOffset(position, -listView.getPaddingTop());
        }
    }
}
