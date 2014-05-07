/*
 *  Copyright 2014 President and fellows of Harvard University.
 *                 (Author: Akio Sone)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package edu.harvard.iq.dvn.core.storage;

import edu.harvard.iq.dvn.core.util.FileUtil;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.infinispan.schematic.document.ParsingException;
import org.modeshape.jcr.ConfigurationException;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.NoSuchRepositoryException;
import org.modeshape.jcr.RepositoryConfiguration;

/**
 *
 * @author Akio Sone
 */
@Singleton
@Startup
public class ModeShapeServiceBean {

    private static final Logger logger
        = Logger.getLogger(ModeShapeServiceBean.class.getName());

//    @Resource(lookup = "custom/test-irods-connection")
//    Properties irodsConnectionAttributes;
    
    
    private org.modeshape.jcr.ModeShapeEngine engine;
//    private javax.jcr.Session session;

    public ModeShapeEngine getEngine() {
        return engine;
    }

//    public Session getSession() {
//        return session;
//    }
    String repoName;

    public String getRepoName() {
        return repoName;
    }

//    public void setRepoName(String repoName) {
//        this.repoName = repoName;
//    }
    private javax.jcr.Repository repository;

    public Repository getRepository() {
        return repository;
    }

//    public void setRepository(Repository repository) {
//        this.repository = repository;
//    }
//    
    public static String DEFAULT_REPO_NAME = "default";

    @PostConstruct
    void init() {
        try {
            engine = new ModeShapeEngine();
            engine.start();
            RepositoryConfiguration config = RepositoryConfiguration
                .read(FileUtil.getModeShapeConfigFile());
            // new URL("file:///C:/tmp/modeshape/irods/3.7.1/testConfig1.json")
            org.modeshape.common.collection.Problems problems = config.validate();
            
            if (problems.hasErrors()) {
                logger.log(Level.INFO, "repository configuration has problems: exiting");
            } else {
                logger.log(Level.INFO, "no repository configuration problem");
            }

            repository = engine.deploy(config);

            for (String s : repository.getDescriptorKeys()) {
                logger.log(Level.INFO,
                    "repository:descriptors:key={0}={1}",
                    new Object[]{s, repository.getDescriptorValue(s)}
                );
            }
            
            

            repoName = config.getName();
            logger.log(Level.INFO, "repo name:{0}", repoName);

            // projection.initialize();
            //session = repository.login("default");
            
            if (repository == null){
                logger.log(Level.SEVERE, "repository is still null: failed to initialize");
            } else {
                logger.log(Level.INFO, "repository is NOT null: initialization successful");
            }
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "FileNotFoundException was thrown in init()", ex);
        } catch (ParsingException ex) {
            logger.log(Level.SEVERE, "ParsingException was thrown in init()", ex);
        } catch (ConfigurationException ex) {
            logger.log(Level.SEVERE, "ConfigurationException was thrown in init()", ex);
        } catch (RepositoryException ex) {
            logger.log(Level.SEVERE, "RepositoryException was thrown in init()", ex);
        }
    }

    @PreDestroy
    public void repoEngineShutdown() {
        try {
//            if (session != null) {
//                session.logout();
//            }
            logger.log(Level.INFO, "Shutting down ModeShape:repoName={0}", repoName);
            Future<Boolean> future = engine.undeploy(repoName);
            if (future.get()) { // optional, but blocks until engine is completely
                logger.log(Level.INFO, "Shut down ModeShape:repoName={0}", repoName);
            }
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, "InterruptedException in repoEngineShutdown()", ex);
        } catch (ExecutionException ex) {
            logger.log(Level.SEVERE, "ExecutionException in repoEngineShutdown()", ex);
        } catch (NoSuchRepositoryException ex) {
            logger.log(Level.SEVERE, "NoSuchRepositoryException in repoEngineShutdown()", ex);
        }
    }
}
