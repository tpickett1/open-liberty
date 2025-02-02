/*******************************************************************************
 * Copyright (c) 2020, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.jaxrs21.client.fat;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21ClientCXFRxInvokerTest;
import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21ClientCallbackTest;
import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21ClientCompletionStageRxInvokerTest;
import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21ClientJerseyRxInvokerTest;
import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21ClientLTPATest;
//import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21ClientRestEasyRxInvokerTest;
import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21ClientSSLProxyAuthTest;
import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21ClientSSLTest;
import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21ComplexClientTest;
import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21ExecutorsTest;
import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21ReactiveSampleTest;
import com.ibm.ws.jaxrs21.client.fat.test.JAXRS21TimeoutClientTest;

import componenttest.custom.junit.runner.AlwaysPassesTest;
import componenttest.rules.repeater.JakartaEE10Action;
import componenttest.rules.repeater.JakartaEE9Action;
import componenttest.rules.repeater.RepeatTests;

@RunWith(Suite.class)
@SuiteClasses({ AlwaysPassesTest.class,
                JAXRS21ClientCallbackTest.class,
                JAXRS21ClientCompletionStageRxInvokerTest.class,
                JAXRS21ClientCXFRxInvokerTest.class,
                JAXRS21ClientJerseyRxInvokerTest.class,
                JAXRS21ClientLTPATest.class,
                JAXRS21ClientSSLProxyAuthTest.class,
                JAXRS21ClientSSLTest.class,
                JAXRS21ComplexClientTest.class,
                JAXRS21ExecutorsTest.class,
                JAXRS21ReactiveSampleTest.class,
                JAXRS21TimeoutClientTest.class
                })
public class FATSuite {
    @ClassRule
    public static RepeatTests r = RepeatTests.withoutModification()
                    .andWith(new JakartaEE9Action().alwaysAddFeature("jsonb-2.0"))
                    .andWith(new JakartaEE10Action().alwaysAddFeature("jsonb-3.0"));
;
}
