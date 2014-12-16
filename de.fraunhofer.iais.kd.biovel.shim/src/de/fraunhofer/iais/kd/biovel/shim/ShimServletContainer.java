package de.fraunhofer.iais.kd.biovel.shim;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * A jersey <code>ServletContainer</code> which sets up the REST resources and
 * configures the shim implementation according to the configuration file read.
 * The configuration file is named by an init-parameters as defined in web.xml.
 * A system property, for example given as VM argument, which names the
 * configuration file has precedence before the init-parameter.
 */
public class ShimServletContainer extends ServletContainer {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ShimServletContainer.class.getName());

    public static final String BIOSTIF_SERVER_CONF = "biostif.server.conf";
    public static final String GEOSERVER_URL = "GEOSERVER_URL";
    public static final String GEOSERVER_USER = "GEOSERVER_USER";
    public static final String GEOSERVER_PASSWD = "GEOSERVER_USER";
    public static final String GEOSERVER_RELOAD = "GEOSERVER_RELOAD";
    public static final String PUBLIC_WORKSPACE = "PUBLIC_WORKSPACE";
    public static final String DATA_URL = "DATA_URL";
    

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            initBase(this.getServletConfig());
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private void initBase(ServletConfig config) throws ServletException {
        String root = new File(new File("mmp.temp").getAbsolutePath()).getParentFile().getAbsolutePath();
        final String logClassInfo = "--- " + ShimServletContainer.class.getName();
        LOG.info(logClassInfo +".initBase(..) called in: " + root);

        String confFileName = null;
        if ((confFileName = System.getProperty(BIOSTIF_SERVER_CONF)) == null) {
            confFileName = config.getInitParameter(BIOSTIF_SERVER_CONF);
        }
        if (confFileName == null) {
            throw new ServletException(logClassInfo + " system-property and init-param \""
                    + BIOSTIF_SERVER_CONF + "\" not specified");
        }

        File confFile = new File(confFileName);
        if (!confFile.isAbsolute()) {
            confFile = new File(config.getServletContext().getRealPath("/"), confFileName);
        }
        if (!confFile.canRead()) {
            throw new ServletException(logClassInfo + " cannot read file: \"" + confFileName + "\" path: " + confFile.getAbsolutePath());
        }

        Properties props = new Properties();
        try {
            FileInputStream fis = new FileInputStream(confFile);
            props.load(fis);
        } catch (IOException e) {
            throw new ServletException(" cannot read properties: " + confFile + " : " + e.getMessage(), e);
        }

        config.getServletContext().setAttribute(BIOSTIF_SERVER_CONF, props);
        LOG.info(logClassInfo +".initBase(..) successful");
    }
}
