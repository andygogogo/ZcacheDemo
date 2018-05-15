package com.zcache.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.dynamic.DynamicSdk;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.taobao.accs.ACCSClient;
import com.taobao.accs.AccsClientConfig;
import com.taobao.accs.AccsException;
import com.taobao.accs.base.AccsAbstractDataListener;
import com.taobao.accs.base.TaoBaseService;
import com.taobao.accs.utl.ALog;
import com.taobao.atlas.update.AtlasUpdater;
import com.taobao.atlas.update.model.UpdateInfo;
import com.taobao.update.wrapper.AppInfoHelper;
import com.taobao.weex.activity.WeexActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

// import com.taobao.share.ShareUtils;


public class WelcomActivity extends Activity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 0x0003;
    private static final String TAG = "WelcomActivity";
    private static final boolean ADD_LAYOUT = true;

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (ADD_LAYOUT) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.layout_test);
            layout.setVisibility(View.VISIBLE);
        }

        TextView textView = (TextView) findViewById(R.id.textView);
        TextView channelTextView = (TextView)findViewById(R.id.channel_tv);

//        textView.setText(getString(R.string.ttid) + "." + new DuplicateClazz().dup() + "_tpatch");
        /**渠道ID获取示例**/
        String ttid = AppInfoHelper.getTTID();
        Log.i(TAG, "ttid from atlas api:" + ttid);
        ttid = getResources().getString(R.string.ttid);
        Log.i(TAG, "ttid from system api:" + ttid);
        /**渠道ID获取示例**/
        textView.setText("应用版本号：" + BuildConfig.VERSION_NAME );
        channelTextView.setText("渠道Id:" + ttid);


        try {
            ACCSClient.getAccsClient(AccsClientConfig.DEFAULT_CONFIGTAG).registerDataListener("accs", new AccsAbstractDataListener() {
                @Override
                public void onData(String s, String s1, String s2, byte[] bytes, TaoBaseService.ExtraInfo extraInfo) {
                    ALog.i("WelcomActivity", "onData", "serviceId", s, "userId", s1, "dataId", s2, "data", new String(bytes));

                }

                @Override
                public void onBind(String s, int i, TaoBaseService.ExtraInfo extraInfo) {

                }

                @Override
                public void onUnbind(String s, int i, TaoBaseService.ExtraInfo extraInfo) {

                }

                @Override
                public void onSendData(String s, String s1, int i, TaoBaseService.ExtraInfo extraInfo) {
                    ALog.i("WelcomActivity", "onSendData", "serviceId", s, "dataId", s1, "errorCode", i);
                }

                @Override
                public void onResponse(String s, String s1, int i, byte[] bytes, TaoBaseService.ExtraInfo extraInfo) {
                    ALog.i("WelcomActivity", "onResponse", "serviceId", s, "dataId", s1, "errorCode", i, "data", new String(bytes));
                }
            });
        } catch (AccsException e) {
            ALog.e("WelcomActivity", "registerDataListener", e);
            e.printStackTrace();
        }
        findViewById(R.id.goto_bundle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName(WelcomActivity.this, "com.taobao.firstbundle.FirstBundleActivity");
                    startActivity(intent);
                } catch (Throwable t) {
                    Log.e(TAG, "no firstbundle", t);
                    Toast.makeText(WelcomActivity.this, "Bundle不存在", Toast.LENGTH_SHORT).show();
                }

            }
        });

        findViewById(R.id.goto_bundle1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName(WelcomActivity.this, "com.taobao.secondbundle.SecondBundleActivity");
                    startActivity(intent);
                } catch (Throwable t) {
                    Log.e(TAG, "no secondbundle", t);
                    Toast.makeText(WelcomActivity.this, "Bundle不存在", Toast.LENGTH_SHORT).show();
                }

            }
        });


        final Context context = this;
        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Updater.update(context);
//                    }
//                }).start();

            }
        });


        Button haBtn = (Button) findViewById(R.id.ha);
        haBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(WelcomActivity.this, "待接入", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent();
//                intent.setClass(WelcomActivity.this, HAActivity.class);
//                WelcomActivity.this.startActivity(intent);
            }
        });

        Button dexPatchBtn = (Button) findViewById(R.id.dexpatch);
        dexPatchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dexPatchUpdate(getBaseContext());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                };
                new Thread(runnable).start();
            }
        });

        Button weexBtn = (Button) findViewById(R.id.weex);
        weexBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent();
               intent.setClassName(WelcomActivity.this, "com.taobao.weex.activity.WeexActivity");
               startActivity(intent);
            }
        });

        Button mtopBtn = (Button) findViewById(R.id.mtop);
        mtopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(WelcomActivity.this, "敬请期待", Toast.LENGTH_SHORT).show();
            }
        });

        Button accsBtn = (Button) findViewById(R.id.accs);
        accsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(WelcomActivity.this, "敬请期待", Toast.LENGTH_SHORT).show();
            }
        });

        Button orangeBtn = (Button) findViewById(R.id.orange);
        orangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(WelcomActivity.this, "敬请期待", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.welcome_main_scan, menu);
//        MenuItemCompat.setShowAsAction(menu.findItem(R.id.welcome_action_scan), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return true;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        String content = result.getContents();
        if(result != null) {
            if(content == null) {
                Log.d(TAG, "scan result null, return");
                Toast.makeText(this, "scan result null", Toast.LENGTH_LONG).show();
                return;
            }
            Log.d(TAG, "Scanned: " + result.getContents());
            Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

            if (content.contains("dynamicdeploy") || content.contains("dexpatchdeploy")){
                // 获取到扫码结果后跳转
                try {
                    com.taobao.tao.update.Updater.getInstance(WelcomActivity.this).triggerEmasDynamicDeployment(result.getContents());
                } catch (Throwable t) {
                    Log.e(TAG, "invalid update data", t);
                    Toast.makeText(this, "invalid update data", Toast.LENGTH_LONG).show();
                }

            } else {
                try {
                    handleWeexResult(content);
                } catch (Throwable t) {
                    Log.e(TAG, "invalid weex data", t);
                    Toast.makeText(this, "invalid weex data", Toast.LENGTH_LONG).show();
                }

            }
        }
    }


    private void handleWeexResult(String result) {
        Uri mUri = Uri.parse(result);
        if(mUri == null) {
            Log.e("MainScanActivity", "scan result invalid, return");
        } else {
            Intent activityIntent = new Intent(this, WeexActivity.class);
            activityIntent.setData(mUri);
            activityIntent.setAction("com.taobao.android.intent.action.WEEX");
            this.startActivity(activityIntent);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        DynamicSdk.getInstance().requestConfig();
    }

    public static void dexPatchUpdate(Context context) throws FileNotFoundException {
        UpdateInfo info = loadDexPatchInfo(context);
        if (null == info) {
            Toast.makeText(context,"dexpatch 失败，请检查log，按照文档步骤操作",Toast.LENGTH_LONG).show();
            return;
        }
        File patchFile = new File(context.getExternalCacheDir(), info.baseVersion + "@" + info.baseVersion + ".tpatch");
        try {
            AtlasUpdater.dexpatchUpdate(context, info, patchFile, new AtlasUpdater.IDexpatchMonitor() {
                @Override
                public void merge(boolean success, String bundleName, long version, String errMsg) {
                    Log.d("update", "merge: " + success + " " + bundleName + " " + version + " " + errMsg);
                }

                @Override
                public void install(boolean success, String bundleName, long version, String errMsg) {
                    Log.d("update", "install: " + success + " " + bundleName + " " + version + " " + errMsg);
                }
            });
            Log.d("update", "update success");
        } catch (Exception e) {
            Log.e("update", "更新失败", e);
        }
    }

    private static UpdateInfo loadDexPatchInfo(Context context) throws FileNotFoundException {
        String versionName = null;
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(versionName)) {
            Log.e("update", "loadDexPatchInfo: versionName is empty !");
            return null;
        }

        File patchsFile = new File(context.getExternalCacheDir(), "dexpatch-" + versionName + ".json");
        if (!patchsFile.exists()) {
            Log.e("update", "dexpatch更新信息不存在， 将patchs.json push到目录中.");
            Log.e("update", "dexpatch文档地址 :https://alibaba.github.io/atlas/update/dexpatch_use_guide.html");
            return null;
        }
        UpdateInfo info = null;
        String jsonStr = readFile(patchsFile);
        try {
            info = parseDexPatchJson(new JSONObject(jsonStr));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info;
    }

    private static String readFile(File patchsFile) throws FileNotFoundException {
        return new String(com.taobao.atlas.dex.util.FileUtils.readFile(patchsFile));
    }

    private static UpdateInfo parseDexPatchJson(JSONObject data) throws JSONException {
        if (null == data) return null;
        JSONObject updateJson = data.getJSONArray("patches").getJSONObject(0);
        JSONArray bundles = updateJson.getJSONArray("bundles");
        if (null == bundles || bundles.length() < 1) {
            return null;
        }
        UpdateInfo updateInfo = new UpdateInfo();
        updateInfo.dexPatch = updateJson.optBoolean("dexPatch", true);
        updateInfo.baseVersion = data.getString("baseVersion");
        updateInfo.updateBundles = new ArrayList<>(bundles.length());
        for (int i = 0; i < bundles.length(); i++) {
            JSONObject object = bundles.getJSONObject(i);
            JSONArray dependencies = object.getJSONArray("dependency");
            List<String> dep = new ArrayList<>();
            if (dependencies != null && dependencies.length() > 0) {
                for (int j = 0; j < dependencies.length(); j++) {
                    dep.add((String) dependencies.get(j));
                }
            }
            UpdateInfo.Item item = new UpdateInfo.Item();
            item.dependency = dep;
            item.dexpatchVersion = object.getLong("dexpatchVersion");
            item.isMainDex = object.getBoolean("isMainDex");
            item.reset = object.optBoolean("reset", false);
            item.patchType = object.getInt("patchType");
            item.name = object.getString("name");
            item.unitTag = object.getString("unitTag");
            item.srcUnitTag = object.getString("srcUnitTag");
            updateInfo.updateBundles.add(item);
        }
        return updateInfo;
    }

}
