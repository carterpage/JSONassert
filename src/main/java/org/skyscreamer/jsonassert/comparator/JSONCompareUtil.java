package org.skyscreamer.jsonassert.comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Utility class that contains Json manipulation methods
 */
public final class JSONCompareUtil {
    private JSONCompareUtil() {}

    public static boolean isNull(Object value) {
        return value.getClass().getSimpleName().equals("Null");
    }

    public static String classToType(Object value) {
        if (value instanceof JSONArray) {
            return "an array";
        } else if (value instanceof JSONObject) {
            return "an object";
        } else if (value instanceof String) {
            return "a string";
        } else {
            return value.getClass().getName();
        }
    }

    public static Map<Object,JSONObject> arrayOfJsonObjectToMap(JSONArray array, String uniqueKey) throws JSONException {
        Map<Object, JSONObject> valueMap = new HashMap<Object, JSONObject>();
        for(int i = 0 ; i < array.length() ; ++i) {
            JSONObject jsonObject = (JSONObject)array.get(i);
            Object id = jsonObject.get(uniqueKey);
            valueMap.put(id, jsonObject);
        }
        return valueMap;
    }

    public static String findUniqueKey(JSONArray expected) throws JSONException {
        // Find a unique key for the object (id, name, whatever)
        JSONObject o = (JSONObject)expected.get(0); // There's at least one at this point
        for(String candidate : getKeys(o)) {
            Object candidateValue = o.get(candidate);
            if (isSimpleValue(candidateValue)) {
                Set<Object> seenValues = new HashSet<Object>();
                seenValues.add(candidateValue);
                boolean isUsableKey = true;
                for(int i = 1 ; i < expected.length() ; ++i) {
                    JSONObject other = (JSONObject)expected.get(i);
                    if (!other.has(candidate)) {
                        isUsableKey = false;
                        break;
                    }
                    Object comparisonValue = other.get(candidate);
                    if (!isSimpleValue(comparisonValue) || seenValues.contains(comparisonValue)) {
                        isUsableKey = false;
                        break;
                    }
                    seenValues.add(comparisonValue);
                }
                if (isUsableKey) {
                    return candidate;
                }
            }
        }
        // No usable unique key :-(
        return null;
    }

    public static List<Object> jsonArrayToList(JSONArray expected) throws JSONException {
        List<Object> jsonObjects = new ArrayList<Object>(expected.length());
        for(int i = 0 ; i < expected.length() ; ++i) {
            jsonObjects.add(expected.get(i));
        }
        return jsonObjects;
    }

    public static boolean allSimpleValues(JSONArray array) throws JSONException {
        for(int i = 0 ; i < array.length() ; ++i) {
            if (!isSimpleValue(array.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSimpleValue(Object o) {
        return !(o instanceof JSONObject) && !(o instanceof JSONArray);
    }

    public static boolean allJSONObjects(JSONArray array) throws JSONException {
        for(int i = 0 ; i < array.length() ; ++i) {
            if (!(array.get(i) instanceof JSONObject)) {
                return false;
            }
        }
        return true;
    }

    public static boolean allJSONArrays(JSONArray array) throws JSONException {
        for(int i = 0 ; i < array.length() ; ++i) {
            if (!(array.get(i) instanceof JSONArray)) {
                return false;
            }
        }
        return true;
    }

    public static Set<String> getKeys(JSONObject jsonObject) {
        Set<String> keys = new TreeSet<String>();
        Iterator<?> iter = jsonObject.keys();
        while(iter.hasNext()) {
            keys.add((String)iter.next());
        }
        return keys;
    }
}
