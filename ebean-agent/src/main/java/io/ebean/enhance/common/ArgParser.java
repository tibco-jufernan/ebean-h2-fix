package io.ebean.enhance.common;

import java.util.HashMap;

/**
 * Helper to parse javaagent or ant string arguments.
 */
class ArgParser {

  /**
  * Parse the args returning as name value pairs.
  */
  static HashMap<String,String> parse(String args){

    HashMap<String,String> map = new HashMap<>();

    if (args != null){
      String[] split = args.split(";");
      for (String nameValuePair : split) {
        String[] nameValue = nameValuePair.split("=");
        if (nameValue.length == 2){
          map.put(nameValue[0].toLowerCase(), nameValue[1]);
        }
      }
    }

    return map;
  }
}
