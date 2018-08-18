package com.dangbei.voice.control.server.service;

import com.dangbei.voice.control.receiver.ICallbackListener;
import com.dangbei.voice.control.receiver.ISceneNotifyListener;

/**
 * Created by Byron on 2018/8/18.
 */

public interface ServiceConnectionListener {
    /**
     * 服务绑定成功的监听回调
     * 注册或者注销场景TTS回调监听
     *
     * @param callbackListener 返回TTS到客户端的Binder
     */
    void onCallbackNotify(ICallbackListener callbackListener);

    /**
     * 服务绑定成功的监听回调
     * 注册或者注销场景回调监听
     *
     * @param sceneNotifyListener 返现场景状态到客户端的Binder
     */
    void onSceneNotifyListener(ISceneNotifyListener sceneNotifyListener);
}
