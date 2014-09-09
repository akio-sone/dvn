/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.storage;

import edu.harvard.iq.dvn.core.study.Study;
import edu.harvard.iq.dvn.core.util.FileUtil;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author asone
 */
@Singleton
@Startup
public class IrodsConfigurationBean {

    private static final Logger logger
            = Logger.getLogger(IrodsConfigurationBean.class.getName());

    Properties irodsConfigParams = null;

    String irodsStorageRootDir = "/odumMain/home/irods";

    String irodsDeterminantToken = "ODUM-IRODS_";

    String irodsProtocol = "irods://";

    String irodsServerName = null;

    String portNumber = null;

    @PostConstruct
    void init() {

        irodsConfigParams = FileUtil.getIRODSPropertiesFile();
        
        if (irodsConfigParams == null) {
            logger.log(Level.INFO, "irods properties file is still null");
            throw new EJBException("properties irodsConfigParams is still null: irods.config.file is not or incorrectly set in the JVM options of GlassFish Server ");
        } else {

            irodsServerName = irodsConfigParams.getProperty("host");
            logger.log(Level.INFO, "irodsServerName={0}", irodsServerName);

            portNumber = irodsConfigParams.getProperty("port");
            logger.log(Level.INFO, "irodsPortNumber={0}", portNumber);

        }

    }

    public String getIRODSStoragePath(String irodsStorageRootDir,
            String storageDir, String fileSystemName) {
        if (StringUtils.isNotBlank(irodsServerName) && 
                StringUtils.isNotBlank(portNumber)) {
            StringBuilder sb = new StringBuilder(irodsProtocol);
            sb.append(irodsServerName).append(":").append(portNumber).append(irodsStorageRootDir);
            sb.append("/").append(storageDir);
            sb.append("/").append(fileSystemName);
            logger.log(Level.INFO, "irods file location to be ={0}", sb.toString());
            return sb.toString();
        } else {
            throw new IllegalArgumentException("irodsServerName and portNumber are null or blank");
        }
        
    }

    
    public boolean isIRODScase(Study study){
        logger.log(Level.INFO, "irodsDeterminant: studyId={0}", study.getStudyId());
        String irodsDeterminant = study.getStudyId();
        boolean isIRODScase = false;
        String storageDir = null;
        if (StringUtils.isNotEmpty(irodsDeterminant) && 
            (irodsDeterminant.startsWith(irodsDeterminantToken))){
            logger.log(Level.INFO, "IRODS storage case: true");
            isIRODScase = true;
            storageDir = study.getAuthority()+"/"+study.getStudyId();
        } else {
            logger.log(Level.INFO, "IRODS storage case: false");
        }
        return isIRODScase;
    }
    
    public String getIRODSStorageDir(Study study){
        logger.log(Level.INFO, "irodsDeterminant: studyId={0}", study.getStudyId());
        String irodsDeterminant = study.getStudyId();
        String storageDir = null;
        if (StringUtils.isNotEmpty(irodsDeterminant) && 
            (irodsDeterminant.startsWith(irodsDeterminantToken))){
            logger.log(Level.INFO, "IRODS storage case");

            storageDir = study.getAuthority()+"/"+study.getStudyId();
        } else {
            logger.log(Level.INFO, "not IRODS storage case: stydyId={0}", 
                    irodsDeterminant);
        }
        return storageDir;
    }
    
    
}
