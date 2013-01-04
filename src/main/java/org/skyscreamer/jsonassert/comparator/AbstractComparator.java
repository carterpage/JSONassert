package org.skyscreamer.jsonassert.comparator;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.*;

/**
 * This class provides a skeletal implementation of the {@link JSONComparator}
 * interface, to minimize the effort required to implement this interface. <p/>
 */
public abstract class AbstractComparator implements JSONComparator {

    /**
     * Compares JSONObject provided to the expected JSONObject, and returns the results of the comparison.
     *
     * @param expected Expected JSONObject
     * @param actual   JSONObject to compare
     * @throws JSONException
     */
    @Override
    public final JSONCompareResult compareJSON(JSONObject expected, JSONObject actual) throws JSONException {
        JSONCompareResult result = new JSONCompareResult();
        compareJSON("", expected, actual, result);
        return result;
    }

    /**
     * Compares JSONArray provided to the expected JSONArray, and returns the results of the comparison.
     *
     * @param expected Expected JSONArray
     * @param actual   JSONArray to compare
     * @throws JSONException
     */
    @Override
    public final JSONCompareResult compareJSON(JSONArray expected, JSONArray actual) throws JSONException {
        JSONCompareResult result = new JSONCompareResult();
        compareJSONArray("", expected, actual, result);
        return result;
    }

    protected void checkJsonObjectKeysActualInExpected(String prefix, JSONObject expected, JSONObject actual, JSONCompareResult result) {
        Set<String> actualKeys = getKeys(actual);
        for (String key : actualKeys) {
            if (!expected.has(key)) {
                result.fail("Strict checking failed.  Got but did not expect: " + prefix + key);
            }
        }
    }

    protected void checkJsonObjectKeysExpectedInActual(String prefix, JSONObject expected, JSONObject actual, JSONCompareResult result) throws JSONException {
        Set<String> expectedKeys = getKeys(expected);
        for (String key : expectedKeys) {
            Object expectedValue = expected.get(key);
            if (actual.has(key)) {
                Object actualValue = actual.get(key);
                String fullKey = prefix + key;
                compareValues(fullKey, expectedValue, actualValue, result);
            } else {
                result.fail("Does not contain expected key: " + prefix + key);
            }
        }
    }

    protected void compareJSONArrayOfJsonObjects(String key, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        String uniqueKey = findUniqueKey(expected);
        if (uniqueKey == null) {
            // An expensive last resort
            recursivelyCompareJSONArray(key, expected, actual, result);
            return;
        }
        Map<Object, JSONObject> expectedValueMap = arrayOfJsonObjectToMap(expected, uniqueKey);
        Map<Object, JSONObject> actualValueMap = arrayOfJsonObjectToMap(actual, uniqueKey);
        for (Object id : expectedValueMap.keySet()) {
            if (!actualValueMap.containsKey(id)) {
                result.fail(key + "[]: Expected but did not find object where " + uniqueKey + "=" + id);
                continue;
            }
            JSONObject expectedValue = expectedValueMap.get(id);
            JSONObject actualValue = actualValueMap.get(id);
            compareValues(key + "[" + uniqueKey + "=" + id + "]", expectedValue, actualValue, result);
        }
        for (Object id : actualValueMap.keySet()) {
            if (!expectedValueMap.containsKey(id)) {
                result.fail(key + "[]: Contains object where " + uniqueKey + "=" + id + ", but not expected");
            }
        }
    }

    protected void compareJSONArrayOfSimpleValues(String key, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        @SuppressWarnings("unchecked")
        Map<Object, Integer> expectedCount = CollectionUtils.getCardinalityMap(jsonArrayToList(expected));
        @SuppressWarnings("unchecked")
        Map<Object, Integer> actualCount = CollectionUtils.getCardinalityMap(jsonArrayToList(actual));
        for (Object o : expectedCount.keySet()) {
            if (!actualCount.containsKey(o)) {
                result.fail(key + "[]: Expected " + o + ", but not found");
            } else if (!actualCount.get(o).equals(expectedCount.get(o))) {
                result.fail(key + "[]: Expected contains " + expectedCount.get(o) + " " + o
                        + " actual contains " + actualCount.get(o));
            }
        }
        for (Object o : actualCount.keySet()) {
            if (!expectedCount.containsKey(o)) {
                result.fail(key + "[]: Contains " + o + ", but not expected");
            }
        }
    }

    protected void compareJSONArrayWithStrictOrder(String key, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        for (int i = 0; i < expected.length(); ++i) {
            Object expectedValue = expected.get(i);
            Object actualValue = actual.get(i);
            compareValues(key + "[" + i + "]", expectedValue, actualValue, result);
        }
    }

    // This is expensive (O(n^2) -- yuck), but may be the only resort for some cases with loose array ordering, and no
    // easy way to uniquely identify each element.
    protected void recursivelyCompareJSONArray(String key, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        Set<Integer> matched = new HashSet<Integer>();
        for (int i = 0; i < actual.length(); ++i) {
            Object actualElement = actual.get(i);
            boolean matchFound = false;
            for (int j = 0; j < expected.length(); ++j) {
                if (matched.contains(j) || !actual.get(i).getClass().equals(expected.get(j).getClass())) {
                    continue;
                }
                if (actualElement instanceof JSONObject) {
                    if (compareJSON((JSONObject) expected.get(j), (JSONObject) actualElement).passed()) {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                } else if (actualElement instanceof JSONArray) {
                    if (compareJSON((JSONArray) expected.get(j), (JSONArray) actualElement).passed()) {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                } else if (actualElement.equals(expected.get(j))) {
                    matched.add(j);
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                result.fail("Could not find match for element " + actualElement);
                return;
            }
        }
    }
}
