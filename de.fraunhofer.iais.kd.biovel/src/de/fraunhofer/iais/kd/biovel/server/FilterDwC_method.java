package de.fraunhofer.iais.kd.biovel.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class FilterDwC_method {
	
	
	/*test with:
	 * 
	 * 			OutputStream out = null;	
			
			String test1 = "214031548" + ","+"214031644";			
			InputStream fis = new ByteArrayInputStream(test1.getBytes());			
			
//			filterDwC(is, fis, out);
			filterDwC(doc, fis, out);
	 * 
	 * 
	 */
	
	
	
	public OutputStream filterDwC(InputStream dwcIs, InputStream filterIs, OutputStream out){

		
			BufferedReader isBufferedReader = new BufferedReader(new InputStreamReader(filterIs));
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
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder;
			Document doc = null;			
			
			try {
				
				docBuilder = docFactory.newDocumentBuilder();
				doc = docBuilder.parse(dwcIs);
				
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
			
			NodeList nodeList = doc.getElementsByTagName("*");
			
			//remove filtered occurenceNodes  
			for (int i=nodeList.getLength(); i>0; i--) {
				
				Node node = nodeList.item(i);
				
				if(node != null && node.getLocalName().equals("TaxonOccurrence") && node.hasAttributes()){

					String gbifKey = getGbifKey(node);

					if(gbifKey.length() > 0){
						if(!filterList.contains(gbifKey)){
							node.getParentNode().removeChild(node);
						}
					}
				}
				
			}
			
			//remove dataProvider Nodes without occurenceNodes 
			for (int j=nodeList.getLength(); j>0; j--) {
				
				Node node = nodeList.item(j);
				
				if(node != null && node.getLocalName().equals("dataProvider") && node.hasChildNodes()){
					
					Document d;
					try {
						d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
					    d.appendChild( d.importNode(node, true));
						
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
			
			//remove whitespaces after removeChild
			for (int k=nodeList.getLength(); k > 0; k--) {
				
				Node child = nodeList.item(k);
				if (child instanceof Element) {
		            removeWhitespaceNodes((Element) child);
		        }
			}

			
			StringWriter sw = new StringWriter();
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

			String result = sw.toString();		

			byte[] messagebytes = result.getBytes();
			ByteArrayOutputStream baos = new ByteArrayOutputStream(messagebytes.length);
			try {
				baos.write(messagebytes);
				baos.writeTo(out);
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return out;		
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
		        Node child1 = children.item(i+1);
		        
		        if ((child instanceof Text && ((Text) child).getData().trim().length() == 0) && (child1 instanceof Text && ((Text) child1).getData().trim().length() == 0)) {
		        
		        	e.removeChild(child);
		        } else if (child instanceof Element) {
		            
		        	removeWhitespaceNodes((Element) child);
		        }
		    }
		}
	
	private String getGbifKey(Node node){
		
		String attribute = "";
		
		NamedNodeMap mmn = node.getAttributes();
		if (mmn.getLength() > 0) {
			for (int k = 0; k < mmn.getLength(); k++) {
				if(mmn.item(k).getLocalName().equals("gbifKey")){
					attribute = mmn.item(k).getNodeValue();
				}					
			}
		}		
		return attribute;
	}

}
