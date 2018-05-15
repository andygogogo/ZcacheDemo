package com.zcache.demo;

import java.io.Serializable;

/**
 * Created by jason on 18/4/16.
 */

public class PrivateCloudEmasConfig implements Serializable {
    public EMASInfo private_cloud_config;

    @Override
    public String toString() {
        return "PrivateCloudEmasConfig{" +
                "private_cloud_config=" + private_cloud_config +
                '}';
    }
}
