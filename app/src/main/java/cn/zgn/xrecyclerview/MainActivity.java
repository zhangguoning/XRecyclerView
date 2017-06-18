package cn.zgn.xrecyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.zgn.library.XRecyclerView;
import cn.zgn.library.xrecyclerView.SimpleDecoration;
import cn.zgn.library.xrecyclerView.util.LogUtil;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private XRecyclerView recyclerView;
    private DemoAdapter adapter ;
    private List<DemoDataBean> adapterData = new ArrayList<>();
    private Random random = new Random();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (XRecyclerView) this.findViewById(R.id.recyclerView);

        for (int i =0 ; i<30; i++){
            DemoDataBean bean = new DemoDataBean();
            bean.setName("Name - " + i);
            adapterData.add(bean);
        }


        adapter = new DemoAdapter();
        adapter.addAll(adapterData);

        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new SimpleDecoration(this,LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        recyclerView.setLoadingMoreEnabled(true);
        recyclerView.setPullRefreshEnabled(true);
        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                LogUtil.e(TAG,"XRecyclerView-->onRefresh()");
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.refreshComplete();
                    }
                },1500);
            }

            @Override
            public void onLoadMore() {
                LogUtil.e(TAG,"XRecyclerView-->onLoadMore()");
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.refreshComplete();
                        DemoDataBean bean = new DemoDataBean();
                        bean.setName("新添加-"+random.nextInt(10000));
                        adapter.add(bean);
                        adapter.notifyDataSetChanged();
                    }
                },1500);
            }
        });
    }

    public void refreshRecV(View view) {
        recyclerView.performPullRefresh(true);
    }
}
