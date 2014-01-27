package de.fraunhofer.iais.kd.biovel.shim.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

public class ShimTransformerSlwCsv2OccCsv {
    
    private static final Logger LOG = Logger.getLogger(ShimTransformerSlwCsv2OccCsv.class.getName());


    /**
     * @param dwcInputStream darwin core data to be transformed to csv format
     * @param xslFile the xslt program
     * @return String result
     */
    public String transform(InputStream is) {

        String[] firstline = null;
        
        char separator = ',';
        char quotechar = '"';
        
        CSVReader reader = new CSVReader(new InputStreamReader(is), separator, quotechar);
//        CSVReader reader = new CSVReader(new InputStreamReader(is), separator);

        try {
            firstline = reader.readNext();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        String[] firstline_new = new String[firstline.length];
        
        for(int i = 0; i < firstline.length; i++){
            
            String term = firstline[i];
            
             switch(term){
                
                case "scientificName":
                    term = "nameComplete";
                    break;                
                case "scientificNameAuthorship":
                    term = "authorship";
                    break;                
                case "specificEpithet":
                    term = "specificEpithet";
                    break;
                case "infraspecificEpithet":
                    term = "infraspecificEpithet";
                    break;
                case "start":
                    term = "earliestDateCollected";
                    break;
                case "stop":
                    term = "latestDateCollected";
                    break;
                case "identifiedBy":
                    term = "collector";
                    break;
                case "recordedBy":
                    term = "dataProviderName";
                    break;
                case "occurrenceId":
                    term = "occurrenceID";
                    break;
            }
             
             firstline_new[i]=term;
            
        }
        
//        System.out.println("fistrline.len: " + firstline.length + " newfirst.length: " + firstline_new.length);
        
        StringBuffer result = new StringBuffer();
        
        String stringFirstLine = StringUtils.join(firstline_new, ",");
        result.append(stringFirstLine).append("\n");
        
        int loop = 0;
        String[] nextLine = null;
        try {
            while ((nextLine = reader.readNext()) != null) {
                
                String[] newNextLine = new String[nextLine.length];
                
                for(int tt = 0; tt<nextLine.length;tt++){
                    
                        if(nextLine[tt].contains(",")){
                            String line = '"'+nextLine[tt] + '"';
                            newNextLine[tt] = line;                         
                        } else {
                            newNextLine[tt] = nextLine[tt];
                        }               
                    }
                
                String stringLine = StringUtils.join(newNextLine, ",");
                result.append(stringLine).append("\n");
                loop++;
                
//                System.out.println("\n" + loop + " nextline.len: "+ nextLine.length + " newnext.len: " + newNextLine.length + "\n nl: "+stringLine);
                
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String msg = "SLW2CSV has added '"+ loop +"' rows";
        LOG.info(msg);
        return result.toString();
    }

}
