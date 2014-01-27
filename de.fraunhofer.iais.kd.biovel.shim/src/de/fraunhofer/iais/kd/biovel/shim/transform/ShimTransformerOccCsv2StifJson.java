package de.fraunhofer.iais.kd.biovel.shim.transform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import au.com.bytecode.opencsv.CSVReader;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import de.fraunhofer.iais.kd.biovel.shim.ShimServletContainer;
import de.fraunhofer.iais.kd.biovel.shim.util.ShimDateConverter;

public class ShimTransformerOccCsv2StifJson {

    private static final Logger LOG = Logger.getLogger(ShimTransformerOccCsv2StifJson.class.getName());

    private Client client;

    @SuppressWarnings("rawtypes")
    private Map valueMap;
    @SuppressWarnings("rawtypes")
    private Map descriptionMap;
    @SuppressWarnings("rawtypes")
    private Map tableMap;
    
    String popuplabel = "";
    String time = "";
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String transform(InputStream is, String popuplabel, String time, ServletConfig config) {
        
        if(popuplabel != null){
            this.popuplabel = popuplabel;
        }
        if(time != null){
            this.time = time;
        }

        CSVReader reader = new CSVReader(new InputStreamReader(is));

        List<String[]> lines = null;
        try {
            lines = reader.readAll();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        boolean docHeader = true;
        List<String> firstLine = Arrays.asList(lines.get(0));
        if(firstLine.indexOf("decimalLatitude") == -1){
            docHeader = false;
        }       
        
        List<String> csvHeader = null;
        
        if(!docHeader){
            
            csvHeader = firstLine;
        
        }else{
            
            // read CSV Header (von Cherian)
            Properties props = (Properties) config.getServletContext().getAttribute(ShimServletContainer.BIOSTIF_SERVER_CONF);
            String urlCsvHeader = props.getProperty("URL_CSV_HEADER");
            
            if(urlCsvHeader == null || urlCsvHeader.length() == 0){
                urlCsvHeader = props.getProperty("URL_CSV_HEADER_LOCAL");
            }

            URL url;
            URLConnection conn = null;
            try {
                url = new URL(urlCsvHeader);
                conn = url.openConnection();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            System.out.println("conn.getContentLength(): " + conn.getContentLength());
             
            if(conn.getContentLength() < 1){
                
                LOG.info("SHIM OCC_Csv2Json: get the local URL_CSV_HEADER");
                urlCsvHeader = props.getProperty("URL_CSV_HEADER_LOCAL");
                
                try {
                    url = new URL(urlCsvHeader);
                    conn = url.openConnection();
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            if(conn.getContentLength() < 1){
                return"ERROR: please check the value of the URL_CSV_HEADER on biostif.server.config file";
            }
            
            try (InputStream isc = conn.getInputStream();) {

                try (BufferedReader isBufferedReader = new BufferedReader(new InputStreamReader(isc));) {

                    String line = "";
                    while ((line = isBufferedReader.readLine()) != null) {
                        csvHeader = Arrays.asList(line.split(","));
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        int latPos = csvHeader.indexOf("decimalLatitude");
        int lonPos = csvHeader.indexOf("decimalLongitude");
        
        int idPos = csvHeader.indexOf("occurrenceID");
        int loop = 0;
        int firstline = 0;

        if (idPos < 0) {
            try {
                throw new ParserConfigurationException("No ID field found");
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        if (latPos < 0) {
            try {
                throw new ParserConfigurationException("No field with information of latitude coordinate found");
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        if (lonPos < 0) {
            try {
                throw new ParserConfigurationException("No field with information of longitude coordinate found");
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        if(docHeader){
            //document has a header
            firstline = 1;          
        }
        
        StringBuffer jsonResult = new StringBuffer();

        for (int j = lines.size(); j > firstline; j--) {
            
            if(lines.get(j - 1).length == csvHeader.size()){
                
                String id = lines.get(j - 1)[idPos];
                        
                String lat = lines.get(j - 1)[latPos].replace(",", ".");
                String lon = lines.get(j - 1)[lonPos].replace(",", ".");
                
//              if(lat.indexOf("n") > 0 || lat.indexOf("n") > 0
//                      || lat.indexOf("W") > 0 || lat.indexOf("W") > 0 
//                      || lat.indexOf("-") > 0 || lon.indexOf("-") > 0
////                        || (lat.equals("0")  && lon.equals("0"))
////                        || (lat.equals("0.0")&& lon.equals("0.0"))
//                      ){
//                  
//                  lat = "";
//                  lon = "";
//              }
                
                try{
                    Double.parseDouble(lat);
                    Double.parseDouble(lon);
                } catch (Exception e) {
                    
                    lat = "";
                    lon = "";
                    
                }
                
                if (lat.length() > 0 && lon.length() > 0 ) {
                    
                    double dlat = 0;
                    double dlon = 0;
                    
                    try {
                        dlat = Double.parseDouble(lat);
                        dlon = Double.parseDouble(lon);
                    } catch (NumberFormatException e) {
                        LOG.info("Row number " + (j - 1) + " with no valid lat lon information with occurrenceID: " + id + " will not be converted");
                        continue;
                    }
                    
    
                    loop++;
    
                    valueMap = new HashMap();
                    descriptionMap = new HashMap();
                    tableMap = new HashMap();
    
                    // collect values from CSV line
                    valueMap.put("lat", dlat);
                    valueMap.put("lon", dlon);
    
                    addLineValuesToMap(csvHeader, lines.get(j - 1));
    
                    String description = createDescription();
                    valueMap.put("description", description);
                    valueMap.put("tableContent", tableMap);
    
                    String jsonItem = createJson();
                    jsonResult.append(",").append(jsonItem);
                }
                
            } else{
                
                if(lines.get(j - 1).length > csvHeader.size()){
                    LOG.warning("line "+ (j - 1) + " number of row elements -" + lines.get(j - 1).length+ "- is greater then from header -" + csvHeader.size() + " -> line dropped");
                }else if(lines.get(j - 1).length < csvHeader.size()){
                    LOG.warning("line "+ (j - 1) + " number of row elements -" + lines.get(j - 1).length+ "- is smaller then from header -" + csvHeader.size() + " -> line dropped");
                }
                
            }
        }
        
        if(loop > 0)jsonResult.deleteCharAt(0);

        LOG.info(loop + "rows of " + (lines.size()-1) + "from csv was converted");

        return "["+jsonResult.toString()+"]";
    }
    
    @SuppressWarnings("unchecked")
    private void addLineValuesToMap(List<String> csvHeader, String[] csvLine) {

        List<String> valueMapElements = Arrays.asList("occurrenceid", "latestdatecollected", "earliestdatecollected");
        List<String> descriptionMapElements = Arrays.asList("namecomplete", "dataprovidername", "occurrenceid", "earliestdatecollected", "latestdatecollected", "dataresourcerights");

        String placeValue = "";
        if(popuplabel.length() == 0){
            placeValue = "country";
        } else {
            placeValue = popuplabel;
        }
        
        for (int i = 0; i < csvHeader.size() - 1; i++) {
            
            int ix = csvHeader.indexOf(csvHeader.get(i));
            
            if (csvHeader.get(i).toLowerCase().equals(placeValue.toLowerCase())) {
                valueMap.put("place", csvLine[ix]);
            }
            
            if (valueMapElements.contains(csvHeader.get(i).toLowerCase())) {
                
                if (csvHeader.get(i).toLowerCase().equals("occurrenceid")) {
                    valueMap.put("id", csvLine[ix]);
                }
                
                if (time.length() > 0 && csvHeader.get(i).toLowerCase().equals(time.toLowerCase()) ) {
                    if(csvLine[ix].length() > 0){
                        
                        System.out.println("1 time: " + csvLine[ix]);
                        String timeValue = new ShimDateConverter().convert(csvLine[ix]);
                        
                        valueMap.put("time", timeValue);
                    } else {
                        valueMap.put("time", "");
                    }
                }
                if (time.length() == 0 && csvHeader.get(i).toLowerCase().equals("latestdatecollected")) {
                                
                    if(csvLine[ix].length() > 0){
                        
                        System.out.println("2 time: " + csvLine[ix]);
                        String timeValue = new ShimDateConverter().convert(csvLine[ix]);
                        
                        System.out.println("value after: " + timeValue + "\n");
                        
                        valueMap.put("time", timeValue);
                    } else {
                        valueMap.put("time", "");
                    }                                           
                }
                
                if (time.length() == 0 && csvHeader.get(i).toLowerCase().equals("earliestdatecollected") &&
                        (valueMap.get("time") == null || valueMap.get("time").toString().length() == 0)) {
                    if(csvLine[ix].length() > 0){
                        
                        System.out.println("3 time: " + csvLine[ix]);
                        String timeValue = new ShimDateConverter().convert(csvLine[ix]);
                        
                        System.out.println("value after: " + timeValue + "\n");
                        
                        valueMap.put("time", timeValue);
                    } else {
                        valueMap.put("time", "");
                    }
                }
            }

            if (descriptionMapElements.contains(csvHeader.get(i).toLowerCase())) {
                
                descriptionMap.put(csvHeader.get(i), csvLine[ix]);  
                
            }
            tableMap.put(csvHeader.get(i), csvLine[ix]);
        }

    }
    
    private String createJson() {

        String json = "";

        try {

            ObjectMapper mapper = new ObjectMapper();
            if(valueMap != null)json = mapper.writeValueAsString(valueMap);

        } catch (JsonGenerationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return json;
    }
    
    private String createDescription() {

        String description = "";

        description = "<p><table><tr>"
                + "<th>TaxonName</th>"
                + "<th>OccurrenceID</th>"
                + "<th>DataProvider</th>"
                + "<th>earliestDateCollected</th>"
                + "<th>latestDateCollected</th>"
                + "<th>dataResourceRights</th>"
                + "</tr>"
                + "<tr>"
                + "<td>";
        
                
            if(descriptionMap.get("nameComplete") != null){
                description += descriptionMap.get("nameComplete").toString().replace('"', '\"');
            } else {
                description +="";
            }
        
            description+= "</td>"
                + "<td>";
            
            if(descriptionMap.get("occurrenceID") != null){
                description += descriptionMap.get("occurrenceID").toString().replace('"', '\"');
            } else {
                description +="";
            }
        
            description+= "</td>"
                + "<td>";
                    
            if(descriptionMap.get("dataProviderName") != null){
                description += descriptionMap.get("dataProviderName").toString().replace('"', '\"');
            } else {
                description +="";
            }
            
            description+= "</td>"
                    + "<td>";
                        
            if(descriptionMap.get("earliestDateCollected") != null){
                description += descriptionMap.get("earliestDateCollected").toString().replace('"', '\"');
            } else {
                description +="";
            }
                
            description+= "</td>"
                    + "<td>";
                            
            if(descriptionMap.get("latestDateCollected") != null){
                description += descriptionMap.get("latestDateCollected").toString().replace('"', '\"');
            } else {
                description +="";
            }
            
            description+= "</td>"
                    + "<td>";
                            
            if(descriptionMap.get("dataResourceRights") != null){
                description += descriptionMap.get("dataResourceRights").toString().replace('"', '\"');
            } else {
                description +="";
            }

            description += "</td></tr></table></p>";

        return description;
    }
}
