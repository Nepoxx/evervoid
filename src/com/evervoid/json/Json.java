package com.evervoid.json;

import static com.evervoid.utils.ResourceUtils.getResourceDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A Json node.
 */
public class Json implements Iterable<Json>, Jsonable
{
	/**
	 * A Json node can have multiple types: Object (string -> Json node mapping), List (Multiple Json nodes), String, Number
	 * (hybrid int/float), Boolean, or Null.
	 */
	public enum JsonType
	{
		/**
		 * Json representing a simple {@link Boolean} value.
		 */
		BOOLEAN,
		/**
		 * Json representing a {@link Collection} of elements (all also Jsons).
		 */
		LIST,
		/**
		 * Json representing a null value, check for NULL in order to avoid ugly null pointer exceptions.
		 */
		NULL,
		/**
		 * Json representing a {@link Number}, format is included in the Json.
		 */
		NUMBER,
		/**
		 * Json representing a {@link Jsonable} object.
		 */
		OBJECT,
		/**
		 * Json representing a simple {@link String}.
		 */
		STRING;
	}

	/**
	 * The format for Json decimals. determines the number of significant values after the decimal point.
	 */
	private static final DecimalFormat sDoubleFormat = new DecimalFormat("#.####################");
	/**
	 * Maximum length that a line can reach in .toPrettyPrint()
	 */
	private static final int sPrettyStringMaximumLength = 72;

	/**
	 * Parse a Json file and return a Json object
	 * 
	 * @param jsonFile
	 *            The Json file to parse
	 * @return The parsed representation
	 */
	public static Json fromFile(final File jsonFile)
	{
		String s = "";
		try {
			// I hate Java IO
			final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile)));
			String line;
			while ((line = reader.readLine()) != null) {
				s += line.trim() + "\n";
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		return fromString(s);
	}

	/**
	 * Parse a Json file and return a Json object
	 * 
	 * @param jsonFile
	 *            The Json file to parse
	 * @return The parsed representation
	 */
	public static Json fromFile(final String jsonFile)
	{
		return fromFile(new File(getResourceDir() + jsonFile.replace("/", File.separator)));
	}

	/**
	 * Parse a Json String and return a Json object
	 * 
	 * @param jsonString
	 *            The Json string to parse
	 * @return The parsed representation
	 */
	public static Json fromString(final String jsonString)
	{
		return new JsonParser(jsonString).parse();
	}

	/**
	 * @return A new null-valued Json node
	 */
	public static Json getNullNode()
	{
		return new Json(JsonType.NULL);
	}

	/**
	 * This node's boolean value, if it is a boolean
	 */
	private boolean aBoolean = false;
	/**
	 * This node's decimal value, if it is a number
	 */
	private double aDouble = 0;
	/**
	 * Holds whether a Json Number node has been initialized from an integer or a decimal value. Used in serialization to String
	 * to determine the representation to use.
	 */
	private boolean aFromInt = false;
	/**
	 * This node's int value, it is is a number
	 */
	private int aInt = 0;
	/**
	 * This node's list of Json nodes, if it is a list
	 */
	private List<Json> aList = null;
	/**
	 * This node's map of String -> Json nodes, if it is an object
	 */
	private Map<String, Json> aObject = null;
	/**
	 * This node's String value, if it is a String
	 */
	private String aString = "";
	/**
	 * This node's type
	 */
	private final JsonType aType;

	/**
	 * Creates a new Json node of type Object.
	 */
	public Json()
	{
		this(JsonType.OBJECT);
		aObject = new HashMap<String, Json>();
	}

	/**
	 * Creates a new Json node of type Boolean.
	 * 
	 * @param b
	 *            The boolean value of the node
	 */
	public Json(final boolean b)
	{
		this(JsonType.BOOLEAN);
		aBoolean = b;
	}

	/**
	 * Creates a new Json node of type List.
	 * 
	 * @param list
	 *            The list of Json nodes to contain
	 */
	public Json(final Collection<?> list)
	{
		if (list == null) {
			aType = JsonType.NULL;
		}
		else {
			aType = JsonType.LIST;
			aList = new ArrayList<Json>(list.size());
			for (final Object element : list) {
				final Json j = makeJson(element);
				if (j != null || element == null) {
					aList.add(j);
				}
			}
		}
	}

	/**
	 * Creates a new Json node of type Float.
	 * 
	 * @param number
	 *            The float value of the node
	 */
	public Json(final double number)
	{
		this(JsonType.NUMBER);
		aDouble = number;
		aInt = (int) Math.floor(number);
		aFromInt = false;
	}

	/**
	 * Creates a new Json node of type Float.
	 * 
	 * @param number
	 *            The float value of the node
	 */
	public Json(final float number)
	{
		this((double) number);
	}

	/**
	 * Creates a new Json node of type Integer.
	 * 
	 * @param integer
	 *            The int value of the node
	 */
	public Json(final int integer)
	{
		this(JsonType.NUMBER);
		aDouble = integer;
		aInt = integer;
		aFromInt = true;
	}

	/**
	 * Creates a new Json node of type List.
	 * 
	 * @param elements
	 *            The list of Json elements to contain
	 */
	public Json(final Jsonable[] elements)
	{
		if (elements == null) {
			aType = JsonType.NULL;
		}
		else {
			aType = JsonType.LIST;
			aList = new ArrayList<Json>(elements.length);
			for (final Jsonable j : elements) {
				aList.add(j.toJson());
			}
		}
	}

	/**
	 * Private constructor of typed Json nodes
	 * 
	 * @param type
	 *            The type of node
	 */
	private Json(final JsonType type)
	{
		aType = type;
	}

	/**
	 * Creates a new Json node of type Long.
	 * 
	 * @param number
	 *            The Long value of the node
	 */
	public Json(final Long number)
	{
		this(number.toString());
	}

	/**
	 * Creates a new Json node of type Object, with the specified map as attributes. T1's toString() method is used for the key
	 * part of the map, and the value is the Json representation of T2 instances.
	 * 
	 * @param map
	 *            The map of attributes that the object will contain
	 */
	public <T1, T2> Json(final Map<T1, T2> map)
	{
		if (map == null) {
			aType = JsonType.NULL;
		}
		else {
			aType = JsonType.OBJECT;
			aObject = new HashMap<String, Json>();
			for (final T1 key : map.keySet()) {
				setAttribute(key.toString(), map.get(key));
			}
		}
	}

	/**
	 * Creates a new Json node of type String
	 * 
	 * @param str
	 *            The String value of the node
	 */
	public Json(final String str)
	{
		if (str == null) {
			aType = JsonType.NULL;
		}
		else {
			aType = JsonType.STRING;
			aString = str;
		}
	}

	/**
	 * Compares two Json nodes.
	 * 
	 * @return True if and only if both nodes are equal.
	 */
	@Override
	public boolean equals(final Object json)
	{
		if (super.equals(json)) {
			return true;
		}
		if (!json.getClass().equals(this.getClass())) {
			return false;
		}
		return getHash().equals(((Json) json).getHash());
	}

	/**
	 * Retrieve an attribute in an Object node. Example: On {"a": "b"}, getAttribute("a") returns Json("b"). The String value of
	 * the Json will be all lower case.
	 * 
	 * @param attribute
	 *            The name of the attribute
	 * @return The Json node representing the attribute
	 */
	public Json getAttribute(final String attribute)
	{
		return aObject.get(attribute.toLowerCase());
	}

	/**
	 * Returns an Iterable over the attributes of an Object node. Always sorted in order to guarantee consistent output. This
	 * way, comparing two Json nodes is as easy as comparing their string representation.
	 * 
	 * @return An Iterable over the list of attributes of this Object node
	 */
	public Iterable<String> getAttributes()
	{
		final List<String> keys = new ArrayList<String>(aObject.keySet());
		Collections.sort(keys);
		return keys;
	}

	/**
	 * @return The boolean value of this node
	 */
	public Boolean getBoolean()
	{
		if (aType.equals(JsonType.NULL)) {
			return null;
		}
		return aBoolean;
	}

	/**
	 * Retrieve a Boolean attribute in an Object node. Equivalent to getAttribute(attribute).getBoolean()
	 * 
	 * @param attribute
	 *            The name of the attribute
	 * @return The boolean value stored at the specified attribute
	 */
	public Boolean getBooleanAttribute(final String attribute)
	{
		return getAttribute(attribute).getBoolean();
	}

	/**
	 * @return The double value of this node
	 */
	public Double getDouble()
	{
		if (aType.equals(JsonType.NULL)) {
			return null;
		}
		return aDouble;
	}

	/**
	 * Retrieve a Double attribute in an Object node. Equivalent to getAttribute(attribute).getDouble()
	 * 
	 * @param attribute
	 *            The name of the attribute
	 * @return The Double value stored at the specified attribute
	 */
	public Double getDoubleAttribute(final String attribute)
	{
		return getAttribute(attribute).getDouble();
	}

	/**
	 * @return The float value of this node
	 */
	public Float getFloat()
	{
		if (aType.equals(JsonType.NULL)) {
			return null;
		}
		return (float) aDouble;
	}

	/**
	 * Retrieve a Float attribute in an Object node. Equivalent to getAttribute(attribute).getFloat()
	 * 
	 * @param attribute
	 *            The name of the attribute
	 * @return The Float value stored at the specified attribute
	 */
	public Float getFloatAttribute(final String attribute)
	{
		return getAttribute(attribute).getFloat();
	}

	/**
	 * A representative hash that can be used for comparison purposes.
	 * 
	 * @return The hash value for this Json.
	 */
	public String getHash()
	{
		try {
			final byte[] digest = MessageDigest.getInstance("MD5").digest(toString().getBytes("UTF-8"));
			final StringBuilder hex = new StringBuilder(digest.length * 2);
			for (final byte element : digest) {
				hex.append(Character.forDigit((element & 0xf0) >> 4, 16));
				hex.append(Character.forDigit(element & 0x0f, 16));
			}
			return hex.toString();
		}
		catch (final Exception e) {
			// This will never happen
		}
		return "";
	}

	/**
	 * @return The int value of this node
	 */
	public Integer getInt()
	{
		if (aType.equals(JsonType.NULL)) {
			return null;
		}
		return aInt;
	}

	/**
	 * Retrieve an Integer attribute in an Object node. Equivalent to getAttribute(attribute).getInt()
	 * 
	 * @param attribute
	 *            The name of the attribute
	 * @return The Integer value stored at the specified attribute
	 */
	public int getIntAttribute(final String attribute)
	{
		return getAttribute(attribute).getInt();
	}

	/**
	 * @return The list of children Json nodes in a Json list
	 */
	public List<Json> getList()
	{
		if (aType.equals(JsonType.NULL)) {
			return null;
		}
		return aList;
	}

	/**
	 * Retrieve a List attribute in an Object node. Equivalent to iterating on getAttribute(attribute)
	 * 
	 * @param attribute
	 *            The name of the attribute
	 * @return An iterable object corresponding to the List stored at the specified attribute
	 */
	public List<Json> getListAttribute(final String attribute)
	{
		return getAttribute(attribute).getList();
	}

	/**
	 * Returns an element from a Json list
	 * 
	 * @param index
	 *            The index of the element
	 * @return The element at the specified index
	 */
	public Json getListItem(final int index)
	{
		return aList.get(index);
	}

	/**
	 * @return The Long value of this node
	 */
	public Long getLong()
	{
		if (aType.equals(JsonType.NULL)) {
			return null;
		}
		return new Long(aString);
	}

	/**
	 * Retrieve a Long attribute in an Object node. Equivalent to getAttribute(attribute).getLong()
	 * 
	 * @param attribute
	 *            The name of the attribute
	 * @return The Long value stored at the specified attribute
	 */
	public Long getLongAttribute(final String attribute)
	{
		return getAttribute(attribute).getLong();
	}

	/**
	 * @return The string value of this node
	 */
	public String getString()
	{
		if (aType.equals(JsonType.NULL)) {
			return null;
		}
		return aString;
	}

	/**
	 * Retrieve a String attribute in an Object node. Equivalent to getAttribute(attribute).getString()
	 * 
	 * @param attribute
	 *            The name of the attribute
	 * @return The String value stored at the specified attribute
	 */
	public String getStringAttribute(final String attribute)
	{
		return getAttribute(attribute).getString();
	}

	/**
	 * Retrieve a List of Strings attribute in an Object node. Equivalent to iterating on getAttribute(attribute) and converting
	 * to String at each iteration
	 * 
	 * @param attribute
	 *            The name of the attribute
	 * @return An iterable object corresponding to the List of Strings stored at the specified attribute
	 */
	public List<String> getStringListAttribute(final String attribute)
	{
		final Json child = getAttribute(attribute);
		final List<String> l = new ArrayList<String>(child.aList.size());
		for (final Json j : child.aList) {
			l.add(j.getString());
		}
		return l;
	}

	/**
	 * @return The type of this node
	 */
	public JsonType getType()
	{
		return aType;
	}

	/**
	 * Returns whether this Object node has the specified attribute
	 * 
	 * @param attribute
	 *            The attribute to query
	 * @return Whether this Object node has the attribute or not
	 */
	public boolean hasAttribute(final String attribute)
	{
		return isObject() && aObject.containsKey(attribute);
	}

	/**
	 * @return Whether this Json node is a Boolean or not
	 */
	public boolean isBoolean()
	{
		return aType.equals(JsonType.BOOLEAN);
	}

	/**
	 * @return Whether this Json node is a List or not
	 */
	public boolean isList()
	{
		return aType.equals(JsonType.LIST);
	}

	/**
	 * @return Whether this node is null or not
	 */
	public boolean isNull()
	{
		return aType.equals(JsonType.NULL);
	}

	/**
	 * @return Whether this Json node is a Number or not
	 */
	public boolean isNumber()
	{
		return aType.equals(JsonType.NUMBER);
	}

	/**
	 * @return Whether this Json node is an Object or not
	 */
	public boolean isObject()
	{
		return aType.equals(JsonType.OBJECT);
	}

	/**
	 * @return Whether this Json node is a String or not
	 */
	public boolean isString()
	{
		return aType.equals(JsonType.STRING);
	}

	/**
	 * Iterates over the Json nodes contained in a List node.
	 */
	@Override
	public Iterator<Json> iterator()
	{
		if (aType.equals(JsonType.LIST)) {
			return aList.iterator();
		}
		return null;
	}

	/**
	 * Attempts to created a Json representation of the object. Json can only contain certain values, so the element must be one
	 * of a set of types; the ones currently accepted are: Jsonable, Integer, Double, Boolean, String, Float. If the element is
	 * null, they Json receives a JsonNullNode. If the element is not within these accepted types, the function returns a null.
	 * 
	 * @param element
	 *            The object to be converted into a Json.
	 * @return the Json representation of the object if it can be created, null otherwise.
	 */
	public Json makeJson(final Object element)
	{
		final Json j;
		if (element == null) {
			j = Json.getNullNode();
		}
		else if (element instanceof Jsonable) {
			j = ((Jsonable) element).toJson();
		}
		else if (element instanceof Integer) {
			j = new Json((Integer) element);
		}
		else if (element instanceof Double) {
			j = new Json((Double) element);
		}
		else if (element instanceof Boolean) {
			j = new Json((Boolean) element);
		}
		else if (element instanceof String) {
			j = new Json((String) element);
		}
		else if (element instanceof Float) {
			j = new Json((Float) element);
		}
		else {
			j = null;
		}
		return j;
	}

	/**
	 * Sets a Json attribute with the appropriate key. Calls makeJson to create a Json from the element. If the element is not
	 * within the types that makeJson accepts, no elemnet is added to it.
	 * 
	 * @param key
	 *            The name of the attribute.
	 * @param element
	 *            The element to set
	 * @return This Json with the new element (for chainability); null if the setting failed.
	 */
	public Json setAttribute(final String key, final Object element)
	{
		aObject.put(key.toLowerCase(), makeJson(element));
		return this;
	}

	/**
	 * Set an attribute in an Object node to a List of Json nodes
	 * 
	 * @param key
	 *            The name of the attribute
	 * @param elements
	 *            The list of nodes that the attribute should contain
	 * @return This (for chainability)
	 */
	public Json setListAttribute(final String key, final Collection<?> elements)
	{
		final Json j;
		if (elements == null) {
			j = new Json();
		}
		else {
			j = new Json(JsonType.LIST);
			j.aList = new ArrayList<Json>(elements.size());
			for (final Object elem : elements) {
				j.aList.add(makeJson(elem));
			}
		}
		return setAttribute(key, j);
	}

	/**
	 * Set an attribute in an Object node to a Map of elements
	 * 
	 * @param key
	 *            The name of the attribute
	 * @param map
	 *            The map of String -> Json elements
	 * @return This (for chainability)
	 */
	public <T1, T2> Json setMapAttribute(final String key, final Map<T1, T2> map)
	{
		return setAttribute(key, new Json(map));
	}

	/**
	 * @return How many elements are in this List or Object node
	 */
	public int size()
	{
		if (isObject()) {
			return aObject.size();
		}
		if (isList()) {
			return aList.size();
		}
		return 0;
	}

	/**
	 * Writes this Json node to a File
	 * 
	 * @param file
	 *            The File to write to
	 * @return Whether the write was successful or not
	 */
	public boolean toFile(final File file)
	{
		try {
			new FileOutputStream(file).write(toString().getBytes("UTF-8"));
			return true;
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Writes this Json node to a file
	 * 
	 * @param filename
	 *            The name of the file to write to
	 * @return Whether the write was successful or not
	 */
	public boolean toFile(final String filename)
	{
		return toFile(new File(filename));
	}

	/**
	 * Json objects themselves implement Jsonable. This has no effects but makes method signatures much lighter, to avoid
	 * overloading methods with both Json and Jsonable arguments.
	 * 
	 * @return This
	 */
	@Override
	public Json toJson()
	{
		return this; // That was hard
	}

	/**
	 * Serialize this Json object to a human-readable Json string
	 * 
	 * @return A pretty Json string
	 */
	public String toPrettyString()
	{
		return toPrettyString("");
	}

	/**
	 * Serialize this Json object to a human-readable Json string
	 * 
	 * @param prefix
	 *            Prefix that will be prepended to each line
	 * @return A pretty Json string
	 */
	public String toPrettyString(final String prefix)
	{
		final String plain = toString();
		if (plain.length() < sPrettyStringMaximumLength) {
			return plain;
		}
		switch (aType) {
			case NUMBER:
			case STRING:
			case BOOLEAN:
			case NULL:
				// Same as regular string representation
				return toString();
			case LIST:
				if (aList.isEmpty()) {
					return "[]";
				}
				String str = "[";
				for (final Json j : aList) {
					str += "\n" + prefix + "\t" + j.toPrettyString(prefix + "\t") + ",";
				}
				return str.substring(0, str.length() - 1) + "\n" + prefix + "]";
			case OBJECT:
				if (aObject.isEmpty()) {
					return "{}";
				}
				String obj = "{";
				for (final String key : getAttributes()) {
					obj += "\n" + prefix + "\t" + JsonParser.keyString(key) + aObject.get(key).toPrettyString(prefix + "\t")
							+ ",";
				}
				return obj.substring(0, obj.length() - 1) + "\n" + prefix + "}";
		}
		return "{}";
	}

	/**
	 * Serialize this Json object to a Json string.
	 * 
	 * @return A Json string.
	 */
	@Override
	public String toString()
	{
		switch (aType) {
			case NUMBER:
				if (aFromInt) {
					return String.valueOf(aInt);
				}
				return sDoubleFormat.format(aDouble);
			case STRING:
				return JsonParser.sanitizeString(aString);
			case BOOLEAN:
				return Boolean.toString(aBoolean);
			case NULL:
				return "null";
			case LIST:
				if (aList.isEmpty()) {
					return "[]";
				}
				String str = "[";
				for (final Json j : aList) {
					str += j.toString() + ", ";
				}
				return str.substring(0, str.length() - 2) + "]";
			case OBJECT:
				if (aObject.isEmpty()) {
					return "{}";
				}
				String obj = "{";
				for (final String key : getAttributes()) {
					obj += JsonParser.keyString(key) + aObject.get(key) + ", ";
				}
				return obj.substring(0, obj.length() - 2) + "}";
		}
		return "{}";
	}
}
