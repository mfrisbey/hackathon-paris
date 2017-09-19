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

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingServlet(
        paths = {"/bin/facebook/webhook"}
)
public class FacebookServlet extends SlingAllMethodsServlet {
    
    private static final long serialVersionUID = 8232363484487708721L;

    private static final Logger log = LoggerFactory.getLogger(FacebookServlet.class);
    
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
        request.getParameter("json");
        JSONParser json = new JSONParser();
        var messageData = {
                recipient: {
                  id: recipientId
                },
                message: {
                  text: messageText
                }
              };

              callSendAPI(messageData);
    }
    
    function callSendAPI(messageData) {
        request({
          uri: 'https://graph.facebook.com/v2.6/me/messages',
          qs: { access_token: PAGE_ACCESS_TOKEN },
          method: 'POST',
          json: messageData

        }, function (error, response, body) {
          if (!error && response.statusCode == 200) {
            var recipientId = body.recipient_id;
            var messageId = body.message_id;

            console.log("Successfully sent generic message with id %s to recipient %s", 
              messageId, recipientId);
          } else {
            console.error("Unable to send message.");
            console.error(response);
            console.error(error);
          }
        });  
      }
}
