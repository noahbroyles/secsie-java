package dict;

import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.json.JSONException;


/**
 * This class is meant to model a Dictionary structure similar to Python. I made it because I was not at all satisfied with hash tables and such.
 * <br><br>
 *
 * This dictionary class only supports string keys, <b>not</b> integers or other primitives. However, it allows getting and setting of values with a nested selector.<br>
 * For example, in the following code: <br>
 * <pre><code>
 * Dictionary dict = new Dictionary();
 * dict.set("current_total_price_set.shop_money.amount", "32.94", "\\.");
 * System.out.println(dict.dumps());
 * </code></pre>
 * 5 things are happening:
 * <ol>
 *     <li>a new Dictionary called <code>dict</code> is created</li>
 *     <li>a new nested Dictionary inside <code>dict</code> called <code>current_total_price_set</code> is created</li>
 *     <li>a new nested Dictionary inside <code>current_total_price_set</code> called <code>shop_money</code> is created</li>
 *     <li>a value called <code>amount</code> is set to <code>"32.94"</code> inside <code>shop_money</code></li>
 *     <li><code>{"current_total_price_set":{"shop_money":{"amount":"32.94"}}}</code> is printed to the console</li>
 * </ol>
 * Selectors can be separated by the user's choice of value, but they must be a regular expression, for example <code>"\\."</code> to separate by dot(<code>.</code>).
 *
 * @author Noah Broyles
 *
 */
public class Dictionary extends JSONObject {
	
	/*  Constructors  */

	/**
	 * Creates a new empty Dictionary
	 */
	public Dictionary() {
		super();
	}

	/**
	 * Creates a new Dictionary from a JSON string
	 *
	 * @param fromJson A String of valid JSON
	 */
	public Dictionary(String fromJson) {
		super(fromJson);
	}

	/**
	 * Creates a new Dictionary from a Map.
	 */
	public Dictionary(Map<?, ?> map) {
		super(map);
	}

	/**
	 * Creates a Dictionary from a String[] of keys and an Object[] of values. The arrays <br>must</br> be of equal length.
	 * The Dictionary will be created by mapping each key in the <code>keys</code> array to the Object in the corresponding position in the <code>values</code> array.
	 *
	 * @param keys A String array containing the keys
	 * @param values An Object array containing the values
	 */
	public Dictionary(String[] keys, Object[] values) {
		super(dict(keys, values));
	}

	/**
	 * Returns a copy of a Dictionary
	 *
	 * @param d the dict to copy
	 */
	public Dictionary (Dictionary d) {
		super(d, JSONObject.getNames(d));
	}

	/**
	 * Creates a new Dictionary from a JSON file.
	 *
	 * @param jsonFilePath A path to a valid JSON file
	 * @return a new Dictionary from the JSON
	 * @throws IOException if the file can't be read
	 */
	public static Dictionary fromJson(String jsonFilePath) throws IOException {
		String json = Files.readString(Paths.get(jsonFilePath));
		return new Dictionary(json);
	}

	
	/**
	 * Get the value at the end of the selector from the Dictionary. Selectors are separated by dots(.)
	 *
	 * @param selector the selector to lookup
	 * @return the value the selector points to
	 * @throws KeyError if you try anything stupid
	 */
	public Object select(String selector) throws KeyError {
		return this.select(selector, "\\.");
	}

	public boolean selectBoolean(String selector) throws KeyError {
		return (boolean) this.select(selector, "\\.");
	}

	public int selectInt(String selector) throws KeyError {
		return (int) this.select(selector, "\\.");
	}

	public double selectDouble(String selector) throws KeyError {
		return (double) this.select(selector, "\\.");
	}

	public String selectString(String selector) throws KeyError {
		return (String) this.select(selector, "\\.");
	}

	public BigInteger selectBigInteger(String selector) throws KeyError {
		return new BigInteger(this.select(selector, "\\.").toString());
	}

	public BigDecimal selectBigDecimal(String selector) throws KeyError {
		return new BigDecimal(this.select(selector, "\\.").toString());
	}

	public Dictionary selectDictionary(String selector) throws KeyError {
		return (Dictionary) (this.select(selector, "\\."));
	}

	
	/**
	 * Get the value a selector points to from the Dictionary.
	 *
	 * @param selector the selector to lookup
	 * @param separator the regex to split the selector by
	 * @return the (Object) value that the selector points to
	 * @throws KeyError if you try to select past a value into a path that doesn't exist.
	 */
	public Object select(String selector, String separator) throws KeyError {
		String[] keyPath = selector.split(separator);

		// The goal is to return the final object at the end of the keyPath
		try {
			Object finalObject;

			// Get all the way to the final nested JSONObject
			JSONObject obj = new Dictionary(this);
			for (int i = 0; i < keyPath.length - 1; i++) {
				obj = (JSONObject) obj.get(keyPath[i]);
			}
			finalObject = obj.get(keyPath[keyPath.length - 1]);

			return finalObject;
		} catch (ClassCastException ex) {
			throw new KeyError(selector, "The specified selector be selected because not all nested values are Dictionaries");
		}

	}

	/**
	 * Set's the final path at the end of a selector to a given value. Supports nested Dictionaries. If a Dictionary on the path
	 * doesn't yet exist, it will be created for you.
	 *
	 * @param selector The selector to use
	 * @param value The value to assign
	 * @param separator The regex to split the selector by.
	 * @throws KeyError If an object which is not already a dictionary would be overridden.
	 */
	public void set(String selector, Object value, String separator) throws KeyError {
		// Split out the path
		String[] keyPath = selector.split(separator);

		// This will change as we walk through the path
		Dictionary currentDict = this;
		// The goal is to set the final object at the end of the keyPath
		for (int i = 0; i < keyPath.length - 1; i++) {
			// Let's see how far into the dict the path exists
			try {
				// Reassign the currentDict to the value of the current key
				currentDict = (Dictionary) currentDict.get(keyPath[i]);
			} catch (JSONException ex) {
				// This path does not yet exist, add it
				currentDict.put(keyPath[i], new Dictionary());
				// Reassign the current dict to the new dictionary, so we can progress along the path
				currentDict = (Dictionary) currentDict.get(keyPath[i]);
			} catch (ClassCastException ex) {
				/* This means that the path already does exist, but it ends here, in which case you would be
				 * overriding the current value. This is not good. I think we will throw a KeyError. */
				throw new KeyError(keyPath[i], "Value would be overridden with nested Dictionary");
			}
		}

		// Finally, put the final value in its place.
		currentDict.put(keyPath[keyPath.length - 1], value);
	}


	/**
	 * Create a map from two equal length arrays of keys and values
	 *
	 * @param keys a String[] of keys
	 * @param values an Object[] of equal length to keys
	 * @return a Map with matching key-value pairs
	 */
	private static Map<String, Object> dict(String[] keys, Object[] values) {
		assert keys.length == values.length;

		int c = 0;
		Map<String, Object> map = new HashMap<>();
		for (String key: keys) {
			map.put(key, values[c]);
			c++; // hehe
		}

		return map;
	}


	/**
	 * Returns a JSON string representation of the object
	 *
	 * @param indent The amount of space to indent to prettify the output
	 * @return a JSON string
	 */
	public String dumps(int indent) {
		return this.toString(indent);
	}

	/**
	 * Returns a JSON string representation of the object
	 *
	 * @return a JSON string
	 */
	public String dumps() {
		return this.toString();
	}

}
