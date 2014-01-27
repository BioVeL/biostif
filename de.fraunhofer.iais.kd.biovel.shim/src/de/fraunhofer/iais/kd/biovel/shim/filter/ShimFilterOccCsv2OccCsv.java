package de.fraunhofer.iais.kd.biovel.shim.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;

import org.apache.commons.lang3.StringUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import de.fraunhofer.iais.kd.biovel.shim.ShimServletContainer;

import au.com.bytecode.opencsv.CSVReader;

public class ShimFilterOccCsv2OccCsv {
    
    private static final Logger LOG = Logger.getLogger(ShimFilterOccCsv2OccCsv.class.getName());

    private Client client;

    /**
     * @param dwcInputStream darwin core data to be transformed to csv format
     * @param xslFile the xslt program
     * @return String result
     */
    public String filter(ServletConfig config, InputStream fileStream, InputStream iDs) {
        
        
        //read filterList
        BufferedReader fisBufferedReader = new BufferedReader(new InputStreamReader(iDs));
        String filterString = "";
        String fline;

        try {
            while ((fline = fisBufferedReader.readLine()) != null) {
                filterString += fline;
            }

            fisBufferedReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<String> filterList = Arrays.asList(filterString.split(","));
        
        // read CSV Header (von Cherian)
        Properties props = (Properties) config.getServletContext().getAttribute(ShimServletContainer.BIOSTIF_SERVER_CONF);
        String urlCsvHeader = props.getProperty("URL_CSV_HEADER");
        
        if(urlCsvHeader == null || urlCsvHeader.length() == 0){
            urlCsvHeader = "http://localhost:8080/biostif/data/csvHeader_beta.txt";
        }

        List<String> csvHeader = null;      
        String[] firstline = null;
        CSVReader reader = null;
        
        this.client = Client.create();        
        ClientResponse cr =
                this.client.resource(urlCsvHeader).header("Accept-Charset", "UTF-8").get(ClientResponse.class);
        InputStream entity = cr.getEntityInputStream();
        BufferedReader isBufferedReader = new BufferedReader(new InputStreamReader(entity));
            
            String line = "";
            
            try {
                while ((line = isBufferedReader.readLine()) != null) {
                    csvHeader = Arrays.asList(line.split(","));
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
//                e1.printStackTrace();
                String msg = "Filter OCC_CSV - Cannot read Stream from Request";
                LOG.info(msg);
            }
        
            
//           -------------- 
//        try {
//            URL url = new URL(urlCsvHeader);
//            URLConnection conn = url.openConnection();
//            InputStream isc = conn.getInputStream();
//            BufferedReader isBufferedReader = new BufferedReader(new InputStreamReader(isc));
//            
//            String line = "";
//            
//            while ((line = isBufferedReader.readLine()) != null) {
//                csvHeader = Arrays.asList(line.split(","));
//            }
//            
//            isBufferedReader.close();
//            isc.close();
//            
//            char separator = ',';
//            char quotechar = '"';
//            
//            reader = new CSVReader(new InputStreamReader(fileStream), separator, quotechar);
//            firstline = reader.readNext();
//            
//        } catch (MalformedURLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }       
        
        List<String[]> lines = new LinkedList<String[]>();
        
        int idx = csvHeader.indexOf( "occurrenceID" );
        int loop = 0;
        boolean docHeader = true;
        List<String> firstLine = Arrays.asList(firstline);
        if(firstLine.indexOf("decimalLatitude") == -1){
            docHeader = false;
            
            loop++;
            
            String idValue = firstline[idx];
            if(filterList.contains(idValue)){
                String[] newFirstLine = new String[firstline.length];
                
                for(int tt = 0; tt<firstline.length;tt++){
                    
                        if(firstline[tt].contains(",")){
                            String tmpLine = '"'+firstline[tt] + '"';
                            newFirstLine[tt] =tmpLine;                         
                        } else {
                            newFirstLine[tt] =firstline[tt];
                        }               
                    }
                lines.add(newFirstLine);    
            }
            
        }
        
        if (firstLine.indexOf("decimalLatitude") < 0) {
            
            LOG.info("Apparently no header with decimalLatitude column " + 
                     firstline + " -> get headers from repository document");
        } else {
            csvHeader = firstLine;
            idx = csvHeader.indexOf( "occurrenceID" );
            LOG.info("Header from document: ");
        }
        
        
        String[] nextLine = null;
        int ixID = 0;
        
        try {
            while ((nextLine = reader.readNext()) != null) {
                
                loop++;
                
                String idValue = nextLine[idx];

                if(idValue.length() == 0){
                    ixID++;
                } else {
                
                    if(filterList.contains(idValue)){
                        
                        String[] newNextLine = new String[nextLine.length];
                        
                        for(int tt = 0; tt<nextLine.length;tt++){
                            
                                if(nextLine[tt].contains(",")){
                                    String tmpLine = '"'+nextLine[tt] + '"';
                                    newNextLine[tt] = tmpLine;                         
                                } else {
                                    newNextLine[tt] = nextLine[tt];
                                }               
                            }
                        lines.add(newNextLine);                 
                    }
                }
            }
        
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        LOG.info("Filterresult: # lines:" + loop + " filtered lines: " + lines.size());
        LOG.info("Filterresult: not avaiable occourenceIDs in document: " + ixID);
        
        StringBuffer filterResult = new StringBuffer();

        if(lines.size() == 0){
            
            filterResult.append("Error while filtering: "+lines.size() + " from " + loop + " lines filtered, because " + ixID + " occurrenceIDs was not foundet in the document");
            
        } else {
        
            if(docHeader){
                //document has a header
                String stringFirstLine = StringUtils.join(firstLine, ",");
                filterResult.append(stringFirstLine).append("\n");
            }
            
            for (int j = lines.size(); j > 0; j--) {
                
    //          for(int tt = 0; tt<lines.get(j - 1).length;tt++){
    //          System.out.println("- " + lines.get(j - 1)[tt] +"\n");
    //          }
                
                String stringLine = StringUtils.join(lines.get(j-1), ",");
                filterResult.append(stringLine).append("\n");
                
    //          System.out.println(stringLine);
            }
        }
        
        return filterResult.toString(); 

    }

}
