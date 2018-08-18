package com.dangbei.voice.control.dbclient;

import android.content.Context;

import com.dangbei.voice.control.client.AbstractVoiceReceiver;
import com.dangbei.voice.control.client.IVoiceResultListener;

/**
 * Created by Byron on 2018/8/18.
 */

public class DangBeiClient extends AbstractVoiceReceiver {
    private static DangBeiClient instance;

    public static DangBeiClient getInstance(Context context) {
        if (instance == null) {
            synchronized (DangBeiClient.class) {
                if (instance == null) {
                    instance = new DangBeiClient(context);
                }
            }
        }
        return instance;
    }

    private DangBeiClient(Context context) {
        super(context);
    }

    @Override
    public void disconnect() {
        super.disconnect();
        instance = null;
    }

    /**
     * 发送消息给服务端
     *
     * @param event               事件
     * @param data                数据
     * @param voiceResultListener 服务端回调
     */
    public void sendMessageToServer(String event, String data, IVoiceResultListener voiceResultListener) {
        sendMessage(event, data, voiceResultListener);
    }

}
