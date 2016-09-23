package com.chetuan.findcar.http;

/**
 * Created by xiyuan_fengyu on 2016/8/25.
 */
public class UrlList {

    public static final String serverRoot = "http://192.168.1.66:8080/payDemo/";

    /**
     参数：
     payType: Int   0支付宝    1微信
     */
    public static final String payCreate = serverRoot + "pay/create";

}
