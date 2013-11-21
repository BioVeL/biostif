package de.fraunhofer.iais.kd.biovel.common.org.json.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import de.fraunhofer.iais.kd.biovel.common.contract.Check;

/**
 * @author sylla
 */
public class JSONCapableHelper {

    private static ClassLoader classLoader;

    public static void setClassLoader(ClassLoader classLoader) {
        JSONCapableHelper.classLoader = classLoader;
    }

    /**
     * constructs an object of type &lt;T&gt; from the JSONObject
     * <code>jo</code>; the object may be an instance of <code>aClass</code> or
     * an instance of a subclass of <code>aClass</code>
     * 
     * @param &lt;T&gt; is used to name the type of <code>aClass</code>
     * @param aClass specifies the class of the object to be constructed.
     *            <b>pre:</b> not <code>null</code>
     * @param jo a JSON object. <b>pre:</b> not <code>null</code>
     * @return an object of class <code>aClass</code>. <b>post: </b> not
     *         <code>null</code>.
     * @throws RuntimeException caused by ClassNotFoundException,
     *             NoSuchMethodException, InstantionException
     */
    public static <T> T makeObject(Class<T> aClass, JSONObject jo, Object... rest) {
        Check.argNotNull(aClass);
        Check.argNotNull(jo);
        Check.argNotNull(rest);
        Check.isTrue(rest.length <= 1);
        T newObj = null;
        try {
            String classInfo = jo.getString("class");
            String[] classSpec = classInfo.split(" +");
            Check.isTrue(classSpec[0].equals("class"));
            Class<T> objClass;
            if (classLoader == null) {
                objClass = (Class<T>) Class.forName(classSpec[1]);
            } else {
                objClass = (Class<T>) classLoader.loadClass(classSpec[1]);
            }

            Constructor<T> ctr;
            if (rest.length == 0) {
                ctr = objClass.getConstructor(JSONObject.class);
                newObj = ctr.newInstance(jo);
            } else {
                ctr = objClass.getConstructor(JSONObject.class, rest[0].getClass());
                newObj = ctr.newInstance(jo, rest[0]);
            }
            Check.canBeAssignedWith(aClass, newObj.getClass());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        Check.notNull(newObj);
        return newObj;
    }

    /**
     * constructs an array&lt;T&gt; using {@link #makeObject} to construct the
     * elements of the array.
     * 
     * @param <T>
     * @param elementClass <b>pre:</b> not <code>null</code>
     * @param ja <b>pre:</b> not <code>null</code>
     * @return an array of class &lt;<code>aClass</code>&gt[];. <br>
     *         <b>post:</b> not <code>null</code>.
     */
    public static <T> T[] makeArray(Class<? extends T> elementClass, JSONArray ja) {
        Check.argNotNull(elementClass);
        Check.argNotNull(ja);
        final int len = ja.length();
        final T[] result = (T[]) Array.newInstance(elementClass, len);
        for (int i = 0; i < len; i++) {
            result[i] = makeObject(elementClass, ja.getJSONObject(i));
        }
        return result;
    }

    /**
     * adds all elements of <code>jsonArray</code> to
     * <code>resultCollection</code>.
     * 
     * @param <T> the element type
     * @param resultCollection where the elements of <code>jsonArray</code> are
     *            to be added.
     * @param elementClass the type or supertype of elements to be included in
     *            <code>jsonArray</code>
     * @param jsonArray containing elements of type <code>elementClass</code>
     * @return resultCollection with all Objects made from ja added in the same
     *         sequence as in <code>jsonArray</code>
     */
    public static <T> Collection<T> addToCollection(Collection<T> resultCollection, Class<? extends T> elementClass,
                                                    JSONArray jsonArray) {
        final int len = jsonArray.length();
        for (int i = 0; i < len; i++) {
            resultCollection.add(makeObject(elementClass, jsonArray.getJSONObject(i)));
        }
        return resultCollection;
    }

    /**
     * adds elements constructed form the members of <code>ja</code> to the
     * collection <code>resultCollection</code>.
     * 
     * @param resultCollection new elements will be added to this collection.
     * @param elementClass the class / superclass of the collections elements
     * @param ja holds the elements to be inserted into
     *            <code>resultCollection</code>.
     * @param optArg an optional parameter to be provided as additional
     *            parameter to the constructor of the element classes.
     * @param <T> the type / supertype of the collections elements
     * @param <U> the generic type of <code>resultCollection</code>.
     * @return <code>resultCollection</code> filled up with additional elements.
     */
    public static <T, U extends Collection<T>> U fillCollection(U resultCollection, Class<? extends T> elementClass,
                                                                JSONArray ja, Object... optArg) {
        Check.argNotNull(optArg);
        Check.isTrue(optArg.length <= 1);
        final int len = ja.length();
        for (int i = 0; i < len; i++) {
            if (optArg.length == 0) {
                resultCollection.add(makeObject(elementClass, ja.getJSONObject(i)));
            } else {
                resultCollection.add(makeObject(elementClass, ja.getJSONObject(i), optArg[0]));
            }
        }
        return resultCollection;
    }

    /**
     * returns a {@link JSONArray} containing {@link JSONObject}-s as retrieved
     * from the elements of <code>list</code>.
     * 
     * @param <T> the type of <code>list</code>s elements.
     * @param list build a JSONArray from this list.
     * @return a {@link JSONArray} containing {@link JSONObject}-s as retrieved
     *         from the elements of <code>list</code>.
     */
    public static <T extends IJSONCapable> JSONArray makeJSONArray(List<T> list) {
        JSONArray result = new JSONArray();
        for (T el : list) {
            result.put(el.toJSONObject());
        }
        return result;
    }
}
