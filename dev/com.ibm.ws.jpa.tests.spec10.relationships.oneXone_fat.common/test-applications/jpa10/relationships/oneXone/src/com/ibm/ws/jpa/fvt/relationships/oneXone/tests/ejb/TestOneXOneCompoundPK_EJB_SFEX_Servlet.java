/*******************************************************************************
 * Copyright (c) 2019, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.jpa.fvt.relationships.oneXone.tests.ejb;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.servlet.annotation.WebServlet;

import org.junit.Test;

import com.ibm.ws.testtooling.testinfo.JPAPersistenceContext;
import com.ibm.ws.testtooling.testinfo.JPAPersistenceContext.PersistenceContextType;
import com.ibm.ws.testtooling.testinfo.JPAPersistenceContext.PersistenceInjectionType;
import com.ibm.ws.testtooling.testinfo.TestExecutionContext;
import com.ibm.ws.testtooling.vehicle.web.EJBDBTestVehicleServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/TestOneXOneCompoundPK_EJB_SFEX_Servlet")
public class TestOneXOneCompoundPK_EJB_SFEX_Servlet extends EJBDBTestVehicleServlet {
    private final String testLogicClassName = "com.ibm.ws.jpa.fvt.relationships.oneXone.testlogic.OneXOneCompoundPKTestLogic";

    private final HashMap<String, JPAPersistenceContext> jpaPctxMap = new HashMap<String, JPAPersistenceContext>();

    private final static String ejbJNDIName = "ejb/OneXOneSFExEJB";

    @PostConstruct
    private void initFAT() {
        jpaPctxMap.put("test-jpa-resource-cmex",
                       new JPAPersistenceContext("test-jpa-resource-cmex", PersistenceContextType.CONTAINER_MANAGED_ES, PersistenceInjectionType.JNDI, "java:comp/env/jpa/OneXOne_CompoundPK_CMEX"));
    }

    /*
     * Verify that the JPA provider can manage OneXOne relationships where the
     * entity on the inverse side of the relationship has a compound (Embeddable/ID Class) primary key.
     * Entities and IdClass are defined in annotation.
     *
     * Test Strategy:
     *
     * 1) Start a new transaction
     * 2) Create ICompoundPKOneXOneEntityA(id=1)
     * Create ICompoundPKOneXOneEntityB(id=1)
     * Set ICompoundPKOneXOneEntityA(id=1) to reference ICompoundPKOneXOneEntityB(id=1) in
     * a OneXOne relationship
     * 3) Commit the transaction
     * 4) Clear the persistence context
     * 5) Find ICompoundPKOneXOneEntityA(id=1), access ICompoundPKOneXOneEntityB(id=1) from
     * ICompoundPKOneXOneEntityA(id=1)'s OneXOne relationship field.
     *
     * Test passes if the relationship properly references ICompoundPKOneXOneEntityB(id=1)
     */

    @Test
    public void jpa10_Relationships_OneXOne_CompoundPK_Embeddable_001_Ano_CMEX_SF_EJB() throws Exception {
        final String testName = "jpa10_Relationships_OneXOne_CompoundPK_Embeddable_001_Ano_CMEX_SF_EJB";
        final String testMethod = "testOneXOneCompoundPK001";

        final TestExecutionContext testExecCtx = new TestExecutionContext(testName, testLogicClassName, testMethod);

        final HashMap<String, JPAPersistenceContext> jpaPCInfoMap = testExecCtx.getJpaPCInfoMap();
        jpaPCInfoMap.put("test-jpa-resource", jpaPctxMap.get("test-jpa-resource-cmex"));

        HashMap<String, java.io.Serializable> properties = testExecCtx.getProperties();
        properties.put("EntityAName", "EmbedIDOOEntA");
        properties.put("EntityBName", "EmbedIDOOEntB");

        executeDDL("JPA10_ONEXONE_DELETE_${dbvendor}.ddl");
        executeTestVehicle(testExecCtx, ejbJNDIName);
    }

    @Test
    public void jpa10_Relationships_OneXOne_CompoundPK_Embeddable_001_XML_CMEX_SF_EJB() throws Exception {
        final String testName = "jpa10_Relationships_OneXOne_CompoundPK_Embeddable_001_XML_CMEX_SF_EJB";
        final String testMethod = "testOneXOneCompoundPK001";

        final TestExecutionContext testExecCtx = new TestExecutionContext(testName, testLogicClassName, testMethod);

        final HashMap<String, JPAPersistenceContext> jpaPCInfoMap = testExecCtx.getJpaPCInfoMap();
        jpaPCInfoMap.put("test-jpa-resource", jpaPctxMap.get("test-jpa-resource-cmex"));

        HashMap<String, java.io.Serializable> properties = testExecCtx.getProperties();
        properties.put("EntityAName", "XMLEmbedIDOOEntA");
        properties.put("EntityBName", "XMLEmbedIDOOEntB");

        executeDDL("JPA10_ONEXONE_DELETE_${dbvendor}.ddl");
        executeTestVehicle(testExecCtx, ejbJNDIName);
    }

    @Test
    public void jpa10_Relationships_OneXOne_CompoundPK_IDClass_001_Ano_CMEX_SF_EJB() throws Exception {
        final String testName = "jpa10_Relationships_OneXOne_CompoundPK_IDClass_001_Ano_CMEX_SF_EJB";
        final String testMethod = "testOneXOneCompoundPK001";

        final TestExecutionContext testExecCtx = new TestExecutionContext(testName, testLogicClassName, testMethod);

        final HashMap<String, JPAPersistenceContext> jpaPCInfoMap = testExecCtx.getJpaPCInfoMap();
        jpaPCInfoMap.put("test-jpa-resource", jpaPctxMap.get("test-jpa-resource-cmex"));

        HashMap<String, java.io.Serializable> properties = testExecCtx.getProperties();
        properties.put("EntityAName", "IDClassOOEntA");
        properties.put("EntityBName", "IDClassOOEntB");

        executeDDL("JPA10_ONEXONE_DELETE_${dbvendor}.ddl");
        executeTestVehicle(testExecCtx, ejbJNDIName);
    }

    @Test
    public void jpa10_Relationships_OneXOne_CompoundPK_IDClass_001_XML_CMEX_SF_EJB() throws Exception {
        final String testName = "jpa10_Relationships_OneXOne_CompoundPK_IDClass_001_XML_CMEX_SF_EJB";
        final String testMethod = "testOneXOneCompoundPK001";

        final TestExecutionContext testExecCtx = new TestExecutionContext(testName, testLogicClassName, testMethod);

        final HashMap<String, JPAPersistenceContext> jpaPCInfoMap = testExecCtx.getJpaPCInfoMap();
        jpaPCInfoMap.put("test-jpa-resource", jpaPctxMap.get("test-jpa-resource-cmex"));

        HashMap<String, java.io.Serializable> properties = testExecCtx.getProperties();
        properties.put("EntityAName", "XMLIDClassOOEntA");
        properties.put("EntityBName", "XMLIDClassOOEntB");

        executeDDL("JPA10_ONEXONE_DELETE_${dbvendor}.ddl");
        executeTestVehicle(testExecCtx, ejbJNDIName);
    }

}
