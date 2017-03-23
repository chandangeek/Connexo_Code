/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CertificateInfos {

    public int total;
    public List<CertificateInfo> certificates = new ArrayList<>();

    public CertificateInfos() {
    }

//    public CertificateInfos(Iterable<? extends RecurrentTask> allTasks, Thesaurus thesaurus, TimeService timeService, Locale locale, Clock clock) {
//        for (RecurrentTask each : allTasks) {
//            if (each.getNextExecution() != null)  {
//                tasks.add(new TaskInfo(each, thesaurus, timeService, locale, clock));
//                total++;
//            }
//        }
//    }

}
