package models;

import java.util.LinkedHashMap;
import java.util.Map;

public enum EntryType { 
    
    BLOCK, SCROLL, FLOW; 
    
    public static Map<String,String> options() {
        LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
        for (EntryType e : EntryType.values())
            options.put(e.name(), e.name());
        return options;
    }
    
}


