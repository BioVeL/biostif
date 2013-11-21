package de.fraunhofer.iais.kd.biovel.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import de.fraunhofer.iais.kd.biovel.common.contract.ApplicationException;

public class BiovelHelper {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(BiovelHelper.class.getName());

    public static void ttt() {
    }

    public static String readTextContentFromFile(File dataFile) {
        FileInputStream fis = null;
        BufferedReader reader = null;
        StringBuilder sb;
        try {
            fis = new FileInputStream(dataFile);
            InputStreamReader in = new InputStreamReader(fis, "UTF-8");
            reader = new BufferedReader(in);
            sb = new StringBuilder(1000);
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                sb.append(readData);
                buf = new char[1024];
            }
        } catch (Exception exn) {
            throw new ApplicationException("cannot get data resource: " + dataFile.getAbsolutePath(), exn);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException exn) {
                throw new ApplicationException("2nd level exception, cannot close data resource "
                        + dataFile.getAbsolutePath(), exn);

            }
        }
        String result = sb.toString();
        return result;
    }

}
