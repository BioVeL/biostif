package de.fraunhofer.iais.kd.biovel.server;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;


public class MapVariableResolver implements XPathVariableResolver {
	  // local store of variable name -> variable value mappings
	  Map<String, String> variableMappings = new HashMap<String, String>();

	  // a way of setting new variable mappings 
	  public void setVariable(String key, String value)  {
	    variableMappings.put(key, value);
	  }

	  // override this method in XPathVariableResolver to 
	  // be used during evaluation of the XPath expression      
	  @Override
	  public Object resolveVariable(QName varName) {
	    // if using namespaces, there's more to do here
	    String key = varName.getLocalPart();
	    return variableMappings.get(key);
	  }
	}
