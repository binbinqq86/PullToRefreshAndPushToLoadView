package com.binbin.pulltorefreshandpushtoloadview.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

import com.binbin.pulltorefreshandpushtoloadview.R;

import java.security.NoSuchAlgorithmException;

/**
 * Created by -- on 2016/11/2.
 * 自定义下拉刷新上拉加载的基类，可以扩展多种可滑动view(ListView,GridView,RecyclerView...)
 * 第五版：第三版的基础上进行改进，增加上拉加载更多。。。
 * 遗留问题：布局加入padding margin出现问题，不满一屏的处理，item点击问题
 */

public class PullToRefreshAndPushToLoadView5 extends ViewGroup {
    private static final String TAG = "tianbin";
    private Context mContext;
    private Scroller mScroller;
    /**
     * 在被判定为滚动之前用户手指可以移动的最大值。
     */
    private int touchSlop;

    /**
     * 下拉头的高度
     */
    private int hideHeaderHeight;
    /**
     * 上拉底部的高度
     */
    private int hideFooterHeight;

    /**
     * 用于存储上次更新时间
     */
    private SharedPreferences preferences;

    /**
     * 下拉头的View
     */
    private View header;
    /**
     * 上拉底部的View
     */
    private View footer;

    /**
     * 需要去刷新和加载的View
     */
    private View mView;
    /**
     * 本控件的宽高
     */
    private int maxWidth,maxHeight;

    /**
     * footer的进度条
     */
    private ProgressBar pbFooter;
    /**
     * footer的文字描述
     */
    private TextView tvLoadMore;
    /**
     * 刷新时显示的进度条
     */
    private ProgressBar progressBar;

    /**
     * 指示下拉和释放的箭头
     */
    private ImageView arrow;

    /**
     * 指示下拉和释放的文字描述
     */
    private TextView description;

    /**
     * 上次更新时间的文字描述
     */
    private TextView updateAt;

    /**
     * 上次更新时间的毫秒值
     */
    private long lastUpdateTime;

    /**
     * 一分钟的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_MINUTE = 60 * 1000;

    /**
     * 一小时的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_HOUR = 60 * ONE_MINUTE;

    /**
     * 一天的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_DAY = 24 * ONE_HOUR;

    /**
     * 一月的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_MONTH = 30 * ONE_DAY;

    /**
     * 一年的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_YEAR = 12 * ONE_MONTH;

    /**
     * 上次更新时间的字符串常量，用于作为SharedPreferences的键值
     */
    private static final String UPDATED_AT = "updated_at";
    /**
     * 为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，使用id来做区分
     */
    private int mId = -1;

    /**
     * 当前是否在view的顶部，只有View滚动到头的时候才允许下拉
     */
    private boolean isTop;
    /**
     * 当前是否在view的底部，只有View滚动到底的时候才允许上拉
     */
    private boolean isBottom;
    /**
     * 上次手指按下时的屏幕纵坐标
     */
    private float mLastY = -1;
    /**
     * 第一次手指按下时的屏幕纵坐标
     */
    private float mFirstY = -1;
    /**
     * 当前处理什么状态，可选值有STATUS_PULL_TO_REFRESH, STATUS_RELEASE_TO_REFRESH,
     * STATUS_REFRESHING 和 STATUS_REFRESH_FINISHED
     */
    private int currentStatus = STATUS_REFRESH_FINISHED;

    /**
     * 记录上一次的状态是什么，避免进行重复操作
     */
    private int lastStatus = currentStatus;
    /**
     * 下拉状态
     */
    public static final int STATUS_PULL_TO_REFRESH = 0;

    /**
     * 释放立即刷新状态
     */
    public static final int STATUS_RELEASE_TO_REFRESH = 1;

    /**
     * 正在刷新状态
     */
    public static final int STATUS_REFRESHING = 2;

    /**
     * 刷新完成或未刷新状态
     */
    public static final int STATUS_REFRESH_FINISHED = 3;

    /**
     * 当前处理什么状态，STATUS_LOAD_NORMAL, STATUS_LOADING
     */
    private int currentFooterStatus=STATUS_LOAD_NORMAL;
    /**
     * 上拉状态
     */
    public static final int STATUS_LOAD_NORMAL = 4;
    /**
     * 正在加载状态
     */
    public static final int STATUS_LOADING = 5;
    /**
     * 下拉刷新上拉加载的回调接口
     */
    private PullToRefreshAndPushToLoadMoreListener mListener;

    /**
     * 是否正在加载
     */
    private boolean isLoading;
    /**
     * 是否正在刷新
     */
    private boolean isRefreshing;
    /**
     * 是否正在自动刷新
     */
    private boolean autoRefresh;
    /**
     * 是否正在完成刷新
     */
    private boolean isFinishingRefresh=false;

    private static final float DEFAULT_RATIO = 2f;
    /**
     * 判断是否可以拖动（防止多指触摸）
     */
    private boolean canDrag=false;
    /**
     * 拖动阻力系数
     */
    private float ratio = DEFAULT_RATIO;

    private int screenHeight;
    /**
     * 是否支持下拉刷新
     */
    private boolean canRefresh=true;
    /**
     * 是否支持上拉加载
     */
    private boolean canLoadMore=true;

    /**
     * 是否支持滑动到底部自动加载更多
     */
    private boolean canAutoLoadMore=false;

    /**
     * 是否已经完成header,mView,footer的布局
     */
    private boolean hasFinishedLayout=false;

    /**
     * 是否正在触摸
     */
    private boolean isTouching=false;

    private Handler handler = new Handler(Looper.getMainLooper());

    public PullToRefreshAndPushToLoadView5(Context context) {
        this(context, null);
    }

    public PullToRefreshAndPushToLoadView5(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshAndPushToLoadView5(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PullToRefreshAndPushToLoadView5(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context mContext) {
        this.mContext = mContext;
        mScroller = new Scroller(mContext);
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        header = LayoutInflater.from(mContext).inflate(R.layout.refresh_header, null, false);
        progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
        arrow = (ImageView) header.findViewById(R.id.arrow);
        description = (TextView) header.findViewById(R.id.description);
        updateAt = (TextView) header.findViewById(R.id.updated_at);

//        footer = LayoutInflater.from(mContext).inflate(R.layout.loadmore_footer, null, false);
//        pbFooter = (ProgressBar) footer.findViewById(R.id.pb);
//        tvLoadMore = (TextView) footer.findViewById(R.id.tv_load_more);

        LinearLayout ll=new LinearLayout(mContext);
//        LinearLayout.LayoutParams llp=new LinearLayout.LayoutParams(-2,-2);
//        llp.gravity= Gravity.CENTER;
//        llp.height=dp2px(60);
        pbFooter=new ProgressBar(mContext);
//        llp.rightMargin= dp2px(16);
//        pbFooter.setLayoutParams(llp);
        tvLoadMore=new TextView(mContext);
        tvLoadMore.setText(getResources().getString(R.string.load_more_normal));
//        tvLoadMore.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
//        tvLoadMore.setGravity(Gravity.CENTER);
//        llp.rightMargin=0;
//        tvLoadMore.setLayoutParams(llp);
//        ll.addView(pbFooter);
//        ll.addView(tvLoadMore);
        footer=tvLoadMore;

        touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        refreshUpdatedAtValue();
        addView(header, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            if(childView.getVisibility()!=View.GONE){
                //获取每个子view的自己高度宽度，取最大的就是viewGroup的大小
                measureChild(childView, widthMeasureSpec, heightMeasureSpec);
                maxWidth = Math.max(maxWidth,childView.getMeasuredWidth());
                maxHeight = Math.max(maxHeight,childView.getMeasuredHeight());
            }
        }
        //为ViewGroup设置宽高
        setMeasuredDimension(maxWidth, maxHeight);
        Log.e(TAG, "onMeasure: " );
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        Log.e(TAG, "onFinishInflate: ================" +getChildCount());
    }

    /**
     * 进行一些关键性的初始化操作，比如：将下拉头向上偏移进行隐藏，给ListView注册touch事件。
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e(TAG, "onLayout: " );
        if(!hasFinishedLayout){
            mView=getChildAt(1);
            addView(footer);
            hasFinishedLayout=true;

            if(canLoadMore&&canAutoLoadMore){
                setAutoLoadMore();
            }
        }
        if(hideHeaderHeight==0){
            hideHeaderHeight = -header.getHeight();
        }
        if(hideFooterHeight==0){
            hideFooterHeight=footer.getHeight();
//            Log.e(TAG, "onLayout: "+hideFooterHeight+"@"+hideHeaderHeight);
        }
        int top=hideHeaderHeight;
//        header.layout(0,top,maxWidth,top+header.getMeasuredHeight());
//        top+=header.getMeasuredHeight();
//        mView.layout(0,top,maxWidth,top+mView.getMeasuredHeight());
//        top+=mView.getMeasuredHeight();
//        footer.layout(0,top,maxWidth,top+footer.getMeasuredHeight());
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                childView.layout(0, top, maxWidth, top+childView.getMeasuredHeight());
                top+=childView.getMeasuredHeight();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //每次首先进行判断是否在列表顶部或者底部
        judgeIsTop();
        judgeIsBottom();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if(event.getPointerId(event.getActionIndex())==0){
                    mLastY = event.getY(0);
                    mFirstY = event.getY();
                    isTouching=true;
                    canDrag=true;
                }else{
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(!canDrag){
                    break;
                }
                int pointerIndex=event.findPointerIndex(0);
//                float totalDistance = event.getY() - mFirstY;
                float deltaY = event.getY(pointerIndex) - mLastY;
                mLastY = event.getY(pointerIndex);
//                if (Math.abs(deltaY) < touchSlop) {
//                    Log.e(TAG,deltaY+"=============Math.abs(deltaY) < touchSlop============="+touchSlop);
//                    return false;
//                }
                boolean showTop=deltaY>=0 && isTop;
                boolean hideTop=deltaY<=0 && getScrollY()<0;
//                boolean noMove=deltaY==0;//当不动的时候屏蔽一切事件，防止列表滚动
                boolean showBottom=deltaY<=0 && isBottom;
                boolean hideBottom=deltaY>=0 && getScrollY()>0;

//                Log.e(TAG, "dispatchTouchEvent: "+ratio+"+++"+isTop+"###"+getScrollY()+"$$$"+deltaY);
                if((showBottom&&canLoadMore)||hideBottom){
                    if(deltaY<0){
                        if(getScrollY()>=hideFooterHeight){
                            ratio += 0.05f;
                        }
                    }else{
                        ratio=1;
                    }
                    int dy=(int) (deltaY / ratio);
                    if(deltaY>0 && Math.abs(dy)>Math.abs(getScrollY())){
                        //当滑动距离大于可滚动距离时，进行调整
                        dy=Math.abs(getScrollY());
                    }
                    scrollBy(0, -dy);
                    return true;
                }else if ((showTop&&canRefresh)||hideTop) {
                    //说明头部显示，自己处理滑动，无论上滑下滑均同步移动（==0代表滑动到顶部可以继续下拉）
                    if (deltaY < 0) {//来回按住上下移动：下拉逐渐增加难度，上拉不变
                        ratio = 1;//此处如果系数不是1，则会出现列表跳动的现象。。。暂未解决！！！
                    } else {
                        if(Math.abs(getScrollY())>=-hideHeaderHeight){
                            ratio += 0.05f;//当头部露出以后逐步增加下拉难度
                        }
                    }
                    int dy=(int) (deltaY / ratio);
                    if(deltaY<0 && Math.abs(dy)>Math.abs(getScrollY())){
                        //当滑动距离大于可滚动距离时，进行调整
                        dy=-Math.abs(getScrollY());
                    }
//                    Log.e(TAG, "dispatchTouchEvent: "+"###"+getScrollY()+"%%%"+dy);
                    scrollBy(0, -dy);
//                    Log.e(TAG, "dispatchTouchEvent: "+"###"+getScrollY()+"&&&"+dy);
                    if (currentStatus != STATUS_REFRESHING){
                        if (getScrollY() <= hideHeaderHeight) {
                            currentStatus = STATUS_RELEASE_TO_REFRESH;
                        } else {
                            currentStatus = STATUS_PULL_TO_REFRESH;
                        }
                        // 时刻记得更新下拉头中的信息
                        updateHeaderView();
                        lastStatus = currentStatus;
                    }
                    return true;
                }else{
                    return super.dispatchTouchEvent(event);
                }
//                break;
            case MotionEvent.ACTION_POINTER_UP:
            default:
                //重置==============================================
                if(event.getPointerId(event.getActionIndex())==0){
                    canDrag=false;
                }
                ratio = DEFAULT_RATIO;
                isTouching=false;
                //处理顶部==========================================
                if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
                    // 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
                    backToTop();
                } else if (currentStatus == STATUS_PULL_TO_REFRESH) {
                    // 松手时如果是下拉状态，就去调用隐藏下拉头的任务
                    hideHeader(false);
                } else if (currentStatus == STATUS_REFRESHING) {
                    if (getScrollY() <= hideHeaderHeight) {
                        //回弹
                        backToTop();
                    }
                }
                //处理底部===========================================
                if(getScrollY()>0 && getScrollY()<hideFooterHeight && !isLoading){
                    //松手时隐藏底部
                    hideFooter();
                }else if(getScrollY()>=hideFooterHeight){
                    //显示底部，开始加载更多
                    showFooter();
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void hideFooter(){
        currentFooterStatus=STATUS_LOAD_NORMAL;
        isLoading=false;
        mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
        invalidate();
    }

    private void showFooter(){
        currentFooterStatus=STATUS_LOADING;
        updateFooterView();
        mScroller.startScroll(0, getScrollY(), 0, hideFooterHeight - getScrollY());
        invalidate();
        if (mListener != null && !isLoading) {
            isLoading = true;
            mListener.onLoadMore();
        }
    }

    private void autoLoadMore(){
        if (mListener != null && !isLoading) {
            currentFooterStatus=STATUS_LOADING;
            updateFooterView();
            mScroller.startScroll(0, 0, 0, hideFooterHeight);
            invalidate();
            isLoading = true;
            mListener.onLoadMore();
        }
    }

    /**
     * 当所有的加载逻辑完成后，记录调用一下，否则你的View将一直处于正在加载状态。
     */
    public void finishLoading(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                hideFooter();
            }
        });
    }

    private void backToTop() {
        currentStatus = STATUS_REFRESHING;
        updateHeaderView();
        mScroller.startScroll(0, getScrollY(), 0, hideHeaderHeight - getScrollY());
        invalidate();
        if (mListener != null && !isRefreshing) {
            isRefreshing = true;
            mListener.onRefresh();
        }
    }

    private void hideHeader(boolean isRefreshFinished) {
        currentStatus = STATUS_REFRESH_FINISHED;
        lastStatus=currentStatus;
        autoRefresh=false;
        isRefreshing = false;
        isFinishingRefresh=false;
        if(isRefreshFinished){
            //只有刷新完成才更新时间，反弹不算
            preferences.edit().putLong(UPDATED_AT + mId, System.currentTimeMillis()).commit();
        }
        mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
        invalidate();
    }

    /**
     * 自动刷新
     */
    public void autoRefresh(){
        if (mListener != null && !isRefreshing) {
            currentStatus = STATUS_REFRESHING;
            updateHeaderView();
            mScroller.startScroll(0, 0, 0, hideHeaderHeight);
            invalidate();
            isRefreshing = true;
            autoRefresh=true;//放在updateHeaderView后面
            mListener.onRefresh();
        }
    }

    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则你的View将一直处于正在刷新状态。
     */
    public void finishRefreshing() {
        isFinishingRefresh=true;
        handler.post(new Runnable() {
            @Override
            public void run() {
                description.setText(getResources().getString(R.string.refresh_success));
                ((View)arrow.getParent()).setVisibility(View.GONE);
                updateAt.setVisibility(View.GONE);
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideHeader(true);
            }
        },1000);
    }

    @Override
    public void computeScroll() {
        // TODO Auto-generated method stub
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }else{
            //刷新动画完成以后初始化头部并且没有正在显示刷新成功的状态
            if(!isFinishingRefresh){
                updateHeaderView();
            }
            //加载动画完成后更新footer
            updateFooterView();
        }
    }

    /**
     * 根据是否滚动到最底部去进行自动加载更多的操作
     */
    private void setAutoLoadMore(){
        if(mView!=null){
            if(mView instanceof AbsListView){
                AbsListView absListView= (AbsListView) mView;
                absListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        //此处有两种选择：1、绝对的滚动到最底部。2、滚动到最后一个元素就开始去加载，不必显示footer
                        if(isTouching){
                            return;
                        }
                        judgeIsBottom();
                        if(isBottom){
                            autoLoadMore();
                        }
//                        if(view.getLastVisiblePosition()==totalItemCount-1){
//                            autoLoadMore();
//                        }
                    }
                });
            }else if(mView instanceof RecyclerView){
                RecyclerView recyclerView= (RecyclerView) mView;
                recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if(isTouching){
                            return;
                        }
                        judgeIsBottom();
//                        View lastChild = recyclerView.getLayoutManager().findViewByPosition(recyclerView.getAdapter().getItemCount()-1);
                        if(isBottom){
                            //绝对的底部
                            autoLoadMore();
                        }
//                        else if(lastChild!=null){
//                            //最后一个元素可见，但不一定完全可见
//                            autoLoadMore();
//                        }
                    }
                });
            }
        }
    }

    /**
     * 根据当前View的滚动状态来设定 {@link #isBottom}
     * 的值，每次都需要在触摸事件中第一个执行，这样可以判断出当前应该是滚动View，还是应该进行上拉。
     */
    private void judgeIsBottom() {
        if (mView instanceof AbsListView) {
            AbsListView absListView = (AbsListView) mView;
            //返回的是当前屏幕中的第最后一个子view，非整个列表
            View lastChild = absListView.getChildAt(absListView.getLastVisiblePosition()-absListView.getFirstVisiblePosition());
            if (lastChild != null) {
                int lastVisiblePos = absListView.getLastVisiblePosition();//不必完全可见，当前屏幕中最后一个可见的子view在整个列表的位置
                if (lastVisiblePos == absListView.getAdapter().getCount()-1 && lastChild.getBottom() == absListView.getMeasuredHeight()) {
                    // 如果最后一个元素的下边缘，距离父布局值为view的高度，就说明View滚动到了最底部，此时应该允许上拉加载
                    isBottom = true;
                } else {
                    isBottom = false;
                }
            } else {
                // 如果View中没有元素，也应该允许下拉刷新，但不允许上拉
                isBottom = false;
            }
        } else if (mView instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) mView;
            View lastChild = recyclerView.getLayoutManager().findViewByPosition(recyclerView.getAdapter().getItemCount()-1);//lastChild不必须完全可见
            View firstVisibleChild = recyclerView.getChildAt(0);//返回的是当前屏幕中的第一个子view，非整个列表
//            if(lastChild!=null){
//                Log.e("tianbin",lastChild.getBottom()+"==="+recyclerView.getChildAt(0).getTop()+"==="+recyclerView.getLayoutManager().getDecoratedBottom(lastChild));
//            }else{
//                Log.e("tianbin","+++++++++");
//            }
            if (firstVisibleChild != null) {
//                Log.e(TAG, "judgeIsBottom: "+"@@@@@@@@@@@@@@"+"#"+ recyclerView.getMeasuredHeight());
                if (lastChild != null &&
                        recyclerView.getLayoutManager().getDecoratedBottom(lastChild) == recyclerView.getMeasuredHeight()) {
//                    Log.e(TAG, "judgeIsBottom: "+"==================" );
                    isBottom = true;
                } else {
                    isBottom = false;
                }
            } else {
                //没有元素也允许刷新，but不允许上拉
                isBottom = false;
            }
        } else {
            isBottom = true;
        }
    }
    /**
     * 根据当前View的滚动状态来设定 {@link #isTop}
     * 的值，每次都需要在触摸事件中第一个执行，这样可以判断出当前应该是滚动View，还是应该进行下拉。
     */
    private void judgeIsTop() {
        if (mView instanceof AbsListView) {
            AbsListView absListView = (AbsListView) mView;
            View firstChild = absListView.getChildAt(0);//返回的是当前屏幕中的第一个子view，非整个列表
            if (firstChild != null) {
                int firstVisiblePos = absListView.getFirstVisiblePosition();//不必完全可见，当前屏幕中第一个可见的子view在整个列表的位置
                if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
                    // 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
                    isTop = true;
                } else {
                    isTop = false;
                }
            } else {
                // 如果ListView中没有元素，也应该允许下拉刷新
                isTop = true;
            }
        } else if (mView instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) mView;
            View firstChild = recyclerView.getLayoutManager().findViewByPosition(0);//firstChild不必须完全可见
            View firstVisibleChild = recyclerView.getChildAt(0);//返回的是当前屏幕中的第一个子view，非整个列表
//            if(firstChild!=null){
//                Log.e("tianbin",firstChild.getTop()+"==="+recyclerView.getChildAt(0).getTop());
//            }else{
//                Log.e("tianbin","+++++++++");
//            }
            if (firstVisibleChild != null) {
                if (firstChild != null && recyclerView.getLayoutManager().getDecoratedTop(firstChild) == 0) {
                    isTop = true;
                } else {
                    isTop = false;
                }
            } else {
                //没有元素也允许刷新
                isTop = true;
            }
        } else {
            isTop = true;
        }
    }

    /**
     * 刷新下拉头中上次更新时间的文字描述。
     */
    private void refreshUpdatedAtValue() {
        lastUpdateTime = preferences.getLong(UPDATED_AT + mId, -1);
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastUpdateTime;
        long timeIntoFormat;
        String updateAtValue;
        if (lastUpdateTime == -1) {
            updateAtValue = getResources().getString(R.string.not_updated_yet);
        } else if (timePassed < 0) {
            updateAtValue = getResources().getString(R.string.time_error);
        } else if (timePassed < ONE_MINUTE) {
            updateAtValue = getResources().getString(R.string.updated_just_now);
        } else if (timePassed < ONE_HOUR) {
            timeIntoFormat = timePassed / ONE_MINUTE;
            String value = timeIntoFormat + "分钟";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else if (timePassed < ONE_DAY) {
            timeIntoFormat = timePassed / ONE_HOUR;
            String value = timeIntoFormat + "小时";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else if (timePassed < ONE_MONTH) {
            timeIntoFormat = timePassed / ONE_DAY;
            String value = timeIntoFormat + "天";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else if (timePassed < ONE_YEAR) {
            timeIntoFormat = timePassed / ONE_MONTH;
            String value = timeIntoFormat + "个月";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else {
            timeIntoFormat = timePassed / ONE_YEAR;
            String value = timeIntoFormat + "年";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        }
        updateAt.setVisibility(View.VISIBLE);
        updateAt.setText(updateAtValue);
    }

    /**
     * 更新底部信息
     */
    private void updateFooterView(){
        if(currentFooterStatus==STATUS_LOAD_NORMAL){
            tvLoadMore.setText(getResources().getString(R.string.load_more_normal));
            pbFooter.setVisibility(View.GONE);
        }else if(currentFooterStatus==STATUS_LOADING){
            tvLoadMore.setText(getResources().getString(R.string.load_more_loading));
            pbFooter.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新下拉头中的信息
     */
    private void updateHeaderView() {
        ((View)arrow.getParent()).setVisibility(View.VISIBLE);
        if (lastStatus != currentStatus && lastStatus!=STATUS_REFRESH_FINISHED) {
            if (currentStatus == STATUS_PULL_TO_REFRESH) {
                description.setText(getResources().getString(R.string.pull_to_refresh));
                arrow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                rotateArrow();
            } else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
                description.setText(getResources().getString(R.string.release_to_refresh));
                arrow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                rotateArrow();
            } else if (currentStatus == STATUS_REFRESHING) {
                description.setText(getResources().getString(R.string.refreshing));
                progressBar.setVisibility(View.VISIBLE);
                arrow.clearAnimation();
                arrow.setVisibility(View.GONE);
            }
        }else if(currentStatus==STATUS_REFRESH_FINISHED&&lastStatus==STATUS_REFRESH_FINISHED){
            //说明刷新完成或者第一次进入
            description.setText(getResources().getString(R.string.pull_to_refresh));
            arrow.clearAnimation();
            arrow.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }else if(autoRefresh){
            //自动刷新单独处理
            description.setText(getResources().getString(R.string.refreshing));
            progressBar.setVisibility(View.VISIBLE);
            arrow.clearAnimation();
            arrow.setVisibility(View.GONE);
        }
        refreshUpdatedAtValue();
    }

    /**
     * 根据当前的状态来旋转箭头。
     */
    private void rotateArrow() {
        float pivotX = arrow.getWidth() / 2f;
        float pivotY = arrow.getHeight() / 2f;
        float fromDegrees = 0f;
        float toDegrees = 0f;
        if (currentStatus == STATUS_PULL_TO_REFRESH) {
            fromDegrees = 180f;
            toDegrees = 360f;
        } else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
            fromDegrees = 0f;
            toDegrees = 180f;
        }
        RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
        animation.setDuration(300);
        animation.setFillAfter(true);
        arrow.startAnimation(animation);
    }

    public boolean isCanLoadMore() {
        return canLoadMore;
    }

    public void setCanLoadMore(boolean canLoadMore) {
        this.canLoadMore = canLoadMore;
    }

    public boolean isCanRefresh() {
        return canRefresh;
    }

    public void setCanRefresh(boolean canRefresh) {
        this.canRefresh = canRefresh;
    }

    public boolean isCanAutoLoadMore() {
        return canAutoLoadMore;
    }

    public void setCanAutoLoadMore(boolean canAutoLoadMore) {
        this.canAutoLoadMore = canAutoLoadMore;
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }

    public boolean isLoading() {
        return isLoading;
    }

    /**
     * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
     */
    public interface PullToRefreshAndPushToLoadMoreListener {

        /**
         * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。注意此方法是在主线程中调用的， 需要另开线程来进行耗时操作。
         */
        void onRefresh();

        /**
         * 加载更多时会去回调此方法，在方法内编写具体的加载更多逻辑。注意此方法是在主线程中调用的， 需要另开线程来进行耗时操作。
         */
        void onLoadMore();

    }

    /**
     * 给下拉刷新控件注册一个监听器。
     *
     * @param listener 监听器的实现。
     * @param id       为了防止不同界面的下拉刷新在上次更新时间上互相有冲突， 请不同界面在注册下拉刷新监听器时一定要传入不同的id。
     *                 如果不用时间则可以不传递此参数
     */
    public void setOnRefreshAndLoadMoreListener(PullToRefreshAndPushToLoadMoreListener listener, int id) {
        mListener = listener;
        mId = id;
    }

    /**
     * 给下拉刷新控件注册一个监听器。
     *
     * @param listener 监听器的实现。
     */
    public void setOnRefreshAndLoadMoreListener(PullToRefreshAndPushToLoadMoreListener listener) {
        setOnRefreshAndLoadMoreListener(listener, mId);
    }

    private int dp2px(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,mContext.getResources().getDisplayMetrics());
    }

}
