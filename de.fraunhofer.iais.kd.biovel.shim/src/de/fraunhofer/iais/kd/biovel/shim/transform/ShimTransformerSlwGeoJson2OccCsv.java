package de.fraunhofer.iais.kd.biovel.shim.transform;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;

public class ShimTransformerSlwGeoJson2OccCsv {

    private static final Logger LOG = Logger.getLogger(ShimTransformerSlwGeoJson2OccCsv.class.getName());

    /**
     * @param geoJson data to be transformed to csv format
     * @return String result
     */
    public String transform(InputStream is) {

        StringBuffer result = new StringBuffer();
        int loop = 0;
//        request:
//        http://localhost:8080/shim/rest/transform?source=http%3A%2F%2Fslwgeo.artdata.slu.se%3A8080%2Fgeoserver%2FSLW%2Fows%3Fservice%3DWFS%26version%3D1.0.0%26request%3DGetFeature%26typeName%3DSLW%3ABioVel%26maxFeatures%3D5%26outputFormat%3Djson

//        bsp: http://outerthought.org/blog/415-ot.htm
//        http://fasterxml.github.com/jackson-core/javadoc/2.0.6/
//        http://fasterxml.github.com/jackson-databind/javadoc/2.0.6/

        boolean haeder = true;
        String[] names = null;

        JsonFactory f = new MappingJsonFactory();
        JsonParser jp = null;
        try {
            jp = f.createJsonParser(is);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JsonToken current = null;

        try {
            current = jp.nextToken();
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (current != JsonToken.START_OBJECT) {
            System.out.println("Error: root should be object: quiting.");

            LOG.info("Error: JSON root should be object: quiting.");
            final String msg = "Error: JSON root should be object: quiting";
            return msg;
        }

        try {
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jp.getCurrentName();
                // move from field name to field value
                current = jp.nextToken();
                if (fieldName.equals("features")) {
                    if (current == JsonToken.START_ARRAY) {
                        // For each of the records in the array

                        while (jp.nextToken() != JsonToken.END_ARRAY) {
                            // read the record into a tree model,
                            // this moves the parsing position to the end of it
                            JsonNode node = jp.readValueAsTree();
                            // And now we have random access to everything in the object

                            JsonNode propertiesNode = node.get("properties");
                            int nodeSize = propertiesNode.size();

                            if (haeder) {

                                haeder = false;
                                names = new String[nodeSize];
                                int loop1 = 0;

                                Iterator<String> namesIterator = propertiesNode.fieldNames();
                                String[] firstline = new String[nodeSize];

                                while (namesIterator.hasNext()) {

                                    String term = namesIterator.next();
                                    names[loop1] = term;

                                    switch (term) {

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

                                    firstline[loop1] = term;
                                    loop1++;
                                }

                                String stringFirstLine = StringUtils.join(firstline, ",");
                                result.append(stringFirstLine).append("\n");
                            }

                            String[] nextLine = new String[nodeSize];
                            for (int i = 0; i < names.length; i++) {
                                String value = propertiesNode.get(names[i]).asText();

                                if (value.contains(",")) {
                                    value = '"' + value + '"';
                                }
                                nextLine[i] = value;
                            }
                            String stringLine = StringUtils.join(nextLine, ",");
                            result.append(stringLine).append("\n");
                            
//                            System.out.println("\n "+ loop + "nl: "+stringLine);

                            loop++;
                        }

                    } else {
                        System.out.println("Error: records should be an array: skipping.");
                        jp.skipChildren();
                    }
                } else {
//                System.out.println("Unprocessed property: " + fieldName);
                    jp.skipChildren();
                }
            }
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String msg = "SlwJson2Csv has added '" + loop + "' rows";
        LOG.info(msg);
        
        return result.toString();
    }
}
