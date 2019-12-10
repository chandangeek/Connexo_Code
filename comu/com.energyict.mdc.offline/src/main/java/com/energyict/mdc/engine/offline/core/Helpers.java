/*
 * Helpers.java
 *
 * Created on 15 oktober 2003, 13:20
 */

package com.energyict.mdc.engine.offline.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Koen
 */
public class Helpers {

    private static final Log logger = LogFactory.getLog(Helpers.class);

    static public Class translateClass(Class cls) {
        if ("int".compareTo(cls.getName()) == 0) {
            cls = java.lang.Integer.class;
        }
        if ("long".compareTo(cls.getName()) == 0) {
            cls = java.lang.Long.class;
        }
        if ("char".compareTo(cls.getName()) == 0) {
            cls = java.lang.Character.class;
        }
        if ("byte".compareTo(cls.getName()) == 0) {
            cls = java.lang.Byte.class;
        }
        if ("double".compareTo(cls.getName()) == 0) {
            cls = java.lang.Double.class;
        }
        if ("float".compareTo(cls.getName()) == 0) {
            cls = java.lang.Float.class;
        }
        if ("short".compareTo(cls.getName()) == 0) {
            cls = java.lang.Short.class;
        }
        if ("boolean".compareTo(cls.getName()) == 0) {
            cls = java.lang.Boolean.class;
        }
        return cls;
    }

    static public BeanInfo getBeanInfo(Object instance) {
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(instance.getClass(), instance.getClass().getSuperclass());
        } catch (IntrospectionException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("FilePersist, getBeanInfo, " + e.getMessage());
        }
        return beanInfo;
    }

    /*
    * delete all files in a directory, relative to the working directory
    * @param directory name relative to the working directory
    */
    static public void deleteAllFiles(String directory) {
        File file = new File(directory);
        File[] files = file.listFiles();
        if (files != null) {
            for (File file1 : files) {
                file1.delete();
            }
        }
    }

    /*
    * delete a file or a directory relative to the working directory
    * @param str file or directory name
    */
    static public void delete(String str) {
        File file = new File(str);
        file.delete();
    }

    /*
    * create a file or a directory relative to the working directory
    * @param dir directory name to create
    */
    static public void createDirectory(String dir) {
        // Check directory and create if not exist
        File directory;
        directory = new File(dir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    static public boolean moveFiles(String sourceDir, String destinationDir) {
        File source = new File(sourceDir);
        File destination = new File(destinationDir);

        if (source.exists() && destination.exists()) {
            try {
                copyFolderRecursively(source, destination);
                deleteFolderRecursively(source, destination);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * This method handles also the case when the destination file is inside the source file.
     * In this particular case the destination file should not be recursively copied inside.
     * @param source
     * @param destination
     */
    static private void copyFolderRecursively(File source, File destination) throws IOException{
        if(source==null || destination==null)
            return;
        if(!source.isDirectory())
            return;
        if(destination.exists()){
            if(!destination.isDirectory()){
                return;
            }
        } else if (!destination.getAbsolutePath().startsWith(source.getAbsolutePath())){
            //do not create a new directory if the current folder is the destination folder.
            destination.mkdir();
        }
        if(source.listFiles()==null || source.listFiles().length==0)
            return;

        for(File currentSourceFile: source.listFiles()){
            File currentDestinationFile = new File(destination, currentSourceFile.getName());
            if(currentSourceFile.isDirectory() ){
                //in the current folder is the destination folder, skip it, otherwise this method will not stop
                if (!destination.getAbsolutePath().startsWith(currentSourceFile.getAbsolutePath())) {
                    copyFolderRecursively(currentSourceFile, currentDestinationFile);
                }
            }else{
                if(currentDestinationFile.exists())
                    continue;
                Files.copy(currentSourceFile.toPath(), currentDestinationFile.toPath());
            }
        }
    }

    /**
     * This method handles the deletion of a directory. It also handles the case when the destination directory
     * is inside the source directory.
     * @param source
     * @param destination
     */
    static private void deleteFolderRecursively(File source, File destination) {
        if (source == null)
            return;
        if (source.exists()) {
            for(File currentFile : source.listFiles()) {
                if(currentFile.isDirectory()) {
                    //in the current folder is the destination folder, skip it, since you do not want it to be deleted.
                    if(!destination.getAbsolutePath().startsWith(currentFile.getAbsolutePath())) {
                        deleteFolderRecursively(currentFile, destination);
                        currentFile.delete();
                    }
                } else {
                    currentFile.delete();
                }
            }
            source.delete();
        }
    }
}