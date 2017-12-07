package com.example.administrator.ximalayafm;

import android.app.ActionBar;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.ximalayafm.download.DownloadTrackActivity;
import com.example.administrator.ximalayafm.fragment.AlbumListFragment;
import com.example.administrator.ximalayafm.fragment.PayTrackFragment;
import com.example.administrator.ximalayafm.fragment.RadiosFragment;
import com.example.administrator.ximalayafm.fragment.ScheduleFragment;
import com.example.administrator.ximalayafm.fragment.TracksFragment;
import com.example.administrator.ximalayafm.fragment.base.BaseFragment;
import com.example.administrator.ximalayafm.pay.PayActivity;
import com.example.administrator.ximalayafm.util.ToolUtil;
import com.example.administrator.ximalayafm.view.ViewFindUtils;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.AccessTokenManager;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.category.CategoryList;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.schedule.Schedule;
import com.ximalaya.ting.android.opensdk.model.track.SearchTrackList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.appnotification.XmNotificationCreater;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;
import com.ximalaya.ting.android.sdkdownloader.XmDownloadManager;


import org.xutils.x;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * ClassName:MainFragmentActivity
 *
 * @author jack.qin
 * @Date 2015-5-25 下午5:51:12
 * @see
 * @since Ver 1.1
 */
public class MainFragmentActivity extends FragmentActivity implements View.OnKeyListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private static final String[] CONTENT = new String[]{"点播", "直播", "推荐" ,"付费","专辑" };
    private static final String TAG = "MainFragmentActivity";

    private TextView mTextView;
    private ImageView mBtnPreSound;
    private ImageView mBtnPlay;
    private ImageView mBtnNextSound;
    private SeekBar mSeekBar;
    private ImageView mSoundCover;
    private ProgressBar mProgress;

    private ViewPager mViewPager;
    private PagerTabStrip mIndicator;
    private PagerAdapter mAdapter;

    private Context mContext;

    private XmPlayerManager mPlayerManager;

    private boolean mUpdateProgress = true;

    private TracksFragment mTracksFragment;
    private RadiosFragment mRadiosFragment;
    private ScheduleFragment mScheduleFragment;
    private AlbumListFragment mAlbumListFragment;
    private PayTrackFragment mPayTrackFragment;
    private BaseFragment mCurrFragment;
    private EditText search;
    private SegmentTabLayout tabLayout;
    private View mDecorView;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        initView();
        // 是否使用防劫持方案
//        XmPlayerConfig.getInstance(this).usePreventHijack(false);
        mPlayerManager = XmPlayerManager.getInstance(mContext);
        Notification mNotification = XmNotificationCreater.getInstanse(this).initNotification(this.getApplicationContext(), MainFragmentActivity.class);

        // 如果之前贵方使用了 `XmPlayerManager.init(int id, Notification notification)` 这个初始化的方式
        // 请参考`4.8 播放器通知栏使用`重新添加新的通知栏布局,否则直接升级可能导致在部分手机播放时崩溃
        // 如果不想使用sdk内部搞好的notification,或者想自建notification 可以使用下面的  init()函数进行初始化
        mPlayerManager.init((int) System.currentTimeMillis(), mNotification);
//		mPlayerManager.init();
        mPlayerManager.addPlayerStatusListener(mPlayerStatusListener);
        mPlayerManager.addAdsStatusListener(mAdsListener);
        mPlayerManager.addOnConnectedListerner(new XmPlayerManager.IConnectListener() {
            @Override
            public void onConnected() {
                mPlayerManager.removeOnConnectedListerner(this);

                mPlayerManager.setPlayMode(XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP);
                Toast.makeText(MainFragmentActivity.this, "播放器初始化成功", Toast.LENGTH_SHORT).show();
            }
        });

        // 此代码表示播放时会去监测下是否已经下载
        XmPlayerManager.getInstance(this).setCommonBusinessHandle(XmDownloadManager.getInstance());

        Toast.makeText(MainFragmentActivity.this, "" + AccessTokenManager.getInstanse().getUid(), Toast.LENGTH_SHORT).show();

        Map<String, String> map = new HashMap<String, String>();
        CommonRequest.getCategories(map, new IDataCallBack<CategoryList>() {
            @Override
            public void onSuccess(CategoryList object) {
                System.out.println("object = [" + object + "]");
            }

            @Override
            public void onError(int code, String message) {
                System.out.println("code = [" + code + "], message = [" + message + "]");
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (mPlayerManager != null) {
            mPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
        }
        XmPlayerManager.release();
        CommonRequest.release();
        super.onDestroy();
    }

    private IXmPlayerStatusListener mPlayerStatusListener = new IXmPlayerStatusListener() {

        @Override
        public void onSoundPrepared() {
            Log.i(TAG, "onSoundPrepared");
            mSeekBar.setEnabled(true);
            mProgress.setVisibility(View.GONE);
        }

        @Override
        public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel) {
            Log.i(TAG, "onSoundSwitch index:" + curModel);
            PlayableModel model = mPlayerManager.getCurrSound();
            if (model != null) {
                String title = null;
                String coverUrl = null;
                if (model instanceof Track) {
                    Track info = (Track) model;
                    title = info.getTrackTitle();
                    coverUrl = info.getCoverUrlLarge();
                } else if (model instanceof Schedule) {
                    Schedule program = (Schedule) model;
                    title = program.getRelatedProgram().getProgramName();
                    coverUrl = program.getRelatedProgram().getBackPicUrl();
                } else if (model instanceof Radio) {
                    Radio radio = (Radio) model;
                    title = radio.getRadioName();
                    coverUrl = radio.getCoverUrlLarge();
                }
                mTextView.setText(title);
                x.image().bind(mSoundCover ,coverUrl);
            }
            updateButtonStatus();
        }


        private void updateButtonStatus() {
            if (mPlayerManager.hasPreSound()) {
                mBtnPreSound.setEnabled(true);
            } else {
                mBtnPreSound.setEnabled(false);
            }
            if (mPlayerManager.hasNextSound()) {
                mBtnNextSound.setEnabled(true);
            } else {
                mBtnNextSound.setEnabled(false);
            }
        }

        @Override
        public void onPlayStop() {
            Log.i(TAG, "onPlayStop");
            mBtnPlay.setImageResource(R.mipmap.play);
        }

        @Override
        public void onPlayStart() {
            Log.i(TAG, "onPlayStart");
            mBtnPlay.setImageResource(R.mipmap.pause);
        }

        @Override
        public void onPlayProgress(int currPos, int duration) {
            String title = "";
            PlayableModel info = mPlayerManager.getCurrSound();
            if (info != null) {
                if (info instanceof Track) {
                    title = ((Track) info).getTrackTitle();
                } else if (info instanceof Schedule) {
                    title = ((Schedule) info).getRelatedProgram().getProgramName();
                } else if (info instanceof Radio) {
                    title = ((Radio) info).getRadioName();
                }
            }
            mTextView.setText(title + "[" + ToolUtil.formatTime(currPos) + "/" + ToolUtil.formatTime(duration) + "]");
            if (mUpdateProgress && duration != 0) {
                mSeekBar.setProgress((int) (100 * currPos / (float) duration));
            }
        }

        @Override
        public void onPlayPause() {
            Log.i(TAG, "onPlayPause");
            mBtnPlay.setImageResource(R.mipmap.play);
        }

        @Override
        public void onSoundPlayComplete() {
            Log.i(TAG, "onSoundPlayComplete");
            mBtnPlay.setImageResource(R.mipmap.play);
            XmPlayerManager.getInstance(mContext).pause();
        }

        @Override
        public boolean onError(XmPlayerException exception) {
            Log.i(TAG, "onError " + exception.getMessage());
            mBtnPlay.setImageResource(R.mipmap.play);
            return false;
        }

        @Override
        public void onBufferProgress(int position) {
            mSeekBar.setSecondaryProgress(position);
        }

        public void onBufferingStart() {
            mSeekBar.setEnabled(false);
            mProgress.setVisibility(View.VISIBLE);
        }

        public void onBufferingStop() {
            mSeekBar.setEnabled(true);
            mProgress.setVisibility(View.GONE);
        }

    };

    private IXmAdsStatusListener mAdsListener = new IXmAdsStatusListener() {

        @Override
        public void onStartPlayAds(Advertis ad, int position) {
            Log.i(TAG, "onStartPlayAds, Ad:" + ad.getName() + ", pos:" + position);
            if (ad != null) {
                x.image().bind(mSoundCover ,ad.getImageUrl());
            }
        }

        @Override
        public void onStartGetAdsInfo() {
            Log.i(TAG, "onStartGetAdsInfo");
            mBtnPlay.setEnabled(false);
            mSeekBar.setEnabled(false);
        }

        @Override
        public void onGetAdsInfo(AdvertisList ads) {
            Log.i(TAG, "onGetAdsInfo " + (ads != null));
        }

        @Override
        public void onError(int what, int extra) {
            Log.i(TAG, "onError what:" + what + ", extra:" + extra);
        }

        @Override
        public void onCompletePlayAds() {
            Log.i(TAG, "onCompletePlayAds");
            mBtnPlay.setEnabled(true);
            mSeekBar.setEnabled(true);
            PlayableModel model = mPlayerManager.getCurrSound();
            if (model != null && model instanceof Track) {
                x.image().bind(mSoundCover ,((Track) model).getCoverUrlLarge());
            }
        }

        @Override
        public void onAdsStopBuffering() {
            Log.i(TAG, "onAdsStopBuffering");
        }

        @Override
        public void onAdsStartBuffering() {
            Log.i(TAG, "onAdsStartBuffering");
        }
    };

    /**
     * 搜索接口
     * @param searchText 要查询的字符串
     */
    private void getSearch(String searchText){
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.SEARCH_KEY, searchText);
        CommonRequest.getSearchedTracks(map, new IDataCallBack<SearchTrackList>(){
            @Override
            public void onSuccess(@Nullable SearchTrackList searchTrackList) {
                List<Track> tracks = searchTrackList.getTracks();
                for (Track track : tracks) {
                    Log.e(TAG, "onSuccess:search::::::: "+track.getAnnouncer());
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    class SlidingPagerAdapter extends FragmentPagerAdapter {
        public SlidingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = null;
            if (0 == position) {
                if (mTracksFragment == null) {
                    mTracksFragment = new TracksFragment();
                }
                f = mTracksFragment;
            } else if (1 == position) {
                if (mRadiosFragment == null) {
                    mRadiosFragment = new RadiosFragment();
                }
                f = mRadiosFragment;
            } else if (2 == position) {
                if (mScheduleFragment == null) {
                    mScheduleFragment = new ScheduleFragment();
                }
                f = mScheduleFragment;
            }
            else if(4 == position) {
                if(mAlbumListFragment == null) {
                    mAlbumListFragment = new AlbumListFragment();//专辑
                }
                f = mAlbumListFragment;
            }
            else if(3 == position) {
                if(mPayTrackFragment == null) {
                    mPayTrackFragment = new PayTrackFragment();//付费
                }
                f = mPayTrackFragment;
            }
            return f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length];
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }

    private void initView() {
        setContentView(R.layout.act_main);
        mContext = MainFragmentActivity.this;
        //findviewByid
        search = ((EditText) findViewById(R.id.edit_text_main));
        mTextView = (TextView) findViewById(R.id.message);
        mBtnPreSound = (ImageView) findViewById(R.id.pre_sound);
        mBtnPlay = (ImageView) findViewById(R.id.play_or_pause);
        mBtnNextSound = (ImageView) findViewById(R.id.next_sound);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSoundCover = (ImageView) findViewById(R.id.sound_cover);
        mProgress = (ProgressBar) findViewById(R.id.buffering_progress);
        mDecorView = getWindow().getDecorView();
        tabLayout = ViewFindUtils.find(mDecorView, R.id.indicator);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        //创建Tab
        tl_3();
        //各类监听事件
        search.setOnKeyListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        mBtnPreSound.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);
        mBtnNextSound.setOnClickListener(this);
    }
    //创建Tab
    private void tl_3() {
        mAdapter = new SlidingPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        tabLayout.setTabData(CONTENT);
        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
//                if (position == 0) {
//                    mCurrFragment = mTracksFragment;
//                } else if (position == 1) {
//                    mCurrFragment = mRadiosFragment;
//                } else if (position == 2) {
//                    mCurrFragment = mScheduleFragment;
//                    if (mCurrFragment != null) {
//                        mCurrFragment.refresh();
//                    }
//                }  else if(position == 3) {
//                    mCurrFragment = mAlbumListFragment;
//                    if(mCurrFragment != null) {
//                        mCurrFragment.refresh();
//                    }
//                }
//                else {
//                    mCurrFragment = null;
//                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(0);
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.pre_sound:
                mPlayerManager.playPre();
                break;
            case R.id.play_or_pause:
                if (mPlayerManager.isPlaying()) {
                    mPlayerManager.pause();
                } else {
                    mPlayerManager.play();
                }
                break;
            case R.id.next_sound:
                mPlayerManager.playNext();
                break;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            menu.add(0 ,1 ,0 ,"下载").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0 ,4 ,0 ,"付费").setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        menu.add(0 ,2 ,0 ,"测试1");
        menu.add(0 ,3 ,0 ,"测试2");
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == 1) {
            startActivity(new Intent(MainFragmentActivity.this , DownloadTrackActivity.class));
        } else if(itemId == 4) {
            startActivity(new Intent(MainFragmentActivity.this , PayActivity.class));
        } else if(itemId == 2) {
            XmPlayerManager.getInstance(MainFragmentActivity.this).hasNextSound();
        } else if(itemId == 3) {
            XmPlayerManager.getInstance(MainFragmentActivity.this).hasPreSound();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        //这里注意要作判断处理，ActionDown、ActionUp都会回调到这里，不作处理的话就会调用两次
        if (KeyEvent.KEYCODE_ENTER == i && KeyEvent.ACTION_DOWN == keyEvent.getAction()) {
            //处理事件
            //调取搜索声音接口
            getSearch(search.getText().toString());
//            mCurrFragment = mScheduleFragment;
//                if (mCurrFragment != null) {
//                    mCurrFragment.refresh();
//                }
//            mScheduleFragment
            mViewPager.setCurrentItem(2);
            return true;
        }
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mUpdateProgress = false;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mPlayerManager.seekToByPercent(seekBar.getProgress() / (float) seekBar.getMax());
        mUpdateProgress = true;
    }
}
