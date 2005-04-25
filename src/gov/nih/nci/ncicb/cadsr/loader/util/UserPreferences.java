package gov.nih.nci.ncicb.cadsr.loader.util;

import java.util.prefs.Preferences;
import java.util.*;

public class UserPreferences {

  Preferences prefs = Preferences.userRoot().node("UMLLOADER");

  public String getXmiDir() {
    return prefs.get("xmiDir","/");
  }

  public void setXmiDir(String dir) {
    prefs.put("xmiDir",dir);
  }

  public List<String> getRecentFiles() {
    return new ArrayList(Arrays.asList(prefs.get("recentFiles", "").split("\\$\\$")));

  }

  public void addRecentFile(String filePath) {
    List<String> files = getRecentFiles();
    
    if(!files.contains(filePath)) {
      if(files.size() > 4)
        files.remove(0);
      files.add(filePath);
    } else {
      if(files.size() > 4)
        files.remove(0);
      files.remove(filePath);
      files.add(filePath);
    }
    
    StringBuilder sb = new StringBuilder();
    for(String s : files) {
      if(sb.length() > 0)
        sb.append("$$");
      sb.append(s);
    }
    prefs.put("recentFiles", sb.toString());
  }

}
