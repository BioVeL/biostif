package de.fraunhofer.iais.kd.biovel.shim.raster;

import java.net.URI;

//import de.fraunhofer.iais.kd.mmp.common.contract.MmpException;

/**
 * Contract for mediating layer operation on the GeoServer.
 * 
 * @author utaddei
 */
public interface IGeoServerAdapter {

    /**
     * Gets the base URI of the GeoServer. This is generally
     * 'http://localhost:8080/geoserver/rest'.
     * 
     * @return a base URI
     */
    URI getBaseUri();

    /**
     * Adds a layer to the GeoServer.
     * 
     * @param layerName
     * @return the URI of the new layer (not of the FeatureType!).
     */
    URI addLayer(String layerName);

    /**
     * Removes a layer (and the FeatureType) from the GeoServer.
     * 
     * @param layerName
     */
    void removeLayer(String layerName);

    /**
     * Crates a new style. By convention, styles are defined per layer and per
     * attribute. A style created with this method will by called
     * 'layerName_attributeName'.
     * 
     * @param layerName
     * @param attributeName (apparently this parameter is/should not be used)
     * @return the URI of the style
     */
    URI postStyle(String layerName, String attributeName);

    /**
     * Creates the style data.
     * 
     * @param styleName the style name, which is, by convention,
     *            'layerName_attributeName'.
     * @param styleData the SLD/XML representation of this style
     */
    void putStyle(String styleName, String styleData);

    /**
     * Deletes the style, if it exits.
     * <p>
     * postcondition: The style does not exist.
     * 
     * @param styleName not <code>null</code>, the style name, which is, by
     *            convention, 'layerName_attributeName'.
     * @throws MmpException if deletion of an existing style failed. Information
     *             about the failure is logged.
     */
    void deleteStyle(String styleName);

    /**
     * Sets the workspace in which this adapter should work.
     * 
     * @param wsName the workspace name. Cannot be null.
     */
    void setWorkspace(String wsName);

    /**
     * Sets the datastore which is the parent of the added layers.
     * 
     * @param dsName the datastore name. Cannot be null.
     */
    void setDatastore(String dsName);

    /**
     * Gets the workspace name.
     * 
     * @return
     */
    String getWorkspace();

    /**
     * Gets the datastore name.
     * 
     * @return
     */
    String getDatastore();

    void setPasswd(String passwd);

    void setUser(String user);

    void deleteLayersAndStyles();

    boolean exists(String layername);
}
