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

package org.wyona.yanel.servlet.communication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class wraps a HttpServlerRequest and does parameter decoding. Further it handles multipart requests for file upload. 
 */
public class HttpRequest extends HttpServletRequestWrapper {
    
    private static final Logger log = LogManager.getLogger(HttpRequest.class);
    
    public static String form_encoding = "UTF-8";
    public static String container_encoding = "ISO-8859-1";
    
    protected List items;

    private Exception exception;
    private HashMap cachedParameters;

    /**
     *
     */
    public HttpRequest(HttpServletRequest request) throws ServletException {
        this(request, -1);
    }

    /**
     * @param maxFileSize Maximum file size
     */
    public HttpRequest(HttpServletRequest request, long maxFileSize) throws ServletException {
        super(request);
        if (isMultipartRequest()) {
            cacheParameters();

            try {
                DiskFileItemFactory factory = new DiskFileItemFactory();

                // INFO: Set factory constraints
                int maxSize = 64000; // TODO: Make configurable
                factory.setSizeThreshold(maxSize);
                //log.info("Size threshold: " + factory.getSizeThreshold() + " bytes");

                File repoDir = new File(System.getProperty("java.io.tmpdir"));
                factory.setRepository(repoDir);
                //log.info("Repository directory where temporary files will be created in order to handle files beyond the size threshold: " + factory.getRepository());

                // INFO: Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);
                if (maxFileSize >= 0) {
                    upload.setFileSizeMax(maxFileSize);
                    log.warn("DEBUG: Maximum file size: " + upload.getFileSizeMax() + " bytes");
                } else {
                    log.warn("No maximum file size limit set, hence unlimited!");
                }
                 
                // INFO: Parse the request
                // TODO: Make copy of request and when parsing fails, then re-parse being more fault tolerant
                items = upload.parseRequest(request);
                //items = upload.parseRequest(this);
                            
            } catch (Exception e) {
                log.error(e, e);
                if (e.getClass().getName().indexOf("FileSizeLimitExceededException") >= 0) { 
                //if (e instanceof org.apache.commons.fileupload.FileUploadBase$FileSizeLimitExceededException) {
                    setMultipartRequestException(e); // TODO: Set a yanel based exception in order to hide apache commons implementation
                } else {
                    setMultipartRequestException(e);
                }
                //log.warn("Create empty list of items in order to prevent NullPointer exceptions!");
                //items = new java.util.ArrayList(); // INFO: Create empty list, such that no NullPointer is being generated
            }
        } else {
            //log.debug("No multipart request '" + request.getServletPath() + "', hence do nothing.");
        }
    }

    /**
     * Remember exception, such that resources and Yanel itself will be able to check and access it
     * @param exception Exception which has occured while parsing request
     */
    private void setMultipartRequestException(Exception exception) {
        this.exception = exception;
    }

    /**
     * Get exception which might have happened while parsing multipart request
     * @return exception when there was an exception and null when there was no exception
     */
    public Exception getMultipartRequestException() {
        return exception;
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        if(!isMultipartRequest()) {
            String value = super.getParameter(name);
            //log.warn("DEBUG: Value of parameter '" + name + "': " + value);
            return fixEncoding(value);
        }

        if (items != null) {
            Iterator iter = this.items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem)iter.next();
                if (item.getFieldName().equals(name) && item.isFormField()) {
                    return item.getString(); // TODO: fix encoding ?
                }
            }
        } else {
            log.warn("items object is null! But let's try cached parameters...");
            if (cachedParameters != null) {
                String value = (String) cachedParameters.get(name);
                log.warn("DEBUG: Value of parameter '" + name + "': " + value);
                return value;
            } else {
                log.warn("Cached parameters also null!");
            }
        }

        //log.debug("No such parameter: " + name);
        return null;
    }

    /**
     *
     */
    private String fixEncoding(String str) {
        if (form_encoding == null || container_encoding == null || str == null) {
            return str;
        }
        // Form and container encoding are equal, skip expensive value decoding
        if (container_encoding.equals(form_encoding)) {
            return str;
        }
        return decode(str);
    }

    private String decode(String str) {
        if (str == null) return null;
        try {
            if (container_encoding == null)
                container_encoding = "ISO-8859-1";
            byte[] bytes = str.getBytes(container_encoding);
            return new String(bytes, form_encoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Unsupported Encoding Exception", uee);
        }
    }
    
    /**
     * Not really implemented yet.
     * @see javax.servlet.ServletRequestWrapper#getParameterMap()
     */
    public Map getParameterMap() {
        if(!isMultipartRequest()) {
            return super.getParameterMap();
        }
        Map map = new HashMap();
        Iterator iter = this.items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem)iter.next();
            if (item.isFormField()) {
                map.put(item.getFieldName(), item.getString()); // TODO: fix encoding
                // TODO: fix multiple parameters for one name -> create array
            } else {
                // TODO
            }
        }
        return map;        
    }
    
    /**
     * @see javax.servlet.ServletRequestWrapper#getParameterNames()
     */
    public Enumeration getParameterNames() {
        if(!isMultipartRequest()) {
            return super.getParameterNames();
        }
        // use a set to avoid duplicate entries
        HashSet set = new HashSet();

        if (items != null) {
            Iterator iter = this.items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem)iter.next();
                if (item.isFormField()) {
                    // don't add file upload fields
                    set.add(item.getFieldName());
                }
            }
        } else {
            log.warn("items object is null!");
        }
        return new Vector(set).elements();
    }
    
    /**
     * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        if(!isMultipartRequest()) {
            // TODO: fix encoding
            return super.getParameterValues(name);
        }
        ArrayList values = new ArrayList();
        Iterator iter = this.items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem)iter.next();
            if (item.getFieldName().equals(name)) {
                values.add(item.getString()); // TODO: fix encoding ?
            }
        }
        return (String[]) values.toArray(new String[values.size()]);
    }

    /**
     *
     */
    public boolean isMultipartRequest() {
        return ServletFileUpload.isMultipartContent((HttpServletRequest) this.getRequest());
    }

    // methods for multipart requests
    
    /**
     * Gets the field names of all file upload fields.
     * @return enumeration of field names
     */
    public Enumeration getFileNames() {
        if(!isMultipartRequest()) {
            return null;
        }
        Vector parameterNames = new Vector(); 
        Iterator iter = this.items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem)iter.next();
            if (!item.isFormField()) {
                parameterNames.addElement(item.getFieldName());
            }
        }
        return parameterNames.elements();
    }
    
    /**
     * Gets the filename of the uploaded file on the clients computer.
     * @param name Input field name
     * @return filename
     */
    public String getFilesystemName(String name) {
        if(!isMultipartRequest()) {
            return null;
        }
        Iterator iter = this.items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem)iter.next();
            if (item.getFieldName().equals(name)) {
                return fixFileName(item.getName());
            }
        }
        return null;
    }
    
    /**
     * Gets the content-type of the uploaded file.
     * @param name Input field name
     * @return content-type
     */
    public String getContentType(String name) {
        if(!isMultipartRequest()) {
            return null;
        }
        Iterator iter = this.items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem)iter.next();
            if (item.getFieldName().equals(name)) {
                return item.getContentType();
            }
        }
        return null;
    
    }
    
    /**
     * Gets an input stream of the uploaded file
     * @param name
     * @return input stream
     * @throws IOException
     */
    public InputStream getInputStream(String name) throws IOException {
        if(!isMultipartRequest()) {
            return null;
        }
        Iterator iter = this.items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem)iter.next();
            if (item.getFieldName().equals(name)) {
                return item.getInputStream();
            }
        }
        return null;
    }    
    
    protected String fixFileName(String name) {
        // some browsers may send the whole path:
        int i = name.lastIndexOf("\\");
        if (i > -1) {
            name = name.substring(i + 1);
        }
        i = name.lastIndexOf("/");
        if (i > -1) {
            name = name.substring(i + 1);
        }
        name = name.replaceAll(" |&|%|\\?", "_");
        return name;
    }

    /**
     *
     */
    private void cacheParameters() {
        log.warn("DEBUG: Try to cache parameters...");
        cachedParameters = new HashMap();
        Enumeration names = super.getParameterNames();
        if (names.hasMoreElements()) {
            log.warn("DEBUG: Has parameters."); 
        } else {
            log.warn("DEBUG: Has no parameters."); 
        }
        while(names.hasMoreElements()) {
            String name = (String) names.nextElement();
            log.warn("DEBUG: Cache parameter '" + name + "'...");
            cachedParameters.put(name, super.getParameter(name));
        }
    }
}
