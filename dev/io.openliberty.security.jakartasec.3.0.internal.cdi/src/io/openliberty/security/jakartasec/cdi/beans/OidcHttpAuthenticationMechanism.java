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
package io.openliberty.security.jakartasec.cdi.beans;

import static io.openliberty.security.oidcclientcore.authentication.JakartaOidcAuthorizationRequest.IS_CONTAINER_INITIATED_FLOW;

import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.security.auth.Subject;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.security.javaeesec.JavaEESecConstants;
import com.ibm.ws.security.javaeesec.cdi.beans.Utils;
import com.ibm.ws.security.javaeesec.properties.ModulePropertiesProvider;
import com.ibm.ws.webcontainer.security.AuthResult;
import com.ibm.ws.webcontainer.security.ProviderAuthenticationResult;
import com.ibm.wsspi.security.token.AttributeNameConstants;

import io.openliberty.security.jakartasec.JakartaSec30Constants;
import io.openliberty.security.jakartasec.OpenIdAuthenticationMechanismDefinitionHolder;
import io.openliberty.security.jakartasec.OpenIdAuthenticationMechanismDefinitionWrapper;
import io.openliberty.security.jakartasec.credential.OidcTokensCredential;
import io.openliberty.security.jakartasec.identitystore.OpenIdContextUtils;
import io.openliberty.security.oidcclientcore.authentication.AuthorizationRequestUtils;
import io.openliberty.security.oidcclientcore.authentication.OriginalResourceRequest;
import io.openliberty.security.oidcclientcore.client.Client;
import io.openliberty.security.oidcclientcore.client.ClientManager;
import io.openliberty.security.oidcclientcore.client.OidcClientConfig;
import io.openliberty.security.oidcclientcore.exceptions.AuthenticationResponseException;
import io.openliberty.security.oidcclientcore.exceptions.TokenRequestException;
import io.openliberty.security.oidcclientcore.storage.OidcStorageUtils;
import io.openliberty.security.oidcclientcore.storage.Storage;
import io.openliberty.security.oidcclientcore.storage.StorageFactory;
import io.openliberty.security.oidcclientcore.token.JakartaOidcTokenRequest;
import io.openliberty.security.oidcclientcore.token.TokenResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.security.enterprise.identitystore.openid.AccessToken;
import jakarta.security.enterprise.identitystore.openid.IdentityToken;
import jakarta.security.enterprise.identitystore.openid.OpenIdContext;
import jakarta.security.enterprise.identitystore.openid.RefreshToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Default
@ApplicationScoped
public class OidcHttpAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final TraceComponent tc = Tr.register(OidcHttpAuthenticationMechanism.class);

    @Inject
    IdentityStoreHandler identityStoreHandler;

    private ModulePropertiesProvider mpp = null;
    private final Utils utils;
    private final AuthorizationRequestUtils requestUtils;

    public OidcHttpAuthenticationMechanism() {
        mpp = getModulePropertiesProvider();
        utils = getUtils();
        requestUtils = getRequestUtils();
    }

    private OpenIdAuthenticationMechanismDefinitionWrapper getOpenIdAuthenticationMechanismDefinition(HttpServletRequest request) {
        Properties props = mpp.getAuthMechProperties(OidcHttpAuthenticationMechanism.class);
        /*
         * Build the baseURL from the incoming HttpRequest as the redirectURL may contain baseURL variable, such as ${baseURL}/Callback
         */
        String baseURL = requestUtils.getBaseURL(request);
        OpenIdAuthenticationMechanismDefinitionHolder openIdAuthenticationMechanismDefinitionHolder = (OpenIdAuthenticationMechanismDefinitionHolder) props.get(JakartaSec30Constants.OIDC_ANNOTATION);
        return new OpenIdAuthenticationMechanismDefinitionWrapper(openIdAuthenticationMechanismDefinitionHolder.getOpenIdAuthenticationMechanismDefinition(), baseURL);
    }

    @SuppressWarnings("unchecked")
    protected ModulePropertiesProvider getModulePropertiesProvider() {
        Instance<ModulePropertiesProvider> modulePropertiesProviderInstance = getCDI().select(ModulePropertiesProvider.class);
        if (modulePropertiesProviderInstance != null) {
            return modulePropertiesProviderInstance.get();
        }
        return null;
    }

    // Protected to allow unit testing
    protected Utils getUtils() {
        return new Utils();
    }

    protected AuthorizationRequestUtils getRequestUtils() {
        return new AuthorizationRequestUtils();
    }

    @SuppressWarnings("rawtypes")
    @FFDCIgnore(IllegalStateException.class)
    protected CDI getCDI() {
        try {
            return CDI.current();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request,
                                                HttpServletResponse response,
                                                HttpMessageContext httpMessageContext) throws AuthenticationException {
        AuthenticationStatus status = AuthenticationStatus.SEND_FAILURE;

        Client client = getClient(request);

        boolean alreadyAuthenticated = isAlreadyAuthenticated(request);

        if (isAuthenticationRequired(httpMessageContext, alreadyAuthenticated) && !containsStoredState(request, response, client)) {
            status = processStartFlow(client, httpMessageContext);
        } else if (isCallbackRequest(request)) {
            status = processCallback(client, httpMessageContext);
        } else if (alreadyAuthenticated) {
            status = processExpiredTokenResult(processExpiredToken(client, request, response), client, httpMessageContext);
        }

        return status;
    }

    // Protected to allow unit testing
    protected Client getClient(HttpServletRequest request) {
        return ClientManager.getClientFor(getOpenIdAuthenticationMechanismDefinition(request));
    }

    /**
     * Authentication is required when "when the caller tries to access a protected resource without being authenticated,
     * or when the caller explicitly initiates authentication, without being authenticated for the current request"
     *
     * @param alreadyAuthenticated
     */
    private boolean isAuthenticationRequired(HttpMessageContext httpMessageContext, boolean alreadyAuthenticated) {
        return isProtectedResourceWithoutBeingAuthenticated(httpMessageContext, alreadyAuthenticated)
               || isProgrammaticAuthenticationWithoutBeingAuthenticated(httpMessageContext, alreadyAuthenticated);
    }

    private boolean isProtectedResourceWithoutBeingAuthenticated(HttpMessageContext httpMessageContext, boolean alreadyAuthenticated) {
        return httpMessageContext.isProtected() && !alreadyAuthenticated;
    }

    private boolean isProgrammaticAuthenticationWithoutBeingAuthenticated(HttpMessageContext httpMessageContext, boolean alreadyAuthenticated) {
        return (isNewAuthentication(httpMessageContext.getAuthParameters())) && !alreadyAuthenticated;
    }

    private boolean isNewAuthentication(AuthenticationParameters authParameters) {
        if (authParameters != null) {
            return authParameters.isNewAuthentication();
        }
        return false;
    }

    private boolean containsStoredState(HttpServletRequest request, HttpServletResponse response, Client client) {
        String state = request.getParameter(OpenIdConstant.STATE);
        if (state == null) {
            return false;
        }
        OidcClientConfig clientConfig = client.getOidcClientConfig();
        Storage storage = StorageFactory.instantiateStorage(request, response, clientConfig.isUseSession());
        return storage.get(OidcStorageUtils.getStateStorageKey(state)) != null;
    }

    private boolean isAlreadyAuthenticated(HttpServletRequest request) {
        return request.getUserPrincipal() != null;
    }

    private AuthenticationStatus processStartFlow(Client client, HttpMessageContext httpMessageContext) {
        HttpServletRequest request = httpMessageContext.getRequest();
        request.setAttribute(IS_CONTAINER_INITIATED_FLOW, isContainerInitiatedFlow(httpMessageContext.getAuthParameters()));
        ProviderAuthenticationResult providerAuthenticationResult = client.startFlow(request, httpMessageContext.getResponse());
        return processStartFlowResult(providerAuthenticationResult, httpMessageContext);
    }

    private AuthenticationStatus processStartFlowResult(ProviderAuthenticationResult providerAuthenticationResult, HttpMessageContext httpMessageContext) {
        AuthenticationStatus status = AuthenticationStatus.SEND_FAILURE;

        if (providerAuthenticationResult != null) {
            AuthResult authResult = providerAuthenticationResult.getStatus();

            if (AuthResult.REDIRECT_TO_PROVIDER.equals(authResult)) {
                status = httpMessageContext.redirect(providerAuthenticationResult.getRedirectUrl());
            } else if (AuthResult.SEND_401.equals(authResult)) {
                status = httpMessageContext.responseUnauthorized();
            }
        }

        return status;
    }

    /**
     * Section https://jakarta.ee/specifications/security/3.0/jakarta-security-spec-3.0.html#authentication-dialog states that
     * a callback is detected "when a request contains a state request parameter."
     */
    private boolean isCallbackRequest(HttpServletRequest request) {
        return request.getParameter(OpenIdConstant.STATE) != null;
    }

    /**
     * Use SEND_CONTINUE rather than SEND_FAILURE for failures since JaspiServiceImpl converts a SEND_FAILURE to AuthenticationResult(AuthResult.RETURN, detail)
     * and the WebContainerSecurityCollaboratorImpl allows access to unprotected resources for AuthResult.RETURN. SEND_CONTINUE will prevent this by properly
     * returning a 401 and not continue to the redirectUri.
     */
    private AuthenticationStatus processCallback(Client client, HttpMessageContext httpMessageContext) throws AuthenticationException {
        HttpServletRequest request = httpMessageContext.getRequest();
        HttpServletResponse response = httpMessageContext.getResponse();

        OidcClientConfig clientConfig = client.getOidcClientConfig();

        Optional<String> originalRequestUrl = getOriginalRequestUrlForRedirect(request, response, clientConfig);
        if (originalRequestUrl.isPresent()) {
            return httpMessageContext.redirect(originalRequestUrl.get());
        }

        AuthenticationStatus status = AuthenticationStatus.SEND_CONTINUE;
        Optional<HttpServletRequest> originalResourceRequest = getOriginalResourceRequest(clientConfig, httpMessageContext);
        try {
            ProviderAuthenticationResult providerAuthenticationResult = client.continueFlow(request, response);
            status = processContinueFlowResult(providerAuthenticationResult, httpMessageContext, client);
        } catch (AuthenticationResponseException | TokenRequestException e) {
            Tr.error(tc, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        if (status == AuthenticationStatus.SUCCESS && originalResourceRequest.isPresent()) {
            httpMessageContext.setRequest(originalResourceRequest.get());
            httpMessageContext.getMessageInfo().setRequestMessage(originalResourceRequest.get());
        }
        return status;
    }

    private Optional<String> getOriginalRequestUrlForRedirect(HttpServletRequest request, HttpServletResponse response, OidcClientConfig clientConfig) {
        if (clientConfig.isRedirectToOriginalResource()) {
            String currentRequestUrl = request.getRequestURL().toString();
            String originalRequestUrl = getOriginalRequestUrl(request, response, clientConfig.isUseSession());
            if (originalRequestUrl != null && !originalRequestUrl.equals(currentRequestUrl)) {
                originalRequestUrl = appendCodeAndStateParams(originalRequestUrl, request);
                return Optional.of(originalRequestUrl);
            }
        }
        return Optional.empty();
    }

    private String getOriginalRequestUrl(HttpServletRequest request, HttpServletResponse response, boolean useSession) {
        String state = request.getParameter(OpenIdConstant.STATE);
        if (state == null) {
            return null;
        }
        Storage storage = StorageFactory.instantiateStorage(request, response, useSession);
        String originalRequestUrl = storage.get(OidcStorageUtils.getOriginalReqUrlStorageKey(state));
        if (originalRequestUrl == null) {
            return null;
        }
        String originalRequestUrlWithoutQueryParams = originalRequestUrl.split(Pattern.quote("?"))[0];
        return originalRequestUrlWithoutQueryParams;
    }

    private String appendCodeAndStateParams(String originalRequestUrl, HttpServletRequest request) {
        originalRequestUrl += "?code=" + request.getParameter(OpenIdConstant.CODE);
        originalRequestUrl += "&state=" + request.getParameter(OpenIdConstant.STATE);
        return originalRequestUrl;
    }

    private Optional<HttpServletRequest> getOriginalResourceRequest(OidcClientConfig clientConfig, HttpMessageContext context) {
        AuthenticationParameters authParams = context.getAuthParameters();
        HttpServletRequest originalResourceRequest = null;
        if (shouldRestoreOriginalRequest(clientConfig, authParams)) {
            HttpServletRequest request = context.getRequest();
            HttpServletResponse response = context.getResponse();
            originalResourceRequest = getOriginalResourceRequest(request, response, clientConfig.isUseSession());
        }
        return Optional.ofNullable(originalResourceRequest);
    }

    private boolean shouldRestoreOriginalRequest(OidcClientConfig clientConfig, AuthenticationParameters authParams) {
        return clientConfig.isRedirectToOriginalResource() && isContainerInitiatedFlow(authParams);
    }

    private boolean isContainerInitiatedFlow(AuthenticationParameters authParams) {
        return (authParams == null || !authParams.isNewAuthentication());
    }

    protected OriginalResourceRequest getOriginalResourceRequest(HttpServletRequest request, HttpServletResponse response, boolean useSession) {
        return new OriginalResourceRequest(request, response, useSession);
    }

    private AuthenticationStatus processContinueFlowResult(ProviderAuthenticationResult providerAuthenticationResult, HttpMessageContext httpMessageContext,
                                                           Client client) throws AuthenticationException {
        AuthenticationStatus status = AuthenticationStatus.SEND_FAILURE;

        if (providerAuthenticationResult != null) {
            AuthResult authResult = providerAuthenticationResult.getStatus();

            if (AuthResult.SUCCESS.equals(authResult)) {
                status = handleOidcLogin(providerAuthenticationResult, httpMessageContext, client);
            }
        }

        return status;
    }

    private AuthenticationStatus handleOidcLogin(ProviderAuthenticationResult providerAuthenticationResult, HttpMessageContext httpMessageContext,
                                                 Client client) throws AuthenticationException {
        return createAndValidateOidcTokensCredential(providerAuthenticationResult, client, httpMessageContext);
    }

    AuthenticationStatus createAndValidateOidcTokensCredential(ProviderAuthenticationResult providerAuthenticationResult, Client client,
                                                               HttpMessageContext httpMessageContext) throws AuthenticationException {
        HttpServletRequest request = httpMessageContext.getRequest();
        HttpServletResponse response = httpMessageContext.getResponse();
        OidcTokensCredential credential = createOidcTokensCredential(providerAuthenticationResult, request, response, client);
        return validateCredentials(credential, httpMessageContext);
    }

    private OidcTokensCredential createOidcTokensCredential(ProviderAuthenticationResult providerAuthenticationResult, HttpServletRequest request, HttpServletResponse response,
                                                            Client client) {
        OidcTokensCredential credential = null;

        Hashtable<String, Object> customProperties = providerAuthenticationResult.getCustomProperties();
        if (customProperties != null) {
            TokenResponse tokenResponse = (TokenResponse) customProperties.get(JakartaOidcTokenRequest.AUTH_RESULT_CUSTOM_PROP_TOKEN_RESPONSE);
            if (tokenResponse != null) {
                credential = new OidcTokensCredential(tokenResponse, client, request, response);
            }
        }

        return credential;
    }

    private AuthenticationStatus validateCredentials(OidcTokensCredential credential, HttpMessageContext httpMessageContext) throws AuthenticationException {
        int rspStatus;
        String issuer = JavaEESecConstants.DEFAULT_REALM; // TODO: Set to "iss" claim from the identity token.
        Subject clientSubject = httpMessageContext.getClientSubject();
        AuthenticationStatus status = utils.handleAuthenticate(getCDI(), issuer, credential, clientSubject, httpMessageContext);

        if (status == AuthenticationStatus.SUCCESS) {
            setOpenIdContextInSubject(clientSubject, credential.getOpenIdContext());

            Map<String, Object> messageInfoMap = httpMessageContext.getMessageInfo().getMap();
            messageInfoMap.put("jakarta.servlet.http.authType", "JAKARTA_OIDC");
            messageInfoMap.put("jakarta.servlet.http.registerSession", Boolean.TRUE.toString());

            rspStatus = HttpServletResponse.SC_OK;
        } else if (status == AuthenticationStatus.NOT_DONE) {
            // set SC_OK, since if the target is not protected, it'll be processed.
            rspStatus = HttpServletResponse.SC_OK;
        } else {
            rspStatus = HttpServletResponse.SC_UNAUTHORIZED;
            status = AuthenticationStatus.SEND_CONTINUE;
        }

        httpMessageContext.getResponse().setStatus(rspStatus);

        return status;
    }

    private void setOpenIdContextInSubject(Subject clientSubject, OpenIdContext openIdContext) {
        if (openIdContext != null) {
            clientSubject.getPrivateCredentials().add(openIdContext);
            Hashtable<String, Object> hashtable = utils.getSubjectExistingHashtable(clientSubject);
            if (hashtable != null) {
                String newCacheKey = getNewCustomCacheKeyValue(openIdContext);
                if (newCacheKey != null) {
                    hashtable.put(AttributeNameConstants.WSCREDENTIAL_CACHE_KEY, newCacheKey);
                }
            }
        }
    }

    private String getNewCustomCacheKeyValue(OpenIdContext openIdContext) {
        IdentityToken idToken = openIdContext.getIdentityToken();
        if (idToken != null) {
            return String.valueOf(idToken.hashCode());
        }
        AccessToken accessToken = openIdContext.getAccessToken();
        if (accessToken != null) {
            return String.valueOf(accessToken.hashCode());
        }
        return null;
    }

    private AuthenticationStatus processExpiredTokenResult(ProviderAuthenticationResult providerAuthenticationResult, Client client,
                                                           HttpMessageContext httpMessageContext) throws AuthenticationException {
        AuthenticationStatus status = AuthenticationStatus.SEND_FAILURE;

        if (providerAuthenticationResult != null) {
            AuthResult authResult = providerAuthenticationResult.getStatus();

            if (AuthResult.REDIRECT_TO_PROVIDER.equals(authResult)) {
                status = httpMessageContext.redirect(providerAuthenticationResult.getRedirectUrl());
            } else if (AuthResult.SUCCESS.equals(authResult)) {
                status = updateOpenIdContextWithRefreshedTokens(providerAuthenticationResult, client, httpMessageContext);
            }
        }

        return status;
    }

    private AuthenticationStatus updateOpenIdContextWithRefreshedTokens(ProviderAuthenticationResult providerAuthenticationResult, Client client,
                                                                        HttpMessageContext httpMessageContext) throws AuthenticationException {
        return createAndValidateOidcTokensCredential(providerAuthenticationResult, client, httpMessageContext);
    }

    private ProviderAuthenticationResult processExpiredToken(Client client, HttpServletRequest request, HttpServletResponse response) {
        OpenIdContext openIdContext = OpenIdContextUtils.getOpenIdContextFromSubject();

        if (openIdContext == null) {
            // TODO add debug. should not be here.
            return new ProviderAuthenticationResult(AuthResult.FAILURE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        IdentityToken idToken = openIdContext.getIdentityToken();
        boolean isAccessTokenExpired = openIdContext.getAccessToken().isExpired();
        boolean isIdTokenExpired = false;
        String idTokenString = null;
        if (idToken != null) {
            isIdTokenExpired = idToken.isExpired();
            idTokenString = idToken.getToken();
        }
        String refreshTokenString = getRefreshToken(openIdContext);
        return client.processExpiredToken(request, response, isAccessTokenExpired, isIdTokenExpired, idTokenString, refreshTokenString);
    }

    private String getRefreshToken(OpenIdContext openIdContext) {
        Optional<RefreshToken> optionalRefreshToken = openIdContext.getRefreshToken();
        if (optionalRefreshToken.isPresent()) {
            RefreshToken refreshToken = optionalRefreshToken.get();
            if (refreshToken != null) {
                return refreshToken.getToken();
            }
        }
        return null;
    }

    @Override
    public void cleanSubject(HttpServletRequest request,
                             HttpServletResponse response,
                             HttpMessageContext httpMessageContext) {

    }

}
