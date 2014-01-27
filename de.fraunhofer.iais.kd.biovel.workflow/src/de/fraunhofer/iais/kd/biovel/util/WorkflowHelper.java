package de.fraunhofer.iais.kd.biovel.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import de.fraunhofer.iais.kd.biovel.common.contract.Check;

public class WorkflowHelper {
    private static final Logger LOG = Logger.getLogger(WorkflowHelper.class.getName());

    private static final SimpleDateFormat ID_OF_CURRENT_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

    /**
     * returns a path and after assuring, that this path denotes a directory for
     * reading and writing.
     * 
     * @param workingDirPath
     * @return a path, not null.
     */
    public static File useAsWorkingDirectory(String workingDirName) {
        Check.notNull(workingDirName, "directory name expected:" + workingDirName);
        File resultPath = new File(workingDirName);
        final String pathString = resultPath.getAbsolutePath();
        Check.isTrue(resultPath.isDirectory(), "not a directory: " + pathString);
        Check.isTrue(resultPath.canRead(), "cannot read: " + pathString);
        Check.isTrue(resultPath.canWrite(), "cannot write: " + pathString);
        LOG.info("using working_dir_path: " + pathString);
        return resultPath;
    }

    public static String currentWorkingDirectory() {
        return new File(new File("mmp.temp").getAbsolutePath()).getParentFile().getAbsolutePath();
    }

    /**
     * provides a String, showing the current time at a resolution of 1ms. This
     * String may be used as part of an identifier or filename. The String is
     * made of digits and underscores only. The first and the last character are
     * digits.
     * 
     * @return a String showing the current time, usable as part of identifiers.
     */
    public static String idOfCurrentTime() {
        return ID_OF_CURRENT_TIME_FORMAT.format(new Date());
    }

    /**
     * holds the current thread in sleep mode for <code>milliSec</code>.
     * <p>
     * InterruptedException-s are not expected.
     * 
     * @param milliSec
     * @throws WorkspaceException if an InterruptedException occurs.
     */
    public static void sleep(long milliSec) {
        try {
            Thread.sleep(milliSec);
        } catch (InterruptedException e) {
            throw new WorkflowException("interrupt not expected", e);
        }
    }

    /**
     * The contents of <code>inStream</code> are copied into file
     * <code>copy</code>. <code>inStream</code> is closed.
     * 
     * @param inStream contents are copied
     * @param copy
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void storeInto(InputStream inStream, File copy) throws FileNotFoundException, IOException {
        OutputStream outStream = new FileOutputStream(copy);
    
        try {
            byte[] buffer = new byte[1024];
            int readLen = 0;
            while ((readLen = inStream.read(buffer)) >= 0) {
                outStream.write(buffer, 0, readLen);
            }
        } finally {
            try {
                outStream.close();
            } finally {
                inStream.close();
            }
        }
    }

    public static void copyFile(File original, File copy) throws FileNotFoundException, IOException {
        InputStream inStream = new FileInputStream(original);
        storeInto(inStream, copy);
    }

}
