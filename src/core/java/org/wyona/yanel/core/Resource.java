/*
 * Copyright 2006 Wyona
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.wyona.org/licenses/APACHE-LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wyona.yanel.core;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.wyona.yanel.core.attributes.translatable.TranslationManager;
import org.wyona.yanel.core.map.Realm;

/**
 *
 */
public abstract class Resource {

    private static Logger log = LogManager.getLogger(Resource.class);

    // TODO: make protected members private (access in subclasses via getter/setter methods)
    protected ResourceTypeDefinition rtd;
    protected ResourceTypeIdentifier rti;
    protected ResourceConfiguration rc;
    protected Yanel yanel;
    private String path;
    protected Realm realm;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    private Environment environment;
    protected Map parameters;

    public static final String DEFAULT_LANGUAGE = "en";

    /**
     *
     */
    public Resource() {
        rtd = null;
        this.parameters = new HashMap();
    }

    /**
     *
     */
    public void setRTD(ResourceTypeDefinition rtd) {
        this.rtd = rtd;
    }

    /**
     * Get resource type definition
     */
    public ResourceTypeDefinition getRTD() {
        return rtd;
    }

    /**
     *
     */
    public void setYanel(Yanel yanel) {
        this.yanel = yanel;
    }

    /**
     *
     */
    public Yanel getYanel() {
        return yanel;
    }

    /**
     * Get universal name of resource, e.g. <{http://www.wyona.org/yanel/resource/1.0}xml/>
     */
    public String getResourceTypeUniversalName() {
        return rtd.getResourceTypeUniversalName();
    }

    /**
     * Get local name of resource, e.g. "xml"
     */
    public String getResourceTypeLocalName() {
        return rtd.getResourceTypeLocalName();
    }

    /**
     * Get namespace of resource, e.g. "http://www.wyona.org/yanel/resource/1.0"
     */
    public String getResourceTypeNamespace() {
        return rtd.getResourceTypeNamespace();
    }

    /**
     * Get path this resource is associated with
     */
    public String getPath() {
        return path;
    }

    /**
     * Set path this resource is associated with
     * @param path Path this resource is associated with
     */
    public void setPath(String path) {
        this.path = path;
    }

    public Realm getRealm() {
        return realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    /**
     * @deprecated
     * @see #getConfiguration
     */
    public ResourceTypeIdentifier getRTI() {
        log.warn("DEPRECATED: see #getConfiguration");
        return rti;
    }

    /**
     * @deprecated
     * @see #setConfiguration
     */
    public void setRTI(ResourceTypeIdentifier rti) {
        log.warn("DEPRECATED: see #setConfiguration");
        this.rti = rti;
    }

    /**
     * Set resource configuration
     */
    public void setConfiguration(ResourceConfiguration rc) {
        this.rc = rc;
    }

    /**
     * Get resource configuration
     */
    public ResourceConfiguration getConfiguration() {
        return rc;
    }

    /**
     * @deprecated use getEnvironment().getRequest() instead  
     */
    public HttpServletRequest getRequest() {
        return environment.getRequest();
    }

    /**
     * @deprecated use getEnvironment().setRequest() instead 
     */
    public void setRequest(HttpServletRequest request) {
        if (environment != null) {
            environment.setRequest(request);
        } else {
            log.error("Environment is null!");
        }
    }

    /**
     * @deprecated use getEnvironment().getResponse() instead  
     */
    public HttpServletResponse getResponse() {
        return environment.getResponse();
    }

    /**
     * @deprecated use getEnvironment().setResponse() instead
     */
    public void setResponse(HttpServletResponse response) {
        if (environment != null) {
            environment.setResponse(response);
        } else {
            log.error("Environment is null!");
        }
    }

    /**
     * Gets the parameter map of this resource.
     * @return map with parameter names as keys and parameter values as values.
     */
    public Map getParameters() {
        return parameters;
    }

    /**
     * Sets the parameter map of this resource.
     * If a parameter map already exists, it will be replaced.
     * @param parameters map with parameter names as keys and parameter values as values.
     */
    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }
    
    /**
     * Get the parameter with the given name.
     * @param name Name of parameter
     * @return parameter object or null if no parameter with this name exists.
     */
    public Object getParameter(String name) {
        return this.parameters.get(name);
    }

    /**
     * Get the parameter value with the given name as a string.
     * If the parameter is a string array, returns the first string.
     * @param name Name of parameter
     * @return parameter string or null if no parameter with this name exists.
     */
    public String getParameterAsString(String name) {
        Object param = this.parameters.get(name);
        if (param == null) {
            return null;
        }
        if (param instanceof String[]) {
            return ((String [])param)[0];
        }
        return param.toString();
    }

    /**
     * Gets the parameter values with the given name as a string array.
     * @param name
     * @return array containing the parameter values, or empty array if parameter does not exist.
     */
    public String[] getParameterAsStringValues(String name) {
        Object param = this.parameters.get(name);
        if (param == null) {
            return new String[0];
        }
        if (param instanceof String) {
            String[] values = new String[1];
            values[0] = (String)param;
            return values;
        } else if (param instanceof String[]) {
            return (String [])param;
        }
        return null;
    }

    /**
     * Sets the parameter with the given name and value.
     * @param name
     * @param value
     */
    public void setParameter(String name, Object value) {
        this.parameters.put(name, value);
    }

    /**
     * Get content language resp. the language of this translation (see getRequestedLanguage re localization)
     */
    public String getContentLanguage() throws Exception {
        String language = null;

        TranslationManager translationManager = getRealm().getTranslationManager();
        if (translationManager != null) {
            language = translationManager.getLanguage(this);
            //log.debug("Use translation manager to determine content language: " + language);
        }
        //if (ResourceAttributeHelper.hasAttributeImplemented(this, "Translatable", "1")) {
        //    language = ((TranslatableV1)this).getLanguage(); 
        //}
        if (language != null) return language;

        language = getResourceConfigProperty("language");
        if (language != null) return language;

        // TODO: Shouldn't we move the check of the realm translation manager into the generic resource? (also see XMLResource and translatable interface)

        language = getRealm().getDefaultLanguage();
        if (language != null) return language;
        
        return DEFAULT_LANGUAGE;

        //return getRequestedLanguage();
    }

    /**
     * Get language (localization).
     */
    public String getRequestedLanguage() throws Exception {
        return getRealm().getLanguageHandler().getLocalizationLanguage(this);
    }

    /**
     * Get property value from resource configuration
     * @param name Name of resource property
     * @return value of resource property and null when property not set
     */
    public String getResourceConfigProperty(String name) throws Exception {
        ResourceConfiguration rc = getConfiguration();

        if (yanel.getTargetEnvironment() != null && rc !=  null) {
            log.debug("Try to get property value of '" + name + "' for target environment: " + yanel.getTargetEnvironment());
            String value = rc.getProperty(name, yanel.getTargetEnvironment());
            if (value != null) {
                return value;
            } else {
                log.debug("There seems to be me no property '" + name + "' with target environment '" + yanel.getTargetEnvironment() + "' set, hence fallback to property without target environment...");
            }
        }

        if (rc != null) return rc.getProperty(name);

        // INFO: For backwards compatibility reasons...
        return getRTI().getProperty(name);
    }
    
    /**
     * Get property value from resource configuration
     */
    public String[] getResourceConfigProperties(String name) throws Exception {
        ResourceConfiguration rc = getConfiguration();

        if (yanel.getTargetEnvironment() != null && rc !=  null) {
            log.debug("Try to get values of multi-valued property '" + name + "' for target environment: " + yanel.getTargetEnvironment());
            String[] values = rc.getProperties(name, yanel.getTargetEnvironment());
            if (values != null) {
                return values;
            } else {
                log.debug("There seems to be me no multi-valued property '" + name + "' with target environment '" + yanel.getTargetEnvironment() + "' set, hence fallback to property without target environment...");
            }
        }

        if (rc != null) return rc.getProperties(name);

        return getRTI().getProperties(name);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
        this.request = environment.getRequest();
        this.response = environment.getResponse();
    }
}
