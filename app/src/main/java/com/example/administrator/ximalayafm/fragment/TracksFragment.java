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

import com.example.administrator.ximalayafm.R;
import com.example.administrator.ximalayafm.data.ViewHolder;
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
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TracksFragment extends BaseFragment {
    private static final String TAG = "TracksFragment";
    private Context mContext;
    private GridView mListView;
    private TrackAdapter mTrackAdapter;
    private ArrayAdapter listAdapter;
    private ListView listview;
    private List<String> list =  new ArrayList<>();
    private int mPageId = 1;
    private TrackHotList mTrackHotList = null;
    private boolean mLoading = false;

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
    private void getCategory(){
        Map<String, String> map = new HashMap<String, String>();
        CommonRequest.getCategories(map, new IDataCallBack<CategoryList>() {
            @Override
            public void onSuccess(CategoryList object) {
                categories = object.getCategories();
                if (categories.size()>0){
                    for (Category category : categories) {
                        list.add(category.getCategoryName());
                    }
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(int code, String message) {
            }
        });
    }
    private void loadData() {
        if (mLoading) {
            return;
        }
//        mPageId=1;
        mLoading = true;
        Map<String, String> param = new HashMap<String, String>();
        param.put(DTransferConstants.CATEGORY_ID, "" + category_id);
        param.put(DTransferConstants.PAGE, "" + mPageId);
        param.put(DTransferConstants.PAGE_SIZE, "" + 200);
        CommonRequest.getHotTracks(param, new IDataCallBack<TrackHotList>() {
            @Override
            public void onSuccess(TrackHotList object) {
                Log.e("pageid", "onSuccess: "+mPageId);
                if (object != null && object.getTracks() != null && object.getTracks().size() != 0) {
                    mPageId++;
                    mTrackHotList = object;
                    mTrackAdapter.notifyDataSetChanged();
                }
                List<Track> list = mTrackHotList.getTracks();
                List li = new ArrayList();
                for (int i = 0; i < list.size(); i++) {
//                    li.add(list.get(i).getPlayUrl64M4a());
                    Track sound = list.get(i);
                    li.add( sound.getAnnouncer() == null ? sound.getTrackTags() : sound.getAnnouncer().getNickname());
                }

                System.out.println(li);
                mLoading = false;
            }

            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "onError " + code + ", " + message);
                mLoading = false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_main, container, false);
        mListView = (GridView) view.findViewById(R.id.list);
        listview =(ListView) view.findViewById(R.id.listview);
        return view;
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
        listAdapter = new ArrayAdapter<String>(getContext(),R.layout.support_simple_spinner_dropdown_item,list);
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
                long albumId = album.getAlbumId();
                AlbumListFragment df = new AlbumListFragment();
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
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                category_id = categories.get(i).getId();
                mPageId = 1;
                loadData();
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
        super.onDestroyView();
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
