package com.zcache.demo;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by jason on 18/4/10.
 */

public class EMASInfo implements Serializable{
    public String AppKey;
    public String AppSecret;
    public String ACCSDoman = "acs.emas-ha.cn";
    public Map<String, String> IPStrategy;
    public String MTOPDoman;
    public String ChannelID;
    public String CacheURL;
    public String HAOSSBucketName;
    public String HAUniversalHost;
    public String HARSAPublicKey;
    public String StartActivity;

    @Override
    public String toString() {
        return "EMASInfo{" +
                "AppKey='" + AppKey + '\'' +
                ", AppSecret='" + AppSecret + '\'' +
                ", ACCSDoman='" + ACCSDoman + '\'' +
                ", IPStrategy=" + IPStrategy +
                ", MTOPDoman='" + MTOPDoman + '\'' +
                ", ChannelID='" + ChannelID + '\'' +
                ", CacheURL='" + CacheURL + '\'' +
                ", HAOSSBucketName='" + HAOSSBucketName + '\'' +
                ", HAUniversalHost='" + HAUniversalHost + '\'' +
                ", HARSAPublicKey='" + HARSAPublicKey + '\'' +
                ", StartActivity='" + StartActivity + '\'' +
                '}';
    }
}
