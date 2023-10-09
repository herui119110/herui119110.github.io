/*
 *  Copyright (C) [SonicCloudOrg] Sonic Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.cloud.sonic.driver.common.tool;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.cloud.sonic.driver.common.models.BaseResp;
import org.cloud.sonic.driver.common.models.ErrorMsg;

import java.util.HashMap;
import java.util.Map;

public class RespHandler {
    public static final int DEFAULT_REQUEST_TIMEOUT = 15000;
    private int requestTimeout = 15000;

    public void setRequestTimeOut(int timeOut) {
        requestTimeout = timeOut;
    }

    public BaseResp getResp(HttpRequest httpRequest) throws SonicRespException {
        return getResp(httpRequest, requestTimeout);
    }

    public BaseResp getRespV2(HttpRequest httpRequest) throws SonicRespException {
        return getRespV2(httpRequest, requestTimeout);
    }

    public BaseResp getRespV2(HttpRequest httpRequest, int timeout) throws SonicRespException {
        synchronized (this) {
            try {
                return initRespV2(httpRequest.addHeaders(initHeader()).timeout(timeout).execute());
            } catch (HttpException | IORuntimeException e) {
                e.printStackTrace();
                throw new SonicRespException(e.getMessage());
            }
        }
    }

    private BaseResp initRespV2(HttpResponse response) {
        String body = response.body();
        BaseResp err = JSON.parseObject(body, BaseResp.class);
        if (body.contains("traceback") || body.contains("stacktrace")) {
            ErrorMsg errorMsg = JSONObject.parseObject(err.getValue().toString(), ErrorMsg.class);
            err.setErr(errorMsg);
            err.setValue(null);
        }
        err.setCode(response.getStatus());
        return err;
    }

    public BaseResp getResp(HttpRequest httpRequest, int timeout) throws SonicRespException {
        synchronized (this) {
            try {
                return initResp(httpRequest.addHeaders(initHeader()).timeout(timeout).execute().body());
            } catch (HttpException | IORuntimeException e) {
                e.printStackTrace();
                throw new SonicRespException(e.getMessage());
            }
        }
    }

    public BaseResp initResp(String response) {
        if (response.contains("traceback") || response.contains("stacktrace")) {
            return initErrorMsg(response.replace("stacktrace", "traceback"));
        } else {
            return JSON.parseObject(response, BaseResp.class);
        }
    }

    public Map<String, String> initHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        return headers;
    }

    public BaseResp initErrorMsg(String resp) {
        BaseResp err = JSON.parseObject(resp, BaseResp.class);
        ErrorMsg errorMsg = JSONObject.parseObject(err.getValue().toString(), ErrorMsg.class);
        err.setErr(errorMsg);
        err.setValue(null);
        return err;
    }

    public boolean ping(HttpRequest httpRequest) {
        synchronized (this) {
            try {
                HttpResponse response = httpRequest.addHeaders(initHeader()).timeout(requestTimeout).execute();
               return response.isOk();
            } catch (HttpException | IORuntimeException e) {
                return false;

            }
        }
    }
}
