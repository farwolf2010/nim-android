package com.farwolf.netIm.module;

import com.farwolf.netIm.init.NetIM;
import com.farwolf.weex.annotation.WeexModule;
import com.farwolf.weex.app.WeexApplication;
import com.farwolf.weex.base.WXModuleBase;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@WeexModule(name = "nim")
public class WXNetImModule extends WXModuleBase {

    JSCallback callback;

    @JSMethod
    public void regist(final HashMap param){
       final String appKey=param.get("appKey")+"";
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean mainProcess = NIMUtil.isMainProcess(WeexApplication.getInstance());

                NetIM.init(WeexApplication.getInstance(),appKey);
            }
        });


//        NimUIKit.getOptions()
    }

    @JSMethod
    public void login(final HashMap param,final JSCallback callback){

        String account=param.get("account")+"";
        String token=param.get("token")+"";
        final HashMap res=new HashMap();
        AbortableFuture loginRequest =   NimUIKit.login(new LoginInfo(account, token), new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo param) {
                res.put("err",0);
                HashMap m=new HashMap();
                m.put("account",param.getAccount());
                m.put("appkey",param.getAppKey());
                m.put("token",param.getToken());
                res.put("user",m);
                callback.invoke(res);
            }

            @Override
            public void onFailed(int code) {
                res.put("err",code);
                callback.invoke(res);
            }

            @Override
            public void onException(Throwable exception) {
                res.put("err",-2);
                callback.invoke(res);
            }
        });


    }



    @JSMethod
    public void openP2P(HashMap param){

        String account=param.get("account")+"";
        NimUIKit.startP2PSession(getContext(), account);

    }

    @JSMethod
    public void openTeam(HashMap param){

        String teamId=param.get("teamId")+"";
        NimUIKit.startTeamSession(getContext(), teamId);

    }

    @JSMethod
    public void setMsgCallback(JSCallback callback){
        this.callback=callback;
        regist();
    }

    @JSMethod
    public void setRead(String id){
        NIMClient.getService(MsgService.class).clearUnreadCount(id,SessionTypeEnum.P2P);
    }


    @JSMethod
    public void recent(final JSCallback callback){
        NIMClient.getService(MsgService.class).queryRecentContacts()
                .setCallback(new RequestCallbackWrapper<List<RecentContact>>() {
                    @Override
                    public void onResult(int code, List<RecentContact> recents, Throwable e) {
                        // recents参数即为最近联系人列表（最近会话列表）
                        List ary=new ArrayList();
                        for(RecentContact r:recents){
                            HashMap m=new HashMap();
                            m.put("sessionId",r.getContactId());
                            m.put("time",r.getTime());
                            m.put("lastMessage",r.getContent());
                            m.put("unreadCount",r.getUnreadCount());
                            ary.add(m);
                        }
                        callback.invoke(ary);
                    }
                });
    }



    public void regist(){
        Observer<List<IMMessage>> incomingMessageObserver =
                new Observer<List<IMMessage>>() {
                    @Override
                    public void onEvent(List<IMMessage> messages) {
                        // 处理新收到的消息，为了上传处理方便，SDK 保证参数 messages 全部来自同一个聊天对象。
                      if(callback!=null){
                          int count= NIMClient.getService(MsgService.class).getTotalUnreadCount();
                          HashMap m= new HashMap<>();
                          m.put("unread",count);
                          callback.invokeAndKeepAlive(m);
                      }
                    }
                };
        NIMClient.getService(MsgServiceObserve.class)
                .observeReceiveMessage(incomingMessageObserver, true);
    }


    @JSMethod(uiThread = false)
    public int unread(){
        int count= NIMClient.getService(MsgService.class).getTotalUnreadCount();
        return count;
    }

}
