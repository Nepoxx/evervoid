package com.evervoid.json;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Json strings and returns Json objects. Regular usage: Json results = new JsonParser(jsonString).parse();
 */
public class JsonParser
{
    /**
     * Matches comments
     */
    private static Pattern sCommentPattern = Pattern.compile("^//[^\\r\\n]*|^/\\*[\\s\\S]*?\\*/");
    /**
     * Matches float numbers
     */
    private static Pattern sFloatPattern = Pattern.compile("^-?\\d*\\.\\d+");
    /**
     * Matches integers
     */
    private static Pattern sIntPattern = Pattern.compile("^-?\\d+");
    /**
     * Matches the key part of a key -> value mapping ("key": value)
     */
    private static Pattern sObjectKeyPattern = Pattern
                    .compile("^([\\\"']?[^:\\\"'\\\\]*(?:\\\\.[^:\"'\\\\]*)*[\"']?)\\s*:\\s*");
    /**
     * Matches a double-quoted string
     */
    private static Pattern sStringDoublePattern = Pattern.compile("^\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"");
    /**
     * Matches a single-quoted string
     */
    private static Pattern sStringSinglePattern = Pattern.compile("^'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'");

    /**
     * Takes a plain string and returns a proper key string for use in a Json object
     * 
     * @param s
     *            The string to sanitize
     * @return A String suitable for usage as key in a Json object
     */
    public static String keyString(final String s)
    {
        final String cleaner = s.trim().toLowerCase();
        if (cleaner.contains("\"") || cleaner.contains("'") || cleaner.contains(" ")) {
            return sanitizeString(cleaner) + ": ";
        }
        return cleaner + ": ";
    }

    /**
     * Takes a key string and returns a plain string
     * 
     * @param s
     *            The key string
     * @return The corresponding plain string
     */
    public static String plainKeyString(final String s)
    {
        final String trimmed = s.trim();
        if (trimmed.startsWith("\"") || trimmed.startsWith("'")) {
            return trimmed.substring(1, trimmed.length() - 1).replace("\\\\", "\\").replace("\\\"", "\"")
                            .replace("\r", "").replace("\\n", "\n");
        }
        return trimmed;
    }

    /**
     * Takes a double-quoted, escaped string and returns a plain string
     * 
     * @param s
     *            The sanitized string
     * @return The plain string
     */
    public static String plainString(final String s)
    {
        return s.substring(1, s.length() - 1).replace("\\\\", "\\").replace("\\\"", "\"").replace("\r", "")
                        .replace("\\n", "\n");
    }

    /**
     * Takes a plain string and returns a double-quoted, escaped string
     * 
     * @param s
     *            The string to sanitize
     * @return The sanitized string
     */
    public static String sanitizeString(final String s)
    {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n") + "\"";
    }

    /**
     * Removes leading Json comments from given string
     * 
     * @param str
     *            The string to remove leading comments from
     * @return The string without leading comments
     */
    private static String stripLeadingComments(final String str)
    {
        String trimmed = str.trim();
        Matcher commentMatcher = sCommentPattern.matcher(trimmed);
        while (commentMatcher.find()) {
            trimmed = trimmed.substring(commentMatcher.end()).trim();
            commentMatcher = sCommentPattern.matcher(trimmed);
        }
        return trimmed;
    }

    /**
     * The raw unparsed string.
     */
    private final String aRawString;

    /**
     * Creates a new Json parser
     * 
     * @param jsonString
     *            The string that this parser is meant to parse
     */
    JsonParser(final String jsonString)
    {
        aRawString = jsonString.trim();
    }

    /**
     * Parses the string provided in the constructor
     * 
     * @return The Json object representing the string
     */
    Json parse()
    {
        return parseString(aRawString).getJson();
    }

    /**
     * Parses a string partially
     * 
     * @param str
     *            The string to parse
     * @return A parsing result object containing the parsed Json node and the length of the parsed portion of the
     *         String
     */
    private JsonParsingResult parseString(final String str)
    {
        final String trimmed = stripLeadingComments(str);
        // Try object:
        if (trimmed.startsWith("{")) {
            final int initialLength = trimmed.length();
            String dict = stripLeadingComments(trimmed.substring(1));
            final Json node = new Json();
            while (!dict.startsWith("}")) {
                final Matcher keyMatch = sObjectKeyPattern.matcher(dict);
                if (keyMatch.find()) {
                    dict = stripLeadingComments(dict.substring(keyMatch.group().length()));
                    final JsonParsingResult result = parseString(dict);
                    node.setAttribute(plainKeyString(keyMatch.group(1)), result.getJson());
                    dict = stripLeadingComments(dict.substring(result.getOffset()));
                    if (dict.startsWith(",")) {
                        dict = stripLeadingComments(dict.substring(1));
                    }
                } else {
                    break;
                }
            }
            return new JsonParsingResult(node, initialLength - dict.length() + 1);
        }
        // Try boolean:
        if (trimmed.startsWith("true")) {
            return new JsonParsingResult(new Json(true), 4);
        }
        if (trimmed.startsWith("false")) {
            return new JsonParsingResult(new Json(false), 5);
        }
        // Try null:
        if (trimmed.startsWith("null")) {
            return new JsonParsingResult(Json.getNullNode(), 4);
        }
        // Try string:
        if (trimmed.startsWith("\"")) {
            final Matcher stringDoubleMatcher = sStringDoublePattern.matcher(trimmed);
            if (stringDoubleMatcher.find()) {
                return new JsonParsingResult(new Json(plainString(stringDoubleMatcher.group())),
                                stringDoubleMatcher.group());
            }
        }
        if (trimmed.startsWith("'")) {
            final Matcher stringSingleMatcher = sStringSinglePattern.matcher(trimmed);
            if (stringSingleMatcher.find()) {
                return new JsonParsingResult(new Json(plainString(stringSingleMatcher.group())),
                                stringSingleMatcher.group());
            }
        }
        // Try list:
        if (trimmed.startsWith("[")) {
            final int initialLength = trimmed.length();
            String list = stripLeadingComments(trimmed.substring(1));
            final List<Json> results = new ArrayList<Json>();
            while (!list.startsWith("]")) {
                final JsonParsingResult result = parseString(list);
                results.add(result.getJson());
                list = list.substring(result.getOffset()).trim();
                if (list.startsWith(",")) {
                    list = stripLeadingComments(list.substring(1));
                } else {
                    break;
                }
            }
            return new JsonParsingResult(new Json(results), initialLength - list.length() + 1);
        }
        // Try float:
        final Matcher floatMatcher = sFloatPattern.matcher(trimmed);
        if (floatMatcher.find()) {
            return new JsonParsingResult(new Json(Float.valueOf(floatMatcher.group())), floatMatcher.group());
        }
        // Try int:
        final Matcher intMatcher = sIntPattern.matcher(trimmed);
        if (intMatcher.find()) {
            return new JsonParsingResult(new Json(Integer.valueOf(intMatcher.group())), intMatcher.group());
        }
        // If all fails, return a blank node
        return new JsonParsingResult(new Json(), str);
    }
}
