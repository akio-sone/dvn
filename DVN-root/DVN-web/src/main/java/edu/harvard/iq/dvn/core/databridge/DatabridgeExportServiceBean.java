/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.databridge;

import ORG.oclc.oai.harvester2.app.RawWrite;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.renci.databridge.contrib.dataloader.DataLoader;
import org.xml.sax.SAXException;

/**
 *
 * @author asone
 */
@Stateless
public class DatabridgeExportServiceBean {
    private static final Logger logger = 
            Logger.getLogger(DatabridgeExportServiceBean.class.getName());

    String outFileName="databridge-export";
    String metadataPrefix="ddi";
    String absPathToDatafile= System.getProperty("dvn.databridge.ddi.export.dir");

    String verb ="ListRecords";
    
    
    @PostConstruct
    void init() {
        if (absPathToDatafile == null) {
            absPathToDatafile=System.getProperty("jhove.conf.dir");
            logger.log(Level.INFO, "absPathToDatafile is set to={0}", absPathToDatafile);
        } else {
            logger.log(Level.INFO, "absPathToDatafile={0}", absPathToDatafile);
        }
        

    }
            
    public void doExport(String setSpec){
        logger.log(Level.INFO, 
                "********** DatabridgeExportServiceBean#doExport starts here **********");
        logger.log(Level.INFO, "OAI set name recived={0}", setSpec);


        
        

        logger.log(Level.INFO, "********** DatabridgeExportServiceBean#doExport ends here **********");
    }
    
    
}
