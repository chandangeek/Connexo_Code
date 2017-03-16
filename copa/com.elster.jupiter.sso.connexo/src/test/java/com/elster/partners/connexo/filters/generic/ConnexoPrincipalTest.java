/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.generic;

import java.util.Arrays;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * Created by dragos on 2/1/2016.
 */
public class ConnexoPrincipalTest {
    @Test
    public void testUser(){
        ConnexoPrincipal principal = new ConnexoPrincipal(1, "TestUser", Arrays.asList("Role1", "Role2"), "token");

        assertTrue(principal.isValid());
        assertEquals(principal.getName(), "TestUser");
        assertEquals(principal.getToken(), "token");
        assertEquals(principal.getUserId(), 1);
        assertEquals(principal.getRoles(), Arrays.asList("Role1", "Role2"));
        assertTrue(principal.isUserInRole("Role1"));
        assertFalse(principal.isUserInRole("Role3"));
    }

    @Test
    public void testNullUser(){
        ConnexoPrincipal principal = new ConnexoPrincipal(1, null, Arrays.asList("Role1", "Role2"), "token");

        assertFalse(principal.isValid());
        assertNull(principal.getName());
    }

    @Test
    public void testNullRoles(){
        ConnexoPrincipal principal = new ConnexoPrincipal(1, "TestUser", null, "token");

        assertFalse(principal.isValid());
        assertNull(principal.getRoles());
    }

    @Test
    public void testEmptyRoles(){
        ConnexoPrincipal principal = new ConnexoPrincipal(1, "TestUser", Arrays.asList(), "token");

        assertFalse(principal.isValid());
        assertEquals(principal.getRoles().size(), 0);
    }
}
