package de.fraunhofer.iais.kd.biovel.shim.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import de.fraunhofer.iais.kd.biovel.shim.util.ShimDateConverter;

import au.com.bytecode.opencsv.CSVReader;

public class ShimTransformerAllCsv2Json {

    private static final Logger LOG = Logger.getLogger(ShimTransformerAllCsv2Json.class.getName());

    @SuppressWarnings("rawtypes")
    private Map valueMap;
    @SuppressWarnings("rawtypes")
    private Map tableMap;
    
   
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String transform(InputStream is, String occurenceid, String lat, String lon, String place, String time, ServletConfig config) {
        
        CSVReader reader = new CSVReader(new InputStreamReader(is));

        List<String[]> lines = null;
        try {
            lines = reader.readAll();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        List<String> csvHeader = Arrays.asList(lines.get(0));

        Integer idPos = null;
        if(occurenceid != null && occurenceid.length() > 0){
            idPos = csvHeader.indexOf(occurenceid);
//            csvHeader.set(idPos, "occurrenceID");
        }
        
        
        Integer latPos = null;
        if(lat != null && lat.length() > 0){
            latPos = csvHeader.indexOf(lat);
//            csvHeader.set(latPos, "decimalLatitude");
        }
        
        Integer lonPos = null;
        if(lon != null && lon.length() > 0){
            lonPos = csvHeader.indexOf(lon);
//            csvHeader.set(lonPos, "decimalLongitude");
        }
        
        Integer placePos = null;
        if(place != null && place.length() > 0){
//            System.out.println("place: " + place);
            placePos = csvHeader.indexOf(place);
//            System.out.println("placePos: " + placePos);
//            csvHeader.set(placePos, "place");
        }
        
        Integer timePos = null;
        if(time != null && time.length() > 0){
            timePos = csvHeader.indexOf(time);
//            csvHeader.set(timePos, "time");
        }
        
        int loop = 0;
        
        StringBuffer jsonResult = new StringBuffer();

        for (int j = lines.size(); j > 1; j--) {
            
            if (lines.get(j - 1).length == csvHeader.size()) {

                loop++;

                valueMap = new HashMap();
                tableMap = new HashMap();

                String tmp_lat = "";
                if (latPos != null) {
                    tmp_lat = lines.get(j - 1)[latPos].replace(",", ".");
                }

                String tmp_lon = "";
                if (lonPos != null) {
                    tmp_lon = lines.get(j - 1)[lonPos].replace(",", ".");
                }

                if (tmp_lat.length() > 0 && tmp_lon.length() > 0) {

                    Double dlat = null;
                    Double dlon = null;

                    try {
                        dlat = Double.parseDouble(tmp_lat);
                        dlon = Double.parseDouble(tmp_lon);
                    } catch (NumberFormatException e) {
///                        continue;
                    }

                    if (dlat != null || dlon != null) {
                        valueMap.put("lat", dlat);
                        valueMap.put("lon", dlon);
                    } else {
                        valueMap.put("lat", "");
                        valueMap.put("lon", "");                        
                    }
                } else {
                    valueMap.put("lat", "");
                    valueMap.put("lon", "");                     
                }

                if (idPos != null) {
                    valueMap.put("occurrenceID", lines.get(j - 1)[idPos]);
                } else {
                    valueMap.put("occurrenceID", "");
                }

                if (placePos != null) {
                    valueMap.put("place", lines.get(j - 1)[placePos]);
                } else {
                    valueMap.put("place", "");
                }

                if (timePos != null) {
                    String timeValue = new ShimDateConverter().convert(lines.get(j - 1)[timePos]);
                    valueMap.put("time", timeValue);
                } else {
                    valueMap.put("time", "");
                }

                for (int i = 0; i < csvHeader.size() - 1; i++) {
                    int ix = csvHeader.indexOf(csvHeader.get(i));
                    tableMap.put(csvHeader.get(i), lines.get(j - 1)[ix]);
                }

                valueMap.put("tableContent", tableMap);

                String jsonItem = createJson();
                jsonResult.append(",").append(jsonItem);

            } else {

                if (lines.get(j - 1).length > csvHeader.size()) {
                    LOG.warning("line " + (j - 1) + " number of row elements -" + lines.get(j - 1).length
                            + "- is greater then from header -" + csvHeader.size() + " -> line dropped");
                } else if (lines.get(j - 1).length < csvHeader.size()) {
                    LOG.warning("line " + (j - 1) + " number of row elements -" + lines.get(j - 1).length
                            + "- is smaller then from header -" + csvHeader.size() + " -> line dropped");
                }

            }
        }
        
        if(loop > 0)jsonResult.deleteCharAt(0);

        LOG.info(loop + "rows of " + (lines.size()-1) + "from csv was converted");

        return "["+jsonResult.toString()+"]";
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
}
