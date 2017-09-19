/*
 * ******************************************************************************
 *  * ADOBE CONFIDENTIAL
 *  * ___________________
 *  *
 *  * Copyright 2016 Adobe Systems Incorporated
 *  * All Rights Reserved.
 *  *
 *  * NOTICE:  All information contained herein is, and remains
 *  * the property of Adobe Systems Incorporated and its suppliers,
 *  * if any.  The intellectual and technical concepts contained
 *  * herein are proprietary to Adobe Systems Incorporated and its
 *  * suppliers and are protected by trade secret or copyright law.
 *  * Dissemination of this information or reproduction of this material
 *  * is strictly forbidden unless prior written permission is obtained
 *  * from Adobe Systems Incorporated.
 *  *****************************************************************************
 */
package com.adobe.cq.facebook.impl.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {

  private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

  public static final CloseableHttpResponse httpPost(Object body, String uri,
      CloseableHttpClient httpClient,
      List<Header> headers) throws IOException {

    HttpPost post = new HttpPost(uri);
    post.addHeader("Content-Type", "application/json");
    if (headers != null) {
      for (Header header : headers) {
        post.addHeader(header);
      }
    }
    if (null != body) {
      ObjectMapper mapper = new ObjectMapper();
      StringEntity jsonBody = new StringEntity(mapper.writeValueAsString(body));
      post.setEntity(jsonBody);
    }

    return httpClient.execute(post);
  }

  public static String httpGetContentIf2xx(String uri,
      CloseableHttpClient httpClient,
      List<Header> headers) throws IOException {
    HttpResponse response = HttpUtil.httpGet(uri, httpClient, headers);
    int responseStatusCode = response.getStatusLine().getStatusCode();
    log.debug("response code: {}", responseStatusCode);
    if (responseStatusCode < 200 || responseStatusCode >= 300) {
      log.warn("The http GET to the endpoint {} failed, code{} , body  {}: ",
          uri, responseStatusCode, EntityUtils.toString(response.getEntity()));
      return null;
    } else {
      return EntityUtils.toString(response.getEntity());
    }
  }

  public static final CloseableHttpResponse httpGet(String uri,
      CloseableHttpClient httpClient,
      List<Header> headers) throws IOException {

    HttpGet get = new HttpGet(uri);
    if (headers != null) {
      for (Header header : headers) {
        get.addHeader(header);
      }
    }
    return httpClient.execute(get);
  }

  public static final String responseInString(CloseableHttpResponse resp) throws IOException {
    return EntityUtils.toString(resp.getEntity());
  }

  public static String getRequestBody(SlingHttpServletRequest request) throws IOException {
    String body = null;

    // found this hack at https://git.corp.adobe.com/DCOCloud/dcocloud/blob/master/commons/src/main/java/com/adobe/dcocloud/commons/user/impl/UserApiHelperServiceImpl.java#L103

    // First try from the parameter map of the Sling request in case
    // Sling has already processed the request (is in a POST)
    final Map<String, RequestParameter[]> params = request.getRequestParameterMap();
    if (params.size() == 1) {
      // The request body is both the key and the parameter name, so
      // just use the key. The alternative would involve something like:
      //  Map.Entry<String, RequestParameter[]> pair = params.entrySet().iterator().next();
      //  final String key = pair.getKey();
      //  final RequestParameter[] paramArray = pair.getValue();
      //  final RequestParameter param = paramArray[0];
      //  body = key;
      //  body = param.getName();

      Set<String> keySet = params.keySet();
      body = keySet.iterator().next();
      return body;
    }

    BufferedReader reader = null;
    try {
      reader = request.getReader();
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      body = sb.toString();
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        log.error("could not close the request body buffer reader, due to {}", e.getMessage(), e);
      }
    }
    return body;
  }

}
