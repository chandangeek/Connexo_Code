/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.privileges.CrlRequest', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.administer.crlRequest', 'privilege.view.crlRequest'],
    admin: ['privilege.administer.crlRequest'],

    all: function () {
        return Ext.Array.merge(Mdc.privileges.CrlRequest.view, Mdc.privileges.CrlRequest.admin);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.CrlRequest.view);
    },
    canEdit: function () {
        return Uni.Auth.checkPrivileges(Mdc.privileges.CrlRequest.admin);
    }
});
