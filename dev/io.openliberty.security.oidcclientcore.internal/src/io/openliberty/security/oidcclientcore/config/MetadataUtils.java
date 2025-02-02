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
package io.openliberty.security.oidcclientcore.config;

import java.util.function.Function;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.ibm.json.java.JSONObject;
import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;

import io.openliberty.security.oidcclientcore.client.OidcClientConfig;
import io.openliberty.security.oidcclientcore.client.OidcProviderMetadata;
import io.openliberty.security.oidcclientcore.discovery.OidcDiscoveryConstants;
import io.openliberty.security.oidcclientcore.exceptions.OidcClientConfigurationException;
import io.openliberty.security.oidcclientcore.exceptions.OidcDiscoveryException;

@Component(service = MetadataUtils.class, immediate = true, configurationPolicy = ConfigurationPolicy.IGNORE)
public class MetadataUtils {

    public static final TraceComponent tc = Tr.register(MetadataUtils.class);

    private static volatile OidcMetadataService oidcMetadataService;

    @Reference(name = OidcMetadataService.KEY_METADATA_SERVICE, policy = ReferencePolicy.DYNAMIC)
    public void setOidcMetadataService(OidcMetadataService oidcMetadataServiceRef) {
        oidcMetadataService = oidcMetadataServiceRef;
    }

    public void unsetOidcMetadataService(OidcMetadataService oidcMetadataServiceRef) {
        oidcMetadataService = null;
    }

    /**
     * Returns a parameterized value from the configured OidcProviderMetadata, or from the OP's discovery document if the value
     * cannot be found in the OidcProviderMetadata.
     *
     * @throws OidcDiscoveryException Thrown if the value cannot be found in the discovery document or if its value is empty.
     * @throws OidcClientConfigurationException Thrown if the client configuration is missing the providerURI.
     */
    public static <T> T getValueFromProviderOrDiscoveryMetadata(OidcClientConfig oidcClientConfig, Function<OidcProviderMetadata, T> metadataMethodToCall,
                                                                String discoveryMetadataKey) throws OidcDiscoveryException, OidcClientConfigurationException {
        OidcProviderMetadata providerMetadata = oidcClientConfig.getProviderMetadata();
        if (providerMetadata != null) {
            T value = metadataMethodToCall.apply(providerMetadata);
            if (value != null && !value.toString().isEmpty()) {
                if (tc.isDebugEnabled()) {
                    Tr.debug(tc, discoveryMetadataKey + " found in the provider metadata: [" + value + "]");
                }
                return value;
            }
        }
        return getValueFromDiscoveryMetadata(oidcClientConfig, discoveryMetadataKey);
    }

    /**
     * Returns a parameterized value from the OP's discovery document.
     *
     * @throws OidcDiscoveryException Thrown if the value cannot be found in the discovery document or if its value is empty.
     * @throws OidcClientConfigurationException Thrown if the client configuration is missing the providerURI.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValueFromDiscoveryMetadata(OidcClientConfig oidcClientConfig, String key) throws OidcDiscoveryException, OidcClientConfigurationException {
        T value = null;
        JSONObject providerDiscoveryMetadata = oidcMetadataService.getProviderDiscoveryMetadata(oidcClientConfig);
        if (providerDiscoveryMetadata != null) {
            value = (T) providerDiscoveryMetadata.get(key);
        }
        if (value == null || value.toString().isEmpty()) {
            String nlsMessage = Tr.formatMessage(tc, "DISCOVERY_METADATA_MISSING_VALUE", key);
            throw new OidcDiscoveryException(oidcClientConfig.getClientId(), oidcClientConfig.getProviderURI(), nlsMessage);
        }
        return value;
    }

    public static String getAuthorizationEndpoint(OidcClientConfig oidcClientConfig) throws OidcDiscoveryException, OidcClientConfigurationException {
        return getValueFromProviderOrDiscoveryMetadata(oidcClientConfig,
                                                       metadata -> metadata.getAuthorizationEndpoint(),
                                                       OidcDiscoveryConstants.METADATA_KEY_AUTHORIZATION_ENDPOINT);
    }

    public static String getTokenEndpoint(OidcClientConfig oidcClientConfig) throws OidcDiscoveryException, OidcClientConfigurationException {
        return getValueFromProviderOrDiscoveryMetadata(oidcClientConfig,
                                                       metadata -> metadata.getTokenEndpoint(),
                                                       OidcDiscoveryConstants.METADATA_KEY_TOKEN_ENDPOINT);
    }

    public static String getUserInfoEndpoint(OidcClientConfig oidcClientConfig) throws OidcDiscoveryException, OidcClientConfigurationException {
        return getValueFromProviderOrDiscoveryMetadata(oidcClientConfig,
                                                       metadata -> metadata.getUserinfoEndpoint(),
                                                       OidcDiscoveryConstants.METADATA_KEY_USERINFO_ENDPOINT);
    }

    public static String getJwksUri(OidcClientConfig oidcClientConfig) throws OidcDiscoveryException, OidcClientConfigurationException {
        return getValueFromProviderOrDiscoveryMetadata(oidcClientConfig,
                                                       metadata -> metadata.getJwksURI(),
                                                       OidcDiscoveryConstants.METADATA_KEY_JWKS_URI);
    }

    public static String getEndSessionEndpoint(OidcClientConfig oidcClientConfig) throws OidcDiscoveryException, OidcClientConfigurationException {
        return getValueFromProviderOrDiscoveryMetadata(oidcClientConfig,
                                                       metadata -> metadata.getEndSessionEndpoint(),
                                                       OidcDiscoveryConstants.METADATA_KEY_ENDSESSION_ENDPOINT);
    }

    public static String getIssuer(OidcClientConfig oidcClientConfig) throws OidcDiscoveryException, OidcClientConfigurationException {
        return getValueFromProviderOrDiscoveryMetadata(oidcClientConfig,
                                                       metadata -> metadata.getIssuer(),
                                                       OidcDiscoveryConstants.METADATA_KEY_ISSUER);
    }

}
