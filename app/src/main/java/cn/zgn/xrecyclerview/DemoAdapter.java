package cn.zgn.xrecyclerview;


import android.view.ViewGroup;

import cn.zgn.library.xrecyclerView.adapter.BaseRecyclerViewAdapter;
import cn.zgn.library.xrecyclerView.adapter.BaseRecyclerViewHolder;
import cn.zgn.xrecyclerview.databinding.ViewItemBinding;

/**
 * Created by ning on 2017/6/18.
 */

public class DemoAdapter extends BaseRecyclerViewAdapter<DemoDataBean> {


    @Override
    public BaseRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(parent,R.layout.view_item);
    }

    public static class ViewHolder extends BaseRecyclerViewHolder<DemoDataBean,ViewItemBinding>{

        public ViewHolder(ViewGroup viewGroup, int layoutId) {
            super(viewGroup, layoutId);
        }

        @Override
        public void onBindViewHolder(DemoDataBean bean, int position) {
            binding.setData(bean);
        }
    }
}
