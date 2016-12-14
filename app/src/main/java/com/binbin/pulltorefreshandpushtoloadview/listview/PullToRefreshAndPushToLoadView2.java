package com.binbin.pulltorefreshandpushtoloadview.listview;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.binbin.pulltorefreshandpushtoloadview.R;

/**
 * Created by -- on 2016/11/2.
 * 自定义下拉刷新上拉加载的基类，可以扩展多种可滑动view(ListView,GridView,RecyclerView...)
 */

public class PullToRefreshAndPushToLoadView2 extends LinearLayout implements View.OnTouchListener{
    private Context mContext;
    /**
     * 在被判定为滚动之前用户手指可以移动的最大值。
     */
    private int touchSlop;

    /**
     * 是否已加载过一次layout，这里onLayout中的初始化只需加载一次
     */
    private boolean loadOnce;
    /**
     * 下拉头的高度
     */
    private int hideHeaderHeight;

    /**
     * 用于存储上次更新时间
     */
    private SharedPreferences preferences;

    /**
     * 下拉头的View
     */
    private View header;

    /**
     * 需要去下拉刷新的ListView
     */
    private ListView listView;

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
     * 下拉头的布局参数
     */
    private MarginLayoutParams headerLayoutParams;

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
     * 当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
     */
    private boolean isTop;
    /**
     * 手指按下时的屏幕纵坐标
     */
    private float yDown;
    /**
     * 当前处理什么状态，可选值有STATUS_PULL_TO_REFRESH, STATUS_RELEASE_TO_REFRESH,
     * STATUS_REFRESHING 和 STATUS_REFRESH_FINISHED
     */
    private int currentStatus = STATUS_REFRESH_FINISHED;;

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
     * 下拉头部回滚的速度
     */
    public static final int SCROLL_SPEED = -20;

    /**
     * 下拉刷新的回调接口
     */
    private PullToRefreshListener mListener;

    /**
     * 是否正在刷新
     */
    private boolean isRefreshing;

    private static final float DEFAULT_RATIO=0.5f;
    /**
     * 拖动阻力系数
     */
    private float ratio=DEFAULT_RATIO;

    private float mLastDistance;

    /**
     * 头部正在被拖动
     */
    private static final int HANDLER_ACTION_UPDATING_HEADER=0;
    /**
     * 头部显示
     */
    private static final int HANDLER_ACTION_UPDATE_HEADER_END_SHOW=1;
    /**
     * 头部隐藏
     */
    private static final int HANDLER_ACTION_UPDATE_HEADER_END_HIDE=2;

    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLER_ACTION_UPDATE_HEADER_END_HIDE:
                    updateHeader((int)msg.obj);
                    preferences.edit().putLong(UPDATED_AT + mId, System.currentTimeMillis()).commit();
                    break;
                case HANDLER_ACTION_UPDATE_HEADER_END_SHOW:
                    updateHeader((int)msg.obj);
                    updateHeaderView();
                    break;
                case HANDLER_ACTION_UPDATING_HEADER:
                    updateHeader((int)msg.obj);
                    break;
            }
        }
    };


    public PullToRefreshAndPushToLoadView2(Context context) {
        this(context,null);
    }

    public PullToRefreshAndPushToLoadView2(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PullToRefreshAndPushToLoadView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PullToRefreshAndPushToLoadView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context mContext){
        this.mContext=mContext;
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        header = LayoutInflater.from(mContext).inflate(R.layout.refresh_header, null, true);
        progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
        arrow = (ImageView) header.findViewById(R.id.arrow);
        description = (TextView) header.findViewById(R.id.description);
        updateAt = (TextView) header.findViewById(R.id.updated_at);
        touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        refreshUpdatedAtValue();
        setOrientation(VERTICAL);
        addView(header, 0);
    }
    /**
     * 进行一些关键性的初始化操作，比如：将下拉头向上偏移进行隐藏，给ListView注册touch事件。
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !loadOnce) {
            hideHeaderHeight = -header.getHeight();
            headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
            headerLayoutParams.topMargin = hideHeaderHeight;
            listView = (ListView) getChildAt(1);
            listView.setOnTouchListener(this);
            loadOnce = true;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        setIsTop();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                yDown=event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float yMove = event.getRawY();
                float distance = yMove - yDown;//滑动的总距离
//                yDown=event.getRawY();
                float deltaY=Math.abs(mLastDistance-distance);
                mLastDistance=distance;
                if (distance < touchSlop) {
                    return false;
                }
                Log.e("tianbin",distance+"###"+headerLayoutParams.topMargin+"###"+isTop);
                if(currentStatus==STATUS_REFRESHING){
                    //如果正在刷新
                    ratio-=0.01;//逐步增加下拉难度
                    headerLayoutParams.topMargin += (int)(deltaY * ratio);
//                    headerLayoutParams.topMargin = (int)(distance * ratio);
                    header.setLayoutParams(headerLayoutParams);
                    return true;
                }else{
                    // 如果手指是上滑状态，并且下拉头是完全隐藏的，就屏蔽下拉事件
                    if (distance <= 0 && headerLayoutParams.topMargin <= hideHeaderHeight) {
                        return false;
                    }
                    if (headerLayoutParams.topMargin > 0) {
                        currentStatus = STATUS_RELEASE_TO_REFRESH;
                    } else {
                        currentStatus = STATUS_PULL_TO_REFRESH;
                    }
                    // 通过偏移下拉头的topMargin值，来实现下拉效果
                    headerLayoutParams.topMargin += (int)(deltaY * ratio);
//                    headerLayoutParams.topMargin = (int)(distance * ratio) + hideHeaderHeight;
                    header.setLayoutParams(headerLayoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
            default:
                ratio=DEFAULT_RATIO;//重置
                mLastDistance=0;
                if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
                    // 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
                    backToTop();
                } else if (currentStatus == STATUS_PULL_TO_REFRESH) {
                    // 松手时如果是下拉状态，就去调用隐藏下拉头的任务
                    hideHeader();
                }else if(currentStatus==STATUS_REFRESHING){
                    if(headerLayoutParams.topMargin>0){
                        //回弹
                        backToTop();
                    }
                }
                break;
        }
        // 时刻记得更新下拉头中的信息
        if (currentStatus == STATUS_PULL_TO_REFRESH
                || currentStatus == STATUS_RELEASE_TO_REFRESH) {
            updateHeaderView();
            // 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
            listView.setPressed(false);
            listView.setFocusable(false);
            listView.setFocusableInTouchMode(false);
            lastStatus = currentStatus;
            // 当前正处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
            return true;
        }
        return false;
    }

    /**
     * 时时更新头部位置
     * @param top
     */
    private void updateHeader(int top){
        headerLayoutParams.topMargin = top;
        header.setLayoutParams(headerLayoutParams);
    }

    /**
     * 正在刷新，回弹到顶部
     */
    private void backToTop() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                int topMargin = headerLayoutParams.topMargin;
                while (true) {
                    topMargin = topMargin + SCROLL_SPEED;
                    if (topMargin <= 0) {
                        topMargin = 0;
                        break;
                    }
                    Message msg=handler.obtainMessage();
                    msg.obj=topMargin;
                    msg.what=HANDLER_ACTION_UPDATING_HEADER;
                    handler.sendMessage(msg);
                    sleeping(10);
                }
                currentStatus = STATUS_REFRESHING;
                Message msg=handler.obtainMessage();
                msg.obj=0;
                msg.what=HANDLER_ACTION_UPDATE_HEADER_END_SHOW;
                handler.sendMessage(msg);
                if (mListener != null&&!isRefreshing) {
                    isRefreshing=true;
                    mListener.onRefresh();
                }
            }
        }.start();
    }

    /**
     * 隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏。
     */
    private void hideHeader(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                int topMargin = headerLayoutParams.topMargin;
                while (true) {
                    topMargin = topMargin + SCROLL_SPEED;
                    if (topMargin <= hideHeaderHeight) {
                        topMargin = hideHeaderHeight;
                        break;
                    }
                    Message msg=handler.obtainMessage();
                    msg.obj=topMargin;
                    msg.what=HANDLER_ACTION_UPDATING_HEADER;
                    handler.sendMessage(msg);
                    sleeping(10);
                }

                currentStatus = STATUS_REFRESH_FINISHED;
                isRefreshing=false;
                Message msg=handler.obtainMessage();
                msg.obj=hideHeaderHeight;
                msg.what=HANDLER_ACTION_UPDATE_HEADER_END_HIDE;
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 根据当前ListView的滚动状态来设定 {@link #isTop}
     * 的值，每次都需要在onTouch中第一个执行，这样可以判断出当前应该是滚动ListView，还是应该进行下拉。
     */
    private void setIsTop() {
        View firstChild = listView.getChildAt(0);
        if (firstChild != null) {
            int firstVisiblePos = listView.getFirstVisiblePosition();
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
        updateAt.setText(updateAtValue);
    }

    /**
     * 使当前线程睡眠指定的毫秒数。
     *
     * @param time
     *            指定当前线程睡眠多久，以毫秒为单位
     */
    private void sleeping(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
     */
    public interface PullToRefreshListener {

        /**
         * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。注意此方法是在子线程中调用的， 你可以不必另开线程来进行耗时操作。
         */
        void onRefresh();

    }
    /**
     * 给下拉刷新控件注册一个监听器。
     *
     * @param listener
     *            监听器的实现。
     * @param id
     *            为了防止不同界面的下拉刷新在上次更新时间上互相有冲突， 请不同界面在注册下拉刷新监听器时一定要传入不同的id。
     *            如果不用时间则可以不传递此参数
     */
    public void setOnRefreshListener(PullToRefreshListener listener, int id) {
        mListener = listener;
        mId = id;
    }

    /**
     * 给下拉刷新控件注册一个监听器。
     *
     * @param listener
     *            监听器的实现。
     */
    public void setOnRefreshListener(PullToRefreshListener listener) {
        setOnRefreshListener(listener,mId);
    }

    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则你的ListView将一直处于正在刷新状态。
     */
    public void finishRefreshing() {
        hideHeader();
    }
    /**
     * 更新下拉头中的信息。
     */
    private void updateHeaderView() {
        if (lastStatus != currentStatus) {
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
            refreshUpdatedAtValue();
        }
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
        animation.setDuration(100);
        animation.setFillAfter(true);
        arrow.startAnimation(animation);
    }
}
