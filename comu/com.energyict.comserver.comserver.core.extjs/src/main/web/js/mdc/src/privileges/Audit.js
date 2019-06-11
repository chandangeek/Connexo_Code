/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.privileges.Audit', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    viewAuditLog: ['privilege.view.auditLog'],

    all: function () {
        return Ext.Array.merge(Mdc.privileges.Audit.viewAuditLog);
    },
    canViewAuditLog: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.Audit.viewAuditLog);
    }
});
