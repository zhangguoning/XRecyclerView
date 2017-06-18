package cn.zgn.library.xrecyclerView;

/**
 * Created by ning on 2017/6/18.
 */

public interface ILoadMoreFooter {
    int STATE_LOADING = 0;
    int STATE_COMPLETE = 1;
    int STATE_NOMORE = 2;

    void setState(int state);
}
