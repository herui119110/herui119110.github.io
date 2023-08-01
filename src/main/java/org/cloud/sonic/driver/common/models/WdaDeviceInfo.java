package org.cloud.sonic.driver.common.models;

import lombok.Data;

@Data
public class WdaDeviceInfo {

    private String state;
    private String iosVersion;
    private String ip;
    private Boolean ready;


}
