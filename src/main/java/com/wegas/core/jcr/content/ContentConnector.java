/*
 * Wegas.
 *
 * http://www.albasim.com/wegas/
 *
 * School of Business and Engineering Vaud, http://www.heig-vd.ch/
 * Media Engineering :: Information Technology Managment :: Comem
 *
 * Copyright (C) 2012
 */
package com.wegas.core.jcr.content;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.wegas.core.jcr.SessionHolder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author Cyril Junod <cyril.junod at gmail.com>
 */
public class ContentConnector {

    static final private org.slf4j.Logger logger = LoggerFactory.getLogger(ContentConnector.class);
    private Session session;
    private String workspace = null;

    protected ContentConnector(Long gameModelId) throws RepositoryException {
        this.workspace = "GM_" + gameModelId;
        this.session = SessionHolder.getSession(this.workspace);
        this.initializeNamespaces();

    }

    protected ContentConnector() throws RepositoryException {
        this.session = SessionHolder.getSession(null);
        this.initializeNamespaces();
    }

    protected Node getNode(String absolutePath) throws RepositoryException {
        try {
            return session.getNode(absolutePath);
        } catch (PathNotFoundException ex) {
            logger.debug("Could not retrieve node ({})", ex.getMessage());
            return null;
        }
    }

    protected boolean nodeExist(String absolutePath) throws RepositoryException {
        return session.nodeExists(absolutePath);
    }

    protected void deleteFile(String absolutePath) throws RepositoryException {
        this.getNode(absolutePath).remove();
        session.save();
    }

    protected NodeIterator listChildren(String path) throws PathNotFoundException, RepositoryException {
        return session.getNode(path).getNodes(WFSConfig.WeGAS_FILE_SYSTEM_PREFIX + "*");
    }

    /*
     * Property getters and setters
     */
    private Property getProperty(String absolutePath, String propertyName) throws RepositoryException {
        Node node = this.getNode(absolutePath);
        if (node == null) {
            return null;
        } else {
            try {
                return node.getProperty(propertyName);
            } catch (PathNotFoundException ex) {
                logger.debug("Inexistant property ({}) on Node[{}]", propertyName, absolutePath);
            }
        }
        return null;
    }

    protected InputStream getData(String absolutePath) throws RepositoryException {
        return this.getProperty(absolutePath, WFSConfig.WFS_DATA).getBinary().getStream();
    }

    protected Node setData(String absolutePath, String mimeType, InputStream data) throws RepositoryException {
        Node newNode = this.getNode(absolutePath);
        newNode.setProperty(WFSConfig.WFS_MIME_TYPE, mimeType);
        newNode.setProperty(WFSConfig.WFS_DATA, session.getValueFactory().createBinary(data));
        newNode.setProperty(WFSConfig.WFS_LAST_MODIFIED, Calendar.getInstance());
        this.save();
        return newNode;
    }

    protected String getMimeType(String absolutePath) throws RepositoryException {
        return this.getProperty(absolutePath, WFSConfig.WFS_MIME_TYPE).getString();
    }

    protected void setMimeType(String absolutePath, String mimeType) throws RepositoryException {
        this.getNode(absolutePath).setProperty(WFSConfig.WFS_MIME_TYPE, mimeType);
    }

    protected String getNote(String absolutePath) throws RepositoryException {
        try {
            return this.getProperty(absolutePath, WFSConfig.WFS_NOTE).getString();
        } catch (NullPointerException ex) {
            return "";
        }
    }

    protected void setNote(String absolutePath, String note) throws RepositoryException {
        note = note == null ? "" : note;
        this.getNode(absolutePath).setProperty(WFSConfig.WFS_NOTE, note);
    }

    protected String getDescription(String absolutePath) throws RepositoryException {
        try {
            return this.getProperty(absolutePath, WFSConfig.WFS_DESCRIPTION).getString();
        } catch (NullPointerException ex) {
            return "";
        }
    }

    protected void setDescription(String absolutePath, String description) throws RepositoryException {
        description = description == null ? "" : description;
        this.getNode(absolutePath).setProperty(WFSConfig.WFS_DESCRIPTION, description);

    }

    protected Calendar getLastModified(String absolutePath) throws RepositoryException {
        return this.getProperty(absolutePath, WFSConfig.WFS_LAST_MODIFIED).getDate();
    }

    protected Long getSize(String absolutePath) throws RepositoryException {
        return this.getProperty(absolutePath, WFSConfig.WFS_DATA).getBinary().getSize();
    }

    /**
     * Jackrabbit doesn't handle workspace deletetion
     *
     * @throws RepositoryException, UnsupportedOperationException
     */
    public void deleteWorkspace() throws RepositoryException {
        //throw new UnsupportedOperationException("Jackrabbit: There is currently no programmatic way to delete workspaces. You can delete a workspace by manually removing the workspace directory when the repository instance is not running.");
        String name = session.getWorkspace().getName();
        SessionHolder.closeSession(workspace);
        Session s = SessionHolder.getSession(null);
        try {
            s.getWorkspace().deleteWorkspace(name);
        } catch (UnsupportedRepositoryOperationException ex) {
            logger.warn("UnsupportedRepositoryOperationException : fallback to clear workspace. Further : improve to remove workspace");
            session = SessionHolder.getSession(workspace);
            this.clearWorkspace();
            SessionHolder.closeSession(workspace);
        }
    }

    public void cloneWorkspace(Long oldGameModelId) throws RepositoryException {
        ContentConnector connector = ContentConnectorFactory.getContentConnectorFromGameModel(oldGameModelId);
        NodeIterator it = connector.listChildren("/");
        String path;
        while (it.hasNext()) {
            path = it.nextNode().getPath();
            session.getWorkspace().clone("GM_" + oldGameModelId, path, path, true);
        }
        session.save();
    }

    public void clearWorkspace() throws RepositoryException {
        NodeIterator it = this.listChildren("/");
        while (it.hasNext()) {
            it.nextNode().remove();
        }
        this.save();
    }

    public void save() {
        if (session.isLive()) {
            try {
                session.save();
            } catch (RepositoryException e) {
                logger.warn(e.getMessage());
            }
        }
    }

    public void exportXML(OutputStream out) throws RepositoryException, IOException, SAXException {
        NodeIterator it = this.listChildren("/");
        OutputFormat format = new OutputFormat("XML", "UTF-8", true);
        XMLSerializer serializer = new XMLSerializer(out, format);
        ContentHandler handler = serializer.asContentHandler();
        handler.startDocument();
        handler.startElement("", "", "root", null);
        while (it.hasNext()) {
            session.exportDocumentView(it.nextNode().getPath(), handler, false, false);
        }
        handler.endElement("", "", "root");
        handler.endDocument();
    }

    /**
     * Check for custom namespaces and register them if they don't exist
     *
     * @throws RepositoryException
     */
    private void initializeNamespaces() throws RepositoryException {
        for (String prefix : WFSConfig.namespaces.keySet()) {
            try {
                session.getWorkspace().getNamespaceRegistry().getURI(prefix);
            } catch (NamespaceException e) {
                session.getWorkspace().getNamespaceRegistry().registerNamespace(prefix, WFSConfig.namespaces.get(prefix));
            }
        }
    }
}
