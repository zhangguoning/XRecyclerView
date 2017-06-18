package cn.zgn.xrecyclerview;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Created by ning on 2017/6/18.
 */

public class DemoDataBean  extends BaseObservable {


    private String name ;

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }
}
