package de.fraunhofer.iais.kd.biovel.shim.transform;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import de.fraunhofer.iais.kd.biovel.shim.ShimServletContainer;
import de.fraunhofer.iais.kd.biovel.shim.util.ShimException;

public class ShimTransformerDwc2OccCsv {


    private Client client;
    
    /**
     * @param dwcReader darwin core data to be transformed to csv format
     * @param xslFile the xslt program
     * @return String result
     */
    public String transform(ServletConfig config, Reader dwcReader, InputStream xsltProgram) {
        
        System.out.println("run ShimTransformerDwc2OccCsv");

        TransformerFactory factory = TransformerFactory.newInstance();

        String result = null;

        try {
            Templates template = factory.newTemplates(new StreamSource(xsltProgram));

            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            // Prepare the input and output files
            Source sourceStream = new StreamSource(dwcReader);
            StringWriter stringWriter = new StringWriter();
            Result resultStream = new StreamResult(stringWriter);

            // Apply the xsl file to the source file and write the result
            xformer.transform(sourceStream, resultStream);
            result = stringWriter.toString();
        } catch (Exception exn) {
            throw new ShimException(exn);
        }

        if (result == null) {
            throw new ShimException("resultString is null");
        }
        
        // read CSV Header (von Cherian)
        
        Properties props = (Properties) config.getServletContext().getAttribute(ShimServletContainer.BIOSTIF_SERVER_CONF);
        String urlCsvHeader = props.getProperty("URL_CSV_HEADER");
        
        if(urlCsvHeader == null || urlCsvHeader.length() == 0){
            urlCsvHeader = "http://localhost:8080/biostif/data/csvHeader_beta.txt";
        }
        
        this.client = Client.create();        
        ClientResponse cr =
                this.client.resource(urlCsvHeader).header("Accept-Charset", "UTF-8").get(ClientResponse.class);
        String header = cr.getEntity(String.class);
        
        if (header == null) {
            throw new ShimException("header is null");
        }
        
        return header +"\n"+result;
    }

    /**
     * @param dwcInputStream
     * @param ids a String with dwc identifiers, the identifiers separated by
     *            whitespace
     * @return
     */
    public String filterDwcByIds(InputStream dwcInputStream, String ids) {

        String xslt = "yyy" + ids + "---";

        TransformerFactory factory = TransformerFactory.newInstance();

        String resultString = null;

        try {
            Templates template = factory.newTemplates(new StreamSource(new StringReader(xslt)));

            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            // Prepare the input and output files
            Source source = new StreamSource(new InputStreamReader(dwcInputStream));
            Result result = new StreamResult(new StringWriter());

            // Apply the xsl file to the source file and write the result
            xformer.transform(source, result);

            resultString = result.toString();

        } catch (Exception exn) {
            throw new ShimException(exn);
        }

        if (resultString == null) {
            throw new ShimException("resultString is null");
        }
        return resultString;
    }

}
