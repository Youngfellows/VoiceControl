package com.dangbei.voice.control.server.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dangbei.voice.control.receiver.ICallbackListener;
import com.dangbei.voice.control.receiver.ISceneNotifyListener;
import com.dangbei.voice.control.receiver.IVoiceReceiver;

/**
 * Created by Byron on 2018/8/18.
 */

public class VoiceReceiverService extends Service {
    private String TAG = this.getClass().getSimpleName();
    private VoiceReceiverBinder mVoiceReceiverBinder;//接收数据的Binder
    private ICallbackListener mCallbackListener;//返回TTS到客户端的Binder
    private ISceneNotifyListener mSceneNotifyListener;//返现场景状态到客户端的Binder
    private static ServiceConnectionListener mConnectionListener;//服务绑定成功的监听回调

    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate 启动音频数据接收的服务");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: xxxxxxx");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mVoiceReceiverBinder == null) {
            mVoiceReceiverBinder = new VoiceReceiverBinder();
        }
        return mVoiceReceiverBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy 接收数据服务销毁了");
    }

    public static void setConnectionListener(ServiceConnectionListener connectionListener) {
        mConnectionListener = connectionListener;
    }

    /**
     * 1.接收数据的Binder
     * 2.注册返回TTS到客户端的Binder
     * 3.注册返现场景状态到客户端的Binder
     * 4.注消返回TTS到客户端的Binder
     * 5.注消返现场景状态到客户端的Binder
     */
    public class VoiceReceiverBinder extends IVoiceReceiver.Stub {
        @Override
        public void onMessage(String event, String data) throws RemoteException {
            Log.i(TAG, "onMessage 接收到的数据: event = " + event + " ,data = " + data);
        }

        @Override
        public void registerListener(ICallbackListener callbackListener) throws RemoteException {
            Log.i(TAG, "registerListener 注册回调,callbackListener = " + callbackListener);
            mCallbackListener = callbackListener;
            if (mConnectionListener != null) {
                mConnectionListener.onCallbackNotify(mCallbackListener);
            }
        }

        @Override
        public void unRegisterListener(ICallbackListener callbackListener) throws RemoteException {
            Log.i(TAG, "unRegisterListener 注销回调");
            mCallbackListener = null;
            if (mConnectionListener != null) {
                mConnectionListener.onCallbackNotify(mCallbackListener);
            }
        }

        @Override
        public void regitsterSceneListener(ISceneNotifyListener sceneNotifyListener) throws RemoteException {
            Log.i(TAG, "regitsterSceneListener 注册场景回调,sceneNotifyListener = " + sceneNotifyListener);
            mSceneNotifyListener = sceneNotifyListener;
            if (mConnectionListener != null) {
                mConnectionListener.onSceneNotifyListener(mSceneNotifyListener);
            }
        }

        @Override
        public void unRegitsterSceneListener(ISceneNotifyListener sceneNotifyListener) throws RemoteException {
            Log.i(TAG, "unRegitsterSceneListener 注销场景回调");
            mSceneNotifyListener = null;
            if (mConnectionListener != null) {
                mConnectionListener.onSceneNotifyListener(mSceneNotifyListener);
            }
        }
    }
}
