package de.fraunhofer.iais.kd.biovel.shim.transform;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

public class DocumentNamespaceResolver implements NamespaceContext {
    // the delegate
    //source:
    //http://www.ibm.com/developerworks/java/library/x-nmspccontext/index.html

    private final Document sourceDocument;

    /**
     * This constructor stores the source document to search the namespaces in
     * it.
     * 
     * @param document source document
     * @return
     */
    public DocumentNamespaceResolver(Document document) {
        this.sourceDocument = document;
    }

    /**
     * The lookup for the namespace uris is delegated to the stored document.
     * 
     * @param prefix to search for
     * @return uri
     */
    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return this.sourceDocument.lookupNamespaceURI(null);
        } else {
            return this.sourceDocument.lookupNamespaceURI(prefix);
        }
    }

    /**
     * This method is not needed in this context, but can be implemented in a
     * similar way.
     */
    @Override
    public String getPrefix(String namespaceURI) {
        return this.sourceDocument.lookupPrefix(namespaceURI);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Iterator getPrefixes(String namespaceURI) {
        // not implemented yet
        return null;
    }

}
