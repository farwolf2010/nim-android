package com.farwolf.netIm.module;

import com.farwolf.netIm.init.NetIM;
import com.farwolf.weex.annotation.WeexModule;
import com.farwolf.weex.app.WeexApplication;
import com.farwolf.weex.base.WXModuleBase;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;

import java.util.HashMap;


@WeexModule(name = "nim")
public class WXNetImModule extends WXModuleBase {


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


}
