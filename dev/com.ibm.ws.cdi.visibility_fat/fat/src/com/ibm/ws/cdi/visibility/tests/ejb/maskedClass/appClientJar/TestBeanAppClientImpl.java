/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.cdi.visibility.tests.ejb.maskedClass.appClientJar;

import javax.enterprise.context.ApplicationScoped;

import com.ibm.ws.cdi.visibility.tests.ejb.maskedClass.libJar.TestBean;

/**
 * An implementation of the TestBean within the app client jar
 */
@ApplicationScoped
public class TestBeanAppClientImpl implements TestBean {

    @Override
    public String getMessage() {
        return "This is TestBean from the app client jar";
    }

}
