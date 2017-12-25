package com.example.administrator.ximalayafm.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.ximalayafm.R;
import com.example.administrator.ximalayafm.data.ViewHolder;
import com.example.administrator.ximalayafm.dialog.ProgressDialogFragment;
import com.example.administrator.ximalayafm.entry.TitleBean;
import com.example.administrator.ximalayafm.fragment.base.BaseFragment;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.album.SubordinatedAlbum;
import com.ximalaya.ting.android.opensdk.model.category.Category;
import com.ximalaya.ting.android.opensdk.model.category.CategoryList;
import com.ximalaya.ting.android.opensdk.model.tag.Tag;
import com.ximalaya.ting.android.opensdk.model.tag.TagList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackHotList;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * 点播界面
 */

public class TracksFragment extends BaseFragment {
    private static final String TAG = "TracksFragment";
    private Context mContext;
    private GridView mListView;
    private TrackAdapter mTrackAdapter;
    private ListViewAdapter listAdapter;
    private ListView listview;
    private long albumId;
    private List<String> titleBeans =  new ArrayList<>();
    private int mPageId = 1;
    private TrackHotList mTrackHotList = null;
    private TrackList mTrackList = null;
    private boolean mLoading = false;
    private  AlbumListFragment df ;
    private CommonRequest mXimalaya;
    private XmPlayerManager mPlayerManager;

    private IXmPlayerStatusListener mPlayerStatusListener = new IXmPlayerStatusListener() {

        @Override
        public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel) {
            if (mTrackAdapter != null) {
                mTrackAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onSoundPrepared() {
        }

        @Override
        public void onSoundPlayComplete() {
        }

        @Override
        public void onPlayStop() {
        }

        @Override
        public void onPlayStart() {
        }

        @Override
        public void onPlayProgress(int currPos, int duration) {
        }

        @Override
        public void onPlayPause() {
        }

        @Override
        public boolean onError(XmPlayerException exception) {
            return false;

        }

        @Override
        public void onBufferingStop() {
        }

        @Override
        public void onBufferingStart() {
        }

        @Override
        public void onBufferProgress(int percent) {
        }

    };


    public void refresh() {
        Log.e(TAG, "---refresh");
        if (hasMore()) {
            loadData();
        }
    }

    private boolean hasMore() {
        if (mTrackHotList != null && mTrackHotList.getTotalPage() <= mPageId) {
            return false;
        }
        return true;
    }
    private Long category_id = 1l;
    private List<Category> categories;
    private HashMap<Long,String> hashMap = new HashMap<>();
    private void getCategory(){
        Map<String, String> map = new HashMap<String, String>();
        CommonRequest.getCategories(map, new IDataCallBack<CategoryList>() {
            @Override
            public void onSuccess(CategoryList object) {
                categories = object.getCategories();
                if (categories.size()>0){
                    for (Category category : categories) {
                        TitleBean titleBean = new TitleBean(category.getCategoryName(),category.getId(),false);
                        hashMap.put(category.getId(),category.getCategoryName());
                        titleBeans.add(category.getCategoryName());
                    }
//                    setListViewSelect();
                }
            }
            @Override
            public void onError(int code, String message) {
            }
        });
    }
    //语音打开界面时设置listview的选中状态
    private void setListViewSelect(){
        String s = hashMap.get(category_id);
        int i = titleBeans.indexOf(s);
        listAdapter.setSelectItem(i);
    }
    private void loadData() {
        if (mLoading) {
            return;
        }
        final ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.show(getActivity(),R.string.loading);
        mLoading = true;
        Map<String, String> param = new HashMap<String, String>();
        param.put(DTransferConstants.CATEGORY_ID, "" + category_id);
        param.put(DTransferConstants.PAGE, "" + mPageId);
        param.put(DTransferConstants.PAGE_SIZE, "" + 200);
        CommonRequest.getHotTracks(param, new IDataCallBack<TrackHotList>() {
            @Override
            public void onSuccess(TrackHotList object) {
                progressDialogFragment.dismiss(getActivity());
                Log.e("pageid", "onSuccess: "+mPageId);
                if (object != null && object.getTracks() != null && object.getTracks().size() != 0) {
                    mPageId++;
                    mTrackHotList = object;
                    if(df!=null){
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.remove(df).commit();
                        df=null;
                    }
                    mTrackAdapter.notifyDataSetChanged();
                    mPlayerManager.playList(mTrackHotList.getTracks().subList(0 ,1), 0);
                }else{
                    Toast.makeText(mContext, "该页面暂无数据", Toast.LENGTH_SHORT).show();
                }
                mLoading = false;
            }

            @Override
            public void onError(int code, String message) {
                progressDialogFragment.dismiss(getActivity());
                Toast.makeText(mContext, "请检查您的网络连接是否正常", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onError " + code + ", " + message);
                mLoading = false;
            }
        });
    }
    private  Long id =0l;
    private void initCategoryId(String type){
        Log.e(TAG, "initCategoryID:type的值" +type);
        if (type!=null&&type!=""){
            switch (type) {
                case "storytelling"://有声书
                    id = 3l;
                    break;
                case "music"://音乐
                    id = 2l;
                    break;
                case "entertainment"://娱乐
                    id = 4l;
                    break;
                case "comic"://评书
                    id = 12l;
                    break;
                case"children"://儿童
                    id = 6l;
                    break;
                case "experience"://3D体验馆
                    id = 29l;
                    break;
                case"information"://资讯
                    id = 1l;
                    break;
                case"talkshow"://脱口秀
                    id = 28l;
                    break;
                case"emotional"://情感生活
                    id = 10l;
                    break;
                case"humanity"://人文
                    id = 39l;
                    break;
                case"foreignlanguages"://英语
                    id = 38l;
                    break;
                case"littleforeignlanguages"://小语种
                    id = 32l;
                    break;
                case"education"://教育培训
                    id = 13l;
                    break;
                case"broadcastPrograms"://广播剧
                    id = 15l;
                    break;
                case"traditionalopera"://戏曲
                    id = 16l;
                    break;
                case"sinology"://国学书院
                    id = 40l;
                    break;
                case"radiostation"://电台
                    id = 17l;
                    break;
                case"finance"://商业财经
                    id = 8l;
                    break;
                case"technology"://IT科技
                    id = 18l;
                    break;
                case"health"://健康养生
                    id = 7l;
                    break;
                case"tourism"://旅游
                    id = 22l;
                    break;
                case"automobile"://汽车
                    id = 21l;
                    break;
                case "animeGame"://动漫
                    id = 24l;
                    break;
                case"movie"://电影
                    id = 23l;
                    break;
                case"partylecture"://党课随声听
                    id = 41l;
                    break;
                case"openclass"://名校公开课
                    id = 30l;
                    break;
                case"fashion"://时尚生活
                    id = 31l;
                    break;
                case"poetry"://诗歌
                    id = 34l;
                    break;
                case"other"://其他
                    id =11l ;
                    break;
                case "historyHumanism"://历史
                    id = 9l;
                    break;
            }
            Log.e(TAG, "initCategoryId: "+id );
        }
        category_id=id;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        View view = inflater.inflate(R.layout.activity_main, container, false);
        mListView = (GridView) view.findViewById(R.id.list);
        listview =(ListView) view.findViewById(R.id.listview);
        Bundle arguments = getArguments();
        Log.e(TAG, "onCreateView: "+(arguments!=null) );
        if (arguments!=null){
            long data = arguments.getLong("DATA");
            Log.e(TAG, "onCreateView:setCategory_id::: "+data );

            category_id = data;
        }
        return view;
    }
    @Subscribe
    public void onEvent(String event){
        boolean result = event.matches("[a-zA-Z]+");
        if (result){
            Log.e(TAG, "onEvent: 事件传递成功： "+event);
            initCategoryId(event);
            mPageId = 1;
//            getCategory();
            setListViewSelect();
            loadData();
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        mXimalaya = CommonRequest.getInstanse();
        mPlayerManager = XmPlayerManager.getInstance(mContext);
        mPlayerManager.addPlayerStatusListener(mPlayerStatusListener);
        mTrackAdapter = new TrackAdapter();
        listAdapter = new ListViewAdapter();
        mListView.setAdapter(mTrackAdapter);
        listview.setAdapter(listAdapter);
        mListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    int count = view.getCount();
                    count = count - 5 > 0 ? count - 5 : count - 1;
                    if (view.getLastVisiblePosition() > count && (mTrackHotList == null || mPageId < mTrackHotList.getTotalPage())) {
                        loadData();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mPlayerManager.playList(mTrackHotList, position);
//                mPlayerManager.playList(mTrackHotList.getTracks().subList(0 ,1), 0);
                SubordinatedAlbum album = mTrackHotList.getTracks().get(position).getAlbum();
                albumId = album.getAlbumId();
                df = new AlbumListFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();//注意。一个transaction 只能commit一次，所以不要定义成全局变量
                Bundle bundle = new Bundle();
                bundle.putLong("id", albumId);
                df.setArguments(bundle);
                fragmentTransaction.replace(R.id.fragment, df);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View arg1, int i, long l) {
                category_id = categories.get(i).getId();
                Log.e(TAG, "onItemClick: "+category_id );
                mPageId = 1;
                loadData();
                listAdapter.setSelectItem(i);
            }
        });
        getCategory();
        loadData();

    }
    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView");
        if (mPlayerManager != null) {
            mPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
        }
        XmPlayerManager.release();
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }
    public class ListViewAdapter extends BaseAdapter{
        private int  selectItem=-1;

        public  void setSelectItem(int selectItem) {
            this.selectItem = selectItem;
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return titleBeans.size();
        }

        @Override
        public Object getItem(int i) {
            return titleBeans.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.list_item, viewGroup, false);
                holder = new ViewHolder();
                holder.content = (ViewGroup) view;
                holder.textView = (TextView) view.findViewById(R.id.textview);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            if (i == selectItem) {
                holder.content.setBackgroundResource(R.color.selected_bg);
            }
            else {
                holder.content.setBackgroundColor(Color.WHITE);
            }
            holder.textView.setText(titleBeans.get(i));
          return view;
        }
        class ViewHolder {
            TextView textView;
            ViewGroup content;
        }
    }
    public class TrackAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mTrackHotList == null || mTrackHotList.getTracks() == null) {
                return 0;
            }
            return mTrackHotList.getTracks().size();
        }

        @Override
        public Object getItem(int position) {
            return mTrackHotList.getTracks().get(position);

        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.track_content, parent, false);
                holder = new ViewHolder();
                holder.content = (ViewGroup) convertView;
                holder.cover = (ImageView) convertView.findViewById(R.id.imageview);
                holder.title = (TextView) convertView.findViewById(R.id.trackname);
                holder.intro = (TextView) convertView.findViewById(R.id.intro);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Track sound = mTrackHotList.getTracks().get(position);
            holder.title.setText(sound.getTrackTitle());
            holder.intro.setText(sound.getAnnouncer() == null ? sound.getTrackTags() : sound.getAnnouncer().getNickname());
            x.image().bind(holder.cover, sound.getCoverUrlLarge());
            PlayableModel curr = mPlayerManager.getCurrSound();
            if (sound.equals(curr)) {
                holder.content.setBackgroundResource(R.color.selected_bg);
            } else {
                holder.content.setBackgroundColor(Color.WHITE);
            }
            return convertView;
        }

    }
}
