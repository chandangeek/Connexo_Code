/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import java.util.Base64;


class BasicAuthenticationCredentials {

    private final String userName;

    BasicAuthenticationCredentials(String authentication){
        String[] tokens = parseBase64Authentication(authentication);
        this.userName = tokens[0];
    }

    String getUserName(){
        return userName;
    }

    private String[] parseBase64Authentication(String authentication) {
        if (!hasText(authentication)) {
            throw new IllegalArgumentException("Empty basic authentication string");
        }

        String decoded = new String(Base64.getDecoder().decode(authentication));
        int index = decoded.indexOf(':');
        if (index == -1) {
            throw new IllegalArgumentException("Invalid basic authentication string");
        }

        return new String[]{decoded.substring(0, index), decoded.substring(index + 1)};
    }

    private boolean hasText(String str) {
        return str != null && !str.isEmpty() && !str.trim().isEmpty();
    }
}
