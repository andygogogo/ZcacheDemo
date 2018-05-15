package com.zcache.demo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.taobao.atlas.runtime.ClassNotFoundInterceptorCallback;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.dynamic.DynamicSdk;
import com.alibaba.dynamicconfigadapter.DefaultDynamicSDKEngine;
import com.alibaba.ha.adapter.AliHaAdapter;
import com.alibaba.ha.adapter.AliHaConfig;
import com.alibaba.ha.adapter.Plugin;
import com.alibaba.ha.adapter.Sampling;
import com.taobao.accs.ACCSClient;
import com.taobao.accs.ACCSManager;
import com.taobao.accs.AccsClientConfig;
import com.taobao.accs.AccsException;
import com.taobao.accs.IAppReceiver;
import com.taobao.accs.common.Constants;
import com.taobao.accs.utl.ALog;
import com.taobao.update.UpdateManager;
import com.taobao.update.common.Config;
import com.taobao.weex.InitConfig;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.WeexCrashListener;
import com.taobao.weex.adapter.ImageAdapter;
import com.taobao.weex.adapter.JSExceptionAdapter;
import com.taobao.weex.adapter.WXUserTrackAdapter;
import com.taobao.weex.adapter.ZcacheHttpAdapter;
import com.taobao.zcache.ZCache;
import com.taobao.zcache.config.ConfigOrigin;
import com.taobao.zcache.config.EnvEnum;
import com.taobao.zcache.config.ZCacheConfigManager;
import com.taobao.zcache.utils.ILog;
import com.taobao.zcache.utils.ZLog;

import org.android.spdy.SpdyProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import anet.channel.SessionCenter;
import anet.channel.strategy.ConnEvent;
import anet.channel.strategy.ConnProtocol;
import anet.channel.strategy.IConnStrategy;
import anet.channel.strategy.IStrategyInstance;
import anet.channel.strategy.IStrategyListener;
import anet.channel.strategy.StrategyCenter;
import anet.channel.strategy.dispatch.HttpDispatcher;
import anetwork.channel.config.NetworkConfigCenter;
import anetwork.channel.http.NetworkSdkSetting;
import mtopsdk.common.util.TBSdkLog;
import mtopsdk.mtop.domain.EnvModeEnum;
import mtopsdk.mtop.global.SwitchConfig;
import mtopsdk.mtop.intf.Mtop;
import mtopsdk.mtop.intf.MtopEnablePropertyType;
import mtopsdk.mtop.intf.MtopSetting;
import mtopsdk.security.LocalInnerSignImpl;

/**
 * Created by jason on 18/1/15.
 */

public class EmasInit {
    public static final int DEBUG = 1;    //测试环境
    public static final int RELEASE = 2;  //发布环境

    /*配置信息*/
    protected String mAppkey = "10000039";//"10000066";//"10000039";//"10000078";//"60039748";
    protected String mAppSecret = "c7795717b2306055f21fb33418c1d011";//"1426c10c5ce57d6cb29e016a816421a7";//"c7795717b2306055f21fb33418c1d011";//"2e00a7e9ab2048daabd4977170d37c4a";//"ab5ff148782b467bb0b310c4acd70abd"//"fe240d4b8f4b31283863cc9d707e2cb1"
    protected String mZcachePrefix = "http://mobilehubdev.taobao.com/eweex/";
    protected String mAccsHost = "acs.emas-ha.cn";
    protected Map<String, String> mIPStrategy;
    protected String mMtopHost = "aserver.emas-ha.cn";
    protected String mAdashHost = "adash.emas-ha.cn";
    protected String mHAOssBucket = "ha-remote-log";
    protected String mHAPubKey;
    protected String mStartActivity = "com.zcache.demo.WelcomActivity";
    protected String mTTid = "1001@DemoApp_Android_" + BuildConfig.VERSION_NAME;
    protected String PUSH_TAG = "POC";
    protected boolean mUseHttp = true;

    protected int mEnv = DEBUG;

    private Application mApplication;
    private static final String TAG = "EmasInit";

    public EmasInit(Application application) {
        this.mApplication = application;
        if (mIPStrategy != null && mIPStrategy.size() > 0) {
            initNetwork();
        }
        StringBuilder builder = new StringBuilder();
        try {
            int id = application.getResources().getIdentifier("ttid", "string", application.getPackageName());
            if (id > 0) {
                mTTid = builder.append(application.getString(id))
                        .append("@")
                        .append(application.getResources().getString(application.getApplicationInfo().labelRes))
                        .append("_")
                        .append("Android")
                        .append("_")
                        .append(BuildConfig.VERSION_NAME).toString();
            }
        } catch (Resources.NotFoundException e) {
            Log.d(TAG, "no channel id in res" + e.toString());
        }


    }

    /********************UPDATE SDK START **************************/
    public void initUpdate() {
        initMtop();
        Config config = new Config();
        config.group = mAppkey + "@android";//AppInfoHelper.getGroup();
        config.ttid = mTTid;
        config.isOutApk = false;
        config.appName = "EMAS Demo";
        UpdateManager.getInstance().init(config,
                new ClassNotFoundInterceptorCallback() {

                    @Override
                    public Intent returnIntent(Intent intent) {
                        Log.e("APP", "returnIntent" + intent.toString());
                        return null;
                    }
                }, true);
    }

    private void initMtop() {

        TBSdkLog.setTLogEnabled(false);
        //关闭密文
        if (mUseHttp) {
            NetworkConfigCenter.setSSLEnabled(false);
        }

        //[option]关闭MTOP请求长链,调用后Mtop请求直接调用NetworkSDK的HttpNetwork发请求
        SwitchConfig.getInstance().setGlobalSpdySwitchOpen(false);

        //关闭MTOPSDK NewDeviceID逻辑
        MtopSetting.setEnableProperty(Mtop.Id.INNER, MtopEnablePropertyType.ENABLE_NEW_DEVICE_ID, false);

        //设置自定义全局访问域名
        MtopSetting.setMtopDomain(Mtop.Id.INNER, mMtopHost, mMtopHost, mMtopHost);

        //设置自定义签名使用的appKey和appSecret
        MtopSetting.setISignImpl(Mtop.Id.INNER, new LocalInnerSignImpl(mAppkey, mAppSecret));

        MtopSetting.setAppVersion(Mtop.Id.INNER, BuildConfig.VERSION_NAME);


        Mtop mtopInstance = Mtop.instance(Mtop.Id.INNER, mApplication.getApplicationContext(), "");

        //设置日常环境
        if (mEnv == DEBUG) {
            mtopInstance.switchEnvMode(EnvModeEnum.TEST);
        }

        mtopInstance.registerTtid(mTTid);

    }
    /********************UPDATE SDK END **************************/


    /********************WEEX SDK START **************************/
    public void initWeex() {
        // init zcache
        ZCacheConfigManager.setConfigOrigin(ConfigOrigin.MTOP);
        if (mEnv == DEBUG) {
            ZCache.setEnv(EnvEnum.DAILY);
        }
        ZCache.setPackageZipPrefix(mZcachePrefix);
        ZCache.initZCache(mApplication, mAppkey, BuildConfig.VERSION_NAME);
        ZLog.setLogImpl(new ILog() {
            @Override
            public void d(String s, String s1) {
                Log.d(s, s1);
            }

            @Override
            public void d(String s, String s1, Throwable throwable) {
                Log.d(s, s1, throwable);
            }

            @Override
            public void e(String s, String s1) {
                Log.e(s, s1);
            }

            @Override
            public void e(String s, String s1, Throwable throwable) {
                Log.e(s, s1, throwable);
            }

            @Override
            public void i(String s, String s1) {
                Log.i(s, s1);
            }

            @Override
            public void i(String s, String s1, Throwable throwable) {
                Log.i(s, s1, throwable);
            }

            @Override
            public void v(String s, String s1) {
                Log.v(s, s1);
            }

            @Override
            public void v(String s, String s1, Throwable throwable) {
                Log.v(s, s1, throwable);
            }

            @Override
            public void w(String s, String s1) {
                Log.w(s, s1);
            }

            @Override
            public void w(String s, String s1, Throwable throwable) {
                Log.w(s, s1, throwable);
            }

            @Override
            public boolean isLogLevelEnabled(int i) {
                return true;
            }
        });
        // dynamic config
        DefaultDynamicSDKEngine.getInstance().initSdk(mApplication, mAppkey + "@android");
        DynamicSdk.getInstance().requestConfig();

        // weex
        InitConfig config = (new InitConfig.Builder())
                //.setImgAdapter(new ImageAdapter())
                .setImgAdapter(new ImageAdapter())
                .setHttpAdapter(new ZcacheHttpAdapter())
                .setUtAdapter(new WXUserTrackAdapter())
                .setJSExceptionAdapter(new JSExceptionAdapter(mApplication))
                .build();
        WXSDKEngine.initialize(mApplication, config);

//        try {
//            WXSDKEngine.registerComponent("richtext", RichText.class);
//            WXSDKEngine.registerModule("testmodule", TestModule.class);
//            WXSDKEngine.registerModule("haTest", TestHaModule.class);
//        } catch (WXException var6) {
//            var6.printStackTrace();
//        }
    }
    /********************WEEX SDK END **************************/


    /********************HA SDK START **************************/
    public void initHA() {
        //开启
        if (mEnv == DEBUG) {
            AliHaAdapter.getInstance().openDebug(true);
        }
        AliHaAdapter.getInstance().changeHost(mAdashHost);
        AliHaAdapter.getInstance().tLogService.changeRemoteDebugHost(mAdashHost);
        AliHaAdapter.getInstance().tLogService.changeRemoteDebugOssBucket(mHAOssBucket);
        if(!TextUtils.isEmpty(mHAPubKey)) {
            AliHaAdapter.getInstance().tLogService.changeRasPublishKey(mHAPubKey);
        }
//        initHACrashreporterAndUt();
        AliHaAdapter.getInstance().openHttp(mUseHttp);
        initAccs();

        AliHaAdapter.getInstance().removePugin(Plugin.crashreporter); //tlog 依赖accs
        AliHaAdapter.getInstance().removePugin(Plugin.ut);

        AliHaAdapter.getInstance().telescopeService.setBootPath(new String[]{mStartActivity}, System.currentTimeMillis());
        AliHaAdapter.getInstance().start(buildAliHaConfig());

        AliHaAdapter.getInstance().crashService.addJavaCrashListener(new WeexCrashListener());
    }


    private AliHaConfig buildAliHaConfig() {
        //ha初始化
        AliHaConfig config = new AliHaConfig();
        config.isAliyunos = false;
        config.appKey = mAppkey;
        config.userNick = "you need set user name";
        config.channel = mTTid;
        config.appVersion = BuildConfig.VERSION_NAME;
        config.application = mApplication;
        config.context = mApplication;

        return config;
    }

    private void initHACrashreporterAndUt() {
        AliHaAdapter.getInstance().startWithPlugin(buildAliHaConfig(), Plugin.crashreporter);
        AliHaAdapter.getInstance().startWithPlugin(buildAliHaConfig(), Plugin.ut);
        AliHaAdapter.getInstance().utAppMonitor.changeSampling(Sampling.All);
    }




    private void initAccs() {
        int env = Constants.RELEASE;
//        if (mEnv == DEBUG) {
//            env = Constants.DEBUG;
//        }
        ACCSManager.setAppkey(mApplication.getApplicationContext(), mAppkey, env);//兼容老接口 如果有任意地方使用老接口，必须setAppkey
        NetworkSdkSetting.init(mApplication.getApplicationContext());
        //关闭AMDC请求
        HttpDispatcher.getInstance().setEnable(false);
        ALog.setUseTlog(false);
        anet.channel.util.ALog.setUseTlog(false);
        ACCSClient.setEnvironment(mApplication.getApplicationContext(), env);

        try {
            AccsClientConfig.Builder taobaoBuilder = new AccsClientConfig.Builder()
                    .setAppKey(mAppkey)
                    .setAppSecret(mAppSecret)
                    .setInappHost(mAccsHost)
                    .setInappPubKey(SpdyProtocol.PUBKEY_PSEQ_EMAS)
                    .setTag(AccsClientConfig.DEFAULT_CONFIGTAG);
            ACCSClient.init(mApplication.getApplicationContext(), taobaoBuilder.build());
            ACCSClient client = ACCSClient.getAccsClient(AccsClientConfig.DEFAULT_CONFIGTAG);
            client.bindApp(mTTid, EmasInit.appReceiver);
        } catch (AccsException e) {
            e.printStackTrace();
        }
    }

    public static IAppReceiver appReceiver = new IAppReceiver() {

        @Override
        public void onUnbindUser(int errorCode) {
            ALog.i(TAG, "onUnbindUser", "ret", errorCode);
        }

        @Override
        public void onUnbindApp(int errorCode) {
            ALog.i(TAG, "onUnbindApp", "ret", errorCode);
        }

        @Override
        public void onBindUser(String userId, int errorCode) {
            ALog.i(TAG, "onBindUser", "userid", userId
                    , " errorCode", errorCode);
        }

        @Override
        public void onBindApp(int errorCode) {
            ALog.i(TAG, "onBindApp", "ret", errorCode);
            try {
                ACCSClient.getAccsClient(AccsClientConfig.DEFAULT_CONFIGTAG).bindUser("123324234");
            } catch (AccsException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSendData(String dataId, int errorCode) {
            ALog.i(TAG, "onSendData", "dataId", dataId, "ret", errorCode);
        }

        @Override
        public void onData(String userId, String dataId, byte[] data) {

        }

        @Override
        public String getService(String serviceId) {
            String service = SERVICES.get(serviceId);
            return service;
        }

        @Override
        public Map<String, String> getAllServices() {
            return SERVICES;
        }
    };

    private static final Map<String, String> SERVICES = new HashMap<String, String>() {
        private static final long serialVersionUID = 2527336442338823324L;

        {
            put("demo_service", "com.taobao.taobao.CallbackService");
        }
    };


    public void initNetwork() {
        SessionCenter.init(mApplication);
        final IStrategyInstance instance = StrategyCenter.getInstance();
        StrategyCenter.setInstance(new IStrategyInstance() {
            @Override
            public void initialize(Context context) {

            }

            @Override
            public void switchEnv() {

            }

            @Override
            public void saveData() {

            }

            @Override
            public String getFormalizeUrl(String s) {
                return null;
            }

            @Override
            public List<IConnStrategy> getConnStrategyListByHost(final  String s) {
                String strategy = mIPStrategy.get(s);
                if (TextUtils.isEmpty(strategy)) {
                    return instance.getConnStrategyListByHost(s);
                }
                final String[] ipPort = strategy.split(":");
                List<IConnStrategy> list = new ArrayList<IConnStrategy>();
                IConnStrategy connStrategy = new IConnStrategy() {
                    @Override
                    public String getIp() {
                        return ipPort[0];
                    }

                    @Override
                    public int getIpType() {
                        return 0;
                    }

                    @Override
                    public int getIpSource() {
                        return 0;
                    }


                    @Override
                    public int getPort() {
                        return Integer.parseInt(ipPort[1]);
                    }

                    @Override
                    public ConnProtocol getProtocol() {
                        return ConnProtocol.valueOf("http2", "0rtt", "emas", false);
                    }

                    @Override
                    public int getConnectionTimeout() {
                        return 0;
                    }

                    @Override
                    public int getReadTimeout() {
                        return 0;
                    }

                    @Override
                    public int getRetryTimes() {
                        return 0;
                    }

                    @Override
                    public int getHeartbeat() {
                        return 0;
                    }
                };
                list.add(connStrategy);
                return list;

            }

            @Override
            public String getSchemeByHost(String s) {
                return instance.getSchemeByHost(s);
            }

            @Override
            public String getSchemeByHost(String s, String s1) {
                return instance.getSchemeByHost(s, s1);
            }

            @Override
            public String getCNameByHost(String s) {
                return null;
            }

            @Override
            public String getClientIp() {
                return null;
            }

            @Override
            public void notifyConnEvent(String s, IConnStrategy iConnStrategy, ConnEvent connEvent) {

            }

            @Override
            public String getUnitByHost(String s) {
                return null;
            }

            @Override
            public void forceRefreshStrategy(String s) {

            }

            @Override
            public void registerListener(IStrategyListener iStrategyListener) {

            }

            @Override
            public void unregisterListener(IStrategyListener iStrategyListener) {

            }
        });

    }
    /********************HA SDK END **************************/

    // 公有云推动SDK初始化
//    public void initPush(Context applicationContext) {
//        PushServiceFactory.init(applicationContext);
//        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
//
//        pushService.register(applicationContext, new CommonCallback() {
//            @Override
//            public void onSuccess(String response) {
//                Log.i(TAG, "init cloudchannel success, deviceId: " + pushService.getDeviceId());
//                pushService.bindTag(CloudPushService.DEVICE_TARGET, new String[]{PUSH_TAG}, null, new CommonCallback() {
//                    @Override
//                    public void onSuccess(String s) {
//                        Log.i(TAG, "bind tag success");
//                    }
//
//                    @Override
//                    public void onFailed(String s, String s1) {
//                        Log.i(TAG, "bind tag failed:" + s  + ";" + s1);
//                    }
//                });
//            }
//
//            @Override
//            public void onFailed(String errorCode, String errorMessage) {
//                Log.e(TAG, "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
//            }
//        });
//    }
}
