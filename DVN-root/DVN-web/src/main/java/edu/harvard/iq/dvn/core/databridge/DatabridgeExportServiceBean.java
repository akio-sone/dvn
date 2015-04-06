/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.databridge;

import ORG.oclc.oai.server.catalog.AbstractCatalog;
import ORG.oclc.oai.server.verb.ListRecords;
import ORG.oclc.oai.server.verb.OAIInternalServerError;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import edu.harvard.iq.dvn.core.web.oai.catalog.DVNOAICatalog;
import edu.harvard.iq.dvn.core.web.oai.catalog.DVNXMLRecordFactory;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.xml.transform.TransformerException;

/**
 *
 * @author asone
 */
@Stateless
public class DatabridgeExportServiceBean {

    private static final Logger logger
        = Logger.getLogger(DatabridgeExportServiceBean.class.getName());
//    private XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
//    String outFileName="databridge-export";
//    String metadataPrefix="ddi";
    String oaicatPropertiesFilename = "oaicat.properties";
    String absPathToDatafile = System.getProperty("dvn.databridge.ddi.export.dir");
    String ddiExportFilename="databridge_ddi_export_";
//
//    String verb ="ListRecords";
//    
//    
    private static final String VERSION = "1.5.56";
    protected HashMap attributesMap = new HashMap();
    protected HashMap attributes = new HashMap();
    
    @PostConstruct
    void init() {

        logger.log(Level.INFO, "+++++++++ DatabridgeExportServiceBean#init() starts here");
//        xstream.setMode(XStream.NO_REFERENCES);
        if (absPathToDatafile == null) {
            absPathToDatafile = System.getProperty("jhove.conf.dir");
            logger.log(Level.INFO, "DatabridgeExportServiceBean#init(): absPathToDatafile is set to={0}", absPathToDatafile);
        } else {
            logger.log(Level.INFO, "DatabridgeExportServiceBean#init(): absPathToDatafile={0}", absPathToDatafile);
        }

        try {
            
            //HashMap attributes = null;
            //ServletContext context = getServletContext();
            Properties properties = null;//(Properties) context.getAttribute(PROPERTIES_SERVLET_CONTEXT_ATTRIBUTE);
//            if (properties == null) {

            final String PROPERTIES_INIT_PARAMETER = "properties";

            logger.log(Level.INFO, "DatabridgeExportServiceBean#init(): properties-null-case");

            String fileName = absPathToDatafile + "/" + oaicatPropertiesFilename;
                        //config.getServletContext().getInitParameter(PROPERTIES_INIT_PARAMETER);

            /*
             * web.xml has the following context-param that is available from
             * ServletContext
             *
             * <context-param> <param-name>properties</param-name>
             * <param-value>oaicat.properties</param-value> </context-param>
             */
            logger.log(Level.INFO, "DatabridgeExportServiceBean#init(): OAICat properties fileName={0}", fileName);

            InputStream in;
//                try {

            in = new FileInputStream(fileName);
//                } catch (FileNotFoundException e) {
//                    logger.log(Level.WARNING, "file not found. Try the classpath:{0}", fileName);
//                    in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
//                }

//            if (in != null) {
                logger.log(Level.INFO, "properties file was found: Load the properties");

                properties = new Properties();

                properties.load(in);

                logger.log(Level.INFO, "calling DatabridgeExportServiceBean#getAttributes(Properties) to add more data");
                
                
                logger.log(Level.INFO, "DatabridgeExportServiceBean#init(): properties before getAttributes():\n{0}", properties.stringPropertyNames());
//                            xstream.toXML(properties));
                attributes = getAttributes(properties);

//                    logger.log(Level.INFO, "DatabridgeExportServiceBean#init(): attributes after getAttributes():\n{0}", 
//                            xstream.toXML(attributes));
                
                    // if (debug) System.out.println("OAIHandler.init: fileName=" + fileName);
//            } else {
//                log.debug("Load context properties");
//                attributes = getAttributes(properties);
//            }
            logger.log(Level.INFO, "store the modified properties=attributes as the value of global in attributesMap");
            logger.log(Level.INFO, "Store global properties");
            
            attributesMap.put("global", attributes);
            
//            logger.log(Level.INFO, "DatabridgeExportServiceBean#init(): attributesMap:\n{0}", 
//                            xstream.toXML(attributesMap));
            

        } catch (FileNotFoundException e) {

        } catch (IllegalArgumentException e) {

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IOException", ex);
        }
        
        logger.log(Level.INFO, "+++++++++ DatabridgeExportServiceBean#init() ends here");

//        if (absPathToDatafile == null) {
//            absPathToDatafile=System.getProperty("jhove.conf.dir");
//            logger.log(Level.INFO, "absPathToDatafile is set to={0}", absPathToDatafile);
//        } else {
//            logger.log(Level.INFO, "absPathToDatafile={0}", absPathToDatafile);
//        }
//        
//
    }
//            
//    public void doExport(String setSpec){
//        logger.log(Level.INFO, 
//                "********** DatabridgeExportServiceBean#doExport starts here **********");
//        logger.log(Level.INFO, "OAI set name recived={0}", setSpec);

//        logger.log(Level.INFO, "********** DatabridgeExportServiceBean#doExport ends here **********");
//    }

    
    public HashMap getAttributes(Properties properties) {
//            HashMap attributes = new HashMap();
        try {
            logger.log(Level.INFO, "+++++++++ DatabridgeExportServiceBean#getAttributes(Properties) starts here");
            

//        Enumeration attrNames = getServletContext().getAttributeNames();
//        while (attrNames.hasMoreElements()) {
//            String attrName = (String)attrNames.nextElement();
//            attributes.put(attrName, getServletContext().getAttribute(attrName));
//        }
            
            logger.log(Level.INFO, "DatabridgeExportServiceBean#getAttributes(Properties): setting the value of OAIHandler.properties");
            
            attributes.put("OAIHandler.properties", properties);

            String missingVerbClassName
                = properties.getProperty("OAIHandler.missingVerbClassName",
                    "ORG.oclc.oai.server.verb.BadVerb");
            
            logger.log(Level.INFO, "DatabridgeExportServiceBean#getAttributes(Properties): setting the value of OAIHandler.missingVerbClass");
            
            Class missingVerbClass = Class.forName(missingVerbClassName);
            
            attributes.put("OAIHandler.missingVerbClass", missingVerbClass);
            
            if (!"true".equals(properties.getProperty("OAIHandler.serviceUnavailable"))) {
                attributes.put("OAIHandler.version", VERSION);
                logger.log(Level.INFO, 
                        "DatabridgeExportServiceBean#getAttributes(Properties): creating an AbstractCatalog instance by calling its factory(Properties) method");
//                ServletContext context = null;
                
//                AbstractCatalog abstractCatalog
//                    = AbstractCatalog.factory(properties, context);
                AbstractCatalog abstractCatalog = new DVNOAICatalog(properties);
                
                abstractCatalog.setRecordFactory(new DVNXMLRecordFactory(properties));
                
                abstractCatalog.setSupportedGranularityOffset(1);
                

                
                
                
                
                
                attributes.put("OAIHandler.catalog", abstractCatalog);
            }
            
//            boolean forceRender = false;
//            if ("true".equals(properties.getProperty("OAIHandler.forceRender"))) {
//                forceRender = true;
//            }
            
//            String xsltName = properties.getProperty("OAIHandler.styleSheet");
//            String appBase = properties.getProperty("OAIHandler.appBase");
//            if (appBase == null) {
//                appBase = "webapps";
//            }
//        if (xsltName != null
//                && ("true".equalsIgnoreCase(properties.getProperty("OAIHandler.renderForOldBrowsers"))
//                        || forceRender)) {
//            InputStream is;
//            try {
//                is = new FileInputStream(appBase + "/" + xsltName);
//            } catch (FileNotFoundException e) {
//                // This is a silly way to skip the context name in the xsltName
//                is = new FileInputStream(getServletContext().getRealPath(xsltName.substring(xsltName.indexOf("/", 1)+1)));
//            }
//            StreamSource xslSource = new StreamSource(is);
//            TransformerFactory tFactory = TransformerFactory.newInstance();
//            Transformer transformer = tFactory.newTransformer(xslSource);
//            logger.log(Level.INFO, "setting the value of OAIHandler.transformer");
//            attributes.put("OAIHandler.transformer", transformer);
//        }
            
            logger.log(Level.INFO, "+++++++++ leaving OAIHandler#getAttributes(Properties)");
            
            return attributes;
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "ClassNotFoundException", ex);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Throwable", ex);
        }
        return attributes;
    }
    
    
    
    public void renderRecords(String setName){
        
        OutputStream outs = null;
        try {
            
            
            String exportFilename = 
                    absPathToDatafile + "/" + ddiExportFilename +setName+".xml";
            logger.log(Level.INFO, "exportFilename={0}", exportFilename);
            
            Properties properties =
                    (Properties)attributes.get("OAIHandler.properties");
            
            logger.log(Level.INFO, "DatabridgeExportServiceBean#renderRecords(): properties:\n{0}",
                    properties.stringPropertyNames());
            
//            String verb = "ListRecords";
//            HashMap serverVerbs = ServerVerb.getVerbs(properties);

            logger.log(Level.INFO, "DatabridgeExportServiceBean#renderRecords(): calling ListRecords");
//            String result = getResult(properties, null, null,
//                    null, serverVerbs, null, null);
            Map requestMap = new HashMap();
            
            requestMap.put("verb", "ListRecords");
            requestMap.put("metadataPrefix", "ddi");
            requestMap.put("set", setName);
            String baseURL = "http://localhost:8080/dvn/OAIHandler";
            
            // public static String construct(Map attributes, Map requestMap, String baseURL)
            String result = ListRecords.construct(attributes, requestMap, baseURL);
            
            logger.log(Level.INFO, "result from ListRecords class:\n{0}", result);
            
            File expf = new File(exportFilename);
            outs = new BufferedOutputStream(new FileOutputStream(expf));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(outs, "utf8"), true);
            pw.println(result);
            outs.close();
            
        } catch (OAIInternalServerError ex) {
            logger.log(Level.SEVERE, "OAIInternalServerError", ex);
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, "TransformerException", ex);
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "FileNotFoundException", ex);
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, "UnsupportedEncodingException", ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IOException", ex);
        } finally {
            logger.log(Level.INFO, "leaving DatabridgeExportServiceBean#renderRecords()");
        }
        
        
    }
}
