package cn.zgn.library;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import cn.zgn.library.xrecyclerView.IRefreshHeader;
import cn.zgn.library.xrecyclerView.LoadMoreFooter;
import cn.zgn.library.xrecyclerView.RefreshHeader;
import cn.zgn.library.xrecyclerView.WrapAdapter;
import cn.zgn.library.xrecyclerView.util.ViewUtil;

import static cn.zgn.library.xrecyclerView.ILoadMoreFooter.STATE_LOADING;

/**
 * Created by ning on 2017/2/7.
 */
public class XRecyclerView extends RecyclerView {
    private LoadingListener mLoadingListener;
    private WrapAdapter mWrapAdapter;
    private SparseArray<View> mHeaderViews = new SparseArray<>();
    private SparseArray<View> mFootViews = new SparseArray<>();
    private boolean pullRefreshEnabled = true;
    private boolean loadingMoreEnabled = false;
    private IRefreshHeader mRefreshHeader;
    private LoadMoreFooter footer;
    private boolean isLoadingData;
    public int previousTotal;
    //    public boolean isnomore;
    private boolean isNetworkDiagnosisEnable = false;

    private float mLastY = -1;
    private static final float DRAG_RATE = 1.75f;
    // 是否是额外添加FooterView
    private boolean isOther = false;

//    private boolean mIsRefreshing ;//是否正在滑动

    public XRecyclerView(Context context) {
        this(context, null);
    }

    public XRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        if (pullRefreshEnabled) {
            RefreshHeader refreshHeader = new RefreshHeader(context);
            mHeaderViews.put(0, refreshHeader);
            mRefreshHeader = refreshHeader;
        }
        LoadMoreFooter footView = new LoadMoreFooter(context);
        addFootView(footView, false);
        mFootViews.get(0).setVisibility(GONE);
//        this.setOnTouchListener((v, event) -> {
//            if (mIsRefreshing) {
//                return true;
//            } else {
//                return false;
//            }
//        });

    }

    /**
     * 改为公有。供外添加view使用,使用标识
     * 注意：使用后不能使用 上拉刷新，否则添加无效
     * 使用时 isOther 传入 true，然后调用 noMoreLoading即可。
     */
    public void addFootView(final View view, boolean isOther) {
        mFootViews.clear();
        mFootViews.put(0, view);
        this.isOther = isOther;
    }

    /**
     * 相当于加一个空白头布局：
     * 只有一个目的：为了滚动条显示在最顶端
     * 因为默认加了刷新头布局，不处理滚动条会下移。
     * 和 setPullRefreshEnabled(false) 一块儿使用
     * 使用下拉头时，此方法不应被使用！
     */
    public void clearHeader() {
        mHeaderViews.clear();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int height = (int) (1.0f * scale + 0.5f);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        View view = new View(getContext());
        view.setLayoutParams(params);
        mHeaderViews.put(0, view);
    }

    public void addHeaderView(View view) {
        if (pullRefreshEnabled && !(mHeaderViews.get(0) instanceof RefreshHeader)) {
            RefreshHeader refreshHeader = new RefreshHeader(getContext());
            mHeaderViews.put(0, refreshHeader);
            mRefreshHeader = refreshHeader;
        }
        mHeaderViews.put(mHeaderViews.size(), view);
    }


    private void loadMoreComplete() {
        isLoadingData = false;
        View footView = mFootViews.get(0);
        if (previousTotal <= getLayoutManager().getItemCount()) {
            if (footView instanceof LoadMoreFooter) {
                footer.setState(LoadMoreFooter.STATE_COMPLETE);
//                mFootViews.clear();
            } else if (footView != null) {
                footView.setVisibility(View.GONE);
            }
        } else {
            if (footView instanceof LoadMoreFooter) {
                footer.setState(LoadMoreFooter.STATE_NOMORE);
//                mFootViews.clear();
            } else if (footView != null) {
                footView.setVisibility(View.GONE);
            }
//            isnomore = true;
        }
        previousTotal = getLayoutManager().getItemCount();
    }

    public void noMoreLoading() {
        isLoadingData = false;
        final View footView = mFootViews.get(0);
//        isnomore = true;
        if (footView instanceof LoadMoreFooter) {
            ((LoadMoreFooter) footView).setState(LoadMoreFooter.STATE_NOMORE);
        } else if (footView != null) {
            footView.setVisibility(View.GONE);
        }
        // 额外添加的footView
        if (isOther && footView != null) {
            footView.setVisibility(View.VISIBLE);
        }
    }

    public void refreshComplete() {
        //  mRefreshHeader.refreshComplate();
        if (isLoadingData) {
            loadMoreComplete();
        } else {
            mRefreshHeader.refreshComplate();
//            mIsRefreshing = false ;
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mWrapAdapter = new WrapAdapter(mHeaderViews, mFootViews, adapter);
        super.setAdapter(mWrapAdapter);
        adapter.registerAdapterDataObserver(mDataObserver);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

        if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadingListener != null && !isLoadingData && loadingMoreEnabled) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            if (layoutManager.getChildCount() > 0
                    && lastVisibleItemPosition >= layoutManager.getItemCount() - 1
                    && layoutManager.getItemCount() > layoutManager.getChildCount()
                    && mRefreshHeader.getState() < RefreshHeader.STATE_REFRESHING
                    && loadingMoreEnabled
                    ) {

                isLoadingData = true;

                if(loadingMoreEnabled && mFootViews.size()>0){
                    footer.setState(STATE_LOADING);
                    footer.setVisibility(View.VISIBLE);
                }


                if (isNetworkDiagnosisEnable) {
                    if (isNetWorkConnected(getContext())) {
                        mLoadingListener.onLoadMore();
                    } else {
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mLoadingListener.onLoadMore();
                            }
                        }, 1000);
                    }
                } else {
                    mLoadingListener.onLoadMore();
                }

            }
        }
    }

    public void performPullRefresh(boolean scroll2Top) {
        if (scroll2Top) {
            this.smoothScrollToPosition(0);
        }

        if (mRefreshHeader.getVisiableHeight() > 0 && mRefreshHeader.getState() < RefreshHeader.STATE_REFRESHING) {
            return;
        }
        int[] height = ViewUtil.getUnDisplayViewSize((View) mRefreshHeader);
        mRefreshHeader.onMove(height[1]);
        //     isnomore = false;
        mLoadingListener.onRefresh();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (isOnTop() && pullRefreshEnabled) {
                    mRefreshHeader.onMove(deltaY / DRAG_RATE);
                    if (mRefreshHeader.getVisiableHeight() > 0 && mRefreshHeader.getState() < RefreshHeader.STATE_REFRESHING) {
                        return false;
                    }
                }
                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && pullRefreshEnabled) {
                    if (mRefreshHeader.releaseAction()) {
                        if (mLoadingListener != null) {
//                            mIsRefreshing = true ;
                            mLoadingListener.onRefresh();
//                            isnomore = false;
                            previousTotal = 0;
                            final View footView = mFootViews.get(0);
                            if (footView != null && footView instanceof LoadMoreFooter) {
                                if (footView.getVisibility() != View.GONE) {
                                    footView.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private int findMin(int[] firstPositions) {
        int min = firstPositions[0];
        for (int value : firstPositions) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    public boolean isOnTop() {
        if (mHeaderViews == null || mHeaderViews.size() == 0) {
            return false;
        }

        View view = mHeaderViews.get(0);
        if (view.getParent() != null) {
            return true;
        } else {
            return false;
        }
    }

    private final AdapterDataObserver mDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            mWrapAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWrapAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    };


    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    public void setPullRefreshEnabled(boolean pullRefreshEnabled) {
        this.pullRefreshEnabled = pullRefreshEnabled;
    }

    public void setLoadingMoreEnabled(boolean loadingMoreEnabled) {
        if (this.loadingMoreEnabled != loadingMoreEnabled) {
            this.loadingMoreEnabled = loadingMoreEnabled;
            if (!loadingMoreEnabled) {
                if (mFootViews != null) {
                    mFootViews.remove(0);
                }
            } else {
                if (mFootViews != null) {
                    footer = new LoadMoreFooter(getContext());
                    addFootView(footer, false);
                }
            }
        }

    }


    public void setLoadMoreGone() {
        if (mFootViews == null) {
            return;
        }
        View footView = mFootViews.get(0);
        if (footView != null && footView instanceof LoadMoreFooter) {
            mFootViews.remove(0);
        }
    }

    public interface LoadingListener {

        void onRefresh();

        void onLoadMore();
    }

    /**
     * 检测网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

//    public void reset() {
////        isnomore = false;
//        final View footView = mFootViews.get(0);
//        if (footView!=null && footView instanceof LoadMoreFooter) {
//            ((LoadMoreFooter) footView).reSet();
//        }
//    }

    public boolean isPullRefreshEnabled() {
        return pullRefreshEnabled;
    }
}
