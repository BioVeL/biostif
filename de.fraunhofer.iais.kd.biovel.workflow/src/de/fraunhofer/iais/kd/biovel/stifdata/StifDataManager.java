package de.fraunhofer.iais.kd.biovel.stifdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sun.jersey.core.util.Base64;

import de.fraunhofer.iais.kd.biovel.common.BiovelHelper;
import de.fraunhofer.iais.kd.biovel.common.contract.Check;
import de.fraunhofer.iais.kd.biovel.util.WorkflowException;
import de.fraunhofer.iais.kd.biovel.util.WorkflowHelper;
import de.fraunhofer.iais.kd.biovel.util.WorkflowProperty;

public class StifDataManager {
    private static final Logger LOG = Logger.getLogger(StifDataManager.class.getName());

    private File stifDataRoot = null;

    private static final SimpleDateFormat ID_OF_TICKER_FORMAT = new SimpleDateFormat("HHmmss.S");

    private static final Object syncMakeDataFile = new Object();
    private static final Object syncMakeReservedFile = new Object();
    private Properties props;

    static final String SUFFIX_RESERVED = "_reserved";

    static final Pattern SUFFIX_PATTERN = Pattern.compile("[\\w-\\.]*");

    public static StifDataManager createByProperties(Properties props) {
        return new StifDataManager(props);
    }

    protected StifDataManager(Properties props) {
        this.props = props;
        String dirName = props.getProperty(WorkflowProperty.DATA_DIR.toString());
        this.stifDataRoot = WorkflowHelper.useAsWorkingDirectory(dirName);
        LOG.info("StifData directory: " + this.stifDataRoot.getAbsolutePath());
    }
    
    public Properties getProperties(){
        return this.props;
    }

    /**
     * a new data-URI is made for a data resource to be PUT within the users
     * workflow run. The URI is already reserved, but the data resource is not
     * available. GET data-URI will return 201 (accepted) until the resource is
     * PUT.
     * 
     * @param username not <code>null</code>, the user name
     * @param workflowRunId not <code>null</code>, the run id of the workflow
     * @param suffix to be used as suffix of the URI. suffix maybe null of
     *            empty, otherwise it must be composed only of alphanumeric or
     *            "_-." characters. if suffix == null then it is handled as
     *            being empty.
     * @return a (relative) data URI, not <code>null</code>.
     */
    public String makeNewDataResourceUri(String username, String workflowRunId, String userLayerName, String suffix) {
        
//        System.out.println("SDM: " + username + " - " + workflowRunId + " - " +userLayerName + " - "+ suffix);
        
        File workflowPath = new File(username, workflowRunId);
        final File runDir = new File(this.stifDataRoot, workflowPath.getPath());
        if (!runDir.exists()) {
            if (!runDir.mkdirs()) {
                throw new WorkflowException("cannot create run directory: " + runDir.getAbsolutePath());
            }
        }
        if (!(runDir.canRead() && runDir.canWrite())) {
            throw new WorkflowException("cannot read and write run directory: " + runDir.getAbsolutePath());
        }
        if (suffix == null) {
            suffix = "";
        }
        checkResourceSuffix(suffix);

        File resultResource = null;
        synchronized (StifDataManager.syncMakeReservedFile) {
            for (int n = 3; (n >= 0) && (resultResource == null); n--) {
                final String filename = userLayerName + WorkflowHelper.idOfCurrentTime() + suffix;
//                System.out.println("SDM filename: " + filename);
                File fileResource = new File(workflowPath, filename);
                File localFileResource = new File(this.stifDataRoot, fileResource.getPath());
                File localFileReserved = new File(this.stifDataRoot, fileResource.getPath() + SUFFIX_RESERVED);
                try {
                    if (localFileReserved.createNewFile()) {
                        if (localFileResource.createNewFile()) {
                            resultResource = fileResource;
                        } else {
                            LOG.info("cannot create new file resource: " + localFileReserved.getAbsolutePath());
                            localFileReserved.delete();
                        }
                    } else {
                        LOG.info("cannot create new file reserved: " + filename);
                    }
                } catch (Exception exn) {
                    fileResource = null;
                    LOG.log(Level.SEVERE, localFileResource.getAbsolutePath() + " or .." + SUFFIX_RESERVED, exn);
                    try {
                        if (localFileReserved != null) {
                            localFileReserved.delete();
                        }
                        if (localFileResource != null) {
                            localFileResource.delete();
                        }
                    } catch (Exception exn2) {
                        LOG.log(Level.SEVERE, "exception in catch body when deleting files", exn2);
                    }
                }
                if ((resultResource == null) && (n > 0)) {
                    LOG.info("wait 10ms and try again");
                    WorkflowHelper.sleep(10L);
                }
            }
            if (resultResource == null) {
                String msg = "create new resource failed";
                LOG.severe(msg);
                throw new WorkflowException(msg);
            }
        }
        Check.notNull(resultResource, "result is missing");
        return resultResource.getPath().replace(File.separatorChar, '/');
    }

    public void putResource(String resourceName, String content) {
        
        String fileName = resourceName;
        File dataFile = new File(this.stifDataRoot, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dataFile);
            fos.write(content.getBytes("UTF-8"));
        } catch (IOException exn) {
            throw new WorkflowException("Cannot write " + dataFile.getAbsolutePath(), exn);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException exn) {
                    throw new WorkflowException("2nd level exception, cannot close output stream: "
                            + dataFile.getAbsolutePath(), exn);
                }
            }
        }
        File dataFileReserved = new File(this.stifDataRoot, fileName + SUFFIX_RESERVED);
        if (dataFileReserved.exists()) {
            dataFileReserved.delete();
        }
    }

    public void putResource(String resourceName, InputStream is) {
        
        String fileName = resourceName;
        File dataFile = new File(this.stifDataRoot, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dataFile);
            byte[] bytes = new byte[1024];
            int read = 0;
            while ((read = is.read(bytes)) != -1) {
                fos.write(bytes, 0, read);
            }
        } catch (IOException exn) {
            throw new WorkflowException("Cannot write " + dataFile.getAbsolutePath(), exn);
        } finally {
            try {
                is.close();
            } catch (IOException exn) {
                throw new WorkflowException("2nd level exception, cannot close input stream: "
                        + dataFile.getAbsolutePath(), exn);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException exn) {
                        throw new WorkflowException("2nd level exception, cannot close output stream: "
                                + dataFile.getAbsolutePath(), exn);
                    }
                }
            }
        }
        File dataFileReserved = new File(this.stifDataRoot, fileName + SUFFIX_RESERVED);
        if (dataFileReserved.exists()) {
            dataFileReserved.delete();
        }
    }

    String getResourceAsText(String filename) {
        File dataFile = new File(this.stifDataRoot, filename);
        String result = BiovelHelper.readTextContentFromFile(dataFile);
        return result;
    }

    /**
     * @param filename
     * @return an InputStream to read the file
     * @throws FileNotFoundException
     */
    public InputStream getResourceInputStream(String filename) throws FileNotFoundException {
        File dataFile = new File(this.stifDataRoot, filename);
        InputStream result = new FileInputStream(dataFile);
        return result;
    }

    /**
     * @param filename
     * @return local resource URI file:... for filename or <code>null</code> if
     *         file does not exist.
     */
    public String getLocalResourceFileURI(String filename) {
        File dataFile = new File(this.stifDataRoot, filename);
        String fileURI = dataFile.toURI().toString();
        if (dataFile.exists()) {
            return fileURI;
        } else {
            return null;
        }
    }

    /**
     * is data resource acceptable, but not yet available.
     * 
     * @param uri
     * @return <code>true</code> if data resource acceptable, but not yet
     *         available, <code>false</code> otherwise.
     */
    public boolean isResourceReserved(String uri) {
        File dataFileReserved = new File(this.stifDataRoot, uri + SUFFIX_RESERVED);
        return dataFileReserved.exists();
    }

    /**
     * is data resource available to GET it.
     * 
     * @param uri
     * @return <code>true</code> if resource data available to GET it,
     *         <code>false</code> otherwise.
     */
    public boolean isResourceStored(String uri) {
        File dataFile = new File(this.stifDataRoot, uri);
        return dataFile.exists() && !isResourceReserved(uri);
    }

    /**
     * check if the suffix is valid to be used as name part within an URI.
     * 
     * @param suffix
     * @throws WorkflowException if suffix is not acceptable
     */
    private void checkResourceSuffix(String suffix) {
        if (!StifDataManager.SUFFIX_PATTERN.matcher(suffix).matches()) {
            throw new WorkflowException("suffix \"" + suffix + "\"" + " does not conform to pattern \""
                    + SUFFIX_PATTERN.pattern() + "\"");
        }
    }

    public String makxeNewDataResource(String suffix) {
        checkResourceSuffix(suffix);

        String filename = null;
        synchronized (StifDataManager.syncMakeDataFile) {
            filename = WorkflowHelper.idOfCurrentTime() + suffix;
            File dataFile = new File(this.stifDataRoot, filename);
            try {
                if (!dataFile.createNewFile()) {
                    WorkflowHelper.sleep(2L);
                    filename = WorkflowHelper.idOfCurrentTime();
                    dataFile = new File(this.stifDataRoot, filename);
                    if (!dataFile.createNewFile()) {
                        throw new WorkflowException("cannot create new data file: " + dataFile.getAbsolutePath());
                    }
                }
            } catch (IOException exn) {
                throw new WorkflowException("create new data file failed: " + dataFile.getAbsolutePath(), exn);
            }
        }
        Check.notNull(filename, "result is missing");
        return filename;
    }

    final static String USERNAME_KEY = "username";
    final static String RUNID_KEY = "runid";

    public String makeCredentials(String username, String runid) {
        Properties props = new Properties();
        props.put(USERNAME_KEY, username);
        props.put(RUNID_KEY, runid);
        StringWriter sw = new StringWriter();
        try {
            props.store(sw, "stifdata workflow credentials");
        } catch (IOException exn) {
            throw new WorkflowException("failed to make credentials", exn);
        }
        String credentials = sw.toString();
        //string instantiation from a byte[] is exactly what is wanted here -> do no touch
        String result = new String(Base64.encode(credentials));
        return result;
    }

    public String getCredentialsUsername(String credentialsEncoded) {
        String result = getElementOfCredentials(credentialsEncoded, USERNAME_KEY);
        return result;
    }

    public String getCredentialsRunId(String credentialsEncoded) {
        String result = getElementOfCredentials(credentialsEncoded, RUNID_KEY);
        return result;
    }

    private String getElementOfCredentials(String credentialsEncoded, String elementKey) {
        String credentialsDecoded = new String(Base64.decode(credentialsEncoded));
        StringReader credentialsReader = new StringReader(credentialsDecoded);
        Properties credentials = new Properties();
        try {
            credentials.load(credentialsReader);
        } catch (IOException exn) {
            throw new WorkflowException(exn);
        }
        return credentials.getProperty(elementKey);
    }

}
