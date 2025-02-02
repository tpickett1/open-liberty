/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.security.fat.common.expectations;

import java.util.Arrays;
import java.util.List;

import com.ibm.websphere.simplicity.log.Log;
import com.ibm.ws.security.fat.common.Constants;

import componenttest.topology.impl.LibertyServer;

public class ServerTraceExpectation extends Expectation {

    public static final String DEFAULT_FAILURE_MSG = "Did not find the expected error message [%s] in the server log.";

    private LibertyServer server = null;

    public ServerTraceExpectation(LibertyServer server, String searchFor) {
        this(null, server, searchFor);
    }

    public ServerTraceExpectation(LibertyServer server, String searchFor, String failureMsg) {
        super(null, Constants.TRACE_LOG, Constants.STRING_MATCHES, searchFor, failureMsg);
        this.server = server;
    }

    public ServerTraceExpectation(String testAction, LibertyServer server, String searchFor) {
        this(testAction, server, searchFor, String.format(DEFAULT_FAILURE_MSG, searchFor));
    }

    public ServerTraceExpectation(String testAction, LibertyServer server, String searchFor, String failureMsg) {
        super(testAction, Constants.TRACE_LOG, Constants.STRING_MATCHES, searchFor, failureMsg);
        this.server = server;
    }

    public ServerTraceExpectation(String testAction, LibertyServer server, String searchType, String searchFor, String failureMsg) {
        super(testAction, Constants.TRACE_LOG, searchType, searchFor, failureMsg);
        this.server = server;
    }

    @Override
    protected void validate(Object contentToValidate) throws Exception {
        addMessageToIgnoredErrors();
        if (Constants.STRING_DOES_NOT_MATCH.equals(checkType) || Constants.STRING_DOES_NOT_CONTAIN.equals(checkType)) {
            if (isMessageLogged()) {
                throw new Exception(failureMsg);
            }
        } else {
            if (!isMessageLogged()) {
                throw new Exception(failureMsg);
            }
        }
    }

    void addMessageToIgnoredErrors() {
        List<String> msgs = Arrays.asList(validationValue);
        server.addIgnoredErrors(msgs);
    }

    boolean isMessageLogged() {
        String errorMsg = waitForStringInLogFile();
        boolean isMessageLogged = errorMsg != null;
        String logMsg = isMessageLogged ? ("Found message: " + errorMsg) : "Did NOT find message [" + validationValue + "] in " + server.getServerName() + " server log!";
        Log.info(getClass(), "isMessageLogged", logMsg);
        return isMessageLogged;
    }

    String waitForStringInLogFile() {
        if (Constants.TRACE_LOG.equals(searchLocation)) {
            return server.waitForStringInTraceUsingMark(validationValue, 100);
        } else {
            return server.waitForStringInLogUsingMark(validationValue, 100);
        }
    }

}