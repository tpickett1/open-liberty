/*******************************************************************************
 * Copyright (c) 2012,2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package servlettest.web;

import java.lang.AutoCloseable;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import com.ibm.ws.javaee.dd.appbnd.ApplicationBnd;
import com.ibm.ws.javaee.dd.appbnd.SecurityRole;
import com.ibm.ws.javaee.dd.appext.ApplicationExt;
import com.ibm.ws.javaee.dd.commonbnd.AuthenticationAlias;
import com.ibm.ws.javaee.dd.commonbnd.EJBRef;
import com.ibm.ws.javaee.dd.commonbnd.Interceptor;
import com.ibm.ws.javaee.dd.commonbnd.ResourceRef;
import com.ibm.ws.javaee.dd.ejbbnd.EJBJarBnd;
import com.ibm.ws.javaee.dd.ejbext.EJBJarExt;
import com.ibm.ws.javaee.dd.ejbext.EnterpriseBean;
import com.ibm.ws.javaee.dd.ejbext.TimeOut;
import com.ibm.ws.javaee.dd.managedbean.ManagedBean;
import com.ibm.ws.javaee.dd.managedbean.ManagedBeanBnd;
import com.ibm.ws.javaee.dd.web.common.LoginConfig;
import com.ibm.ws.javaee.dd.web.common.SecurityConstraint;
import com.ibm.ws.javaee.dd.web.common.UserDataConstraint;
import com.ibm.ws.javaee.dd.webbnd.VirtualHost;
import com.ibm.ws.javaee.dd.webbnd.WebBnd;
import com.ibm.ws.javaee.dd.webext.WebExt;
import com.ibm.ws.javaee.ddmodel.fat.ContainerHelper;
import com.ibm.ws.javaee.ddmodel.wsbnd.HttpPublishing;
import com.ibm.ws.javaee.ddmodel.wsbnd.Port;
import com.ibm.ws.javaee.ddmodel.wsbnd.ServiceRef;
import com.ibm.ws.javaee.ddmodel.wsbnd.WebserviceDescription;
import com.ibm.ws.javaee.ddmodel.wsbnd.WebserviceEndpoint;
import com.ibm.ws.javaee.ddmodel.wsbnd.WebserviceSecurity;
import com.ibm.ws.javaee.ddmodel.wsbnd.WebservicesBnd;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;

@SuppressWarnings("serial")
public class AutoServlet extends HttpServlet {

    private static final String TEST_WAR = "ServletTest";
    private static final String TEST_WAR_NO_BINDINGS = "ServletTestNoBnd";

    private static final String TEST_EJB = "EJBTest.jar";
    private static final String TEST_EJB_NO_BINDINGS = "EJBTestNoBnd.jar";

    //

    private BundleContext bundleContext;

    private final ArrayList<ServiceReference<?>> references =
            new ArrayList<ServiceReference<?>>();

    private void setBundleContext() {
        Bundle bundle = FrameworkUtil.getBundle(HttpServlet.class);
        bundleContext = bundle.getBundleContext();
    }

    private void clearBundleContext() {
        for (ServiceReference<?> ref : references) {
            bundleContext.ungetService(ref);
        }
        references.clear();

        bundleContext = null;
    }

    // Pseudo-closeable ... makes makanging the bundle context state
    // cleaner.

    private class BundleCloseable implements AutoCloseable {
        public BundleCloseable() {
            setBundleContext();
        }

        @Override
        public void close() {
            clearBundleContext();
        }
    }

    private ConfigurationAdmin getConfigurationAdmin() throws Exception {
        ServiceReference<ConfigurationAdmin> ref = bundleContext.getServiceReference(ConfigurationAdmin.class);
        if ( ref == null ) {
            throw new IllegalStateException("No ConfigurationAdmin service");
        }

        references.add(ref);
        return bundleContext.getService(ref);
    }

    private String getConfigurationFilter(String pid, boolean isFactory) {
        if ( isFactory ) {
            return "(" + ConfigurationAdmin.SERVICE_FACTORYPID + "=" + pid + ")";
        } else {
            return "(" + Constants.SERVICE_PID + "=" + pid + ")";
        }
    }    

    private Configuration[] listConfigurations(ConfigurationAdmin ca, String pid, boolean isFactory) throws Exception {
        return ca.listConfigurations( getConfigurationFilter(pid, isFactory) );
    }

    //

    private Container getEarContainer() {
        ServiceReference<ContainerHelper> chRef = bundleContext.getServiceReference(ContainerHelper.class);
        if ( chRef == null ) {
            throw new IllegalStateException("Could not find ContainerHelper reference");
        }

        ContainerHelper ch = bundleContext.getService(chRef);
        return ch.getContainer();
    }

    public Container getWarContainer(String warName) throws UnableToAdaptException {
        ServiceReference<ContainerHelper> chRef = bundleContext.getServiceReference(ContainerHelper.class);
        if ( chRef == null ) {
            return null;
        }

        ContainerHelper ch = bundleContext.getService(chRef);
        return ch.getModuleContainer(warName);
    }

    private Container getEJBJarContainer(String jarName) throws UnableToAdaptException {
        ServiceReference<ContainerHelper> chRef = bundleContext.getServiceReference(ContainerHelper.class);
        if ( chRef == null ) {
            return null;
        }

        ContainerHelper ch = bundleContext.getService(chRef);
        return ch.getModuleContainer(jarName);
    }

    //

    public static final String AutoMessage = "This is AutoServlet.";

    private static final String TEST_NAME = "testName";
    private static final String OK = "OK";
    private static final String FAIL = "Test failed, check logs for output";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String testName = request.getParameter(TEST_NAME);        

        System.out.println("AutoServlet.doGet: Starting test: " + testName);
        logMethods();

        try ( PrintWriter writer = response.getWriter() ) { 
            if ( testName == null ) {
                writer.println(AutoMessage);

            } else {
                Method testMethod;
                try {
                    testMethod = getClass().getMethod(testName);
                } catch ( Throwable th ) {
                    testMethod = null;
                    writer.println(FAIL);
                    writer.println("Error retrieving test method [ " + testName + " ]: [ " + th.getMessage() + " ]");
                }

                if ( testMethod != null ) {
                    try ( BundleCloseable bundleRef = new BundleCloseable() ) {
                        try {
                            Object testResult = testMethod.invoke(this); 
                            writer.println(testResult);
                        } catch ( Throwable th ) {
                            th.printStackTrace();
                            writer.println(FAIL);
                            writer.println("Error running test method [ " + testName + " ]: [ " + th.getMessage() + " ]");
                        }
                    }
                }
            }

        } finally {
            System.out.println("AutoServlet.doGet: Ending test: " + testName);
        }
    }

    private boolean didLogMethods;

    private void logMethods() {
        if ( didLogMethods ) {
            return;
        }

        didLogMethods = true;

        System.out.println("Test class [ " + getClass().getName() + " ]");
        System.out.println("==================================================");
        for ( Method declaredMethod : AutoServlet.class.getDeclaredMethods() ) {
            System.out.println("  Method [ " + declaredMethod + " ]");
        }
        System.out.println("==================================================");
    }

    //

    public String testAutoInstall() {
        return OK;
    }

    //

    public static final String APP_BINDINGS_CONFIG = "com.ibm.ws.javaee.dd.appbnd.ApplicationBnd";
    public static final String APP_EXTENSIONS_CONFIG = "com.ibm.ws.javaee.dd.appext.ApplicationExt";
    public static final String WEB_EXTENSION_CONFIG = "com.ibm.ws.javaee.dd.webext.WebExt";
    public static final String WEB_BINDINGS_CONFIG = "com.ibm.ws.javaee.dd.webbnd.WebBnd";
    public static final String EJB_EXTENSION_CONFIG = "com.ibm.ws.javaee.dd.ejbext.EJBJarExt";
    public static final String EJB_BINDINGS_CONFIG = "com.ibm.ws.javaee.dd.ejbbnd.EJBJarBnd";
    public static final String SECURITY_ROLE_CONFIG = "com.ibm.ws.javaee.dd.appbnd.SecurityRole";

    public static final String[] CONFIGS = { 
            APP_BINDINGS_CONFIG, APP_EXTENSIONS_CONFIG,
            WEB_BINDINGS_CONFIG, WEB_EXTENSION_CONFIG,
            EJB_BINDINGS_CONFIG, EJB_EXTENSION_CONFIG,
            SECURITY_ROLE_CONFIG
    };
    
    public static final int[] CONFIG_COUNTS = { 
            1, 1, // app
            2, 2, // web
            2, 2, // ejb
            1     // sec
    };

    // Simple check to make sure the component impl is present for each
    // configured type

    public String testConfig() throws Exception {
        List<String> configErrors = new ArrayList<String>();

        ConfigurationAdmin ca = getConfigurationAdmin();

        for ( int configNo = 0; configNo < CONFIGS.length; configNo++ ) {
            String pid = CONFIGS[configNo];
            int expectedConfigs = CONFIG_COUNTS[configNo];

            Configuration[] configs = listConfigurations(ca, pid, true);
            validateConfig(configs, pid, expectedConfigs, configErrors);
        }

        if ( configErrors.isEmpty() ) {
            return OK;
        } else {
            return configErrors.toString();
        }
    }

    private void validateConfig(
        Configuration[] configs, String pid,
        int expectedConfigs,
        List<String> configErrors) {
        
        String configError;
        if ( configs == null ) {
            configError = "No configs found for pid " + pid;
        } else if ( configs.length != expectedConfigs ) {
            configError = "Unexpected count of configs for pid " + pid + ": found " + configs.length + " but expected " + expectedConfigs;
        } else {
            configError = null;
        }
        if ( configError != null ) {
            configErrors.add(configError);
        }
    }

    public String testBindingsConfig() {
        return OK; // OBSOLETE
    }
    
    //
    
    public String testWebExtension() throws Exception {
        return testWebExtension(TEST_WAR);
    }

    public String testWebExtensionNoBindings() throws Exception {
        return testWebExtension(TEST_WAR_NO_BINDINGS);
    }

    private String testWebExtension(String testWar) throws Exception {
        Container warContainer = getWarContainer(testWar);

        WebExt webExt = warContainer.adapt(WebExt.class);
        if (webExt == null)
            return "Could not adapt WebExt";

        if (!webExt.isEnableFileServing()) {
            // app: true config: true
            return "Invalid value for enable-file-serving: false";
        }

        if (!"somePage".equals(webExt.getDefaultErrorPage())) {
            // app: undefined config: somePage
            return "Invalid value for default error page: " + webExt.getDefaultErrorPage();
        }

        return OK;
    }

    public String testWebBindings() throws Exception {
        return testWebBindings(TEST_WAR);
    }

    public String testWebBindingsNoBindings() throws Exception {
        return testWebBindings(TEST_WAR_NO_BINDINGS);
    }

    public String testWebBindings(String warName) throws Exception {
        Container warContainer = getWarContainer(warName);
        WebBnd webBnd = warContainer.adapt(WebBnd.class);
        if (webBnd == null)
            return "Could not adapt WebBnd";

        VirtualHost vh = webBnd.getVirtualHost();
        if (vh == null)
            return "Could not find virtual host";

        if (!"default_host".equals(vh.getName())) {
            // app: fromApp config: default_host
            return "Invalid value for virtual host: " + vh.getName();
        }

        List<EJBRef> ejbRefs = webBnd.getEJBRefs();
        if (ejbRefs == null || ejbRefs.isEmpty()) {
            return "EJB refs not found";
        } else if (ejbRefs.size() != 9) {
            // 8 in app, 1 in config
            return "Invalid number of ejb refs: " + ejbRefs.size() + "; expected 9";
        } else {
            EJBRef fromConfig = null;

            if (warName.equals(TEST_WAR)) {
                fromConfig = ejbRefs.get(8);
            } else {
                // no guarantees of order from configured entries, so look for
                // the right one
                for (EJBRef ref : ejbRefs) {
                    if ("ejb/fromConfig".equals(ref.getName())) {
                        fromConfig = ref;
                        break;
                    }
                }
                if (fromConfig == null)
                    return "Could not find any configured ejb ref with name ejb/fromConfig";
            }

            if (!"ejb/fromConfig".equals(fromConfig.getName()))
                return "Invalid ejbRef name: " + fromConfig.getName();
            if (!"ejb/com/ibm/ConfigHome".equals(fromConfig.getBindingName()))
                return "Invalid ejbRef binding name: " + fromConfig.getBindingName();
        }

        List<ResourceRef> resourceRefs = webBnd.getResourceRefs();
        if (resourceRefs == null || resourceRefs.isEmpty())
            return "Could not find resource refs";
        else if (resourceRefs.size() != 1)
            return "Invalid number of resource refs: " + resourceRefs.size() + "; expected 1";
        else {
            ResourceRef fromApp = resourceRefs.get(0);
            if (!"FuelDS".equals(fromApp.getName()))
                return "Invalid resource refname: " + fromApp.getName();
            if (!"jdbc/FuelDS".equals(fromApp.getBindingName()))
                return "Invalid resource ref binding name: " + fromApp.getBindingName();
        }

        int size = webBnd.getDataSources().size() +
                   webBnd.getResourceEnvRefs().size() +
                   webBnd.getEnvEntries().size() +
                   webBnd.getMessageDestinationRefs().size();
        if (size != 0)
            return "Found a resource ref, resource env ref, message destination ref, data source, or env entry that shouldn't exist";

        return OK;
    }

    public String testEJBExtension() throws Exception {
        Container ejbContainer = getEJBJarContainer(TEST_EJB);

        EJBJarExt extensions = ejbContainer.adapt(EJBJarExt.class);
        if (extensions == null)
            return "Could not adapt EJB jar extensions";

        List<EnterpriseBean> ejbs = extensions.getEnterpriseBeans();
        if (ejbs == null || ejbs.isEmpty())
            return "Could not find enterprise beans in ejb jar extensions";

        if (ejbs.size() != 1) {
            return "Invalid number of enterprise beans: " + ejbs.size() + "; expected 1";
        } else {
            EnterpriseBean ejb = ejbs.get(0);
            if (!"TestBean".equals(ejb.getName()))
                return "Invalid ejb name: " + ejb.getName();
            // ResourceRef also lives in commonbnd.
            List<com.ibm.ws.javaee.dd.commonext.ResourceRef> refs = ejb.getResourceRefs();
            if (refs.size() != 4)
                return "Invalid number of resource refs: " + refs.size() + "; expected 4";

            if (ejb.getStartAtAppStart() != null)
                return "Start at app start should not be specified";
            if (!(ejb instanceof com.ibm.ws.javaee.dd.ejbext.Session))
                return "The enterprise bean shoudl be a session bean";

            com.ibm.ws.javaee.dd.ejbext.Session session = (com.ibm.ws.javaee.dd.ejbext.Session) ejb;
            TimeOut t = session.getTimeOut();

            if (t == null)
                return "Time out should be specified on session bean";

            if (t.getValue() != 42)
                return "THe time out value should be 42";
        }
        return OK;
    }

    public String testEJBBindings() throws Exception {
        Container ejbContainer = getEJBJarContainer(TEST_EJB);

        EJBJarBnd bindings = ejbContainer.adapt(EJBJarBnd.class);
        if (bindings == null)
            return "Could not adapt EJB jar bindings";

        List<com.ibm.ws.javaee.dd.ejbbnd.EnterpriseBean> ejbs = bindings.getEnterpriseBeans();
        if (ejbs == null || ejbs.isEmpty())
            return "Could not find enterprise beans in ejb jar bindings";

        System.out.println("Enterprise beans:");
        for ( com.ibm.ws.javaee.dd.ejbbnd.EnterpriseBean ejb : ejbs ) {
            System.out.println("EJB [ " + ejb.getName() + " ] [ " + ejb + " ]");
        }

        if (ejbs.size() != 2) {
            return "Invalid number of enterprise beans: " + ejbs.size() + "; expected 2";

        } else {
            com.ibm.ws.javaee.dd.ejbbnd.EnterpriseBean fromApp = ejbs.get(0);
            com.ibm.ws.javaee.dd.ejbbnd.EnterpriseBean fromConfig = ejbs.get(1);

            if (!"TestBean".equals(fromConfig.getName()))
                return "Invalid ejb name: " + fromConfig.getName();

            if (!"TestBean".equals(fromApp.getName()))
                return "Invalid ejb name: " + fromApp.getName();

            List<ResourceRef> refs = fromConfig.getResourceRefs();
            if (refs == null || refs.size() != 3)
                return "Invalid number of resource refs; expected 3";

            boolean foundAuthAlias = false;
            for (ResourceRef ref : refs) {
                if (ref.getAuthenticationAlias() != null) {
                    foundAuthAlias = true;
                    AuthenticationAlias alias = ref.getAuthenticationAlias();
                    if (!("resRefAuthData".equals(alias.getName()))) {
                        return "Invalid auth alias name: " + alias.getName();
                    }
                }
            }

            if (!foundAuthAlias)
                return "Could not find authentication alias";

        }
        return OK;
    }

    public String testEJBBindingsNoBindings() throws Exception {
        Container ejbContainer = getEJBJarContainer(TEST_EJB_NO_BINDINGS);

        EJBJarBnd bindings = ejbContainer.adapt(EJBJarBnd.class);
        if (bindings == null)
            return "Could not adapt EJB jar bindings";

        List<com.ibm.ws.javaee.dd.ejbbnd.EnterpriseBean> ejbs = bindings.getEnterpriseBeans();
        if (ejbs == null || ejbs.isEmpty())
            return "Could not find enterprise beans in ejb jar extensions";

        if (ejbs.size() != 1) {
            return "Invalid number of enterprise beans: " + ejbs.size() + "; expected 1";
        } else {
            com.ibm.ws.javaee.dd.ejbbnd.EnterpriseBean fromConfig = ejbs.get(0);

            if (!"EJBBndStatefulBean".equals(fromConfig.getName()))
                return "Invalid ejb name: " + fromConfig.getName();

        }
        return OK;
    }

    public String testEJBExtensionNoBindings() throws Exception {
        Container ejbContainer = getEJBJarContainer(TEST_EJB_NO_BINDINGS);

        EJBJarExt extensions = ejbContainer.adapt(EJBJarExt.class);
        if (extensions == null)
            return "Could not adapt EJB jar extensions";

        List<EnterpriseBean> ejbs = extensions.getEnterpriseBeans();
        if (ejbs == null || ejbs.isEmpty())
            return "Could not find enterprise beans in ejb jar extensions";

        if (ejbs.size() != 1) {
            return "Invalid number of enterprise beans: " + ejbs.size() + "; expected 1";
        } else {
            EnterpriseBean ejb = ejbs.get(0);
            if (!"EJBBndStatefulBean".equals(ejb.getName()))
                return "Invalid ejb name: " + ejb.getName();
            // ResourceRef also lives in commonbnd.            
            List<com.ibm.ws.javaee.dd.commonext.ResourceRef> refs = ejb.getResourceRefs();
            if (refs.size() != 4)
                return "Invalid number of resource refs: " + refs.size() + "; expected 4";

            if (ejb.getStartAtAppStart() != null)
                return "Start at app start should not be specified";

        }
        return OK;
    }

    public String testApplicationExtensionFromWebApp() throws Exception {
        Container earContainer = getWarContainer(TEST_WAR);

        ApplicationExt appExt = earContainer.adapt(ApplicationExt.class);
        if (appExt == null)
            return "Could not adapt ApplicationExt";

        if (appExt.isSharedSessionContext()) {
            // app: true config: false
            return "Invalid value for shared-session-context: true";
        }

        return OK;
    }

    public String testApplicationExtension() throws Exception {
        Container earContainer = getEarContainer();

        ApplicationExt appExt = earContainer.adapt(ApplicationExt.class);
        if (appExt == null)
            return "Could not adapt ApplicationExt";

        if (appExt.isSharedSessionContext()) {
            // app: true config: false
            return "Invalid value for shared-session-context: true";
        }

        return OK;
    }

    public String testSecurityRoleOverrides() throws Exception {
        Container earContainer = getEarContainer();

        ApplicationBnd appBnd = earContainer.adapt(ApplicationBnd.class);
        if (appBnd == null)
            return "Could not adapt ApplicationBnd";

        List<SecurityRole> securityRoles = appBnd.getSecurityRoles();
        if (securityRoles == null)
            return "Could not adapt security roles";

        // There should be two security roles. The first should be from the app,
        // the second should be from config.
        if (securityRoles.size() != 2) {
            return "Invalid number of security roles found: " + securityRoles.size() + "; expected 2";
        }
        SecurityRole fromApp = securityRoles.get(0);
        if (!fromApp.getName().equals("snooping")) {
            return "Invalid security role name: " + fromApp.getName();
        }

        SecurityRole fromConfig = securityRoles.get(1);
        if (!fromConfig.getName().equals("user")) {
            return "Invalid security role name: " + fromConfig.getName();
        }

        return OK;

    }

    public String testSecurityRoleOverridesFromWebApp() throws Exception {
        Container earContainer = getWarContainer(TEST_WAR);

        ApplicationBnd appBnd = earContainer.adapt(ApplicationBnd.class);
        if (appBnd == null)
            return "Could not adapt ApplicationBnd";

        List<SecurityRole> securityRoles = appBnd.getSecurityRoles();
        if (securityRoles == null)
            return "Could not adapt security roles";

        // There should be two security roles in server.xml
        if (securityRoles.size() != 2) {
            return "Invalid number of security roles found: " + securityRoles.size() + "; expected 2";
        }

        for (SecurityRole role : securityRoles) {
            if (!role.getName().equals("snooping") && !role.getName().equals("user"))
                return "Invalid security role name: " + role.getName();
        }

        return OK;

    }

    public String testManagedBeanBindings() throws Exception {
        Container ejbContainer = getEJBJarContainer(TEST_EJB);

        ManagedBeanBnd binding = ejbContainer.adapt(ManagedBeanBnd.class);
        if (binding == null)
            return "Could not adapt ManagedBeanBnd";

        List<Interceptor> interceptors = binding.getInterceptors();
        if (!interceptors.isEmpty())
            return "Found invalid interceptor";

        List<ManagedBean> beans = binding.getManagedBeans();
        if (beans == null || beans.isEmpty())
            return "Could not find managed bean from bindings";

        if (beans.size() != 1)
            return "Invalid number of managed beans: " + beans.size() + "; expected 1";
        ManagedBean bean = beans.get(0);
        List<ResourceRef> refs = bean.getResourceRefs();
        if (refs == null || refs.isEmpty())
            return "Could not find resource reference";

        if (refs.size() != 1)
            return "Invalid number of resource references on managed bean: " + refs.size() + "; expected 1";

        ResourceRef ref = refs.get(0);
        if (!ref.getName().equals("jdbc/myBinding"))
            return "Invalid name for resource reference: " + ref.getName();
        if (!ref.getBindingName().equals("jdbc/TestDataSource"))
            return "Invalid bidning name for resource reference: " + ref.getBindingName();

        return OK;
    }

    public String testWebserviceBindingsNoBindings() throws Exception {
        Container warContainer = getWarContainer(TEST_WAR_NO_BINDINGS);

        WebservicesBnd wsbnd = warContainer.adapt(WebservicesBnd.class);
        if (wsbnd == null)
            return "Could not adapt web service binding";

        HttpPublishing httpPublishing = wsbnd.getHttpPublishing();
        if (httpPublishing == null)
            return "Could not find HttpPublishing";

        String contextRoot = httpPublishing.getContextRoot();
        if (contextRoot == null || !contextRoot.equals("someContextRoot"))
            return "Invalid value for context root: " + contextRoot;

        WebserviceSecurity wssec = httpPublishing.getWebserviceSecurity();
        if (wssec == null)
            return "Could not find web service security";

        LoginConfig loginConfig = wssec.getLoginConfig();
        if (loginConfig == null)
            return "Could not find login config";

        String authMethod = loginConfig.getAuthMethod();
        if (authMethod == null || !authMethod.equals("BASIC"))
            return "Invalid value for auth method on login config: " + authMethod;

        if (loginConfig.getFormLoginConfig() != null)
            return "Found a FormLoginConfig element that should be null";

        List<SecurityConstraint> securityConstraints = wssec.getSecurityConstraints();
        if (securityConstraints == null)
            return "Could not find security constraints";
        if (securityConstraints.size() != 1)
            return "Invalid number of security constraints: " + securityConstraints.size() + "; expected 1";

        SecurityConstraint securityConstraint = securityConstraints.get(0);
        if (securityConstraint.getAuthConstraint() == null)
            return "Could not find auth constraint";

        if (securityConstraint.getWebResourceCollections() == null)
            return "Could not find web resource collection";

        if (securityConstraint.getUserDataConstraint() == null)
            return "Could not find user data constraint";

        UserDataConstraint udc = securityConstraint.getUserDataConstraint();
        if (udc.getTransportGuarantee() != 1)
            return "Invalid value for transport guarantee: " + udc.getTransportGuarantee() + "; expected 1";

        List<com.ibm.ws.javaee.dd.common.SecurityRole> securityRoles = wssec.getSecurityRoles();
        if (securityRoles == null)
            return "Could not find security roles";
        if (securityRoles.size() != 1)
            return "Found invalid number of security roles: " + securityRoles.size() + "; expected 1";
        if (securityRoles.get(0).getRoleName() == null)
            return "Could not find role name for security role";

        List<ServiceRef> serviceRefs = wsbnd.getServiceRefs();
        if (serviceRefs == null)
            return "Could not find service refs";
        if (serviceRefs.size() != 3)
            return "Invalid number of service-ref elements: " + serviceRefs.size() + "; expected 3";

        for (ServiceRef serviceRef : serviceRefs) {
            Map<String, String> props = serviceRef.getProperties();
            if (props == null)
                return "Could not find serviceRef properties";
            String property = props.get("http.conduit.tlsClientParameters.disableCNCheck");
            if (property == null)
                return "Could not find disableCNCheck property";
            if (!property.equals("true"))
                return "Invalid value for property: " + property;

            String name = serviceRef.getName();
            if (name == null)
                return "Could not find a name for serviceRef";

            List<Port> ports = serviceRef.getPorts();
            if (ports == null || ports.isEmpty())
                return "Could not find ports for serviceRef";
            if (ports.size() != 1)
                return "Invalid number of ports: " + ports.size() + "; expected 1";
            Port port = ports.get(0);
            if (!"employee0".equals(port.getUserName()))
                return "Invalid username for port: " + port.getUserName();
            if (!"http://ibm.com/ws/jaxws/transport/security/".equals(port.getNamespace()))
                return "Invalid namespace for port: " + port.getNamespace();
            String password = new String(port.getPassword().getChars());
            if (!"emp0pwd".equals(password))
                return "Invalid password for port: " + password;
            if (port.getName() == null)
                return "Could not find port name";

            if ("service/SayHelloPojoService".equals(name)) {
                if (!port.getName().equals("SayHelloPojoPort"))
                    return "Invalid port name value: " + port.getName();
            } else if ("service/SayHelloStatelessService".equals(name)) {
                if (!port.getName().equals("SayHelloStatelessPort"))
                    return "Invalid port name value: " + port.getName();
            } else if ("service/SayHelloSingletonService".equals(name)) {
                if (!port.getName().equals("SayHelloSingletonPort"))
                    return "Invalid port name value: " + port.getName();
            } else {
                return "Found unexpected serviceRef name: " + name;
            }
        }

        List<WebserviceEndpoint> wsEndpoints = wsbnd.getWebserviceEndpoints();
        if (wsEndpoints == null)
            return "Could not find webservice endpoints";
        if (wsEndpoints.size() != 1)
            return "Invalid number of ws endpoints: " + wsEndpoints.size() + "; expected 1";
        WebserviceEndpoint wsep = wsEndpoints.get(0);
        String portComponentName = wsep.getPortComponentName();
        String address = wsep.getAddress();
        if (portComponentName == null)
            return "Could not find port component name";
        if (address == null)
            return "Could not find address for wsep";

        if (!portComponentName.equals("Hello"))
            return "Invalid port component name: " + portComponentName;
        if (!address.equals("/hi"))
            return "Invalid address: " + address;

        Map<String, String> wsEpProps = wsbnd.getWebserviceEndpointProperties();
        if (wsEpProps == null)
            return "Could not find webservice endpoint properties";
        if (wsEpProps.size() != 1)
            return "Invalid number of ws endpoint properties: " + wsEpProps.size() + "; expected 1";
        String value = wsEpProps.get("someAttribute");
        if (value == null)
            return "Could not find value for property someAttribute";
        if (!value.equals("test"))
            return "Invalid value for property someAttribute: " + value;

        return OK;
    }

    public String testWebserviceBindings() throws Exception {
        Container warContainer = getWarContainer(TEST_WAR);

        WebservicesBnd wsbnd = warContainer.adapt(WebservicesBnd.class);
        if (wsbnd == null)
            return "Could not adapt web service binding";

        HttpPublishing httpPublishing = wsbnd.getHttpPublishing();
        if (httpPublishing == null)
            return "Could not find HttpPublishing";

        String contextRoot = httpPublishing.getContextRoot();
        if (contextRoot == null || !contextRoot.equals("someContextRoot"))
            return "Invalid value for context root: " + contextRoot;

        WebserviceSecurity wssec = httpPublishing.getWebserviceSecurity();
        if (wssec == null)
            return "Could not find web service security";

        LoginConfig loginConfig = wssec.getLoginConfig();
        if (loginConfig == null)
            return "Could not find login config";

        String authMethod = loginConfig.getAuthMethod();
        if (authMethod == null || !authMethod.equals("BASIC"))
            return "Invalid value for auth method on login config: " + authMethod;

        if (loginConfig.getFormLoginConfig() != null)
            return "Found a FormLoginConfig element that should be null";

        List<SecurityConstraint> securityConstraints = wssec.getSecurityConstraints();
        if (securityConstraints == null)
            return "Could not find security constraints";
        if (securityConstraints.size() != 1)
            return "Invalid number of security constraints: " + securityConstraints.size() + "; expected 1";

        SecurityConstraint securityConstraint = securityConstraints.get(0);
        if (securityConstraint.getAuthConstraint() == null)
            return "Could not find auth constraint";

        if (securityConstraint.getWebResourceCollections() == null)
            return "Could not find web resource collection";

        if (securityConstraint.getUserDataConstraint() == null)
            return "Could not find user data constraint";

        UserDataConstraint udc = securityConstraint.getUserDataConstraint();
        if (udc.getTransportGuarantee() != 1)
            return "Invalid value for transport guarantee: " + udc.getTransportGuarantee() + "; expected 1";

        List<com.ibm.ws.javaee.dd.common.SecurityRole> securityRoles = wssec.getSecurityRoles();
        if (securityRoles == null)
            return "Could not find security roles";
        if (securityRoles.size() != 1)
            return "Found invalid number of security roles: " + securityRoles.size() + "; expected 1";
        if (securityRoles.get(0).getRoleName() == null)
            return "Could not find role name for security role";

        List<ServiceRef> serviceRefs = wsbnd.getServiceRefs();
        if (serviceRefs == null)
            return "Could not find service refs";
        if (serviceRefs.size() != 4)
            return "Invalid number of service-ref elements: " + serviceRefs.size() + "; expected 4";

        for (ServiceRef serviceRef : serviceRefs) {
            Map<String, String> props = serviceRef.getProperties();
            if (props == null)
                return "Could not find serviceRef properties";
            String property = props.get("http.conduit.tlsClientParameters.disableCNCheck");
            if (property == null)
                return "Could not find disableCNCheck property";
            if (!property.equals("true"))
                return "Invalid value for property: " + property;

            String name = serviceRef.getName();
            if (name == null)
                return "Could not find a name for serviceRef";

            List<Port> ports = serviceRef.getPorts();
            if (ports == null || ports.isEmpty())
                return "Could not find ports for serviceRef";
            if (ports.size() != 1)
                return "Invalid number of ports: " + ports.size() + "; expected 1";
            Port port = ports.get(0);
            if (!"employee0".equals(port.getUserName()))
                return "Invalid username for port: " + port.getUserName();
            if (!"http://ibm.com/ws/jaxws/transport/security/".equals(port.getNamespace()))
                return "Invalid namespace for port: " + port.getNamespace();
            String password = new String(port.getPassword().getChars());
            if (!"emp0pwd".equals(password))
                return "Invalid password for port: " + password;
            if (port.getName() == null)
                return "Could not find port name";

            if ("service/SayHelloPojoService".equals(name)) {
                if (!port.getName().equals("SayHelloPojoPort"))
                    return "Invalid port name value: " + port.getName();
            } else if ("service/SayHelloPojoServiceFromBnd".equals(name)) {
                if (!port.getName().equals("SayHelloPojoFromBndPort"))
                    return "Invalid portnamevalue: " + port.getName();
            } else if ("service/SayHelloStatelessService".equals(name)) {
                if (!port.getName().equals("SayHelloStatelessPort"))
                    return "Invalid port name value: " + port.getName();
            } else if ("service/SayHelloSingletonService".equals(name)) {
                if (!port.getName().equals("SayHelloSingletonPort"))
                    return "Invalid port name value: " + port.getName();
            } else {
                return "Found unexpected serviceRef name: " + name;
            }
        }

        List<WebserviceEndpoint> wsEndpoints = wsbnd.getWebserviceEndpoints();
        if (wsEndpoints == null)
            return "Could not find webservice endpoints";
        if (wsEndpoints.size() != 2)
            return "Invalid number of ws endpoints: " + wsEndpoints.size() + "; expected 2";
        for (WebserviceEndpoint wsep : wsEndpoints) {
            String portComponentName = wsep.getPortComponentName();
            String address = wsep.getAddress();
            if (portComponentName == null)
                return "Could not find port component name";
            if (address == null)
                return "Could not find address for wsep";

            if (!portComponentName.equals("Hello") && !portComponentName.equals("HelloFromBnd"))
                return "Invalid port component name: " + portComponentName;
            if (!address.equals("/hi") && !address.equals("/hi2") )
                return "Invalid address: " + address;
        }
        
        Map<String, String> wsEpProps = wsbnd.getWebserviceEndpointProperties();
        if (wsEpProps == null)
            return "Could not find webservice endpoint properties";
        if (wsEpProps.size() != 1)
            return "Invalid number of ws endpoint properties: " + wsEpProps.size() + "; expected 1";
        String value = wsEpProps.get("someAttribute");
        if (value == null)
            return "Could not find value for property someAttribute";
        if (!value.equals("test"))
            return "Invalid value for property someAttribute: " + value;

        return OK;
    }
}
