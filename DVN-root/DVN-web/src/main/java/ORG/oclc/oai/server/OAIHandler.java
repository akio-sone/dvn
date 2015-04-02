/**
 * Copyright 2006 OCLC Online Computer Library Center Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or
 * agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ORG.oclc.oai.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ORG.oclc.oai.server.catalog.AbstractCatalog;
import ORG.oclc.oai.server.verb.BadVerb;
import ORG.oclc.oai.server.verb.OAIInternalServerError;
import ORG.oclc.oai.server.verb.ServerVerb;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OAIHandler is the primary Servlet for OAICat.
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class OAIHandler extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static final String PROPERTIES_SERVLET_CONTEXT_ATTRIBUTE = OAIHandler.class.getName() + ".properties";
    
    private static final String VERSION = "1.5.56";
//    private static boolean debug = true;
    private static final Logger logger = Logger.getLogger(OAIHandler.class.getName());
    
    private XStream xstream = new XStream(new JsonHierarchicalStreamDriver());


//    private Transformer transformer = null;
//    private boolean serviceUnavailable = false;
//    private boolean monitor = false;
//    private boolean forceRender = false;
    protected HashMap attributesMap = new HashMap();
//    private HashMap serverVerbs = null;
//    private HashMap extensionVerbs = null;
//    private String extensionPath = null;
    
//    private static Logger logger = Logger.getLogger(OAIHandler.class);
//    static {
//        BasicConfigurator.configure();
//    }
    
    private Log log = LogFactory.getLog(OAIHandler.class);
    
    /**
     * Get the VERSION number
     */
    public static String getVERSION() { return VERSION; }
    
    /**
     * init is called one time when the Servlet is loaded. This is the
     * place where one-time initialization is done. Specifically, we
     * load the properties file for this application, and create the
     * AbstractCatalog object for subsequent use.
     *
     * @param config servlet configuration information
     * @exception ServletException there was a problem with initialization
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        logger.log(Level.INFO, "+++++++++ OAIHandler#init() starts here");
        try {
            HashMap attributes = null;
            ServletContext context = getServletContext();
            Properties properties = (Properties) context.getAttribute(PROPERTIES_SERVLET_CONTEXT_ATTRIBUTE);
            if (properties == null) {
                
                final String PROPERTIES_INIT_PARAMETER = "properties";
                
                logger.log(Level.INFO, "properties is null");
                
                log.debug("OAIHandler.init(..): No '" + 
                        PROPERTIES_SERVLET_CONTEXT_ATTRIBUTE + 
                        "' servlet context attribute. Trying to use init parameter '" + 
                        PROPERTIES_INIT_PARAMETER + "'");
                
                String fileName = 
                        config.getServletContext().getInitParameter(PROPERTIES_INIT_PARAMETER);
                
                /*
                    web.xml has the following context-param that is available from
                    ServletContext
                
                    <context-param>
                        <param-name>properties</param-name>
                        <param-value>oaicat.properties</param-value>
                    </context-param>
                */
                
                logger.log(Level.INFO, "fileName taken from servlet config(web.xml)={0}", fileName);
                
                InputStream in;
                try {
                    log.debug("fileName=" + fileName);
                    in = new FileInputStream(fileName);
                } catch (FileNotFoundException e) {
                    log.debug("file not found. Try the classpath: " + fileName);
                    in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
                }
                
                
                if (in != null) {
                    logger.log(Level.INFO, "properties file was found: Load the properties");
                    log.debug("file was found: Load the properties");
                    
                    properties = new Properties();
                    
                    properties.load(in);
                    
                    logger.log(Level.INFO, "calling OAIHandler#getAttributes(Properties) to add more data");
//                    logger.log(Level.INFO, "OAIHandler#init(): properties before getAttributes():\n{0}", 
//                            xstream.toXML(properties));
                    attributes = getAttributes(properties);
                    
//                    logger.log(Level.INFO, "OAIHandler#init(): attributes after getAttributes():\n{0}", 
//                            xstream.toXML(attributes));
                    // if (debug) System.out.println("OAIHandler.init: fileName=" + fileName);

                }
            } else {
                log.debug("Load context properties");
                attributes = getAttributes(properties);
            }
            
            logger.log(Level.INFO, "store the modified properties=attributes as the value of global in attributesMap");
            log.debug("Store global properties");
            attributesMap.put("global", attributes);
//            logger.log(Level.INFO, "OAIHandler#init(): attributesMap:\n{0}", 
//                            xstream.toXML(attributesMap));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ServletException(e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ServletException(e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new ServletException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServletException(e.getMessage());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new ServletException(e.getMessage());
        }
        logger.log(Level.INFO, "+++++++++ OAIHandler#init() ends here");        
    }
    
    public HashMap getAttributes(Properties properties)
    throws Throwable {
        
        logger.log(Level.INFO, "+++++++++ OAIHandler#getAttributes(Properties) starts here");
        
        HashMap attributes = new HashMap();
        Enumeration attrNames = getServletContext().getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String)attrNames.nextElement();
            attributes.put(attrName, getServletContext().getAttribute(attrName));
        }
        
        logger.log(Level.INFO, "setting the value of OAIHandler.properties");
        
        attributes.put("OAIHandler.properties", properties);
//        String temp = properties.getProperty("OAIHandler.debug");
//        if ("true".equals(temp)) debug = true;
        String missingVerbClassName = 
                properties.getProperty("OAIHandler.missingVerbClassName",
                        "ORG.oclc.oai.server.verb.BadVerb");
        
        logger.log(Level.INFO, "setting the value of OAIHandler.missingVerbClass");
        
        Class missingVerbClass = Class.forName(missingVerbClassName);
        attributes.put("OAIHandler.missingVerbClass", missingVerbClass);
        if (!"true".equals(properties.getProperty("OAIHandler.serviceUnavailable"))) {
            attributes.put("OAIHandler.version", VERSION);
            logger.log(Level.INFO, "creating an AbstractCatalog instance by calling its factory(Properties) method");
            AbstractCatalog abstractCatalog = 
                    AbstractCatalog.factory(properties, getServletContext());
            
            attributes.put("OAIHandler.catalog", abstractCatalog);
        }
        
        boolean forceRender = false;
        if ("true".equals(properties.getProperty("OAIHandler.forceRender"))) {
            forceRender = true;
        }
        
        String xsltName = properties.getProperty("OAIHandler.styleSheet");
        String appBase = properties.getProperty("OAIHandler.appBase");
        if (appBase == null) appBase = "webapps";
        if (xsltName != null
                && ("true".equalsIgnoreCase(properties.getProperty("OAIHandler.renderForOldBrowsers"))
                        || forceRender)) {
            InputStream is;
            try {
                is = new FileInputStream(appBase + "/" + xsltName);
            } catch (FileNotFoundException e) {
                // This is a silly way to skip the context name in the xsltName
                is = new FileInputStream(getServletContext().getRealPath(xsltName.substring(xsltName.indexOf("/", 1)+1)));
            }
            StreamSource xslSource = new StreamSource(is);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(xslSource);
            logger.log(Level.INFO, "setting the value of OAIHandler.transformer");
            attributes.put("OAIHandler.transformer", transformer);
        }
        
//        logger.log(Level.INFO, "OAIHandler#getAttributes(Properties): attributes:\n{0}", 
//                            xstream.toXML(attributes));
        
        logger.log(Level.INFO, "+++++++++ leaving OAIHandler#getAttributes(Properties)");
        
        return attributes;
    }
    
    public HashMap getAttributes(String pathInfo) {
        logger.log(Level.INFO, "+++++++++ OAIHandler#getAttributes(String) starts here");

        HashMap attributes = null;
        logger.log(Level.INFO, "OAIHandler#getAttributes(String):pathInfo={0}", pathInfo);
        log.debug("pathInfo=" + pathInfo);
        if (pathInfo != null && pathInfo.length() > 0) {
            if (attributesMap.containsKey(pathInfo)) {
                log.debug("attributesMap containsKey");
                attributes = (HashMap) attributesMap.get(pathInfo);
            } else {
                log.debug("!attributesMap containsKey");
                try {
                    String fileName = pathInfo.substring(1) + ".properties";
                    log.debug("attempting load of " + fileName);
                    InputStream in = Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream(fileName);
                    if (in != null) {
                        log.debug("file found");
                        Properties properties = new Properties();
                        properties.load(in);
                        attributes = getAttributes(properties);
                    } else {
                        log.debug("file not found");
                    }
                    attributesMap.put(pathInfo, attributes);
                } catch (Throwable e) {
                    log.debug("Couldn't load file", e);
                    // do nothing
                }
            }
        } else {
            logger.log(Level.INFO, "OAIHandler#getAttributes(String): no path-based processing");
        }
        if (attributes == null) {
//            log.debug("use global attributes");
            logger.log(Level.INFO, "OAIHandler#getAttributes(String): no addition in this method: using the global attributes file instead");
            attributes = (HashMap) attributesMap.get("global");
        }
        
//        logger.log(Level.INFO, "OAIHandler#getAttributes(String): attributes:\n{0}",
//                xstream.toXML(attributes));

        logger.log(Level.INFO, "+++++++++ leaving OAIHandler#getAttributes(String)");

        return attributes;
    }

    /**
     * Peform the http GET action. Note that POST is shunted to here as well.
     * The verb widget is taken from the request and used to invoke an
     * OAIVerb object of the corresponding kind to do the actual work of the verb.
     *
     * @param request the servlet's request information
     * @param response the servlet's response information
     * @exception IOException an I/O error occurred
     */
    public void doGet(HttpServletRequest request,
            HttpServletResponse response)
    throws IOException {
        
        logger.log(Level.INFO, "++++++++++ OAIHandler#doGet(...) starts here ++++++++++");
        
        HashMap attributes = getAttributes(request.getPathInfo());
        if (!filterRequest(request, response)) {
            return;
        }
        
        
        //log.debug("attributes=" + attributes);
        
        
        Properties properties =
            (Properties) attributes.get("OAIHandler.properties");
        
        logger.log(Level.INFO, "OAIHandler#doGet(): properties:\n{0}",
                properties);
        
        boolean monitor = false;
        if (properties.getProperty("OAIHandler.monitor") != null) {
            monitor = true;
        }
        boolean serviceUnavailable = isServiceUnavailable(properties);
        
        String extensionPath = properties.getProperty("OAIHandler.extensionPath", "/extension");
        // note: the following argument properties has no role in the called metod
        // getVerbs() should be no argument method
        HashMap serverVerbs = ServerVerb.getVerbs(properties);
        
        // the following getExtensionVerbs() uses properties
        HashMap extensionVerbs = ServerVerb.getExtensionVerbs(properties);
        
        Transformer transformer =
            (Transformer) attributes.get("OAIHandler.transformer");
        
        boolean forceRender = false;
        if ("true".equals(properties.getProperty("OAIHandler.forceRender"))) {
            forceRender = true;
        }
        
//      try {
        request.setCharacterEncoding("UTF-8");
//      } catch (UnsupportedEncodingException e) {
//      e.printStackTrace();
//      throw new IOException(e.getMessage());
//      }
        Date then = null;
        if (monitor) then = new Date();
//        if (debug) {
            Enumeration headerNames = request.getHeaderNames();
            //System.out.println("OAIHandler.doGet: ");
            logger.log(Level.INFO, "OAIHandler#doGet(): dumping the contents of headerNames:");
            while (headerNames.hasMoreElements()) {
                String headerName = (String)headerNames.nextElement();
//                System.out.print(headerName);
//                System.out.print(": ");
//                System.out.println(request.getHeader(headerName));
                logger.log(Level.INFO, "headerName={0}:value stored in request={1}", 
                        new Object[]{headerName, request.getHeader(headerName)});
            }
//        }
        if (serviceUnavailable) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
            "Sorry. This server is down for maintenance");
        } else {
            logger.log(Level.INFO, "service available case");
            try {
                String userAgent = request.getHeader("User-Agent");
                if (userAgent == null) {
                    userAgent = "";
                } else {
                    userAgent = userAgent.toLowerCase();
                }
                Transformer serverTransformer = null;
                if (transformer != null) {
                    
                    // return HTML if the client is an old browser
                    if (forceRender
                            || userAgent.indexOf("opera") != -1
                            || (userAgent.startsWith("mozilla")
                                    && userAgent.indexOf("msie 6") == -1
                            /* && userAgent.indexOf("netscape/7") == -1 */)) {
                        serverTransformer = transformer;
                    }
                }
                
                
        logger.log(Level.INFO, "OAIHandler#doGet(): attributes:\n{0}",
                attributes);
        logger.log(Level.INFO, "OAIHandler#doGet(): request:\n{0}",
                request);
        logger.log(Level.INFO, "OAIHandler#doGet(): response:\n{0}",
                response);

        
                
                
                
                logger.log(Level.INFO, "OAIHandler#doGet(): calling getResult() as String");
                
                
                
                
                String result = getResult(attributes, request, response, 
                        serverTransformer, serverVerbs, extensionVerbs, 
                        extensionPath);
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
                logger.log(Level.INFO, 
                        "OAIHandler#doGet(): create writer to dump the above result");
                Writer out = getWriter(request, response);
                out.write(result);
                out.close();
                logger.log(Level.INFO, "OAIHandler#doGet(): closing the writer");
            } catch (FileNotFoundException e) {
                logger.log(Level.WARNING, "SC_NOT_FOUND:{0}", e.getMessage());
//                if (debug) {
//                    e.printStackTrace();
//                    System.out.println("SC_NOT_FOUND: " + e.getMessage());
//                }
                response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            } catch (TransformerException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (OAIInternalServerError e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (SocketException e) {
                System.out.println(e.getMessage());
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (Throwable e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
        if (monitor) {
            StringBuffer reqUri = new StringBuffer(request.getRequestURI().toString());
            String queryString = request.getQueryString();   // d=789
            if (queryString != null) {
                reqUri.append("?").append(queryString);
            }
            Runtime rt = Runtime.getRuntime();
            System.out.println(rt.freeMemory() + "/" + rt.totalMemory() + " "
                    + ((new Date()).getTime()-then.getTime()) + "ms: "
                    + reqUri.toString());
        }
        
        logger.log(Level.INFO, "++++++++++ OAIHandler#doGet(...) ends here ++++++++++");
        
        
    }
    
    /**
     * Should the server report itself down for maintenance? Override this
     * method if you want to do this check another way.
     * @param properties
     * @return true=service is unavailable, false=service is available
     */
    protected boolean isServiceUnavailable(Properties properties) {
        if (properties.getProperty("OAIHandler.serviceUnavailable") != null) {
            return true;
        }
        return false;
    }

    /**
     * Override to do any prequalification; return false if
     * the response should be returned immediately, without
     * further action.
     * 
     * @param request
     * @param response
     * @return false=return immediately, true=continue
     */
    protected boolean filterRequest(HttpServletRequest request,
            HttpServletResponse response) {
        return true;
    }

    public static String getResult(HashMap attributes,
            HttpServletRequest request,
            HttpServletResponse response,
            Transformer serverTransformer,
            HashMap serverVerbs,
            HashMap extensionVerbs,
            String extensionPath)
    throws Throwable {
        logger.log(Level.INFO, "++++++++++ OAIHandler#getResult(...) starts here ++++++++++");
        try {
            boolean isExtensionVerb = extensionPath.equals(request.getPathInfo());
            String verb = request.getParameter("verb");
//            if (debug) {
//                System.out.println("OAIHandler.g<etResult: verb=>" + verb + "<");
//            }
            logger.log(Level.INFO, "OAIHandler.g<etResult: verb=>{0}<", verb);
            
            String result;
            Class verbClass = null;
            if (isExtensionVerb) {
                verbClass = (Class)extensionVerbs.get(verb);
                logger.log(Level.INFO, "The called verb is an extension one");
            } else {
                verbClass = (Class)serverVerbs.get(verb);
                logger.log(Level.INFO, "The called verb is a regular one");
            }
            
            if (verbClass == null) {
                verbClass = (Class) attributes.get("OAIHandler.missingVerbClass");
                logger.log(Level.INFO, "verbClass is null: using the value of OAIHandler.missingVerbClass");
            } else {
                logger.log(Level.INFO, "verbClass is not null");
            }
            
            logger.log(Level.INFO, "Creating 'construct' method of the verb class {0} by using reflection", 
                    verbClass.getName());
            // verbClass => ListRecords, etc.
            // "construct" => method name in ListRecord, etc. and it must be static
            // The above method "construct" takes 4 arguments as specified in 
            // new Class[] array, e.g., ListRecords case
            // 
            // public static String construct(HashMap context,
            // HttpServletRequest request, HttpServletResponse response,
            // Transformer serverTransformer)
            // throws OAIInternalServerError, TransformerException {
            Method construct = verbClass.getMethod("construct",
                    new Class[] {
                        HashMap.class,
                        HttpServletRequest.class,
                        HttpServletResponse.class,
                        Transformer.class}
            );
            
            try {
                logger.log(Level.INFO, "invoking the above construct method with actual arguments and getting result from it");
                // since the construct method is static,
                // the first argument of invoke() must be null
                // 
                result = (String)construct.invoke(null,
                        new Object[] {
                                        attributes,
                                        request,
                                        response,
                                        serverTransformer
                                    }
                );
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
//            if (debug) {
//                System.out.println(result);
//            }
            logger.log(Level.INFO, "OAIHandler#getResult(...):result={0}", result);
            
        logger.log(Level.INFO, "++++++++++ leaving OAIHandler#getResult(...) ++++++++++");
        
            return result;
        } catch (NoSuchMethodException e) {
            logger.log(Level.WARNING, "NoSuchMethodException", e);
            throw new OAIInternalServerError(e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.WARNING, "IllegalAccessException", e);
            throw new OAIInternalServerError(e.getMessage());
        }
    }
    
    /**
     * Get a response Writer depending on acceptable encodings
     * @param request the servlet's request information
     * @param response the servlet's response information
     * @exception IOException an I/O error occurred
     */
    public static Writer getWriter(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
        logger.log(Level.INFO, "+++++++++++ OAIHandler#getWriter(...) starts here +++++++++++");
        Writer out;
        String encodings = request.getHeader("Accept-Encoding");
//        if (debug) {
//            System.out.println("encodings=" + encodings);
//        }
        logger.log(Level.INFO, "OAIHandler#getWriter(...):encodings={0}", encodings);
        
        if (encodings != null && encodings.indexOf("gzip") != -1) {
//          System.out.println("using gzip encoding");
//          log.debug("using gzip encoding");
            response.setHeader("Content-Encoding", "gzip");
            out = new OutputStreamWriter(new GZIPOutputStream(response.getOutputStream()),
            "UTF-8");
//          } else if (encodings != null && encodings.indexOf("compress") != -1) {
//          //  	    System.out.println("using compress encoding");
//          response.setHeader("Content-Encoding", "compress");
//          ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
//          zos.putNextEntry(new ZipEntry("dummy name"));
//          out = new OutputStreamWriter(zos, "UTF-8");
        } else if (encodings != null && encodings.indexOf("deflate") != -1) {
//          System.out.println("using deflate encoding");
//          log.debug("using deflate encoding");
            response.setHeader("Content-Encoding", "deflate");
            out = new OutputStreamWriter(new DeflaterOutputStream(response.getOutputStream()),
            "UTF-8");
        } else {
//          log.debug("using no encoding");
            out = response.getWriter();
        }
        logger.log(Level.INFO, "+++++++++++ OAIHandler#getWriter(...) ends here +++++++++++");
        return out;
    }
    
    /**
     * Peform a POST action. Actually this gets shunted to GET
     *
     * @param request the servlet's request information
     * @param response the servlet's response information
     * @exception IOException an I/O error occurred
     */
    public void doPost(HttpServletRequest request,
            HttpServletResponse response)
    throws IOException {
        doGet(request, response);
    }
}
