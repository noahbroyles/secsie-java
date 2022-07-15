package dict;

import java.util.*;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.json.JSONException;


/**
 * This class is meant to model a dictionary structure similar to Python. I made it because I was not at all satisfied with hash tables and such.
 * The only thing I wasn't able to do was figure out how to use square bracket syntax, e.g. dictionary["key"] = "value".
 * 
 * This dictionary class only supports string keys, but allows lookup of values by an integer dict position. Kinda jank.
 *
 * @author Noah Broyles
 *
 */
public class Dictionary extends JSONObject {
	
	/*  Constructors  */
	// Empty dict constructor
	public Dictionary() {
		super();
	}

	public Dictionary(String fromJson) {
		super(fromJson);
	}

	public Dictionary(Map<?, ?> map) {
		super(map);
	}
	
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
	 * Get the value of a selector from the Dictionary. Selectors are separated by dots(.)
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
	 * Get the value of a selector from the Dictionary. An example of a selector would be "order.price.total_price_usd".
	 * That selector would look for a nested Object called "total_price_usd" inside a Dictionary called "price" inside a Dictionary called "order".
	 *
	 * @param selector the selector to lookup
	 * @param separator the regex to split the selector by
	 * @return the (Object) value that the selector points to
	 * @throws KeyError if you try anything stupid
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
			throw new KeyError(selector, ex.getMessage());
		}

	}

	/**
	 * Set's the final path at the end of a selector to a given value. Supports nested Dictionaries, and if a Dictionary on the path
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
