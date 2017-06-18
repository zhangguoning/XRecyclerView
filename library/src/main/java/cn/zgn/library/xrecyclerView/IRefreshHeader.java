package cn.zgn.library.xrecyclerView;

/**
 * Created by ning on 2017/6/18
 */
public interface IRefreshHeader {
    int STATE_NORMAL = 0;
    int STATE_RELEASE_TO_REFRESH = 1;
    int STATE_REFRESHING = 2;
    int STATE_DONE = 3;

    void setState(int state);

    int getState();

    void onMove(float delta);

    boolean releaseAction();

    void refreshComplate();

    int getVisiableHeight();
}
