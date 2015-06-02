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

    public FileImporterInfos(List<FileImporterFactory> fileImporterFactories, Thesaurus thesaurus) {
        addAll(fileImporterFactories,thesaurus);
    }
    public FileImporterInfo add(FileImporterFactory fileImporter, Thesaurus thesaurus) {
        FileImporterInfo result = new FileImporterInfo(fileImporter.getName(),
                thesaurus.getStringBeyondComponent(fileImporter.getName(),fileImporter.getName()),
                new PropertyUtils().convertPropertySpecsToPropertyInfos(fileImporter.getPropertySpecs()));
        fileImporters.add(result);
        total++;
        return result;
    }



    public void addAll(Iterable<? extends FileImporterFactory> importSchedules, Thesaurus thesaurus) {
        for (FileImporterFactory each : importSchedules) {
            add(each, thesaurus);
        }
    }
}
