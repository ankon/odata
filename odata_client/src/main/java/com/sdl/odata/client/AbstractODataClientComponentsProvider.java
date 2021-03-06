/**
 * Copyright (c) 2014 All Rights Reserved by the SDL Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sdl.odata.client;

import com.sdl.odata.client.api.ODataClientComponentsProvider;
import com.sdl.odata.client.caller.BasicEndpointCaller;
import com.sdl.odata.client.api.exception.ODataClientRuntimeException;
import com.sdl.odata.client.api.marshall.ODataEntityMarshaller;
import com.sdl.odata.client.api.marshall.ODataEntityUnmarshaller;
import com.sdl.odata.client.api.caller.EndpointCaller;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.sdl.odata.client.ODataClientConstants.WebService.CLIENT_SERVICE_URI;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Abstract implementation of {@link ODataClientComponentsProvider}.
 */
public abstract class AbstractODataClientComponentsProvider implements ODataClientComponentsProvider {
    private static final Logger LOG = getLogger(AbstractODataClientComponentsProvider.class);

    private URL webServiceUri;
    private EndpointCaller endpointCaller;
    private ODataEntityUnmarshaller unmarshaller;
    private ODataEntityMarshaller marshaller;

    public AbstractODataClientComponentsProvider(Iterable<String> edmEntityClasses, Properties properties,
                                                 String token) {
        webServiceUri = getServiceUri(properties);
        endpointCaller = new BasicEndpointCaller(properties);
        endpointCaller.setAccessToken(token);

        initComponetsProvider(edmEntityClasses);
    }

    protected void setEntityMarshaller(ODataEntityMarshaller entityMarshaller) {
        this.marshaller = entityMarshaller;
    }

    protected void setEntityUnmarshaller(ODataEntityUnmarshaller entityUnmarshaller) {
        this.unmarshaller = entityUnmarshaller;
    }

    @Override
    public EndpointCaller getEndpointCaller() {
        return endpointCaller;
    }

    @Override
    public ODataEntityUnmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    @Override
    public ODataEntityMarshaller getMarshaller() {
        return marshaller;
    }

    @Override
    public URL getWebServiceUrl() {
        return webServiceUri;
    }

    protected abstract void initComponetsProvider(Iterable<String> edmEntityClasses);

    protected URL getServiceUri(Properties properties) {
        String uriString = properties.getProperty(CLIENT_SERVICE_URI);
        if (uriString == null || uriString.trim().isEmpty()) {
            throw new ODataClientRuntimeException(
                    "Error no service URI property value has been defined (with key of '" + CLIENT_SERVICE_URI + "')");
        }
        try {
            URI uri = new URI(uriString);
            return new URL(uri.toString());
        } catch (URISyntaxException | MalformedURLException | RuntimeException e) {
            throw processedServiceUriPropertyException(e, uriString, CLIENT_SERVICE_URI);
        }
    }

    private static ODataClientRuntimeException processedServiceUriPropertyException(
            Throwable e, String value, String key) {
        return processedPropertyException(e, " OData Service URI is invalid,", value, key);
    }

    private static ODataClientRuntimeException processedPropertyException(
            Throwable e, String message, String value, String key) {
        String errorMessage = "Caught '" + e.getClass().getSimpleName() +
                (e.getMessage() != null ? ":" + e.getMessage() : "") +
                "'" + message + " key='" + key + "' and value= '" + value + "'";
        return new ODataClientRuntimeException(errorMessage);
    }

    protected Iterable<Class<?>> getClassesForNames(Iterable<String> classNames) {
        List<Class<?>> edmEntityClasses = new ArrayList<>();
        for (String className : classNames) {
            try {
                edmEntityClasses.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                LOG.debug("Provided class not found", e);
            }
        }
        return edmEntityClasses;
    }
}
