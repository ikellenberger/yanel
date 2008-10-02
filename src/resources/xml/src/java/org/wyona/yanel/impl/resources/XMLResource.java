/*
 * Copyright 2007 Wyona
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

package org.wyona.yanel.impl.resources;

import org.wyona.yanel.core.Path;
import org.wyona.yanel.core.Resource;
import org.wyona.yanel.core.api.attributes.CreatableV2;
import org.wyona.yanel.core.api.attributes.IntrospectableV1;
import org.wyona.yanel.core.api.attributes.ModifiableV2;
import org.wyona.yanel.core.api.attributes.TranslatableV1;
import org.wyona.yanel.core.api.attributes.VersionableV2;
import org.wyona.yanel.core.api.attributes.ViewableV1;
import org.wyona.yanel.core.api.attributes.ViewableV2;
import org.wyona.yanel.core.api.attributes.WorkflowableV1;
import org.wyona.yanel.core.attributes.translatable.TranslationException;
import org.wyona.yanel.core.attributes.translatable.TranslationManager;
import org.wyona.yanel.core.attributes.versionable.RevisionInformation;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;

import org.wyona.yanel.core.util.PathUtil;
import org.wyona.yanel.core.util.ResourceAttributeHelper;
import org.wyona.yanel.core.workflow.Workflow;
import org.wyona.yanel.core.workflow.WorkflowException;
import org.wyona.yanel.core.workflow.WorkflowHelper;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.Revision;

import javax.servlet.http.HttpServletRequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import org.apache.log4j.Category;

/**
 *
 */
public class XMLResource extends BasicXMLResource implements ModifiableV2, VersionableV2, CreatableV2, IntrospectableV1, TranslatableV1, WorkflowableV1 {

    private static Category log = Category.getInstance(XMLResource.class);


    public View getView(String viewId) throws Exception {
        return getView(viewId, null);
    }

    /**
     * Generates view
     */
    public View getView(String viewId, String revisionName) throws Exception {
        Repository repo = getRealm().getRepository();
        String yanelPath = getResourceConfigProperty("yanel-path");
        InputStream xmlInputStream = getContentXML(repo, yanelPath, revisionName);
        return getXMLView(viewId, xmlInputStream);
    }

    /**
     * Get initial content as XML
     */
    private InputStream getContentXML(Repository repo, String yanelPath, String revisionName) throws Exception {
        if (yanelPath != null) {
            if (log.isDebugEnabled()) log.debug("Yanel Path: " + yanelPath);
            Resource res = yanel.getResourceManager().getResource(getEnvironment(), getRealm(), yanelPath);
            if (ResourceAttributeHelper.hasAttributeImplemented(res, "Viewable", "1")) {
                // TODO: Pass the request ...
                String viewV1path = getRealm().getMountPoint() + yanelPath.substring(1);
                log.warn("Path of view: " + viewV1path);
                View view = ((ViewableV1) res).getView(new Path(viewV1path), null);
                if (view.getMimeType().indexOf("xml") >= 0) {
                    // TODO: Shall the mime-type be transfered?
                    return view.getInputStream();
                }
                log.error("No XML like mime-type: " + getPath() + " (yanel-path: " + yanelPath + ")");
            } else if (ResourceAttributeHelper.hasAttributeImplemented(res, "Viewable", "2")) {
                // TODO: Pass the request ...
                View view = ((ViewableV2) res).getView(null);
                if (view.getMimeType().indexOf("xml") >= 0) {
                    // TODO: Shall the mime-type be transfered?
                    return view.getInputStream();
                }
                log.error("No XML like mime-type: " + getPath() + " (yanel-path: " + yanelPath + ")");
            } else {
                log.error("Resource is not ViewableV1: " + getPath() + " (yanel-path: " + yanelPath + ")");
            }
            return null;
        }

        Node node;
        if (revisionName != null) {
            node = repo.getNode(getPath()).getRevision(revisionName);
        } else {
            node = repo.getNode(getPath());
        }
        return node.getInputStream();
    }

    /**
     * Get mime type
     */
    public String getMimeType(String viewId) throws Exception {
        ViewDescriptor viewDescriptor = getViewDescriptor(viewId);
        String mimeType;
        if (viewDescriptor != null) {
            mimeType = viewDescriptor.getMimeType();
            if (mimeType != null) return mimeType;
        }

        if (viewId != null && viewId.equals(SOURCE_VIEW_ID)) {
            mimeType = getResourceConfigProperty("source-view-mime-type");
            if (mimeType != null) return mimeType;
        }

        mimeType = getResourceConfigProperty("mime-type");
        if (mimeType != null) return mimeType;

        String suffix = PathUtil.getSuffix(getPath());
        if (suffix != null) {
            log.debug("SUFFIX: " + suffix);
            if (suffix.equals("html")) {
                //mimeType = "text/html";
                mimeType = "application/xhtml+xml";
            } else if (suffix.equals("xhtml")) {
                mimeType = "application/xhtml+xml";
            } else if (suffix.equals("xml")) {
                mimeType = "application/xml";
            } else {
                mimeType = "application/xml";
            }
        } else {
            mimeType = "application/xml";
        }
        return mimeType;
    }

    /**
     *
     */
    public Reader getReader() throws Exception {
        return new InputStreamReader(getInputStream(), "UTF-8");
    }

    /**
     *
     */
    public InputStream getInputStream() throws Exception {
        return getRealm().getRepository().getNode(getPath()).getInputStream();
    }

    /**
     *
     */
    public Writer getWriter() throws Exception {
        log.error("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public OutputStream getOutputStream() throws Exception {
        return getRealm().getRepository().getNode(getPath()).getOutputStream();
    }

    /**
     *
     */
    public void write(InputStream in) throws Exception {
        log.warn("Not implemented yet!");
    }

    /**
     *
     */
    public long getLastModified() throws Exception {
        long lastModified;
        String yanelPath = getResourceConfigProperty("yanel-path");
        if (yanelPath != null) {
            log.warn("Get last modified for parameter yanel-path not implemented yet!");
            lastModified = -1;
        } else {
            Node node = getRealm().getRepository().getNode(getPath());
            if (node.isResource()) {
                lastModified = node.getLastModified();
            } else {
                lastModified = 0;
            }
        }

        return lastModified;
    }

    /**
     *
     */
    public Node getRepoNode() throws Exception {
        // TODO: yanel-path is not a repo path actually, but rather another resource! See getContentXML()
        String path = getResourceConfigProperty("yanel-path");
        if (path == null) {
            path = getPath();
        }
        return getRealm().getRepository().getNode(path);
    }

    /**
     * Delete node
     */
    public boolean delete() throws Exception {
        getRealm().getRepository().getNode(getPath()).delete();
        return true;
    }

    /**
     * @see org.wyona.yanel.core.api.attributes.VersionableV2#getRevisions()
     */
    public RevisionInformation[] getRevisions() throws Exception {
        Revision[] revisions = getRepoNode().getRevisions();

        // TODO: Use utility method/class
        RevisionInformation[] revisionInfos = new RevisionInformation[revisions.length];
        for (int i = 0; i < revisions.length; i++) {
            revisionInfos[i] = new RevisionInformation(revisions[i]);
        }
        return revisionInfos;
    }

    public void checkin(String comment) throws Exception {
        Node node = getRealm().getRepository().getNode(getPath());
        Revision revision = node.checkin(comment);
        // set initial workflow state and date:
        Workflow workflow = WorkflowHelper.getWorkflow(this);
        if (workflow != null) {
            setWorkflowState(workflow.getInitialState(), revision.getRevisionName());
        }
        /*
        if (node.isCheckedOut()) {
            String checkoutUserID = node.getCheckoutUserID();
            if (checkoutUserID.equals(userID)) {
                node.checkin();
            } else {
                throw new Exception("Resource is checked out by another user: " + checkoutUserID);
            }
        } else {
            throw new Exception("Resource is not checked out.");
        }
        */
    }

    public void checkout(String userID) throws Exception {
        Node node = getRealm().getRepository().getNode(getPath());
        node.checkout(userID);
        /*
        if (node.isCheckedOut()) {
            String checkoutUserID = node.getCheckoutUserID();
            if (checkoutUserID.equals(userID)) {
                log.warn("Resource " + getPath() + " is already checked out by this user: " + checkoutUserID);
            } else {
                throw new Exception("Resource is already checked out by another user: " + checkoutUserID);
            }
        } else {
            node.checkout(userID);
        }
        */
    }

    public void cancelCheckout() throws Exception {
        Node node = getRealm().getRepository().getNode(getPath());
        node.cancelCheckout();
    }

    public void restore(String revisionName) throws Exception {
        getRealm().getRepository().getNode(getPath()).restore(revisionName);
    }

    public Date getCheckoutDate() throws Exception {
        Node node = getRealm().getRepository().getNode(getPath());
        // return node.getCheckoutDate();
        return null;
    }

    public String getCheckoutUserID() throws Exception {
        Node node = getRealm().getRepository().getNode(getPath());
        return node.getCheckoutUserID();
    }

    public boolean isCheckedOut() throws Exception {
        Node node = getRealm().getRepository().getNode(getPath());
        return node.isCheckedOut();
    }

    /**
     * Get size of generated page
     */
    public long getSize() throws Exception {
        // TODO: If the XML is being transformed then the size will not be the same as the size of the node!
/*
        Node node = getRealm().getRepository().getNode(getPath());
        long size;
        if (node.isResource()) {
            size = node.getSize();
        } else {
            size = 0;
        }
        return size;
*/
        return -1;
    }


    /**
     *
     */
    public void create(HttpServletRequest request) {
        try {
            // TODO: XHTML template should not be hardcoded!
            Repository repo = getRealm().getRepository();

            String title = request.getParameter("rp.title");
            if (title == null || title.length() == 0) {
                log.warn("No title has been specified!");
                title = "No title has been specified!";
            }

            Node newNode = org.wyona.yanel.core.util.YarepUtil.addNodes(repo, getPath().toString(), org.wyona.yarep.core.NodeType.RESOURCE);
            Writer writer = new java.io.OutputStreamWriter(newNode.getOutputStream());
            writer.write("<?xml version=\"1.0\"?>");
            writer.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
            writer.write("<head>");
            writer.write("  <title>" + title + "</title>");
            writer.write("  <link rel=\"neutron-introspection\" type=\"application/neutron+xml\" href=\"?yanel.resource.usecase=introspection\"/>");
            writer.write("</head>");
            writer.write("<body>");
            writer.write("  <h1>" + title + "</h1>");
            writer.write("  <p>Edit this text with <a href=\"http://www.yulup.org\">Yulup</a>!</p>");
            writer.write("</body>");
            writer.write("</html>");
            writer.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     *
     */
    public java.util.HashMap createRTIProperties(HttpServletRequest request) {
        java.util.HashMap map = new java.util.HashMap();
        map.put("xslt", request.getParameter("rp.xslt"));
        map.put("mime-type", request.getParameter("rp.mime-type"));
        map.put("source-view-mime-type", request.getParameter("rp.source-view-mime-type"));
        map.put("workflow-schema", request.getParameter("rp.workflow-schema"));
        map.put("yanel-path", request.getParameter("rp.yanel-path"));

        // TODO: get all parameters, e.g. source-view-mime-type (Security!)
        return map;
    }

    public String getCreateName(String suggestedName) {
        return suggestedName;
    }

    /**
     *
     */
    public String getPropertyType(String name) {
        log.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public Object getProperty(String name) {
        log.warn("Not implemented yet!");
        return null;
    }

    /**
     *
     */
    public String[] getPropertyNames() {
        String[] propertyNames = new String[1];
        propertyNames[0] = "title";
        return propertyNames;
    }

    /**
     *
     */
    public void setProperty(String name, Object value) {
        log.warn("Not implemented yet!");
    }

    /**
     * Get introspection for Introspectable interface
     */
    public String getIntrospection() throws Exception {
        String name = PathUtil.getName(getPath());
        StringBuffer sb = new StringBuffer("<?xml version=\"1.0\"?>");
        sb.append("<introspection xmlns=\"http://www.wyona.org/neutron/2.0\">");

        sb.append("<navigation>");
        sb.append("  <sitetree href=\"./\" method=\"PROPFIND\"/>");
        sb.append("</navigation>");

        sb.append("<resource name=\"" + name + "\">");
        sb.append("<edit mime-type=\"" + this.getMimeType(SOURCE_VIEW_ID) + "\">");
        sb.append("<checkout url=\"?yanel.resource.viewid=" + SOURCE_VIEW_ID + "&amp;yanel.resource.usecase=checkout\" method=\"GET\"/>");
        sb.append("<checkin  url=\"?yanel.resource.usecase=checkin\"  method=\"PUT\"/>");
        sb.append("<release-lock url=\"?yanel.resource.usecase=release-lock\" method=\"GET\"/>");
        sb.append("</edit>");

        sb.append(getWorkflowIntrospection());

        sb.append("</resource>");
        sb.append("</introspection>");
        return sb.toString();
    }

    protected TranslationManager getTranslationManager() throws TranslationException {
        return getRealm().getTranslationManager();
    }

    public String getLanguage() throws TranslationException {
        return getTranslationManager().getLanguage(this);
    }

    public String[] getLanguages() throws TranslationException {
        return getTranslationManager().getLanguages(this);
    }

    public Resource getTranslation(String language) throws TranslationException {
        return getTranslationManager().getTranslation(this, language);
    }

    public void addTranslation(Resource resource, String language) throws TranslationException {
        getTranslationManager().addTranslation(this, resource, language);
    }

    public boolean hasTranslation(String language) throws TranslationException {
        return getTranslationManager().hasTranslation(this, language);
    }

    public void removeTranslation(String language) throws TranslationException {
        getTranslationManager().removeTranslation(this, language);
    }

    /************************************************
     * Workflow                                     *
     ************************************************/

    public void doTransition(String transitionID, String revision) throws WorkflowException {
        WorkflowHelper.doTransition(this, transitionID, revision);
    }

    public View getLiveView(String viewid) throws Exception {
        return WorkflowHelper.getLiveView(this, viewid);
    }

    public boolean isLive() throws WorkflowException {
        return WorkflowHelper.isLive(this);
    }

    public String getWorkflowVariable(String name) throws WorkflowException {
        try {
        return WorkflowHelper.getWorkflowVariable(getRepoNode(), name);
        } catch (Exception e) {
            log.error(e, e);
            throw new WorkflowException(e.getMessage(), e);
        }
    }

    public void setWorkflowVariable(String name, String value) throws WorkflowException {
        try {
            WorkflowHelper.setWorkflowVariable(getRepoNode(), name, value);
        } catch (Exception e) {
            log.error(e, e);
            throw new WorkflowException(e.getMessage(), e);
        }
    }

    public void removeWorkflowVariable(String name) throws WorkflowException {
        try {
            WorkflowHelper.removeWorkflowVariable(getRepoNode(), name);
        } catch (Exception e) {
            log.error(e, e);
            throw new WorkflowException(e.getMessage(), e);
        }
    }

    public String getWorkflowState(String revision) throws WorkflowException {
        try {
            return WorkflowHelper.getWorkflowState(getRepoNode(), revision);
        } catch (Exception e) {
            log.error(e, e);
            throw new WorkflowException(e.getMessage(), e);
        }
    }

    public void setWorkflowState(String state, String revision) throws WorkflowException {
        try {
            WorkflowHelper.setWorkflowState(getRepoNode(), state, revision);
        } catch (Exception e) {
            log.error(e, e);
            throw new WorkflowException(e.getMessage(), e);
        }
    }

    public Date getWorkflowDate(String revision) throws WorkflowException {
        try {
            return WorkflowHelper.getWorkflowDate(getRepoNode(), revision);
        } catch (Exception e) {
            log.error(e, e);
            throw new WorkflowException(e.getMessage(), e);
        }
    }

    public String getWorkflowIntrospection() throws WorkflowException {
        return WorkflowHelper.getWorkflowIntrospection(this);
    }
}
