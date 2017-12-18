package com.example.administrator.ximalayafm.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.ximalayafm.R;
import com.example.administrator.ximalayafm.dao.DBUtil;
import com.example.administrator.ximalayafm.data.ViewHolder;
import com.example.administrator.ximalayafm.fragment.base.BaseFragment;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.httputil.util.freeflow.IFreeFlowBase;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackHotList;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.constants.PlayerConstants;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;
import com.ximalaya.ting.android.sdkdownloader.XmDownloadManager;
import com.ximalaya.ting.android.sdkdownloader.downloadutil.DownloadState;
import com.ximalaya.ting.android.sdkdownloader.downloadutil.IDoSomethingProgress;
import com.ximalaya.ting.android.sdkdownloader.exception.AddDownloadException;


import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by le.xin on 2017/4/21.
 *  1、查询数据库
 *  2、使用分类id获取分类列表数据
 *  3、播放分类列表
 *
 *
 *  推荐界面
 *
 */

public class PayTrackFragment extends BaseFragment  {
    private GridView mListView;
    private Context mContext;
    private TrackAdapter mTrackAdapter;
    private int mPageId = 1;
    private TrackHotList mTrackHotList = null;
    private boolean mLoading = false;
    private String TAG = "PayTrackFragment";
    private Button btn_test;
    private List<String> category_ids = new ArrayList<>();
    private String preID = "0" ;
    private XmPlayerManager xmPlayerManager;
    private TextView nodata;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        mListView = (GridView) view.findViewById(R.id.gridview_schedule);
        nodata = ((TextView) view.findViewById(R.id.nodata_edittext));
        nodata.setVisibility(View.GONE);
        queryDButil();
        return view;
    }
    //查询数据库
    public void queryDButil(){
        Cursor ximalayaFM = DBUtil.getInstance(getActivity().getApplicationContext()).selectData("ximalayaFM", null, null, null, null, null, null);
        Log.e(TAG, "onClick: "+(ximalayaFM != null)+"====="+(ximalayaFM.getCount() > 0)+"=========="+ximalayaFM.moveToNext());
        if (ximalayaFM != null && ximalayaFM.getCount() > 0) {
            do {
                String track_title = ximalayaFM.getString(ximalayaFM.getColumnIndex("track_title"));
                String track_intro = ximalayaFM.getString(ximalayaFM.getColumnIndex("track_intro"));
                String track_tags = ximalayaFM.getString(ximalayaFM.getColumnIndex("track_tags"));
                String download_coun = ximalayaFM.getString(ximalayaFM.getColumnIndex("download_count"));
                String category_id = ximalayaFM.getString(ximalayaFM.getColumnIndex("category_id"));
                Log.e(TAG, "onCreateView: track_title:"+track_title+"\n"+"track_intro:"+track_intro+"\ntrack_tags:"+track_tags+
                        "\ndownload_coun:"+download_coun+"\ncategory:"+category_id);
                category_ids.add(category_id);
            }while(ximalayaFM.moveToNext());
        }
//        Math.random()*category_ids.size().
        for (String category_id : category_ids) {
            if (category_id!=preID){
                loadData(category_id);
                preID=category_id;
            }
        }

    }
    public void loadData(String mCategoryId){
        Map<String, String>map = new HashMap<String, String>();
        map.put(DTransferConstants.CATEGORY_ID, mCategoryId);
        CommonRequest.getHotTracks(map, new IDataCallBack<TrackHotList>() {
            @Override
            public void onSuccess(@Nullable TrackHotList trackHotList) {
                Log.e(TAG, "onSuccess: "+trackHotList.getTracks().size());
                Log.e(TAG, "onSuccess: "+(trackHotList!=null)+"===="+(trackHotList.getTracks()!=null)+"======="+(trackHotList.getTracks().size()>0));
                if (trackHotList!=null&&trackHotList.getTracks()!=null&&trackHotList.getTracks().size()>0){
                    mTrackHotList = trackHotList;
                    mTrackAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        mTrackAdapter = new TrackAdapter();
        mListView.setAdapter(mTrackAdapter);
        xmPlayerManager = XmPlayerManager.getInstance(mContext);
        xmPlayerManager.addPlayerStatusListener(mPlayerStatusListener);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    int count = view.getCount();
                    count = count - 5 > 0 ? count - 5 : count - 1;
                    if (view.getLastVisiblePosition() > count && (mTrackHotList == null || mPageId <= mTrackHotList.getTotalPage())) {
//                        loadData(preID);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(mContext, ""+i, Toast.LENGTH_SHORT).show();
                xmPlayerManager.playList(mTrackHotList,i);
            }
        });

    }
    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView");
        if (xmPlayerManager != null) {
            xmPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
        }
        super.onDestroyView();
    }

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
            PlayableModel curr = xmPlayerManager.getCurrSound();
            if (sound.equals(curr)) {
                holder.content.setBackgroundResource(R.color.selected_bg);
            } else {
                holder.content.setBackgroundColor(Color.WHITE);
            }
            return convertView;
        }

    }
}
