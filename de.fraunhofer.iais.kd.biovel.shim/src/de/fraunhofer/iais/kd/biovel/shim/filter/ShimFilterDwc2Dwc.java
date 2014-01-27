package de.fraunhofer.iais.kd.biovel.shim.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import de.fraunhofer.iais.kd.biovel.shim.ShimServletContainer;

import au.com.bytecode.opencsv.CSVReader;

public class ShimFilterDwc2Dwc {
    
    private static final Logger LOG = Logger.getLogger(ShimFilterDwc2Dwc.class.getName());


    /**
     * @param dwcInputStream darwin core data to be transformed to csv format
     * @param xslFile the xslt program
     * @return String result
     */
    public String filter(ServletConfig config, InputStream fileStream, InputStream iDs) {
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder;
        Document doc = null;

 
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(fileStream);
        } catch (ParserConfigurationException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (SAXException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        
        BufferedReader isBufferedReader = new BufferedReader(new InputStreamReader(iDs));
        String filterString = "";
        String line;

        try {
            while ((line = isBufferedReader.readLine()) != null) {
                filterString += line;
            }

            isBufferedReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<String> filterList = Arrays.asList(filterString.split(","));

//      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//      docFactory.setNamespaceAware(true);
//      DocumentBuilder docBuilder;
//      Document doc = null;            
//      
//      try {
//          
//          docBuilder = docFactory.newDocumentBuilder();
//          doc = docBuilder.parse(dwcIs);
//          
//      } catch (ParserConfigurationException e1) {
//          // TODO Auto-generated catch block
//          e1.printStackTrace();
//      } catch (SAXException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//      } catch (IOException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//      }

        NodeList nodeList = doc.getElementsByTagName("*");

//      System.out.println("liste vor" + nodeList.getLength());

        int ixID = 0;
        
        //remove filtered occurenceNodes  
        for (int i = nodeList.getLength(); i > 0; i--) {

            Node node = nodeList.item(i);

            if ((node != null) && node.getLocalName().equals("TaxonOccurrence") && node.hasAttributes()) {

                String gbifKey = getGbifKey(node);

                if (gbifKey.length() > 0) {
                    if (!filterList.contains(gbifKey)) {
                        node.getParentNode().removeChild(node);
                    }
                } else {
                    ixID++;
                }
            }

        }
        
        StringWriter sw = new StringWriter();
        
        if(nodeList.getLength() == ixID){
            
            sw.append("DwC document contains no TaxonOccurence IDs in " + nodeList.getLength() + " entries");
        } else {
    //      System.out.println("liste nach" + nodeList.getLength());
    
            //remove dataProvider Nodes without occurenceNodes 
            for (int j = nodeList.getLength(); j > 0; j--) {
    
                Node node = nodeList.item(j);
    
                if ((node != null) && node.getLocalName().equals("dataProvider") && node.hasAttributes()) {
    
                    Document d;
                    try {
                        d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                        d.appendChild(d.importNode(node, true));
    
                        NodeList taxList = d.getElementsByTagNameNS("*", "TaxonOccurrence");
    
                        if (taxList.getLength() == 0) {
                            node.getParentNode().removeChild(node);
                        }
    
                    } catch (ParserConfigurationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
    
                }
            }
    
    //      System.out.println("liste nach leeren" + nodeList.getLength());
    
            //remove whitespaces after removeChild
            for (int k = nodeList.getLength(); k > 0; k--) {
    
                Node child = nodeList.item(k);
                if (child instanceof Element) {
                    removeWhitespaceNodes((Element) child);
                }
            }
    
            Transformer serializer;
            try {
                serializer = TransformerFactory.newInstance().newTransformer();
                serializer.transform(new DOMSource(nodeList.item(0)), new StreamResult(sw));
    
            } catch (TransformerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TransformerFactoryConfigurationError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    
    //        System.out.println("result \n" + result);
        }

        return sw.toString();

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
    
    /**
     * Removes whitespace from Document
     * 
     * @see http://forums.java.net/jive/thread.jspa?messageID=345459
     * @param e
     */
    private static void removeWhitespaceNodes(Element e) {

        NodeList children = e.getChildNodes();

        for (int i = children.getLength() - 1; i >= 0; i--) {

            Node child = children.item(i);
            Node child1 = children.item(i + 1);

            if (((child instanceof Text) && (((Text) child).getData().trim().length() == 0))
                    && ((child1 instanceof Text) && (((Text) child1).getData().trim().length() == 0))) {

                e.removeChild(child);
            } else if (child instanceof Element) {

                removeWhitespaceNodes((Element) child);
            }
        }
    }

}
