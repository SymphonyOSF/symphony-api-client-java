package com.symphony.bdk.core.util;


import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

@API(status = API.Status.EXPERIMENTAL)
public class StringUtils {

  private static final String[] EMPTY_STRING_ARRAY = {};

  public static boolean hasText(String str) {
    return (str != null && !str.isEmpty() && containsText(str));
  }

  private static boolean containsText(CharSequence str) {
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  public static String[] tokenizeToStringArray(
      String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

    if (str == null) {
      return EMPTY_STRING_ARRAY;
    }

    StringTokenizer st = new StringTokenizer(str, delimiters);
    List<String> tokens = new ArrayList<>();
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (trimTokens) {
        token = token.trim();
      }
      if (!ignoreEmptyTokens || token.length() > 0) {
        tokens.add(token);
      }
    }
    return toStringArray(tokens);
  }

  public static String[] toStringArray(Collection<String> collection) {
    return (!isEmpty(collection) ? collection.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY);
  }

  public static boolean isEmpty(Collection<?> collection) {
    return (collection == null || collection.isEmpty());
  }
}
