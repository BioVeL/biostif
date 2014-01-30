package de.fraunhofer.iais.kd.biovel.workflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.sun.jersey.spi.container.servlet.ServletContainer;

import de.fraunhofer.iais.kd.biovel.feed.FeedManager;
import de.fraunhofer.iais.kd.biovel.stifdata.StifDataManager;
import de.fraunhofer.iais.kd.biovel.util.WorkflowHelper;

public class WorkflowServletContainer extends ServletContainer {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(WorkflowServletContainer.class.getName());
    
    private ServletConfig config;

    /**
     * used as filename and as optional key into System-properties to access a
     * configuration file
     */
    public static final String BIOSTIF_SERVER_CONF = "biostif.server.conf";

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            config = this.getServletConfig();
            LOG.info("--- WorkflowServletContainer.initBase(..) called in: " + WorkflowHelper.currentWorkingDirectory());

            String confFileName = null;
            if ((confFileName = System.getProperty(BIOSTIF_SERVER_CONF)) == null) {
                confFileName = config.getInitParameter(BIOSTIF_SERVER_CONF);
            }
            if (confFileName == null) {
                throw new ServletException("--- WorkflowServletContainer init-param " + BIOSTIF_SERVER_CONF
                        + " not specified");
            }

            String workingDirPath = config.getServletContext().getRealPath("/");
            File confFile = new File(confFileName);
//            if (!confFile.isAbsolute()) {
//                confFile = new File(workingDirPath, confFileName);
//            }

            Properties props = readConfProperties(confFile);

            baseInit(props);

        } catch (Throwable e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException();
        }
    }

    Properties readConfProperties(File confFile) throws ServletException {
        if (!confFile.canRead()) {
            throw new ServletException("--- WorkflowServletContainer cannot read file: \"" + confFile.getAbsolutePath()
                    + "\"");
        }
        Properties props = new Properties();
        try {
            FileInputStream fis = new FileInputStream(confFile);
            props.load(fis);
        } catch (IOException e) {
            LOG.info(" cannot read properties: " + confFile + " : " + e.getMessage());
            throw new ServletException(" cannot read properties: " + confFile + " : " + e.getMessage(), e);
        }
        return props;
    }

    void baseInit(Properties props) throws ServletException {

        StifDataManager stifDataManager = StifDataManager.createByProperties(props);
        FeedManager feedManager = new FeedManager(props);

        StifDataResource.putManager(stifDataManager);
        FeedResource.putManager(feedManager);

        LOG.info("--- WorkflowServletContainer.initBase(..) successful");
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
