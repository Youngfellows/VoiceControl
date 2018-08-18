package com.dangbei.voice.control.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.dangbei.voice.control.cons.Constant;
import com.dangbei.voice.control.receiver.ICallbackListener;
import com.dangbei.voice.control.receiver.ISceneNotifyListener;
import com.dangbei.voice.control.receiver.IVoiceReceiver;
import com.dangbei.voice.control.utils.IntentUtil;

/**
 * Created by Byron on 2018/8/18.
 */

public abstract class AbstractVoiceReceiver {
    private Context mContext;
    private String TAG = this.getClass().getSimpleName();
    private IVoiceReceiver mVoiceReceiver;
    private boolean isBind = false;//是否绑定成功
    private boolean isConnected = false;//是否连接成功
    private IVoiceResultListener mVoiceResultListener;//接收服务端返回的数据(TTS内容),传递给客户端
    private ISceneNotifyCallback mSceneNotifyCallback;//接收服务端返回的数据(场景变化),传递给客户端
    private String mEvent = "";
    private String mData = "";

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            try {
                Log.i(TAG, "onServiceConnected 绑定接收数据的服务成功了");
                mVoiceReceiver = IVoiceReceiver.Stub.asInterface(iBinder);
                try {
                    iBinder.linkToDeath(mDeathRecipient, 0); //注册死亡监听
                    mVoiceReceiver.registerListener(mCallbackListener);  //注册服务端回调
                    mVoiceReceiver.regitsterSceneListener(mSceneNotifyListener);  //注册服务端回调
                    isConnected = true; //绑定服务连接成功
                    sendMessageAfterConnected();//发送上次发送失败的消息
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onServiceConnected 注册监听回调异常了: " + e.getMessage());
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "onServiceConnected 绑定接收数据的服务异常了: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected 解绑接收数据的服务: mVoiceReceiver = " + mVoiceReceiver);
            if (mVoiceReceiver != null) {
                //注销监听回调
                try {
                    mVoiceReceiver.unRegisterListener(mCallbackListener);
                    mVoiceReceiver.unRegitsterSceneListener(mSceneNotifyListener);
                    mVoiceReceiver = null;
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onServiceDisconnected 注销注册监听回调异常了: " + e.getMessage());
                }
            }
            //绑定服务连接断开了
            isConnected = false;
        }
    };

    /**
     * 注册死亡通知
     */
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.e(TAG, "binderDied binder is died ,mVoiceReceiver = " + mVoiceReceiver);
            if (mVoiceReceiver != null) {
                mVoiceReceiver.asBinder().unlinkToDeath(mDeathRecipient, 0);
                mVoiceReceiver = null;//置空
                isBind = false;
                isConnected = false;
                initService();//从新绑定服务
            }
        }
    };

    public AbstractVoiceReceiver(Context context) {
        this.mContext = context;
        this.initService();//绑定服务
    }

    /**
     * 绑定服务
     */
    private void initService() {
        Log.d(TAG, "initService 准备绑定接收数据服务,是否已经绑定服务: isBind = " + isBind + " ,是否连接成功: isConnected = " + isConnected);
        if (!isBind || !isConnected) {
            try {
                Intent intent = new Intent();
                intent.setAction(Constant.ACTION_VOICE_RECEIVER);
                Intent eintent = new Intent(IntentUtil.createExplicitFromImplicitIntent(mContext, intent));
                Log.d(TAG, "intent = " + intent);
                Log.d(TAG, "eintent =  " + eintent);
                if (eintent != null) {
                    //绑定服务
                    this.isBind = mContext.bindService(eintent, mConnection, Context.BIND_AUTO_CREATE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "绑定服务异常: " + e.getMessage());
                Log.e(TAG, Log.getStackTraceString(e));
            }

            if (isBind) {
                Log.d(TAG, "语音接收绑定服务成功了");
            }
        }
    }

    /**
     * 解绑接受数据服务
     */
    public void disconnect() {
        Log.i(TAG, "disconnect receiver service ,isBind = " + isBind + " ,isConnected = " + isConnected);
        if (isBind) {
            mContext.unbindService(mConnection);
        }
        isBind = false;
        isConnected = false;
    }

    /**
     * 注册回调
     */
    private ICallbackListener mCallbackListener = new ICallbackListener.Stub() {

        @Override
        public void onResult(String result) throws RemoteException {
            Log.i(TAG, "onResult mCallbackListener 客户端接收到服务端的回传数据: mVoiceResultListener = " + mVoiceResultListener + " ,result = " + result);
            if (mVoiceResultListener != null) {
                mVoiceResultListener.onVoiceResult(result);
            }
        }
    };

    /**
     * 注册场景回调
     */
    private ISceneNotifyListener mSceneNotifyListener = new ISceneNotifyListener.Stub() {
        @Override
        public void onSceneNotify(String sceneData) throws RemoteException {
            Log.i(TAG, "onSceneNotify mSceneNotifyListener 服务端场景变化了: mSceneNotifyCallback = " + mSceneNotifyCallback + " ,sceneData = " + sceneData);
            if (mSceneNotifyCallback != null) {
                mSceneNotifyCallback.onNotifyScene(sceneData);
            }
        }
    };

    /**
     * 发送上次没有发送成功的数据
     */
    private void sendMessageAfterConnected() {
        if (!TextUtils.isEmpty(mEvent) && !TextUtils.isEmpty(mData)) {
            sendMessage(mEvent, mData, mVoiceResultListener);
        }
    }

    /**
     * 客户端发送消息给服务端
     *
     * @param event               事件
     * @param data                数据
     * @param voiceResultListener 服务端回调
     */
    public void sendMessage(String event, String data, IVoiceResultListener voiceResultListener) {
        Log.d(TAG, "sendMessage 客户端发送: mVoiceReceiver = " + mVoiceReceiver + " ,event = " + event + " ,data = " + data);
        initService();
        clearMessage();
        if (mVoiceReceiver != null) {
            try {
                mVoiceResultListener = voiceResultListener;
                mVoiceReceiver.onMessage(event, data);
                return;
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.i(TAG, "sendMessage try again");
                saveMessage(event, data, voiceResultListener);
            }
        }
    }

    /**
     * 保存发送失败的消息
     *
     * @param event
     * @param data
     * @param voiceResultListener
     */
    private void saveMessage(String event, String data, IVoiceResultListener voiceResultListener) {
        mEvent = event;
        mData = data;
        mVoiceResultListener = voiceResultListener;
    }

    /**
     * 清空消息
     */
    private void clearMessage() {
        mEvent = "";
        mData = "";
        mVoiceResultListener = null;
    }

//    public void setVoiceResultListener(IVoiceResultListener voiceResultListener) {
//        mVoiceResultListener = voiceResultListener;
//    }

    public void setSceneNotifyCallback(ISceneNotifyCallback sceneNotifyCallback) {
        mSceneNotifyCallback = sceneNotifyCallback;
    }
}
