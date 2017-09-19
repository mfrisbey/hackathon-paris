/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2015 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
package com.adobe.cq.facebook.impl.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

@SlingServlet(
        paths = {"/bin/facebook/webhook"},
        methods = {"POST", "GET"}
)
public class FacebookServlet extends SlingAllMethodsServlet {
    
    private static final long serialVersionUID = 8232363484487708721L;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FacebookServlet.class);
    
    protected CloseableHttpClient httpClient;
    
    public FacebookServlet() {
    }

    /**
     * Processes a request dealing with source control for the companion application.
     */
    @Override
    public void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String hubMode = request.getParameter("hub.mode");
        String verifyToken = request.getParameter("hub.verify_token");
        String challenge = request.getParameter("hub.challenge");
        if (hubMode != null && verifyToken != null && challenge != null && hubMode.equalsIgnoreCase("subscribe") &&
                verifyToken.equalsIgnoreCase("too_many_secrets")) {
          log.info("Validating webhook");
          response.setContentType("text/plain");
          response.setStatus(200);
          response.getWriter().print(challenge);
        } else {
          log.error("Failed validation. Make sure the validation tokens match.");
          response.setStatus(403);
        }  
    }
    
    @Override
    public void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        // mark.frisbey
        JSONObject json = new JSONObject();
        JSONObject recipient = new JSONObject();
        JSONObject message = new JSONObject();
        try {
            recipient.put("id", "mark.frisbey");
            message.put("text", request.getParameter("message"));
            json.put("recipient", recipient);
            json.put("message", message);
            callSendAPI(json);
        } catch (JSONException e) {
            log.error("unexpecte json error building message json", e);
            response.setStatus(500);
            response.getWriter().print("unexpected json error");
        }
    }
    
    @Activate
    protected void activate(BundleContext context, Map<String, Object> config) {
        httpClient = HttpClients.createDefault();
    }
    
    private void callSendAPI(JSONObject json) throws IOException {
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("access_token", "EAAbVkTdGxxoBAFlBmyNwvXMuU5K4uZAy7bqrBnknZBzNUGIK8UFbnsR4tlZBqQGhcTKFgzvgnwazuz0aFJZCETOy7R9F1mT8Kz5bI7vM6GZCVn2dz2kEv45lvNZAf7M9isC2ULlsZAhfaZBZAuQW7OZAZC9Cq9NdEFsDvjr0VJAZCHK7WwZDZD"));
        
        HttpResponse response = HttpUtil.httpPost(json, "https://graph.facebook.com/v2.6/me/messages", httpClient, headers);
        
        int responseStatusCode = response.getStatusLine().getStatusCode();

        String responseBody = "";
         if (response.getEntity() != null) {
           responseBody = EntityUtils.toString(response.getEntity());
         }
         
         log.info("received response");
      }
}
