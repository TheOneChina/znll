package com.web.util;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RequestUtil {
    public RequestUtil() {
        super();
    }

    public static Map<String, String> getRequestParams(HttpServletRequest request) {

        Map<String, String> params = new HashMap<>();
        if (null != request) {
            Set<String> paramsKey = request.getParameterMap().keySet();
            for (String key : paramsKey) {
                params.put(key, request.getParameter(key));
            }
        }
        return params;
    }

}
