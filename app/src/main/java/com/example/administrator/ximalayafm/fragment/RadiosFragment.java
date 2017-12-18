/**
 * RadiosFragment.java
 * com.ximalaya.ting.android.opensdk.test
 * <p/>
 * <p/>
 * ver     date      		author
 * ──────────────────────────────────
 * 2015-5-25 		jack.qin
 * <p/>
 * Copyright (c) 2015, TNT All Rights Reserved.
 */

package com.example.administrator.ximalayafm.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.ximalayafm.R;
import com.example.administrator.ximalayafm.dao.DBUtil;
import com.example.administrator.ximalayafm.data.ViewHolder;
import com.example.administrator.ximalayafm.fragment.base.BaseFragment;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.live.provinces.Province;
import com.ximalaya.ting.android.opensdk.model.live.provinces.ProvinceList;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.radio.RadioCategory;
import com.ximalaya.ting.android.opensdk.model.live.radio.RadioCategoryList;
import com.ximalaya.ting.android.opensdk.model.live.radio.RadioList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * ClassName:RadiosFragment
 *
 * @author jack.qin
 * @Date 2015-5-25		下午8:17:47
 * @see
 * @since Ver 1.1
 *
 * 地方电台直播界面
 *
 */
public class RadiosFragment extends BaseFragment {
    private int mRadioType = 2;
    private RadioAdapter mRadioAdapter;
    private List<Radio> mRadios = new ArrayList<Radio>();
    private ListView mListView;
    private List<Province> list = new ArrayList<>();
    private Context mContext;
    private Long provinceCode = 360000l;
    private XmPlayerManager mPlayerServiceManager;

    private boolean mLoading = false;

    private IXmPlayerStatusListener mPlayerStatusListener = new IXmPlayerStatusListener() {
        @Override
        public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel) {
            if (mRadioAdapter != null) {
                mRadioAdapter.notifyDataSetChanged();
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
    private ListView mGridview;
    private ListViewAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.live_fragment, container, false);
        mListView = (ListView) view.findViewById(R.id.listview_radios);
        mGridview = ((ListView) view.findViewById(R.id.gridview_radios));
        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        mPlayerServiceManager = XmPlayerManager.getInstance(mContext);
        mPlayerServiceManager.addPlayerStatusListener(mPlayerStatusListener);
        mRadioAdapter = new RadioAdapter();
        mGridview.setAdapter(mRadioAdapter);
        mListAdapter = new ListViewAdapter();
        mListView.setAdapter(mListAdapter);
        mGridview.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Radio radio = mRadios.get(position);
//                mPlayerServiceManager.playRadio(radio);
                mPlayerServiceManager.playLiveRadioForSDK(radio ,-1 , 0);
                mRadioAdapter.setSelectItem(position);
                mRadioAdapter.notifyDataSetChanged();
            }
        });
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                provinceCode = list.get(i).getProvinceCode();
                loadRadios();
                mListAdapter.setSelectItem(i);
                mListAdapter.notifyDataSetChanged();
            }
        });
        getRadiosCategory();
        loadRadios();
    }

    @Override
    public void onDestroyView() {
        if (mPlayerServiceManager != null) {
            mPlayerServiceManager.removePlayerStatusListener(mPlayerStatusListener);
        }
        super.onDestroyView();
    }

    @Override
    public void refresh() {
        loadRadios();
    }

    /**
     * 获取直播分类
     */
    public void getRadiosCategory(){
        Map<String, String> map = new HashMap<String, String>();
        CommonRequest.getProvinces(map, new IDataCallBack<ProvinceList>() {
            @Override
            public void onSuccess(@Nullable ProvinceList provinceList) {
                List<Province> provinces = provinceList.getProvinceList();
                if (provinces.size()>0){
                    list.addAll(provinces);
                    mListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    /**
     * 加载直播数据
     */
    public void loadRadios() {
        if (mLoading) {
            return;
        }
        mLoading = true;
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.RADIOTYPE, "" + mRadioType);
        map.put(DTransferConstants.PROVINCECODE, "" + provinceCode);
        CommonRequest.getRadios(map, new IDataCallBack<RadioList>() {

            @Override
            public void onSuccess(RadioList object) {
                if (object != null && object.getRadios() != null) {
                    mRadios.clear();
                    mRadios.addAll(object.getRadios());
                    mRadioAdapter.notifyDataSetChanged();
                }
                mLoading = false;
            }

            @Override
            public void onError(int code, String message) {
                mLoading = false;
            }
        });
    }

    public class ListViewAdapter extends BaseAdapter{
        private int  selectItem=-1;

        public  void setSelectItem(int selectItem) {
            this.selectItem = selectItem;
        }
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
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
            holder.textView.setText(list.get(i).getProvinceName());
            if (i == selectItem) {
                holder.content.setBackgroundResource(R.color.selected_bg);
            }
            else {
                holder.content.setBackgroundColor(Color.WHITE);
            }
            return view;
        }
        class ViewHolder {
            TextView textView;
            ViewGroup content;
        }
    }
    class RadioAdapter extends BaseAdapter {
        private int  selectItem=-1;
        public  void setSelectItem(int selectItem) {
            this.selectItem = selectItem;
        }
        @Override
        public int getCount() {
            return mRadios.size();
        }

        @Override
        public Object getItem(int position) {
            return mRadios.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
                holder = new ViewHolder();
                holder.content = (ViewGroup) convertView;
                holder.title = (TextView) convertView.findViewById(R.id.trackname);
                holder.intro = (TextView) convertView.findViewById(R.id.intro);
                holder.cover = (ImageView) convertView.findViewById(R.id.imageview);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Radio radio = mRadios.get(position);
            holder.title.setText(radio.getRadioName());
            holder.intro.setText(radio.getProgramName());
            x.image().bind(holder.cover, radio.getCoverUrlSmall());
            if (position == selectItem) {
                holder.content.setBackgroundResource(R.color.selected_bg);
            }
            else {
                holder.content.setBackgroundColor(Color.WHITE);
            }
            return convertView;
        }
    }
}

