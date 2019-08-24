package ru.tinkoff.scrollingpagerindicator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

/**
 * @author Nikita Olifer
 */
public class ViewPager2Attacher implements ScrollingPagerIndicator.PagerAttacher<ViewPager2> {

    private RecyclerView.AdapterDataObserver dataObserver;
    private ViewPager2.OnPageChangeCallback onPageChangeListener;
    private ViewPager2 pager;
    private RecyclerView.Adapter<?> attachedAdapter;

    @Override
    public void attachToPager(@NonNull final ScrollingPagerIndicator indicator, @NonNull final ViewPager2 pager) {
        attachedAdapter = pager.getAdapter();
        if (attachedAdapter == null) {
            throw new IllegalStateException("Set adapter before call attachToPager() method");
        }

        this.pager = pager;

        indicator.setDotCount(attachedAdapter.getItemCount());
        indicator.setCurrentPosition(pager.getCurrentItem());

        dataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                indicator.reattach();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                onChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                onChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                onChanged();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                onChanged();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                onChanged();
            }
        };
        attachedAdapter.registerAdapterDataObserver(dataObserver);

        onPageChangeListener = new ViewPager2.OnPageChangeCallback() {

            boolean idleState = true;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixel) {
                final float offset;
                // ViewPager may emit negative positionOffset for very fast scrolling
                if (positionOffset < 0) {
                    offset = 0;
                } else if (positionOffset > 1) {
                    offset = 1;
                } else {
                    offset = positionOffset;
                }
                indicator.onPageScrolled(position, offset);
            }

            @Override
            public void onPageSelected(int position) {
                if (idleState) {
                    indicator.setDotCount(attachedAdapter.getItemCount());
                    indicator.setCurrentPosition(pager.getCurrentItem());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                idleState = state == ViewPager2.SCROLL_STATE_IDLE;
            }
        };
        pager.registerOnPageChangeCallback(onPageChangeListener);
    }

    @Override
    public void detachFromPager() {
        attachedAdapter.unregisterAdapterDataObserver(dataObserver);
        pager.unregisterOnPageChangeCallback(onPageChangeListener);
    }
}
