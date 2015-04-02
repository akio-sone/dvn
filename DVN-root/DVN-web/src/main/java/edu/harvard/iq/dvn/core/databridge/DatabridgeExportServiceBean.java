/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dvn.core.databridge;

import ORG.oclc.oai.server.catalog.AbstractCatalog;
import ORG.oclc.oai.server.verb.OAIInternalServerError;
import ORG.oclc.oai.server.verb.ServerVerb;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;

/**
 *
 * @author asone
 */
@Stateless
public class DatabridgeExportServiceBean {

    private static final Logger logger
        = Logger.getLogger(DatabridgeExportServiceBean.class.getName());
    private XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
//    String outFileName="databridge-export";
//    String metadataPrefix="ddi";
    String oaicatPropertiesFilename = "oaicat.properties";
    String absPathToDatafile = System.getProperty("dvn.databridge.ddi.export.dir");
//
//    String verb ="ListRecords";
//    
//    
    private static final String VERSION = "1.5.56";
    protected HashMap attributesMap = new HashMap();

    @PostConstruct
    void init() {

        logger.log(Level.INFO, "+++++++++ DatabridgeExportServiceBean#init() starts here");

        if (absPathToDatafile == null) {
            absPathToDatafile = System.getProperty("jhove.conf.dir");
            logger.log(Level.INFO, "absPathToDatafile is set to={0}", absPathToDatafile);
        } else {
            logger.log(Level.INFO, "absPathToDatafile={0}", absPathToDatafile);
        }

        try {
            HashMap attributes = null;
            //ServletContext context = getServletContext();
            Properties properties = null;//(Properties) context.getAttribute(PROPERTIES_SERVLET_CONTEXT_ATTRIBUTE);
//            if (properties == null) {

            final String PROPERTIES_INIT_PARAMETER = "properties";

            logger.log(Level.INFO, "properties is null");

            String fileName = absPathToDatafile + "/" + oaicatPropertiesFilename;
                        //config.getServletContext().getInitParameter(PROPERTIES_INIT_PARAMETER);

            /*
             * web.xml has the following context-param that is available from
             * ServletContext
             *
             * <context-param> <param-name>properties</param-name>
             * <param-value>oaicat.properties</param-value> </context-param>
             */
            logger.log(Level.INFO, "fileName taken from servlet config(web.xml)={0}", fileName);

            InputStream in;
//                try {
            logger.log(Level.INFO, "fileName={0}", fileName);
            in = new FileInputStream(fileName);
//                } catch (FileNotFoundException e) {
//                    logger.log(Level.WARNING, "file not found. Try the classpath:{0}", fileName);
//                    in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
//                }

//            if (in != null) {
                logger.log(Level.INFO, "properties file was found: Load the properties");
                logger.log(Level.WARNING, "file was found: Load the properties");

                properties = new Properties();

                properties.load(in);

                logger.log(Level.INFO, "calling DatabridgeExportServiceBean#getAttributes(Properties) to add more data");
                
                
                logger.log(Level.INFO, "DatabridgeExportServiceBean#init(): properties before getAttributes():\n{0}", 
                            xstream.toXML(properties));
                attributes = getAttributes(properties);

                    logger.log(Level.INFO, "DatabridgeExportServiceBean#init(): attributes after getAttributes():\n{0}", 
                            xstream.toXML(attributes));
                
                    // if (debug) System.out.println("OAIHandler.init: fileName=" + fileName);
//            } else {
//                log.debug("Load context properties");
//                attributes = getAttributes(properties);
//            }
            logger.log(Level.INFO, "store the modified properties=attributes as the value of global in attributesMap");
            logger.log(Level.INFO, "Store global properties");
            
            attributesMap.put("global", attributes);
            
            logger.log(Level.INFO, "DatabridgeExportServiceBean#init(): attributesMap:\n{0}", 
                            xstream.toXML(attributesMap));
            

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
            HashMap attributes = new HashMap();
        try {
            logger.log(Level.INFO, "+++++++++ DatabridgeExportServiceBean#getAttributes(Properties) starts here");
            

//        Enumeration attrNames = getServletContext().getAttributeNames();
//        while (attrNames.hasMoreElements()) {
//            String attrName = (String)attrNames.nextElement();
//            attributes.put(attrName, getServletContext().getAttribute(attrName));
//        }
            
            logger.log(Level.INFO, "setting the value of OAIHandler.properties");
            
            attributes.put("OAIHandler.properties", properties);

            String missingVerbClassName
                = properties.getProperty("OAIHandler.missingVerbClassName",
                    "ORG.oclc.oai.server.verb.BadVerb");
            
            logger.log(Level.INFO, "setting the value of OAIHandler.missingVerbClass");
            
            Class missingVerbClass = Class.forName(missingVerbClassName);
            
            attributes.put("OAIHandler.missingVerbClass", missingVerbClass);
            
            if (!"true".equals(properties.getProperty("OAIHandler.serviceUnavailable"))) {
                attributes.put("OAIHandler.version", VERSION);
                logger.log(Level.INFO, 
                        "creating an AbstractCatalog instance by calling its factory(Properties) method");
                ServletContext context = null;
                
                AbstractCatalog abstractCatalog
                    = AbstractCatalog.factory(properties, context);
                
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
    
    
    public void doGet() throws Throwable {
        
        logger.log(Level.INFO, "++++++++++ OAIHandler#doGet(...) starts here ++++++++++");
        
//        the follow line in the original is irrelevant        
//        HashMap attributes = getAttributes(request.getPathInfo());
//        if (!filterRequest(request, response)) {
//            return;
//        }
        
        
        //log.debug("attributes=" + attributes);
        
        
        Properties properties = (Properties)((HashMap)attributesMap.get("OAIHandler.properties")).get("OAIHandler.properties");
        
        logger.log(Level.INFO, "DatabridgeExportServiceBean#doGet(): properties:\n{0}", 
                            xstream.toXML(properties));
        
        
        //    (Properties) attributes.get("OAIHandler.properties");
        boolean monitor = false;
//        if (properties.getProperty("OAIHandler.monitor") != null) {
//            monitor = true;
//        }
//        boolean serviceUnavailable = isServiceUnavailable(properties);
//        String extensionPath = properties.getProperty("OAIHandler.extensionPath", "/extension");
//        
        HashMap serverVerbs = ServerVerb.getVerbs(properties);
//        HashMap extensionVerbs = ServerVerb.getExtensionVerbs(properties);
//        
//        Transformer transformer =
//            (Transformer) attributes.get("OAIHandler.transformer");
//        
//        boolean forceRender = false;
//        if ("true".equals(properties.getProperty("OAIHandler.forceRender"))) {
//            forceRender = true;
//        }
        
//      try {
//        request.setCharacterEncoding("UTF-8");
//      } catch (UnsupportedEncodingException e) {
//      e.printStackTrace();
//      throw new IOException(e.getMessage());
//      }
//        Date then = null;
//        if (monitor) then = new Date();
//        if (debug) {
//            Enumeration headerNames = request.getHeaderNames();
            //System.out.println("OAIHandler.doGet: ");
//            logger.log(Level.INFO, "OAIHandler#doGet(): dumping the contents of headerNames:");
//            while (headerNames.hasMoreElements()) {
//                String headerName = (String)headerNames.nextElement();
////                System.out.print(headerName);
////                System.out.print(": ");
////                System.out.println(request.getHeader(headerName));
//                logger.log(Level.INFO, "headerName={0}:value stored in request={1}", 
//                        new Object[]{headerName, request.getHeader(headerName)});
//            }
//        }
//        if (serviceUnavailable) {
//            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
//            "Sorry. This server is down for maintenance");
//        } else {
            logger.log(Level.INFO, "service available case");
//            try {
//                String userAgent = request.getHeader("User-Agent");
//                if (userAgent == null) {
//                    userAgent = "";
//                } else {
//                    userAgent = userAgent.toLowerCase();
//                }
//                Transformer serverTransformer = null;
//                if (transformer != null) {
//                    
//                    // return HTML if the client is an old browser
//                    if (forceRender
//                            || userAgent.indexOf("opera") != -1
//                            || (userAgent.startsWith("mozilla")
//                                    && userAgent.indexOf("msie 6") == -1
//                            /* && userAgent.indexOf("netscape/7") == -1 */)) {
//                        serverTransformer = transformer;
//                    }
//                }
                logger.log(Level.INFO, "OAIHandler#doGet(): calling getResult() as String");
                String result = getResult(properties, null, null, 
                        null, serverVerbs, null, null);
//              log.debug("result=" + result);
                
//              if (serverTransformer) { // render on the server
//              response.setContentType("text/html; charset=UTF-8");
//              StringReader stringReader = new StringReader(getResult(request));
//              StreamSource streamSource = new StreamSource(stringReader);
//              StringWriter stringWriter = new StringWriter();
//              transformer.transform(streamSource, new StreamResult(stringWriter));
//              result = stringWriter.toString();
//              } else { // render on the client
//              response.setContentType("text/xml; charset=UTF-8");
//              result = getResult(request);
//              }
                logger.log(Level.INFO, "OAIHandler#doGet(): create writer to dump the above result");
//                Writer out = getWriter(request, response);
//                out.write(result);
//                out.close();
                logger.log(Level.INFO, "OAIHandler#doGet(): closing the writer");
//            } catch (FileNotFoundException e) {
//                logger.log(Level.WARNING, "SC_NOT_FOUND:{0}", e.getMessage());
//                response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
//            } catch (TransformerException e) {
//
//            } catch (OAIInternalServerError e) {
//
//            } catch (SocketException e) {

//            } catch (Throwable e) {

//            }
//        }
//        if (monitor) {
//            StringBuffer reqUri = new StringBuffer(request.getRequestURI().toString());
//            String queryString = request.getQueryString();   // d=789
//            if (queryString != null) {
//                reqUri.append("?").append(queryString);
//            }
//            Runtime rt = Runtime.getRuntime();
//            System.out.println(rt.freeMemory() + "/" + rt.totalMemory() + " "
//                    + ((new Date()).getTime()-then.getTime()) + "ms: "
//                    + reqUri.toString());
//        }
        logger.log(Level.INFO, "DatabridgeExportServiceBean#doGet(): result:\n{0}", result);
        logger.log(Level.INFO, "++++++++++ DatabridgeExportServiceBean#doGet(...) ends here ++++++++++");
        
        
    }
    
    public static String getResult(Properties attributes,
            HttpServletRequest request,
            HttpServletResponse response,
            Transformer serverTransformer,
            HashMap serverVerbs,
            HashMap extensionVerbs,
            String extensionPath)
    throws Throwable {
        logger.log(Level.INFO, "++++++++++ DatabridgeExportServiceBean#getResult(...) starts here ++++++++++");
        try {
            //boolean isExtensionVerb = extensionPath.equals(request.getPathInfo());
            String verb = "ListRecords";//request.getParameter("verb");
//            if (debug) {
//                System.out.println("OAIHandler.g<etResult: verb=>" + verb + "<");
//            }
            logger.log(Level.INFO, "OAIHandler.g<etResult: verb=>{0}<", verb);
            
            String result;
            Class verbClass = null;
//            if (isExtensionVerb) {
//                verbClass = (Class)extensionVerbs.get(verb);
//                logger.log(Level.INFO, "The called verb is an extension one");
//            } else {
                verbClass = (Class)serverVerbs.get(verb);
                logger.log(Level.INFO, "The called verb is a regular one");
//            }
            
            if (verbClass == null) {
                verbClass = (Class) attributes.get("OAIHandler.missingVerbClass");
                logger.log(Level.INFO, "verbClass is null: using the value of OAIHandler.missingVerbClass");
            } else {
                logger.log(Level.INFO, "verbClass is not null");
            }
            
            logger.log(Level.INFO, "Creating the verb class {0} by using reflection", 
                    verbClass.getName());
            Method construct = verbClass.getMethod("construct",
                    new Class[] {HashMap.class,
                    HttpServletRequest.class,
                    HttpServletResponse.class,
                    Transformer.class});
            try {
                logger.log(Level.INFO, "getting back result from the invoked verb");
                result = (String)construct.invoke(null,
                        new Object[] {attributes,
                        request,
                        response,
                        serverTransformer});
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
//            if (debug) {
//                System.out.println(result);
//            }
            logger.log(Level.INFO, "DatabridgeExportServiceBean#getResult(...):result={0}", result);
            
        logger.log(Level.INFO, "++++++++++ leaving DatabridgeExportServiceBean#getResult(...) ++++++++++");
        
            return result;
        } catch (NoSuchMethodException e) {
            logger.log(Level.WARNING, "NoSuchMethodException", e);
            throw new OAIInternalServerError(e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.WARNING, "IllegalAccessException", e);
            throw new OAIInternalServerError(e.getMessage());
        }
    }
    
}
