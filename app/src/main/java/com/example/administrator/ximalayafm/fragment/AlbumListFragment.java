package com.example.administrator.ximalayafm.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.ximalayafm.R;
import com.example.administrator.ximalayafm.TingApplication;
import com.example.administrator.ximalayafm.dao.DBUtil;
import com.example.administrator.ximalayafm.data.ViewHolder;
import com.example.administrator.ximalayafm.fragment.base.BaseFragment;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import org.xutils.x;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by le.xin on 2016/11/29.
 */

public class AlbumListFragment extends BaseFragment {
    private static final String TAG = "AlbumListFragment";
    private Context mContext   = getContext();
    ;
    private ListView mListView;
    private TrackAdapter mTrackAdapter;

    private int mPageId = 1;
    private TrackList mTrackHotList = null;
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
    private long albumid;

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

    private void loadData() {
        if (mLoading) {
            return;
        }
        mLoading = true;
        Map<String, String> param = new HashMap<String, String>();
        param.put(DTransferConstants.PAGE, "" + mPageId);
        param.put(DTransferConstants.PAGE_SIZE, "" + 100);
//        param.put(DTransferConstants.ALBUM_ID, 239463+"");
        param.put(DTransferConstants.ALBUM_ID, albumid+"");
        CommonRequest.getTracks(param, new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                if (trackList != null && trackList.getTracks() != null && trackList.getTracks().size() != 0) {
                    mPageId++;

                    if(mTrackHotList == null) {
                        mTrackHotList = trackList;
                    } else {
                        trackList.getTracks().addAll(0 ,mTrackHotList.getTracks());
                        mTrackHotList = trackList;
                    }

                    mTrackAdapter.notifyDataSetChanged();
                }
                mLoading = false;
            }

            @Override
            public void onError(int i, String s) {
                mLoading = false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_albumlist, container, false);
        mListView = (ListView) view.findViewById(R.id.listview_albumlist);
        Bundle bundle = getArguments();
        if (bundle!=null){
            albumid = bundle.getLong("id");
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);


        mXimalaya = CommonRequest.getInstanse();
        mPlayerManager = XmPlayerManager.getInstance(mContext);

        mPlayerManager.addPlayerStatusListener(mPlayerStatusListener);

        mTrackAdapter = new TrackAdapter();
        mListView.setAdapter(mTrackAdapter);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    int count = view.getCount();
                    count = count - 5 > 0 ? count - 5 : count - 1;
                    if (view.getLastVisiblePosition() > count && (mTrackHotList == null || mPageId <= mTrackHotList.getTotalPage())) {
                        loadData();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
                mPlayerManager.playList(mTrackHotList, position);
                ContentValues values = new ContentValues();
                values.put("track_title",mTrackHotList.getTracks().get(position).getTrackTitle());
                values.put("track_intro",mTrackHotList.getTracks().get(position).getTrackIntro());
                values.put("track_tags",mTrackHotList.getTracks().get(position).getTrackTags());
                values.put("download_count",mTrackHotList.getTracks().get(position).getDownloadCount());
                values.put("category_id",mTrackHotList.getTracks().get(position).getCategoryId());
                long result = DBUtil.getInstance(getActivity().getApplicationContext()).insertData(values,"ximalayaFM");
                Log.e("PayTrackFragment", "onItemClick: 数据添加成功 = " + result );
                Log.e(TAG, "onItemClick: "+"renzaisfaljf " );
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("担待是哪个  ===  " + mPlayerManager.hasNextSound()  + "      " + mPlayerManager.getPlayList().size() + "    ");
                    }
                } ,2000);
            }
        });

        loadData();
//        mPlayerManager.playList(mTrackHotList, 0);
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
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item, parent, false);
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
