package com.liar.baiduvoicesdk;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements EventListener{

    String TAG="MainActivity";

    String TYPE_CYY="TYPE_CYY";
    String TYPE_DYY="TYPE_DYY";


    /**
     * 结束
     */
    Button btn_js;

    /**
     * 申请权限
     */
    Button btn_sq;
    /**
     * 长语音识别
     */
    Button btn_cyy;

    /**
     * 短语音识别
     */
    Button btn_dyy;

    /**
     *语音识别内容
     */
    TextView tv_y;

    /**
     *语音识别状态
     */
    TextView tv_status;

    //短语音，静音800ms断句
    int mIntDyy=800;
    //长语音
    int mIntCyy=0;

    String mType="";


    private EventManager asr;//语音识别核心库
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         btn_js =findViewById(R.id.btn_js);
        btn_sq =findViewById(R.id.btn_sq);
        btn_cyy =findViewById(R.id.btn_cyy);
        btn_dyy =findViewById(R.id.btn_dyy);
         tv_y =findViewById(R.id.tv_y);
        tv_status =findViewById(R.id.tv_status);




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0以上的手机对权限进行动态申请
            requestPermission();//申请权限
        }

        //初始化EventManager对象
        asr = EventManagerFactory.create(this, "asr");
        //注册自己的输出事件类
        asr.registerListener(this); //  EventListener 中 onEvent方法

        btn_cyy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mType=TYPE_CYY;
                initBaiduVoice();
                tv_status.setText("长语音识别中");
            }
        });
        btn_dyy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType=TYPE_DYY;
                initBaiduVoice();
                tv_status.setText("短语音识别中");
            }
        });

        btn_js.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
                tv_status.setText("语音识别结束");
            }
        });
        btn_sq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //申请权限
                requestPermission();
            }
        });

    }

    /**初始化百度语音识别**/
    private void initBaiduVoice() {
        if(mType.equals(TYPE_CYY)) {//长语音识别
            //初始化EventManager对象
            Map<String, Object> params = new LinkedHashMap<String, Object>();
            boolean enableOffline = false; // 测试离线命令词，需要改成true
            if (enableOffline) {
                params.put(SpeechConstant.DECODER, 2);
            }
            String event = SpeechConstant.ASR_START; // 替换成测试的event
            // 基于SDK集成2.1 设置识别参数
            params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
            params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0); // 长语音
            // 复制此段可以自动检测错误
            (new AutoCheck(this.getApplicationContext(), new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.what == 100) {
                        AutoCheck autoCheck = (AutoCheck) msg.obj;
                        synchronized (autoCheck) {
                            // 可以用下面一行替代，在logcat中查看代码
                            Log.w(TAG, "百度语音识别AutoCheckMessage：" + autoCheck.obtainErrorMessage());
                        }
                    }
                }
            }, enableOffline)).checkAsr(params);
            String json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
            Log.e(TAG, "百度语音识别输入参数：" + json);
            asr.send(event, json, null, 0, 0);
        }else {//短语音识别
            asr.send(SpeechConstant.ASR_START, null, null, 0, 0);
        }

    }


    //申请权限
    public void requestPermission() {


        if (XXPermissions.hasPermission(MainActivity.this,
                //所需危险权限可以在此处添加：
                Permission.READ_PHONE_STATE,
                Permission.WRITE_EXTERNAL_STORAGE,
                Permission.RECORD_AUDIO)) {
            Log.e(TAG, "已经获得所需所有权限");
        } else {

            XXPermissions.with(this)
                    // 可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                    .constantRequest()
                    // 支持请求6.0悬浮窗权限8.0请求安装权限
                    //.permission(Permission.SYSTEM_ALERT_WINDOW, Permission.REQUEST_INSTALL_PACKAGES)
                    // 不指定权限则自动获取清单中的危险权限
                    .permission(Permission.RECORD_AUDIO,
                            Permission.WRITE_EXTERNAL_STORAGE)
                    .request(new OnPermission() {

                        @Override
                        public void hasPermission(List<String> granted, boolean isAll) {
                            if (isAll) {
                                Toast.makeText(MainActivity.this, "获取权限成功", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(MainActivity.this, "获取权限成功，部分权限未正常授予", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void noPermission(List<String> denied, boolean quick) {
                            if (quick) {
                                Toast.makeText(MainActivity.this, "被永久拒绝授权，请手动授予权限", Toast.LENGTH_SHORT).show();
                                //如果是被永久拒绝就跳转到应用权限系统设置页面
                                XXPermissions.startPermissionActivity(MainActivity.this);
                            } else {
                                Toast.makeText(MainActivity.this, "获取权限失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //发送取消事件
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        //退出事件管理器
        // 必须与registerListener成对出现，否则可能造成内存泄露
        asr.unregisterListener(this);
    }

    @Override
    public void onEvent(String s, String s1, byte[] bytes, int i, int i1) {

        if (s.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            // 识别相关的结果都在这里
            if (s1 == null || s1.isEmpty()) {
                return;
            }
            if (s1.contains("\"final_result\"")) {
                // 一句话的最终识别结果
                Log.e(TAG,s1);
                tv_y.setText(JSON.parseObject(s1).getString("best_result"));
            }
            if(mType.equals(TYPE_DYY)){
                tv_status.setText("短语音识别结束");
            }
        }

    }
}
