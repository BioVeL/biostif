package de.fraunhofer.iais.kd.biovel.common.org.json.util;

import org.json.JSONObject;
import org.json.JSONString;

import de.fraunhofer.iais.kd.biovel.common.contract.Check;

/**
 * Provides implementations of specified methods; {@link #toJSONString()} is a
 * implementation to be wrapped by redefinitions in subclasses.
 * <p>
 * Subclasses of JSONCapable implement a constructor and a methods conforming to
 * this implementation pattern:
 * 
 * <pre>
 *  class Example extends ... {
 *      public Example(JSONObject jo) {
 *          super(jo);
 *          ...
 *      }
 *      public JSONObject toJSONObject() {
 *          JSONObject result = super.toJSONObject();
 *          ...
 *          return result;
 *      }
 *  }
 * </pre>
 */
public class JSONCapable implements IJSONCapable {

    /**
     * returns the class name given by the entry <code>"class"</code> in
     * <code>jo</code>
     * 
     * @param jsonObject not <code>null</code>
     * @return .. as described
     */
    public static String getClassName(JSONObject jsonObject) {
        final String classEntry = jsonObject.getString("class");
        Check.notNull(classEntry, "entry \"class\" does not exist");
        String result = (classEntry.startsWith("class ") ? classEntry.substring(6) : classEntry);
        return result;
    }

    /**
     * standard c'tor
     */
    public JSONCapable() {
        // intentionally empty
    }

    /**
     * c'tor which checks the given object's "class" property
     * 
     * @param object
     */
    public JSONCapable(JSONObject jo) {
        Check.isTrue(getClass().toString().equals(jo.getString("class")));
    }

    /**
     * returns a JSON text serialization depending on the primitive method
     * {@link #toJSONObject()}.
     * 
     * @see org.json.JSONString#toJSONString()
     */
    public String toJSONString() {
        return toJSONObject().toString();
    }

    /**
     * Make a JSON text of this JSONObject. For compactness, no whitespace is
     * added. If this would not result in a syntactically correct JSON text,
     * then null will be returned instead.
     * <p>
     * Formatted strings may be produced by calling <br>
     * {@link JSONObject.toString(int)} <br>
     * on the result of this method.
     * <p>
     * This method is intended to be used as the starting point of redefinitions
     * in subclasses. The key/value pair <br>
     * <code> "class" : this.getClass().toString() </code> <br>
     * is put into the JSONObject returned.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * 
     * @return a printable, displayable, portable, transmittable representation
     *         of the object, beginning with <code>{</code>&nbsp;<small>(left
     *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
     *         brace)</small>.
     *         <p>
     *         The new JSONObject returned holds the key/value pair <br>
     *         <code>"class" : this.getClass().toString()</code>
     * @see JSONString
     * @see JSONObject#toString()
     * @see de.fraunhofer.iais.kd.biovel.common.org.json.util.IJSONCapable#toJSONObject()
     */
    public JSONObject toJSONObject() {
        JSONObject result = new JSONObject();

        result.put("class", this.getClass().toString());

        return result;
    }

    /**
     * compares <code>this</code> to the <code>other</code> by comparing the
     * results of {@link #toJSONString} for equality.
     * 
     * @param other a JSONCapable objects to compare to. May be
     *            <code>null</code>.
     * @return <code>true</code> if the results of applying
     *         {@link #toJSONString()} to <code>this</code> and to
     *         <code>other</code> are equal, <code>false</code> otherwise. <br>
     *         If <code>other == null</code> return <code>false</code>.
     */
    public boolean equalsTo(IJSONCapable other) {
        if (other == null) {
            return false;
        }
        return toJSONString().equals(other.toJSONString());

    }

}
