package de.fraunhofer.iais.kd.biovel.shim.transform;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.fraunhofer.iais.kd.biovel.shim.util.ShimDateConverter;

public class ShimTransformerDwc2StifJson {

    @SuppressWarnings("rawtypes")
    private Map valueMap;
    @SuppressWarnings("rawtypes")
    private Map descriptionMap;
    @SuppressWarnings("rawtypes")
    private Map tableMap;

    String time = "";

    /**
     * @param dwcInputStream darwin core data to be transformed to csv format
     * @param xslFile the xslt program
     * @return String result
     */
    @SuppressWarnings("unchecked")
    public String transform(InputStream is, String time) {

        if(time != null){
            this.time = time;
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = null;
        Document doc = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(is);
        } catch (ParserConfigurationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        NodeList nodeList = null;

        nodeList = doc.getElementsByTagNameNS("*", "TaxonOccurrence");
        System.out.println("\n--------- \n# nodes: " + nodeList.getLength());

        int loop = 0;

        StringBuffer jsonResult = new StringBuffer();

        if (nodeList.getLength() > 0) {

            int nodeListLength = nodeList.getLength();

            for (int i = 0; i < nodeListLength; ++i) {
                
                Node node = nodeList.item(i);

                if (node.hasChildNodes()) {

                    String lat = getChildValue(node, "decimalLatitude").replace(",", ".");
                    String lon = getChildValue(node, "decimalLongitude").replace(",", ".");

                    try {
                        Double.parseDouble(lat);
                        Double.parseDouble(lon);
                    } catch (Exception e) {

                        lat = "";
                        lon = "";

                    }

//                      if(lat.indexOf("n") > 0 || lat.indexOf("n") > 0
//                              || lat.indexOf("W") > 0 || lat.indexOf("W") > 0 
//                              || lat.indexOf("-") > 0 || lon.indexOf("-") > 0
////                                || (lat.equals("0")  && lon.equals("0"))
////                                || (lat.equals("0.0")&& lon.equals("0.0"))
//                              ){
//                          
//                          lat = "";
//                          lon = "";
//                      }

                    if (lat.length() > 0 && lon.length() > 0) {
                        
                        loop++;
                        addNodeValuesToMap(node);

                        String description = createDescription();
                        valueMap.put("description", description);
                        valueMap.put("tableContent", tableMap);

                        String jsonItem = createJson();
                        jsonResult.append(",").append(jsonItem);
                    }
                }
            }
        }

        System.out.println("Nodes with Coordinates: " + loop);

        if (loop > 0){
            jsonResult.deleteCharAt(0);
        }
        
        return "[" + jsonResult.toString() + "]";
    }

    private String getChildValue(Node node, String nodeName) {

        String value = "";
        NodeList nodelist = node.getChildNodes();

        for (int i = 0; i < nodelist.getLength(); i++) {

            if (nodelist.item(i).getLocalName() != null) {
                if (nodelist.item(i).getLocalName().equals(nodeName)) {
                    value = nodelist.item(i).getTextContent();
                }
            }
        }
        return value;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addNodeValuesToMap(Node node) {

        valueMap = new HashMap();
        descriptionMap = new HashMap();
        tableMap = new HashMap();

        if (node.hasAttributes()) {

            String gbifKey = getGbifKey(node).toString().replace('"', '\"');
//          System.out.println("\nTaxonOccurrence: " + gbifKey);
            valueMap.put("id", gbifKey);
            tableMap.put("OccurenceID", gbifKey);
            descriptionMap.put("OccurrenceID", gbifKey);
        }

        //get informations from TaxonOccurrence ParentNodes
        collectParentNodeValues(node.getParentNode());
        
        //get informations from TaxonOccurrence ChildNodes
        collectChildNodeValues(node);
        
        String[] tags = { "catalogNumber", "earliestDateCollected", "latestDateCollected", "nameComplete", "place" };
        for (int i = 0; i < tags.length; i++) {
            if (tableMap.get(tags[i]) == null) {
                tableMap.put(tags[i], "");
            }
        }

        if (valueMap.get("time") == null) {
            valueMap.put("time", "");
        }
        
        if (valueMap.get("time").toString().length() > 0){

            String timeValue = new ShimDateConverter().convert(valueMap.get("time").toString());
            valueMap.put("time", timeValue);
        }
        
        

    }

    private String createDescription() {

        String description = "";

        description =
            "<p><table><tr>" + "<th>TaxonName</th>" + "<th>OccurrenceID</th>" + "<th>DataProvider</th>"
                    + "<th>earliestDateCollected</th>" + "<th>latestDateCollected</th>" + "<th>dataResourceRights</th>"
                    + "</tr>" + "<tr>" + "<td>";

        if (descriptionMap.get("nameComplete") != null) {
            description += descriptionMap.get("nameComplete").toString().replace('"', '\"');
        } else {
            description += "";
        }

        description += "</td>" + "<td>";

        if (descriptionMap.get("OccurrenceID") != null) {
            description += descriptionMap.get("OccurrenceID").toString().replace('"', '\"');
        } else {
            description += "";
        }

        description += "</td>" + "<td>";

        if (descriptionMap.get("dataProvider") != null) {
            description += descriptionMap.get("dataProvider").toString().replace('"', '\"');
        } else {
            description += "";
        }

        description += "</td>" + "<td>";

        if (descriptionMap.get("earliestDateCollected") != null) {
            description += descriptionMap.get("earliestDateCollected").toString().replace('"', '\"');
        } else {
            description += "";
        }

        description += "</td>" + "<td>";

        if (descriptionMap.get("latestDateCollected") != null) {
            description += descriptionMap.get("latestDateCollected").toString().replace('"', '\"');
        } else {
            description += "";
        }

        description += "</td>" + "<td>";

        if (descriptionMap.get("dataResourceRights") != null) {
            description += descriptionMap.get("dataResourceRights").toString().replace('"', '\"');
        } else {
            description += "";
        }

        description += "</td></tr></table></p>";

        return description;
    }

    private String getGbifKey(Node node) {

        String attribute = "";

        NamedNodeMap mmn = node.getAttributes();
        if (mmn.getLength() > 0) {
            for (int k = 0; k < mmn.getLength(); k++) {
                if (mmn.item(k).getLocalName().equals("gbifKey")) {
                    attribute = mmn.item(k).getNodeValue();
                }
            }
        }
        return attribute;
    }

    @SuppressWarnings("unchecked")
    private void collectChildNodeValues(Node node) {
        
        //add all tablevalues here
        String[] tags = { "catalogNumber", "earliestDateCollected", "latestDateCollected" };

        for (int i = 0; i < tags.length; i++) {

            String value = getChildValue(node, tags[i]).toString().replace('"', '\"');
            if (value.length() > 0) {
//              System.out.println(tags[i]+": " + value);                   
                tableMap.put(tags[i], value);
            }
        }
        
        String earliestDateCollected = getChildValue(node, "earliestDateCollected").toString().replace('"', '\"');
        if (earliestDateCollected.length() > 0) {
//          System.out.println("earliestDateCollected: " + earliestDateCollected);                  
            descriptionMap.put("earliestDateCollected", earliestDateCollected);
        }
        
        String latestDateCollected = getChildValue(node, "latestDateCollected").toString().replace('"', '\"');
        if (latestDateCollected.length() > 0) {
            descriptionMap.put("latestDateCollected", latestDateCollected);

            if (time.length() > 0) {
                valueMap.put("time", time);
            } else {
                valueMap.put("time", latestDateCollected);
            }
        }

        String taxonName = getChildValue(node, "nameComplete").toString().replace('"', '\"');
        if (taxonName.length() > 0) {
//          System.out.println("nameComplete: " + taxonName);                   
            descriptionMap.put("nameComplete", taxonName);
            tableMap.put("nameComplete", taxonName);
        }

        String lat = getChildValue(node, "decimalLatitude").replace(",", ".");
        if (lat.length() > 0) {

            double zahl = 0;
            try {
                zahl = Double.parseDouble(lat);
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
//          System.out.println("lat: " + zahl);                 
            valueMap.put("lat", zahl);
        }

        String lon = getChildValue(node, "decimalLongitude").replace(",", ".");
        if (lon.length() > 0) {
            double zahl = 0;
            try {
                zahl = Double.parseDouble(lon);
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
//          System.out.println("lon: " + zahl);                 
            valueMap.put("lon", zahl);
        }
        
        String country = getChildValue(node, "country").toString().replace('"', '\"');
        if (country.length() > 0) {
//          System.out.println("country: " + country);                  
            valueMap.put("place", country);
            tableMap.put("place", country);

        }

        // Taxon Key
//      if(node.getLocalName() != null){
//          if((node.getLocalName().equals("TaxonConcept"))){
//              
//              if(node.hasAttributes()){
//                  
//                  String gbifKey = getGbifKey(node);
//                  if(gbifKey.length() > 0){
//                      System.out.println("TaxonConcept: " + gbifKey);
//                      valueMap.put("TaxonConcept", gbifKey);
//                  }
//              }           
//          }
//      }

        if (node.hasChildNodes()) {

            NodeList nl = node.getChildNodes();

            for (int i = 0; i < nl.getLength(); i++) {
                collectChildNodeValues(nl.item(i));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void collectParentNodeValues(Node node) {

        if (node.getParentNode() != null) {
            Node parent = node.getParentNode();

            if (parent.getLocalName() != null) {

                if (parent.getLocalName().equals("dataResource")) {

//                  if(parent.hasAttributes()){
//                      
//                      String gbifKey = getGbifKey(parent);
//                      if(gbifKey.length() > 0){
//                          System.out.println("dataResource: " + gbifKey);
//                          valueMap.put("dataResource", gbifKey);
//                      }
//                  }

                    if (parent.hasChildNodes()) {

                        String nameValue = getChildValue(parent, "name").toString().replace('"', '\"');
//                      System.out.println("dataResourceName: " + childValue);                  
                        descriptionMap.put("dataResource", nameValue);
                        tableMap.put("dataResource", nameValue);

                        String rightsValue = getChildValue(parent, "rights").toString().replace('"', '\"');
//                      System.out.println("dataProviderName: " + childValue);                  
                        descriptionMap.put("dataResourceRights", rightsValue);
                        tableMap.put("dataResourceRights", nameValue);
                    }
                }
            }

            if (parent.getLocalName() != null) {
                if (parent.getLocalName().equals("dataProvider")) {

//                  if(parent.hasAttributes()){
//                      
//                      String gbifKey = getGbifKey(parent);
//                      System.out.println("dataProvider: " + gbifKey);
//                      valueMap.put("dataProvider", gbifKey);
//                  }

                    if (parent.hasChildNodes()) {

                        String childValue = getChildValue(parent, "name").toString().replace('"', '\"');
//                      System.out.println("dataProviderName: " + childValue);                  
                        descriptionMap.put("dataProvider", childValue);
                        tableMap.put("dataProvider", childValue);
                    }
                }
            }

            collectParentNodeValues(parent);
        }
    }

    private String createJson() {

        String json = "";

        try {

            ObjectMapper mapper = new ObjectMapper();
//          System.out.println(mapper.writeValueAsString(valueMap));
            if (valueMap != null)
                json = mapper.writeValueAsString(valueMap);

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
