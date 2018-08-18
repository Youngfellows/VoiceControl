package com.dangbei.voice.control;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dangbei.voice.control.client.ISceneNotifyCallback;
import com.dangbei.voice.control.client.IVoiceResultListener;
import com.dangbei.voice.control.cons.Constant;
import com.dangbei.voice.control.dbclient.DangBeiClient;
import com.dangbei.voice.control.receiver.ICallbackListener;
import com.dangbei.voice.control.receiver.ISceneNotifyListener;
import com.dangbei.voice.control.server.service.ServiceConnectionListener;
import com.dangbei.voice.control.server.service.VoiceReceiverService;
import com.dangbei.voice.control.utils.IntentUtil;

public class MainActivity extends AppCompatActivity {
    private String TAG = this.getClass().getSimpleName();
    private ICallbackListener mCallbackListener;//返回TTS到客户端的Binder
    private ISceneNotifyListener mSceneNotifyListener;//返现场景状态到客户端的Binder

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //1.注册服务绑定or解绑的回调监听
        //2.获取服务端可以传递数据到客户端的回调接口
        VoiceReceiverService.setConnectionListener(mServiceConnectionListener);
    }

    private ServiceConnectionListener mServiceConnectionListener = new ServiceConnectionListener() {
        @Override
        public void onCallbackNotify(ICallbackListener callbackListener) {
            Log.i(TAG, "onCallbackNotify 传递数据到客户端的接口: callbackListener = " + callbackListener);
            mCallbackListener = callbackListener;
        }

        @Override
        public void onSceneNotifyListener(ISceneNotifyListener sceneNotifyListener) {
            Log.i(TAG, "onSceneNotifyListener 传递数据到客户端的接口: sceneNotifyListener = " + sceneNotifyListener);
            mSceneNotifyListener = sceneNotifyListener;
        }
    };

    /**
     * 启动服务
     *
     * @param view
     */
    public void start(View view) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_VOICE_RECEIVER);
        Intent eIntent = new Intent(IntentUtil.createExplicitFromImplicitIntent(this, intent));
        this.startService(eIntent);
    }

    /**
     * 停止服务
     *
     * @param view
     */
    public void stop(View view) {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_VOICE_RECEIVER);
        Intent eIntent = new Intent(IntentUtil.createExplicitFromImplicitIntent(this, intent));
        this.stopService(eIntent);
    }

    /**
     * 绑定服务
     *
     * @param view
     */
    public void bind(View view) {
        DangBeiClient.getInstance(this);//绑定服务
        //注册场景变化的监听
        DangBeiClient.getInstance(this).setSceneNotifyCallback(new ISceneNotifyCallback() {
            @Override
            public void onNotifyScene(String sceneData) {
                Log.d(TAG, "onNotifyScene 场景变化了: sceneData = " + sceneData);
            }
        });
    }

    /**
     * 解绑服务
     *
     * @param view
     */
    public void unbind(View view) {
        DangBeiClient.getInstance(this).disconnect();
    }

    /**
     * 发送数据给服务端
     *
     * @param view
     */
    public void sendToServer(View view) {
        DangBeiClient.getInstance(this).sendMessageToServer("media.video.search", "ssssyyyhhhxxx" + System.currentTimeMillis(), new IVoiceResultListener() {
            @Override
            public void onVoiceResult(String result) {
                Log.d(TAG, "接收到服务端回传的TTS数据:  result = " + result);
            }
        });

    }

    /**
     * 回传数据给客户端
     *
     * @param view
     */
    public void sendToClient(View view) {
        Log.d(TAG, "sendToClient: mCallbackListener = " + mCallbackListener);
        if (mCallbackListener != null) {
            try {
                mCallbackListener.onResult("这是TTS播报内容" + System.currentTimeMillis());
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "发送TTS播报内容给客户端异常");
            }
        }
    }

    /**
     * 场景变化通知客户端
     *
     * @param view
     */
    public void sceneNotify(View view) {
        Log.d(TAG, "sceneNotify: mSceneNotifyListener = " + mSceneNotifyListener);
        if (mSceneNotifyListener != null) {
            try {
                mSceneNotifyListener.onSceneNotify("当前状态: 播放视频" + System.currentTimeMillis());
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "发送播放场景给客户端异常");
            }
        }
    }
}
