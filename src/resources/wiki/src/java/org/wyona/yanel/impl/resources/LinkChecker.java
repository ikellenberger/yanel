package org.wyona.yanel.impl.resources;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Category;
import org.wyona.yarep.core.Repository;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LinkChecker extends DefaultHandler {

    private static Category log = Category.getInstance(LinkChecker.class);
    private ByteArrayInputStream byteArrayInputStream = null;
    private StringBuffer transformedXmlAsBuffer = null;
    private Repository dataRepo = null;
    private String refererPath = null;
    private DataPath dataPathImpl = null;
    /**
     * this array with protocols will all be handled as external links
     * and therefor they dont have to be checked if they exist in repository
     */
    private String[] externalLinks = {
        "http:", "ftp:", "https:", "mailto:",
        "news:", "file:", "rtsp:", "mms:", "ldap:",
        "gopher:", "nntp:", "telnet:", "wais:",
        "prospero:", "z39.50s", "z39.50r", "vemmi:",
        "imap:", "nfs:", "acap:", "tip:", "pop:",
        "dav:", "opaquelocktoken:", "sip:", "sips:",
        "tel:", "fax:", "modem:", "soap.beep:", "soap.beeps",
        "xmlrpc.beep", "xmlrpc.beeps", "urn:", "go:",
        "h323:", "ipp:", "tftp:", "mupdate:", "pres:",
        "im:", "mtqp", "smb:" };
    
    public LinkChecker(Repository dataRepo, String refererPath, DataPath dataPathImpl) {
        this.dataRepo = dataRepo;
        this.refererPath = refererPath;
        this.dataPathImpl = dataPathImpl;
    }
    
    public void startDocument() throws SAXException {
        transformedXmlAsBuffer = new StringBuffer();
    }

    public void endDocument() throws SAXException {
        setResultInputStream();
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
        String eName = ("".equals(localName)) ? qName : localName;
        
        transformedXmlAsBuffer.append("<" + eName);
        
        if(eName.equals("Link")) {
            for(int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getQName(i);
                String aValue = attrs.getValue(i);
                if(aName.equals("href")) {
                    if(aValue.startsWith("external_")) {
                        //do not check this link cause it is EXTERNAL 
                        aValue = aValue.substring(9);
                    } else {
                        //check internal links if they already exist
                        boolean externalLink = false;
                        for(int j = 0; j < externalLinks.length; j++) {
                            if(aValue.startsWith(externalLinks[j])) {
                                externalLink = true;
                                break;
                            }
                        }
                        if(!externalLink && !resourceExists(aValue)) {
                            log.warn("Link : [" + aValue + "] does not exist");
                            transformedXmlAsBuffer.append(" exist=\"false\"");
                        }
                    }
                }
                transformedXmlAsBuffer.append(" " + aName + "=\"" + replaceEntities(aValue) + "\"");
            }  
        } else {
            for(int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getQName(i);
                String aValue = attrs.getValue(i);
                
                transformedXmlAsBuffer.append(" " + aName + "=\"" + replaceEntities(aValue) + "\"");
            }
        }
        transformedXmlAsBuffer.append(">");
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        String eName = ("".equals(localName)) ? qName : localName;
        transformedXmlAsBuffer.append("</" + eName + ">");
    }

    public void characters(char[] buf, int offset, int len) throws SAXException {
        String s = new String(buf, offset, len);
        transformedXmlAsBuffer.append(replaceEntities(s));
    }
    
    /**
     * Replaces some characters by their corresponding xml entities.
     * @param str
     * @return
     */
    private String replaceEntities(String str) {
        str = str.replaceAll("&", "&amp;");
        str = str.replaceAll("<", "&lt;");
        str = str.replaceAll(">", "&gt;");
        str = str.replaceAll("'", "&apos;");
        str = str.replaceAll("\"", "&quot;");
        return str;
    }

    private void setResultInputStream() {
        try {
            this.byteArrayInputStream = new ByteArrayInputStream(transformedXmlAsBuffer.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            log.error(e, e);
        }
    }
 
    public ByteArrayInputStream getInputStream() {
        return this.byteArrayInputStream;
    }
    
    /**
     * Check if resource exists within repository
     */
    private boolean resourceExists(String path) {
        try {
            String absolutePath = org.wyona.commons.io.PathUtil.concat(refererPath, path);
            //log.debug("Referer: " + refererPath + ", path: " + path);
            //log.debug("Absolute Path: " + absolutePath);
            return dataRepo.existsNode(dataPathImpl.getDataPath(absolutePath));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }
}
