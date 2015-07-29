package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lucian on 5/14/2015.
 */
public class FileImporterInfos {

    public int total;
    public List<FileImporterInfo> fileImporters = new ArrayList<>();

    public FileImporterInfos() {
    }

    public FileImporterInfos(List<FileImporterFactory> fileImporterFactories, Thesaurus thesaurus, PropertyUtils propertyUtils) {
        addAll(fileImporterFactories, thesaurus, propertyUtils);
    }

    private FileImporterInfo add(FileImporterFactory fileImporter, Thesaurus thesaurus, PropertyUtils propertyUtils) {
        FileImporterInfo result = new FileImporterInfo(fileImporter.getName(),
                thesaurus.getStringBeyondComponent(fileImporter.getName(), fileImporter.getName()),
                propertyUtils.convertPropertySpecsToPropertyInfos(fileImporter.getPropertySpecs()));
        fileImporters.add(result);
        total++;
        return result;
    }


    private void addAll(Iterable<? extends FileImporterFactory> importSchedules, Thesaurus thesaurus, PropertyUtils propertyUtils) {
        for (FileImporterFactory each : importSchedules) {
            add(each, thesaurus, propertyUtils);
        }
    }
}
