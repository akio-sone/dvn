/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jcr.Binary;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.InitialContext;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.modeshape.common.util.StringUtil;
import org.modeshape.jcr.api.JcrTools;

/**
 *
 * @author asone
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class IrodsStorageServiceBean {

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    private static final Logger logger
            = Logger.getLogger(IrodsStorageServiceBean.class.getName());

//    @Resource(mappedName = "jcr/repository")
//    private javax.jcr.Repository repository;
    @EJB
    ModeShapeServiceBean modeShapeServiceBean;

    private Session session;

    private JcrTools tools = new JcrTools();
//    @Resource(lookup = "custom/test-irods-connection")
//    Properties irodsConnectionAttributes;

//    private static IRODSFileSystem irodsFileSystem;
//    private static javax.jcr.Session session;
    
    public IrodsStorageServiceBean() {
    }

    @PostConstruct
    void init() {
        logger.log(Level.INFO, "FileUploadManagedBean is created");
//        session = modeShapeServiceBean.getSession();
        if (modeShapeServiceBean.getRepository() == null) {
            logger.log(Level.SEVERE, "repository is still null");
        } else {
            logger.log(Level.INFO, "repository is NOT null");
        }
//            for (String s : repository.getDescriptorKeys()) {
//                logger.log(Level.INFO,
//                        "repository:descriptors:key={0}={1}",
//                        new Object[]{s, repository.getDescriptorValue(s)}
//                );
//            }
//        

    }

//    @PostConstruct
    void checkRepo(javax.jcr.Session session) {
        try {
            String workspaceName = "default";
//            session = repository.login(workspaceName);

            for (String s : modeShapeServiceBean.getRepository().getDescriptorKeys()) {
                logger.log(Level.INFO,
                        "repository:descriptors:key={0}={1}",
                        new Object[]{s, modeShapeServiceBean.getRepository().getDescriptorValue(s)}
                );
            }

            Node root = session.getRootNode();
            logger.log(Level.INFO, "root node name={0}", root.getName());

        } catch (NoSuchWorkspaceException ex) {
            logger.log(Level.SEVERE, "NoSuchWorkspaceException in init()", ex);
        } catch (RepositoryException ex) {
            logger.log(Level.SEVERE, "RepositoryException in init()", ex);
        }
    }

    /*
     * Session session = null;
     *
     * @PostConstruct public void init() { try { // Create a session ... session
     * = repository.login("default"); for (String s :
     * repository.getDescriptorKeys()) { logger.log(Level.INFO,
     * "repository:descriptors:key={0}={1}", new Object[]{s,
     * repository.getDescriptorValue(s)} ); } Node doc =
     * session.getNode("/files/"); tools.printNode(doc); NodeIterator nodeList =
     * doc.getNodes(); while (nodeList.hasNext()) { Node nodeImage =
     * nodeList.nextNode(); }
     *
     * } catch (NoSuchWorkspaceException ex) { logger.log(Level.SEVERE,
     * "NoSuchWorkspaceException", ex); } catch (RepositoryException ex) {
     * logger.log(Level.SEVERE, "RepositoryException", ex); } finally { if
     * (session != null) { session.logout(); } }
     *
     * }
     */
    public void saveFile(String dir, String fileName, InputStream in) {
        logger.log(Level.INFO, " ================== saveFile(InputStream case) starts here ==================");
        String workspaceName = "default";
        session = null;
        try {
            session = modeShapeServiceBean.getRepository().login(workspaceName);
//            logger.log(Level.INFO, "dumping repository keys");
//            for (String s : modeShapeServiceBean.getRepository().getDescriptorKeys()) {
//                logger.log(Level.INFO,
//                        "repository:descriptors:key={0}=>{1}",
//                        new Object[]{s, modeShapeServiceBean.getRepository().getDescriptorValue(s)}
//                );
//            }
            String pathToFile = "/irodsGrid/dataverse/studies/"+dir+"/"+fileName;
            tools.uploadFile(session, pathToFile, in);

            session.save();

        } catch (NoSuchWorkspaceException ex) {
            logger.log(Level.SEVERE, "NoSuchWorkspaceException", ex);
        } catch (RepositoryException ex) {
            logger.log(Level.SEVERE, "RepositoryException", ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IOException", ex);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    
    public void saveFile(String dir, String fileName, File file) {
        logger.log(Level.INFO, " ================== saveFile(File case) starts here ==================");
        String workspaceName = "default";
        session = null;
        try {
            session = modeShapeServiceBean.getRepository().login(workspaceName);
//            logger.log(Level.INFO, "dumping repository keys");
//            for (String s : modeShapeServiceBean.getRepository().getDescriptorKeys()) {
//                logger.log(Level.INFO,
//                        "repository:descriptors:key={0}=>{1}",
//                        new Object[]{s, modeShapeServiceBean.getRepository().getDescriptorValue(s)}
//                );
//            }
            String pathToFile = "/irodsGrid/dataverse/studies/"+dir+"/"+fileName;
            tools.uploadFile(session, pathToFile, file);

            session.save();

        } catch (NoSuchWorkspaceException ex) {
            logger.log(Level.SEVERE, "NoSuchWorkspaceException", ex);
        } catch (RepositoryException ex) {
            logger.log(Level.SEVERE, "RepositoryException", ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IOException", ex);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }
    
    
    public void saveFileWithoutJcrTools(String dir, String fileName, InputStream in) {

        String workspaceName = "default";
        try {

            //establish session and query node by path
            session = modeShapeServiceBean.getRepository().login(workspaceName);
            logger.info("Session established successfuly");
            Node root;// = session.getRootNode();
            
            
            
            Node dsRootNode = null;
            try {
                logger.log(Level.INFO, "try to get the ds-root-node: /irodsGrid");
                dsRootNode = session.getNode("/irodsGrid/");
            } catch (PathNotFoundException ex) {
                logger.log(Level.INFO, "PathNotFoundException was caught: /irodsGrid does not exist");
                logger.log(Level.INFO, "creating /irodsGrid node");
                root = session.getRootNode();

                Node filesNode = root.addNode("irodsGrid", "nt:folder");

                dsRootNode = filesNode;

            }
            logger.log(Level.INFO, "saving the uploaded file");

            //Node filesNode = root.addNode("irodsGrid", "nt:folder");

//            InputStream stream
//                    = new BufferedInputStream(in);
            
            Node storageDir = dsRootNode.addNode(dir, "nt:folder");
            
            
            
            
            // Create an 'nt:file' node at the supplied path ...
            Node fileNode = storageDir.addNode(fileName, "nt:file");
            
            
            
            tools.printNode(fileNode);
            logger.log(Level.INFO, "node: {0} has been added", fileName);

            // Upload the file to that node ...
            logger.log(Level.INFO, "creating the contents node for {0}", fileName);
            Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
            javax.jcr.Binary binary = session.getValueFactory().createBinary(in);
            contentNode.setProperty("jcr:data", binary);

            session.save();
            logger.log(Level.INFO, "{0} has been saved on ModeShape", fileName);
        } catch (NoSuchWorkspaceException ex) {
            logger.log(Level.SEVERE, "NoSuchWorkspaceException", ex);
        } catch (RepositoryException ex) {
            logger.log(Level.SEVERE, "RepositoryException", ex);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }


    
}
