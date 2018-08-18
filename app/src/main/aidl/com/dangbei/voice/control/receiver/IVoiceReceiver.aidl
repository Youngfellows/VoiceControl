package com.dangbei.voice.control.receiver;
import com.dangbei.voice.control.receiver.ICallbackListener;
import com.dangbei.voice.control.receiver.ISceneNotifyListener;

interface IVoiceReceiver {
       void onMessage(String event, String data);

        void registerListener(ICallbackListener callbackListener) ;

        void unRegisterListener(ICallbackListener callbackListener) ;

        void regitsterSceneListener(ISceneNotifyListener sceneNotifyListener);

        void unRegitsterSceneListener(ISceneNotifyListener sceneNotifyListener);
}
