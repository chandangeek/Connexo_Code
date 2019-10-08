/*
 * CommServerGuiJOfflineConfigEntries.java
 *
 * Created on 5 november 2003, 15:37
 */

package com.energyict.mdc.engine.offline.core.registry;

import com.energyict.mdc.engine.offline.gui.UiHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Koen
 */
public class ComServerMobileGuiJConfigEntries {

    private static Map<String, ConfigEntry> map = new HashMap<>();
    private static final String SYSTEM_FILES_DIRECTORY = "systemfilesdirectory";
    private static final String DATA_FILES_DIRECTORY = "datafilesdirectory";
    private static final String TEMP_SYSTEM_FILES_DIRECTORY = "tempsystemfilesdirectory";
    private static final String TEMP_DATA_FILES_DIRECTORY = "tempdatafilesdirectory";
    private static final String DATA_DIRECTORY = "data";

    static {
        map.put(SYSTEM_FILES_DIRECTORY, new ConfigEntry(SYSTEM_FILES_DIRECTORY, UiHelper.translate("mmr.workingDirectoryForSystemFiles"), ConfigEntry.TYPE_FILESTRING, getWorkingDir() + DATA_DIRECTORY));
        map.put(DATA_FILES_DIRECTORY, new ConfigEntry(DATA_FILES_DIRECTORY, UiHelper.translate("mmr.workingDirectoryForDataFiles"),ConfigEntry.TYPE_FILESTRING, getWorkingDir() + DATA_DIRECTORY));
        map.put(TEMP_SYSTEM_FILES_DIRECTORY, new ConfigEntry(TEMP_SYSTEM_FILES_DIRECTORY, UiHelper.translate("mmr.workingDirectoryForSystemFiles"), ConfigEntry.TYPE_FILESTRING, getWorkingDir() + DATA_DIRECTORY));
        map.put(TEMP_DATA_FILES_DIRECTORY, new ConfigEntry(TEMP_DATA_FILES_DIRECTORY, UiHelper.translate("mmr.workingDirectoryForDataFiles"),ConfigEntry.TYPE_FILESTRING, getWorkingDir() + DATA_DIRECTORY));
    }

    static public Map getMap() {
        return map;
    }

    static public String getWorkingDir() {
        File f = new File("");
        return (f.getAbsolutePath() + "\\");
    }
}