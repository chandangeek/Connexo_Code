/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Scs.privileges.ServiceCall
 *
 * Class that defines privileges for ServiceCall
 */

Ext.define('Scs.privileges.ServiceCall', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.view.serviceCalls'],

    admin: ['privilege.administrate.serviceCall'],

    all: function () {
        return Ext.Array.merge(Scs.privileges.ServiceCall.view, Scs.privileges.ServiceCall.admin);
    },

    canView: function () {
        return Uni.Auth.checkPrivileges(Scs.privileges.ServiceCall.view);
    },

    canAdministrate: function() {
        return Uni.Auth.checkPrivileges(Scs.privileges.ServiceCall.admin);
    }
});