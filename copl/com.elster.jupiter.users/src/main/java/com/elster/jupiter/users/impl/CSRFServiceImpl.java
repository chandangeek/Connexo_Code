/*
 * Copyright (c) 2020  by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.CSRFService;

import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 1/30/2020 (15:53)
 */

@Component(
        name = "com.elster.jupiter.users.csrf",
        service = {CSRFService.class},
        immediate = true)
public class CSRFServiceImpl implements CSRFService {
    private Map<String, String> sessions = new ConcurrentHashMap<>();

    @Override
    public String getCSRFToken(String sessionId) {
        return  sessions.get(sessionId);
    }

    @Override
    public void addCSRFToken(String sessionId, String token) {
        if(sessions.get(sessionId) != null){
            sessions.replace(sessionId, token);
        } else {
            sessions.put(sessionId, token);
        }
    }
    @Override
    public void romoveToken(String sessionId) {
        sessions.remove(sessionId);
    }
}
