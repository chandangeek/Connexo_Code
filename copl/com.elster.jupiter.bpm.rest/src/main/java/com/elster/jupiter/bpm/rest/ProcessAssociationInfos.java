/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dragos on 2/26/2016.
 */
public class ProcessAssociationInfos {
    public int total;

    public List<ProcessAssociationInfo> associations = new ArrayList<>();

    public ProcessAssociationInfos() {
    }

    public ProcessAssociationInfos(Iterable<? extends ProcessAssociationInfo> associationInfos) {
        addAll(associationInfos);
    }

    void addAll(Iterable<? extends ProcessAssociationInfo> infos) {
        for (ProcessAssociationInfo each : infos) {
            add(each);
        }
    }

    public void add(ProcessAssociationInfo info) {
        associations.add(info);
        total++;
    }

    public List<ProcessAssociationInfo> getAssociations() {
        return associations;
    }
}
