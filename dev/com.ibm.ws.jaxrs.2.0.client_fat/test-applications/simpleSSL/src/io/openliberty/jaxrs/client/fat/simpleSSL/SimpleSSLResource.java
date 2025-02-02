/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.jaxrs.client.fat.simpleSSL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class SimpleSSLResource {

    @GET
    @Path("echo")
    public String hello() {
        System.out.println("Hello from SimpleSSLResource!");
        return "Hello World!";
    }
}
