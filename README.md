# XRecyclerView
自带下拉刷新和上拉加载更多的RecyclerView , 并且可以手动触发刷新! 内部封装了支持 DataBinding 的 BaseRecyclerViewAdapter 和 BaseRecyclerViewHolder,使其使用RecyclerView更加简单
![](https://github.com/zhangguoning/XRecyclerView/raw/master/preview.gif)
<br/>
Gradle 引入:

在你的 project的 build.gradle 中添加:
```groovy
allprojects{
    repositories {
        mavenCentral()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```
在你的 app 的 build.gradle中 添加:
```groovy
dependencies {
    compile 'com.github.zhangguoning:XRecyclerView:1.0'
}
```
1.布局文件中：
```xml
 <cn.zgn.library.XRecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        />

  <Button
         android:layout_width="wrap_content"
         android:layout_height="wrap_content"
         android:text="点击刷新RecyclerView"
         android:onClick="refreshRecV"/>
  ```
 2.代码中:
 ```java
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

 public void refreshRecV(View view) {
     recyclerView.performPullRefresh(true);//自动刷新RecyclerView
 }
 ```
 其他说明:提供了支持DataBing的 BaseRecyclerViewAdapter 和 BaseRecyclerViewHolder ,
 以及一个简单的 ItemDecoration(SimpleDecoration,一条 1px 的类似于ListView的分割线),
 可以自己定义XRecyclerView的刷新头部和尾部,自定义头部需要实现 IRefreshHeader 接口 , 自定义尾部需要实现 ILoadMoreFooter接口
