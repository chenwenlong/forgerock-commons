/*
 * Copyright 2013 ForgeRock AS.
 *
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 */

package org.forgerock.openam.mobile.example.authentication;

import android.content.Context;
import android.util.Log;
import org.forgerock.openam.mobile.auth.AuthNAction;
import org.forgerock.openam.mobile.auth.AuthNConstants;
import org.forgerock.openam.mobile.auth.AuthenticationClient;
import org.forgerock.openam.mobile.commons.ActionType;
import org.forgerock.openam.mobile.commons.AndroidUtils;
import org.forgerock.openam.mobile.commons.Relay;
import org.forgerock.openam.mobile.commons.RestActions;
import org.forgerock.openam.mobile.commons.UnwrappedResponse;
import org.forgerock.openam.mobile.example.content.TokenDataSource;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * This class determines if the response is valid (not in network terms,
 * the Rest client marks those) for the action that has been set.
 *
 * It then triggers actions as appropriate -
 *  - firing off further async requests, as per AUTH_CONT
 *  - detecting failure and altering the action type
 *  - propagating the messages to any further listeners
 *
 * The "further listeners" are likely to be Activities, which
 * can perform actions based on the returned values from within
 * their Context if necessary.
 *
 */
public class Presenter extends Relay<UnwrappedResponse, String> {

    private static final String TAG = "Presenter";

    //a reference to the application context's android context used for Content Provider communications
    private final Context context = ApplicationContext.getInstance().getContext();
    private final TokenDataSource dataSource = new TokenDataSource();

    //the authentication client
    private AuthenticationClient authNClient;

    //to ensure we don't loop forever in case responses are invalid, network is down etc.
    private int authFailure = 0;
    private static final int FAILURE_LIMIT = 2;

    private TokenDataSource getDataSource() {
        return dataSource;
    }

    //set client
    public void setAuthNClient (AuthenticationClient authNClient) {
        this.authNClient = authNClient;
        authNClient.registerListener(this);
    }

    public AuthenticationClient getAuthNClient() {
        return authNClient;
    }

    //access methods for the view
    public String getSSOToken() {
        return getDataSource().getSSOToken(context);
    }

    public boolean removeLocalSSOToken() {
        return getDataSource().deleteSSOToken(context);
    }

    /**
     * The main bulk of the Presenter class. Here we are responsible for
     * making sure that the asynchronous requests we've sent off to the server
     * are received.
     *
     * The Presenter's duty here is threefold:
     *
     *  - Determine if there have been errors in the response from the client, and
     *    if so, log this and mark the ActionType as failed, using the appropriate
     *    failure response contained within the passed in UnwrappedResponse
     *
     *  - Alter the response-type such that the classes registered as Listeners
     *    to this Presenter receive the correct type - in this case we alter the response
     *    to be of type {@link String}, via the responseValue variable.
     *
     *  - Propagate the response and (possibly altered) action on to further listeners,
     *    such as Activities.
     *
     *
     * @param action The action of whose response we are handling
     * @param response The response returned to us from the client
     */
    @Override
    public void onEvent(ActionType action, UnwrappedResponse response) {

        if (action == RestActions.TRANSPORT_FAIL) {
            AndroidUtils.showToast("Transport failed. Check internet?", context);
            Log.e(TAG, "Transport failed. Aborting Presenter flow.");
            return;
        }

        if (response == null || action == null) {
            throw new NullPointerException("Response and action to the Presenter cannot be null.");
        }

        //anything that doesn't have an entry in the following (e.g. GET_COOKIE_NAME_FAIL)
        //should be handled by one of the listening classes to notify the user
        String responseValue = response.getEntityContent();

        if (action == AuthNAction.AUTH) {
            action = onSuccessfulAuthentication(response);
        } else if (action == AuthNAction.AUTH_FAIL) {
            attemptAuthRestart();
        } else if (action == AuthNAction.LOGOUT) {
            action = deleteSSOToken(action, response);
        } else if (action == AuthNAction.GET_COOKIE_DOMAINS) {
            //do nothing, pass on through, the activity has to handle this
            //as the JSON rep. is more useful than a String breakdown
        } else if (action == AuthNAction.GET_COOKIE_NAME) {
            responseValue = responseValue.substring(responseValue.indexOf("=") + 1).trim();
        } else if (action == AuthNAction.WEB_AUTH) {
            action = webAuth(action, response);
        } else if (action == AuthNAction.VALIDATE) {
            responseValue = responseValue.substring(responseValue.indexOf("=") + 1);
        } else if (action == AuthNAction.VALIDATE_FAIL) {
            action = fail("Validation failed.", response);
        }

        //continue propagating the success/failure messages
        notify(action, responseValue);

    }

    /**
     * Attempts to send the user back to start the auth process,
     * so long as we haven't overstepped our limit of the number of
     * attempts to make
     */
    private void attemptAuthRestart() {

        if (authFailure <= FAILURE_LIMIT) {
            authFailure++;
            getAuthNClient().authenticate();
            AndroidUtils.showToast("Failed auth. Try again.", context);
        }

    }

    /**
     * Ensures that the returnin values from the web authentication are appropriate
     *
     * @param action The response's action type
     * @param response The response from the auth client
     * @return the response's success or failure action type, depending on verification
     */
    private ActionType webAuth(ActionType action, UnwrappedResponse response) {
        String serverSetup = null;

        try {
            serverSetup = getAuthNClient().getOpenAmServerResource().toJSON();
        } catch (JSONException e) {
            action = fail("Unable to read OpenAM Server Config", response);
        }

        if (!getDataSource().storeSSOToken(response.getEntityContent(), context, serverSetup)) {
            action = fail("Unable to store SSO token", response);
        }

        return action;
    }

    /**
     * Checks to perform when we have been returned a successful
     * AUTH signal.
     *
     * @param response the unwrapped HTTP response from the auth request
     */
    private ActionType onSuccessfulAuthentication(UnwrappedResponse response) {

        String responseValue = response.getEntityContent();
        ActionType action = response.getSuccessActionType();

        authFailure = 0; //dont let them just loop forever

        JSONObject data;

        try {
            data = new JSONObject(responseValue);
        } catch (JSONException e) {
            return fail("Unable to read response value", response);
        }

        if (data.has(AuthNConstants.TOKEN_ID)) {

            String tokenId;
            String serverSetup;

            try {
                tokenId = data.getString(AuthNConstants.TOKEN_ID);
                serverSetup = getAuthNClient().getOpenAmServerResource().toJSON();
            } catch (JSONException e) {
                return fail("Unable to load OpenAM Server Config", response);
            }

            if (!getDataSource().storeSSOToken(tokenId, context, serverSetup)) {
                return fail("Unable to store SSO token", response);
            }

        }

        return action;

    }

    /**
     * Deletes the SSO token
     *
     * @param action
     * @param response
     * @return
     */
    private ActionType deleteSSOToken(ActionType action, UnwrappedResponse response) {
        if (!removeLocalSSOToken()) {
            action = fail("Unable to delete SSO token", response);
        }

        return action;
    }

    /**
     * Writes out a log message, returns the failure-state {@link ActionType} from
     * the {@link UnwrappedResponse} as the new action to be propagated
     *
     * @param msg A message to log
     * @param response The response from which to read the failure ActionType
     * @return The failure ActionType to set to further-propagated messages
     */
    private ActionType fail(String msg, UnwrappedResponse response) {
        Log.d(TAG, msg);
        AndroidUtils.showToast(msg, ApplicationContext.getInstance().getContext());
        return response.getFailActionType();
    }



}
