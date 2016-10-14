package com.will.sxlib.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.will.sxlib.util.ErrorCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2016/7/17.
 * <p>一个简单的RecyclerViewAdapter</p>
 * <p>到末尾自动加载更多，加载更多失败后，即{@link #update(boolean)}返回false后,将判定为加载失败
 * 此时将在末尾展示loadingFailedView</p>
 * <p>loadingFailedView的点击默认实现为重新加载，可以通过{@link #setOnReloadListener(OnReloadClickListener)}替换</p>
 * <p>提供了refreshData方法刷新内容</p>
 * <p>提供默认ItemAnimation，可通过重写{@link #getItemAnimation()}返回自定义Animation,{@link #useItemAnimation()}可取消使用ItemAnimation </p>
 */
public abstract class CustomRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;
    private static final int TYPE_LOADING_FAILED = 2;
    private ArrayList<T> data;
    private int pageIndex = 1;
    private boolean isLoading;
    private OnItemClickListener itemClickListener;
    private OnReloadClickListener reloadClickListener;
    private int loadingViewRes;
    private int layoutRes;
    private int loadingFailedViewRes;
    private boolean loadingSuccessful = true;
    private RecyclerView mRecyclerView;

    private int lastAnimatedItemIndex = -1;
    public CustomRecyclerAdapter(int layoutRes, int loadingViewRes, int loadingFailedViewRes){
        data = new ArrayList<>();
        this.layoutRes = layoutRes;
        this.loadingViewRes = loadingViewRes;
        this.loadingFailedViewRes = loadingFailedViewRes;
    }

    /**
     * 返回是否有更多数据，这个返回值将影响到本次加载完后下次到末尾时是否刷新
     * @return 返回值
     */
    public abstract boolean hasMoreData();

    /**
     * <p>加载内容，当下拉至末尾时会调用此方法</p>
     * 因为是加载内容，故多为异步加载，在异步任务完成后，务必调用{@link #update(boolean)} 提交更新，无论成功与否.
     * <p>@建议：如果任务失败/完成过快会造成loadingView显示时间过短，影响视觉效果，可以用postDelayed控制update的延时来控制</p>
     * @param page 加载页数，初始值为1
     *
     */
    public abstract void loadData(int page);


    public abstract void convert(BaseRecyclerViewHolder holder, T item );





    @Override
    public int getItemViewType(int position){
        if(hasMoreData() && position == data.size() ){
            return loadingSuccessful ? TYPE_LOADING : TYPE_LOADING_FAILED;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount(){
        return hasMoreData() ? data.size()+1 : data.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position){
        if(holder instanceof BaseRecyclerViewHolder){
            if( useItemAnimation() && position > lastAnimatedItemIndex){
                animate(holder.itemView);
                lastAnimatedItemIndex = position;
            }
            convert((BaseRecyclerViewHolder) holder,data.get(position));
        }else if(holder instanceof CustomRecyclerAdapter.LoadingViewHolder) {
            if(!isLoading){
                isLoading = true;
                loadData(pageIndex);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if(mRecyclerView == null){
            this.mRecyclerView = recyclerView;
        }
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type){
        View v;
        if(type == TYPE_ITEM){
            v = LayoutInflater.from(parent.getContext()).inflate(layoutRes,parent,false);
            final BaseRecyclerViewHolder holder = new BaseRecyclerViewHolder(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickListener != null){
                        itemClickListener.onItemClicked(data.get(holder.getAdapterPosition()),holder);
                    }
                }
            });
            return holder;
        }else if(type == TYPE_LOADING) {
            v = LayoutInflater.from(parent.getContext()).inflate(loadingViewRes,parent,false);
            return new LoadingViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(loadingFailedViewRes,parent,false);
            return new LoadingFailedViewHolder(v);
        }

    }
    public class LoadingViewHolder extends RecyclerView.ViewHolder{

        public LoadingViewHolder(View v){
            super(v);
        }
    }
    public class LoadingFailedViewHolder extends RecyclerView.ViewHolder{
        public LoadingFailedViewHolder(View v){
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(reloadClickListener == null){
                        reLoadData();
                    }else{
                        reloadClickListener.onReload();
                    }
                }
            });
        }
    }

    /**
     *  异步任务完成后将数据加载进data，并刷新显示
     * @param which 成功/失败,若为失败，则不递增load次数
     * @param newData 新数据
     * @return 添加后data的size
     */
    public int update(boolean which,List<T> newData){
        data.addAll(newData);
        update(which);
        return data.size();
    }

    /**
     *异步任务完成后将数据加载进data，并刷新显示
     * @param which 成功/失败,若为失败，则不递增load次数
     */
    public void update(boolean which){
        isLoading = false;
        loadingSuccessful = which;
        if(which){
            pageIndex++;
        }
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                if(!loadingSuccessful){
                    mRecyclerView.smoothScrollToPosition(getItemCount()-1);
                }
            }
        });
    }
    public RecyclerView getRecyclerView(){
        return mRecyclerView;
    }
    public List<T> getData(){
        return data;
    }

    /**
     * 是否正在加载，配合swipeRefreshLayout时调用此方法，避免加载冲突
     * @return isLoading
     */
    public boolean isLoading(){
        return isLoading;
    }
    /**
     * 清除已有数据重新获得，刷新显示
     * @param newData data
     */
    public void refreshData(List<T> newData){
        data.clear();
        pageIndex = 1;
        update(true,newData);
    }

    /**
     * 清除已有数据并重新调用loadData
     */
    public void refreshData(){
        data.clear();
        pageIndex = 1;
        loadData(1);
    }


    private void reLoadData(){
        loadingSuccessful = true;
        notifyDataSetChanged();

    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.itemClickListener = listener;
    }
    public void setOnReloadListener(OnReloadClickListener listener){
        this.reloadClickListener = listener;
    }
    public interface OnItemClickListener{
        void onItemClicked(Object item,BaseRecyclerViewHolder holder);
    }
    public interface OnRefreshCallback{
        void onSuccess();
        void onFailure(ErrorCode code);
    }
    public interface OnReloadClickListener{
        void onReload();
    }

    /**
     * 为item添加动画。此处使用了延时队列，主要是为了保证最先出现的几个ItemAnimation的次序性
     * <p>但同时的，因为有延时存在，会出现ItemView已经展示完毕动画才开始的情况，故在将任务加入队列时隐藏该view,在队列任务执行时
     * 再将ItemView设为可见.</p>
     * <p>不过我总觉得这可能会出现一些问题...</p>
     * @param view 将要执行动画的View
     */
    private void animate(final View view){
        view.setVisibility(View.INVISIBLE);
        getRecyclerView().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.startAnimation(getItemAnimation());
                view.setVisibility(View.VISIBLE);
            }
        },50);
    }

    /**
     * 重写本方法可使用自定义itemAnimation
     * @return animation
     */
    protected Animation getItemAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(getRecyclerView().getWidth(),getRecyclerView().getX(),0,0);
        translateAnimation.setDuration(300);
        return translateAnimation;
    }

    /**
     * 重写此方法可以取消itemAnimation
     * @return 是否执行animation
     */
    protected boolean useItemAnimation(){
        return true;
    }
}
