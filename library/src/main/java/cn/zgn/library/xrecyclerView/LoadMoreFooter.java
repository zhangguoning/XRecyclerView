package cn.zgn.library.xrecyclerView;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.zgn.library.R;
import cn.zgn.library.xrecyclerView.util.ViewUtil;

/**
 * Created by ning on 2017/6/18
 */
public class LoadMoreFooter extends LinearLayout implements ILoadMoreFooter{
    private TextView mText;
    private Object mAnimationDrawable;
    private ImageView mImage;
    private LinearLayout mContainer ;

    private int mMeasuredHeight ;

    public LoadMoreFooter(Context context) {
        super(context);
        initView(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public LoadMoreFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void initView(Context context) {

        this.setGravity(Gravity.CENTER);
        this.setOrientation(HORIZONTAL);

        mAnimationDrawable = context.getResources().getDrawable(R.drawable.xrecyclerview_footer_loading_anim);

        mImage = new ImageView(context);
        mImage.setImageResource(R.drawable.xrecyclerview_footer_loading);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewUtil.dp2px(context,20),ViewUtil.dp2px(context,20));
        layoutParams.rightMargin = ViewUtil.dp2px(context,10);
        mImage.setLayoutParams(layoutParams);
        mImage.setScaleType(ImageView.ScaleType.FIT_XY);
        mImage.setImageDrawable((Drawable) mAnimationDrawable);

        mText = new TextView(context);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mText.setTextColor(0xff333333);
        mText.setPadding(ViewUtil.dp2px(context,5),0,0,0);
        mText.setLayoutParams(params);

        mAnimationDrawable = mImage.getDrawable();
        startAnim();

        mContainer = new LinearLayout(context);//不要问我什么在这里多嵌套一层, 我特么也不想,反正不多套一层你会崩的怀疑人生,别问我怎么知道的....
        mContainer.setOrientation(HORIZONTAL);
        mContainer.setGravity(Gravity.CENTER);
        RecyclerView.LayoutParams rootParam = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,RecyclerView.LayoutParams.WRAP_CONTENT);
        rootParam.setMargins(ViewUtil.dp2px(context,10),ViewUtil.dp2px(context,10),ViewUtil.dp2px(context,10),ViewUtil.dp2px(context,10));
        mContainer.setLayoutParams(rootParam);
        mContainer.addView(mImage);
        mContainer.addView(mText);
        mContainer.setPadding(ViewUtil.dp2px(context,10),ViewUtil.dp2px(context,10),ViewUtil.dp2px(context,10),ViewUtil.dp2px(context,10));
        this.addView(mContainer);

        ViewGroup.LayoutParams parentLP = this.getLayoutParams();
        if(parentLP!=null){
            this.getLayoutParams().width = -1;
            this.getLayoutParams().height = -2;

        }else{
            this.setLayoutParams(new LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        }
        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMeasuredHeight = getMeasuredHeight();
    }

    private void stopAnim(){
        if(mAnimationDrawable instanceof Animatable){
            Animatable anim = (Animatable) mAnimationDrawable;
            if(anim.isRunning()){
                anim.stop();
            }
        }
    }

    private void startAnim(){
        if(mAnimationDrawable instanceof Animatable){
            Animatable anim = (Animatable) mAnimationDrawable;
            if(!anim.isRunning()){
                anim.start();
            }
        }
    }


    public void setState(int state) {
        switch (state) {
            case ILoadMoreFooter.STATE_LOADING:
                startAnim();
                mImage.setVisibility(View.VISIBLE);
                mText.setText(getContext().getText(R.string.xrecyclerview_loadmore_loading));
                this.setVisibility(View.VISIBLE);
                break;
            case ILoadMoreFooter.STATE_COMPLETE:
                stopAnim();
                mText.setText(getContext().getText(R.string.xrecyclerview_loadmore_normal));
//                this.setVisibility(View.GONE);
                mImage.setVisibility(View.GONE);

                break;
            case ILoadMoreFooter.STATE_NOMORE:
                stopAnim();
                mText.setText(getContext().getText(R.string.xrecyclerview_loadmore_nomore));
                mText.setVisibility(View.GONE);
                this.setVisibility(View.VISIBLE);
                break;
        }
    }
}
