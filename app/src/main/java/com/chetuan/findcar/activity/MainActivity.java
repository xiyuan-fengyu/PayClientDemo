package com.chetuan.findcar.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.chetuan.findcar.R;
import com.chetuan.findcar.value.Weixin;
import com.google.gson.JsonObject;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xiyuan.http.HttpBean;
import com.chetuan.findcar.http.UrlList;
import com.xiyuan.util.JsonUtil;
import com.xiyuan.util.XYLog;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.aliPayTv)
    TextView aliPayTv;
    @Bind(R.id.weixinPayTv)
    TextView weixinPayTv;

    private Handler handler = new Handler();


    private IWXAPI weixinApi;

    private BroadcastReceiver wxPayReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);

        weixinApi = WXAPIFactory.createWXAPI(MainActivity.this, Weixin.APP_ID);
        wxPayReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                XYLog.d("微信支付结果：", intent.getIntExtra(Weixin.ERROR_CODE, -3));
            }
        };
        IntentFilter intentFilter = new IntentFilter(Weixin.ACTION_WEIXIN_PAY);
        registerReceiver(wxPayReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wxPayReceiver);
    }

    @OnClick({R.id.aliPayTv, R.id.weixinPayTv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.aliPayTv:
                XYLog.d("支付宝支付流程开始");
                getAlipayInfo();
                break;
            case R.id.weixinPayTv:
                XYLog.d("微信支付流程开始");
                getWeixinpayInfo();
                break;
        }
    }

    private void getAlipayInfo() {
        new HttpBean(this, UrlList.payCreate + "?payType=0&amount=" + 0.01, 0) {
            @Override
            public void onResponseSuccess(JsonObject jsonObject) {
                boolean flag = false;
                if (jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
                    if (jsonObject.has("data")) {
                        flag = true;
                        final String orderStr = jsonObject.get("data").getAsString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                PayTask payTask = new PayTask(MainActivity.this);
                                final Map<String, String> result = payTask.payV2(orderStr, true);

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, result.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).start();
                    }
                }

                if (!flag) {
                    Toast.makeText(MainActivity.this, "订单创建失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onErrorResponse(Exception e) {
                super.onErrorResponse(e);
                Toast.makeText(MainActivity.this, "订单创建失败", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    private void getWeixinpayInfo() {
        new HttpBean(this, UrlList.payCreate + "?payType=1&amount=" + 0.01, 0) {
            @Override
            public void onResponseSuccess(JsonObject jsonObject) {
                boolean flag = false;
                if (jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
                    if (jsonObject.has("data")) {
                        flag = true;
                        final PayReq payReq = JsonUtil.jsonToObj(jsonObject.get("data"), PayReq.class);
                        weixinApi.sendReq(payReq);
                    }
                }

                if (!flag) {
                    Toast.makeText(MainActivity.this, "订单创建失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onErrorResponse(Exception e) {
                super.onErrorResponse(e);
                Toast.makeText(MainActivity.this, "订单创建失败", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }


}
