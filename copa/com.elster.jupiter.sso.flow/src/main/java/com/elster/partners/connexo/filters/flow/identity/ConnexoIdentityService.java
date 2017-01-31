/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.flow.identity;

import com.elster.partners.connexo.filters.flow.ConnexoUberfireSubject;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by dragos on 5/16/2016.
 */

@ApplicationScoped
public class ConnexoIdentityService {
    private final ThreadLocal<ConnexoUberfireSubject> subjectOnThisThread = new ThreadLocal<>();

    public void setSubject(ConnexoUberfireSubject subject) {
        this.subjectOnThisThread.set(subject);
    }

    public ConnexoUberfireSubject getSubject() {
        return this.subjectOnThisThread.get();
    }
}
