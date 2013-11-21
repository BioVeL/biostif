package de.fraunhofer.iais.kd.biovel.common.org.json.util;

import org.json.JSONObject;
import org.json.JSONString;

/**
 * is the abstraction of the classes, that are serializable to JSON texts via
 * JSONObjects.
 */
public interface IJSONCapable extends JSONString {

    /**
     * Make a JSON text of this JSONObject. For compactness, no whitespace is
     * added. If this would not result in a syntactically correct JSON text,
     * then null will be returned instead.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * 
     * @return a printable, displayable, portable, transmittable representation
     *         of the object, beginning with <code>{</code>&nbsp;<small>(left
     *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
     *         brace)</small>.
     * @see JSONString
     * @see JSONObject#toString()
     */
    public String toJSONString();

    /**
     * returns a <code>JSONObject</code> which represents the state of
     * <code>this</code>.
     * 
     * @return a JSONObject
     */
    public JSONObject toJSONObject();

}
