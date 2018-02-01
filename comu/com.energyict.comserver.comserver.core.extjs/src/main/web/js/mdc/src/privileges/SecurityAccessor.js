/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.privileges.SecurityAccessor', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['view.security.accessors', 'edit.security.accessors'],
    admin: ['edit.security.accessors'],
    all: function () {
        return Ext.Array.merge(Mdc.privileges.SecurityAccessor.view, Mdc.privileges.SecurityAccessor.admin);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.SecurityAccessor.view);
    },
    canAdmin: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.SecurityAccessor.admin);
    }
});
