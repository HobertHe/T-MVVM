package com.code.mvvm.core.view.swipe;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;

import com.code.mvvm.R;
import com.code.mvvm.base.LifecycleFragment;
import com.code.mvvm.core.data.pojo.banner.BannerListVo;
import com.code.mvvm.core.viewmodel.BaseViewModel;
import com.code.mvvm.util.DiffCallback;
import com.code.mvvm.util.DisplayUtil;
import com.trecyclerview.SwipeRecyclerView;
import com.trecyclerview.entity.FootInfo;
import com.trecyclerview.listener.OnLoadMoreListener;
import com.trecyclerview.multitype.Items;
import com.trecyclerview.multitype.MultiTypeAdapter;

import java.util.Collection;

import static com.trecyclerview.LoadingMoreFooter.STATE_LOADING;

/**
 * @author：zhangtianqiu on 18/7/10 16:20
 */
public abstract class SwipeListFragment<T extends BaseViewModel> extends LifecycleFragment<T> implements OnLoadMoreListener {
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    protected SwipeRecyclerView mRecyclerView;

    protected RecyclerView.LayoutManager layoutManager;

    protected MultiTypeAdapter adapter;

    protected String lastid = null;

    protected boolean isLoadMore = true;

    protected boolean isLoading = true;

    protected boolean isRefresh = false;

    protected Items oldItems;

    protected Items newItems;

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_swipe_list;
    }

    @Override
    public void initView(Bundle state) {
        super.initView(state);
        mRecyclerView = getViewById(R.id.recycler_view);
        mSwipeRefreshLayout = getViewById(R.id.swipe_layout);
        //设置刷新时动画的颜色，可以设置4个
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setProgressViewOffset(false, 0, DisplayUtil.dp2px(getActivity(), 48));
            mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_blue_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                lastid = null;
                isRefresh = true;
                onRefreshAction();
            }
        });
        oldItems = new Items();
        newItems = new Items();
        adapter = createAdapter();
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(createLayoutManager());
        mRecyclerView.addOnLoadMoreListener(this);
    }

    @Override
    protected void lazyLoad() {
        super.lazyLoad();
        isLoadMore = false;
    }

    protected void setData(Collection<?> collection) {
        if (isLoadMore) {
            onLoadMoreSuccess(collection);
        } else {
            if (isRefresh) {
                onRefreshSuccess(collection);
            } else {
                onDefaultSuccess(collection);
            }
        }
    }


    @Override
    public void onLoadMore() {
        isLoadMore = true;
        if (isLoading) {
            isLoading = false;
            addFootData();
            getRemoteData();
        }
    }

    protected void onRefreshAction() {
    }

    protected void setBannerData(BannerListVo headAdList) {
        if (isRefresh) {
            newItems.add(headAdList);
        } else {
            oldItems.add(headAdList);
        }
    }


    protected void onRefreshSuccess(Collection<?> collection) {
        newItems.addAll(collection);
        refreshDataChanged();
    }

    protected void onDefaultSuccess(Collection<?> collection) {
        oldItems.addAll(collection);
        adapter.setItems(oldItems);
        notifyDataSetChanged();
    }

    protected void onLoadMoreSuccess(Collection<?> collection) {
        oldItems.remove(oldItems.size() - 1);
        oldItems.addAll(collection);
        notifyMoreDataChanged(oldItems.size() - collection.size(), oldItems.size());
        mRecyclerView.loadMoreComplete();
        isLoading = true;
        isLoadMore = false;
        newItems.clear();
    }

    protected void refreshDataChanged() {
        oldItems.clear();
        oldItems.addAll(newItems);
        notifyDataSetChanged();
//        mRecyclerView.refreshComplete();
        isRefresh = false;
        newItems.clear();
    }


    protected void diffNotifyDataChanged() {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffCallback(oldItems, newItems), true);
        oldItems.clear();
        oldItems.addAll(newItems);
        newItems.clear();
        result.dispatchUpdatesTo(adapter);
    }

    protected void notifyDataSetChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    protected void notifyMoreDataChanged(int positionStart, int itemCount) {
        if (adapter != null) {
            adapter.notifyItemRangeChanged(positionStart, itemCount);
        }
    }

    protected void addFootData() {
        oldItems.add(new FootInfo(STATE_LOADING));
        notifyMoreDataChanged(oldItems.size() - 1, oldItems.size());
    }

    /**
     * @return
     */
    protected abstract MultiTypeAdapter createAdapter();

    /**
     * @return
     */
    protected abstract RecyclerView.LayoutManager createLayoutManager();


}