/*
 * FileManager.java
 *
 * Created on 1 oktober 2003, 10:15
 */

package com.energyict.mdc.engine.offline.persist;

import com.elster.jupiter.orm.Encrypter;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.comserver.OfflineComServer;
import com.energyict.mdc.engine.config.LookupEntry;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.ObjectParser;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.impl.core.remote.CollectionConverter;
import com.energyict.mdc.engine.impl.core.remote.ComServerParser;
import com.energyict.mdc.engine.impl.core.remote.ObjectMapperFactory;
import com.energyict.mdc.engine.impl.web.queryapi.QueryResult;
import com.energyict.mdc.engine.offline.MessageSeeds;
import com.energyict.mdc.engine.offline.core.Helpers;
import com.energyict.mdc.engine.offline.core.RegistryConfiguration;
import com.energyict.mdc.engine.users.OfflineUserInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that allows to save certain business objects (e.g. comserver & users information)
 * to encrypted txt files and also load these objects again, from the same txt files.
 */
public class FileManager implements BusinessDataPersister {

    private static final String UTF8 = "UTF-8";

    private static final String COMSERVER_TASKFILENAME = "task_";
    private static final String COMSERVER_QUERY_ID = "Comserver";
    private static final String COM_JOB_EXECUTION_MODEL_QUERY_ID = "ComJobExecutionModel";
    private static final String USER_INFO_QUERY_ID = "OfflineUserInfo";
    private static final String COMPLETION_CODES_QUERY_ID = "CompletionCodes";
    private static final String EXTENSION = ".txt";
    private static final String SEPARATOR = "/";
    private static final String COMSERVER = "comserver.txt";
    private static final String USERS = "offlinedata";
    private static final String COMPLETIONCODES = "completionCodes.txt";

    private static final String DATA_FILES_DIRECTORY_STRING = "datafilesdirectory";
    private static final String SYSTEM_FILES_DIRECTORY_STRING = "systemfilesdirectory";
    private static final String TEMP_DATA_FILES_DIRECTORY_STRING = "tempdatafilesdirectory";
    private static final String TEMP_SYSTEM_FILES_DIRECTORY_STRING = "tempsystemfilesdirectory";

    private static String DATA_FILES_DIRECTORY = RegistryConfiguration.getDefault().get(DATA_FILES_DIRECTORY_STRING);
    private static String SYSTEM_FILES_DIRECTORY = RegistryConfiguration.getDefault().get(SYSTEM_FILES_DIRECTORY_STRING);
    private static String TEMP_DATA_FILES_DIRECTORY = RegistryConfiguration.getDefault().get(TEMP_DATA_FILES_DIRECTORY_STRING);
    private static String TEMP_SYSTEM_FILES_DIRECTORY = RegistryConfiguration.getDefault().get(TEMP_SYSTEM_FILES_DIRECTORY_STRING);
    private static String COMSERVER_BOOTFILE;
    private static String USERS_FILE;
    private static String COMPLETION_CODES_FILE;
    private static String COMSERVER_TASKFILE;


    private List<OfflineUserInfo> cachedUserInfos = null;
    private List<LookupEntry> cachedCompletionCodes = null;
    private Encrypter encrypter;
    private boolean dataFilesDirectoryError = false;
    private boolean systemFilesDirectoryError = false;

    /**
     * Creates a new instance of FileManager
     */
    public FileManager() {
        initializeDirectories();
        encrypter = new FileEncrypter();
    }

    private void initializeDirectories() {
        Helpers.createDirectory(DATA_FILES_DIRECTORY);
        Helpers.createDirectory(SYSTEM_FILES_DIRECTORY);

        dataFilesDirectoryError = moveDirectoryIfNeeded(DATA_FILES_DIRECTORY, TEMP_DATA_FILES_DIRECTORY, DATA_FILES_DIRECTORY_STRING, TEMP_DATA_FILES_DIRECTORY_STRING);
        systemFilesDirectoryError = moveDirectoryIfNeeded(SYSTEM_FILES_DIRECTORY, TEMP_SYSTEM_FILES_DIRECTORY, SYSTEM_FILES_DIRECTORY_STRING, TEMP_SYSTEM_FILES_DIRECTORY_STRING);

        recomputePaths();
    }

    private boolean moveDirectoryIfNeeded(String source, String destination, String sourceKey, String destinationKey) {
        if (!source.equals(destination)) {
            Helpers.createDirectory(destination);
            if (Helpers.moveFiles(source, destination)) {
                RegistryConfiguration.getDefault().copyKeyValues(sourceKey, destinationKey);
                return false;
            } else {
                RegistryConfiguration.getDefault().copyKeyValues(destinationKey, sourceKey);
                return true;
            }
        }
        return false;
    }

    private void recomputePaths() {
        DATA_FILES_DIRECTORY = RegistryConfiguration.getDefault().get(DATA_FILES_DIRECTORY_STRING);
        SYSTEM_FILES_DIRECTORY = RegistryConfiguration.getDefault().get(SYSTEM_FILES_DIRECTORY_STRING);
        COMSERVER_BOOTFILE = SYSTEM_FILES_DIRECTORY + SEPARATOR + COMSERVER;
        USERS_FILE = SYSTEM_FILES_DIRECTORY + SEPARATOR + USERS;
        COMPLETION_CODES_FILE = SYSTEM_FILES_DIRECTORY + SEPARATOR + COMPLETIONCODES;
        COMSERVER_TASKFILE = DATA_FILES_DIRECTORY + SEPARATOR + COMSERVER_TASKFILENAME;
    }

    public boolean hasDirectoryInitializationError() {
        return dataFilesDirectoryError || systemFilesDirectoryError;
    }

    public String getDataDir() {
        return DATA_FILES_DIRECTORY;
    }

    public String getSystemDir() {
        return SYSTEM_FILES_DIRECTORY;
    }

    /**
     * Return the offline comserver business object that was stored (JSON marshalled) in a file, or null if the file doesn't exist/is corrupted
     */
    public OfflineComServer loadComServer() {
        String fileContent = getFileContent(COMSERVER_BOOTFILE);
        if (fileContent != null) {
            try {
                return (OfflineComServer) (new ComServerParser().parse(new JSONObject(fileContent)));
            } catch (JSONException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public List<OfflineUserInfo> loadUserInfos() {
        if (cachedUserInfos == null) {
            String fileContent = getFileContent(USERS_FILE);
            if (fileContent != null) {
                try {
                    OfflineUserInfo[] userInfos = toArrayObject(new JSONObject(fileContent), new ObjectParser<OfflineUserInfo[]>(), OfflineUserInfo[].class);
                    cachedUserInfos = CollectionConverter.convertGenericArrayToList(userInfos);
                } catch (JSONException e) {
                    cachedUserInfos = null;    //Corrupted file, will be fetched again later on
                }
            } else {
                cachedUserInfos = null;
            }
        }
        return cachedUserInfos == null ? Collections.emptyList() : cachedUserInfos;
    }

    @Override
    public void saveUserInfos(List<OfflineUserInfo> userInfos) {
        cachedUserInfos = userInfos;
        marshalAndStoreObject(userInfos, USER_INFO_QUERY_ID, USERS_FILE);
    }

    @Override
    public List<LookupEntry> loadCompletionCodes() {
        if (cachedCompletionCodes == null) {
            String fileContent = getFileContent(COMPLETION_CODES_FILE);
            if (fileContent != null) {
                try {
                    LookupEntry[] entries = toArrayObject(new JSONObject(fileContent), new ObjectParser<LookupEntry[]>(), LookupEntry[].class);
                    cachedCompletionCodes = CollectionConverter.convertGenericArrayToList(entries);
                } catch (JSONException e) {
                    cachedCompletionCodes = null; //Corrupted file, will be fetched again later on
                }
            } else {
                cachedCompletionCodes = null;
            }
        }
        return cachedCompletionCodes;
    }

    @Override
    public void saveCompletionCodes(List<LookupEntry> codes) {
        cachedCompletionCodes = codes;
        marshalAndStoreObject(codes, COMPLETION_CODES_QUERY_ID, COMPLETION_CODES_FILE);
    }

    /**
     * Read in the content of a file and decrypt it.
     * Return null if the file does not exist, or if it's an empty file
     */
    private String getFileContent(String path) {
        try {
            String cipheredContent = new String(Files.readAllBytes(Paths.get(path)), UTF8);
            if (cipheredContent.isEmpty()) {
                return null;
            } else {
                return new String(encrypter.decrypt(cipheredContent), UTF8);
            }
        } catch (IOException e) {
            return null;
        }
    }

    public void saveComServer(OfflineComServer offlineComServer) {
        marshalAndStoreObject(offlineComServer, COMSERVER_QUERY_ID, COMSERVER_BOOTFILE);
    }

    /**
     * Marshall the given object using JSON, encrypt the result and store it in a file.
     */
    private void marshalAndStoreObject(Object object, String queryId, String fileName) {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        try {
            QueryResult queryResult = QueryResult.forResult(queryId, object);
            mapper.writeValue(writer, queryResult);
        } catch (IOException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new ApplicationException("Error while JSON marshalling an object: " + cause.getMessage(), cause);
        }

        try {
            try (PrintWriter printWriter = new PrintWriter(new FileWriter(fileName))) {
                String encryptedContent = encrypter.encrypt(writer.toString().getBytes());
                printWriter.print(encryptedContent);
            }
        } catch (IOException e) {
            throw new ApplicationException("Error while writing to file '" + fileName + "': " + e);
        }
    }

    /**
     * Load every file that contains a serialized ComJobExecutionModel
     */
    public List<ComJobExecutionModel> loadComJobExecutionModels() {
        List<ComJobExecutionModel> result = new ArrayList<>();
        List<String> fileContents = getFileContents(COMSERVER_TASKFILENAME);

        for (String fileContent : fileContents) {
            try {
                result.add(toObject(new JSONObject(fileContent), new ObjectParser<ComJobExecutionModel>()));
            } catch (JSONException e) {
                //Move on to the next
            }
        }
        return result;
    }

    /**
     * Return the contents of every file in the data folder that starts with a given prefix (e.g. task_)
     */
    private List<String> getFileContents(String fileNamePrefix) {
        List<String> result = new ArrayList<>();
        File folder = new File(DATA_FILES_DIRECTORY);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory() && file.getName().startsWith(fileNamePrefix)) {
                    String fileContent = getFileContent(DATA_FILES_DIRECTORY + "/" + file.getName());
                    if (fileContent != null) {
                        result.add(fileContent);
                    }
                }
            }
        }
        return result;
    }

    private <T> T toObject(JSONObject response, ObjectParser<T> objectParser) {
        try {
            return response != null ? objectParser.parseObject(response, RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_RESULT) : null;
        } catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    private <T> T toArrayObject(JSONObject response, ObjectParser<T> objectParser, Class clazz) {
        try {
            return response != null
                    ? objectParser.parseArray(response, RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_RESULT, clazz)
                    : null;
        } catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    public void saveComJobModels(List<ComJobExecutionModel> comJobModels) {
        for (ComJobExecutionModel comJobModel : comJobModels) {
            saveComJobExecutionModel(comJobModel);
        }
    }

    @Override
    public void saveComJobExecutionModel(ComJobExecutionModel comJobExecutionModel) {
        marshalAndStoreObject(comJobExecutionModel, COM_JOB_EXECUTION_MODEL_QUERY_ID, COMSERVER_TASKFILE + String.valueOf(comJobExecutionModel.getDevice().getId()) + EXTENSION);
    }

}