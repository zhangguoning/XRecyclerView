package cn.zgn.library.xrecyclerView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
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
public class RefreshHeader extends LinearLayout implements IRefreshHeader {
    private Context mContext;
    private View rootView;
    private Object animationDrawable;
    private ImageView mImage;
    private TextView mText;
    private int mState = STATE_NORMAL;
    private int mMeasuredHeight;
    private LinearLayout mContainer ;

    public RefreshHeader(Context context) {
        this(context, null);
    }

    public RefreshHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        this.setGravity(Gravity.CENTER);
        this.setOrientation(HORIZONTAL);

        animationDrawable =  mContext.getResources().getDrawable(R.drawable.xrecyclerview_header_loading_anim);

        mImage = new ImageView(mContext);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewUtil.dp2px(mContext,20),ViewUtil.dp2px(mContext,20));
        mImage.setLayoutParams(layoutParams);
        mImage.setScaleType(ImageView.ScaleType.FIT_XY);
        mImage.setImageDrawable((Drawable) animationDrawable);

        mText = new TextView(mContext);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mText.setTextColor(0xff333333);
        mText.setLayoutParams(params);
        mText.setText(mContext.getString(R.string.xrecyclerview_refresh_normal));
        mText.setPadding(ViewUtil.dp2px(mContext,5),0,0,0);

        mContainer = new LinearLayout(mContext);//不要问我什么在这里多嵌套一层, 我特么也不想,反正不多套一层你会崩的怀疑人生,别问我怎么知道的....
        mContainer.setOrientation(HORIZONTAL);
        mContainer.setGravity(Gravity.CENTER);
        RecyclerView.LayoutParams rootParam = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,RecyclerView.LayoutParams.WRAP_CONTENT);
        rootParam.setMargins(ViewUtil.dp2px(mContext,10),ViewUtil.dp2px(mContext,10),ViewUtil.dp2px(mContext,10),ViewUtil.dp2px(mContext,10));
        mContainer.setLayoutParams(rootParam);
        mContainer.addView(mImage);
        mContainer.addView(mText);
        mContainer.setPadding(ViewUtil.dp2px(mContext,10),ViewUtil.dp2px(mContext,10),ViewUtil.dp2px(mContext,10),ViewUtil.dp2px(mContext,10));
        this.addView(mContainer);


        animationDrawable =  mImage.getDrawable();
        stopAnim();
        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMeasuredHeight = getMeasuredHeight();
        setGravity(Gravity.CENTER_HORIZONTAL);
        ViewGroup.LayoutParams parentLP = this.getLayoutParams();
        if(parentLP!=null){
            this.getLayoutParams().width = -1;
            this.getLayoutParams().height = -2;

        }else{
            this.setLayoutParams(new LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        }
        reset();
    }

    private void stopAnim(){
        if(animationDrawable instanceof Animatable){
            Animatable anim = (Animatable) animationDrawable;
            if(anim.isRunning()){
                anim.stop();
            }
        }
    }

    private void startAnim(){
        if(animationDrawable instanceof Animatable){
            Animatable anim = (Animatable) animationDrawable;
            if(!anim.isRunning()){
                anim.start();
            }
        }
    }


    @Override
    public void onMove(float delta) {
        if (getVisiableHeight() > 0 || delta > 0) {
            setVisiableHeight((int) delta + getVisiableHeight());
            if (mState <= STATE_RELEASE_TO_REFRESH) { // 未处于刷新状态，更新箭头
                if (getVisiableHeight() > mMeasuredHeight) {
                    setState(STATE_RELEASE_TO_REFRESH);
                } else {
                    setState(STATE_NORMAL);
                }
            }
        }
    }
    @Override
    public void setState(int state) {
        if (state == mState) return;
        switch (state) {
            case STATE_NORMAL:
                stopAnim();
                mText.setText(R.string.xrecyclerview_refresh_normal);
                break;
            case STATE_RELEASE_TO_REFRESH:
                if (mState != STATE_RELEASE_TO_REFRESH) {
                    startAnim();
                    mText.setText(R.string.xrecyclerview_refresh_release);
                }
                break;
            case STATE_REFRESHING:
                mText.setText(R.string.xrecyclerview_refresh_refreshing);
                break;
            case STATE_DONE:
                mText.setText(R.string.xrecyclerview_refresh_done);
                break;
            default:
        }
        mState = state;
    }

    @Override
    public boolean releaseAction() {
        boolean isOnRefresh = false;
        int height = getVisiableHeight();
        if (height == 0) // not visible.
            isOnRefresh = false;

        if (getVisiableHeight() > mMeasuredHeight && mState < STATE_REFRESHING) {
            setState(STATE_REFRESHING);
            isOnRefresh = true;
        }
        // refreshing and header isn't shown fully. do nothing.
        if (mState == STATE_REFRESHING && height <= mMeasuredHeight) {
            //return;
        }
        int destHeight = 0; // default: scroll back to dismiss header.
        // is refreshing, just scroll back to show all the header.
        if (mState == STATE_REFRESHING) {
            destHeight = mMeasuredHeight;
        }
        smoothScrollTo(destHeight);

        return isOnRefresh;
    }

    @Override
    public void refreshComplate() {
        setState(STATE_DONE);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                reset();
            }
        }, 500);
    }

    public void reset() {
        smoothScrollTo(0);
        setState(STATE_NORMAL);
    }

    private void smoothScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisiableHeight(), destHeight);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setVisiableHeight((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    private void setVisiableHeight(int height) {
        if (height < 0)
            height = 0;
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    @Override
    public int getVisiableHeight() {
        return mContainer.getHeight();
    }


    public int getState() {
        return mState;
    }
}
