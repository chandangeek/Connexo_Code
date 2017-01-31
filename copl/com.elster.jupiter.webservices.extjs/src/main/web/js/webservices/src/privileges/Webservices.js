/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Wss.privileges.Webservices
 *
 * Class that defines privileges for Webservices
 */

Ext.define('Wss.privileges.Webservices', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.view.webservices'],

    admin: ['privilege.administrate.webservices'],

    all: function () {
        return Ext.Array.merge(Wss.privileges.Webservices.view, Wss.privileges.Webservices.admin);
    },

    canView: function () {
        return Uni.Auth.checkPrivileges(Wss.privileges.Webservices.view);
    },

    canAdministrate: function() {
        return Uni.Auth.checkPrivileges(Wss.privileges.Webservices.admin);
    }
});