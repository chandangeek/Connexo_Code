/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Sct.privileges.ServiceCallType
 *
 * Class that defines privileges for ServiceCallType
 */

Ext.define('Sct.privileges.ServiceCallType', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.administrate.serviceCallTypes',
        'privilege.view.serviceCallTypes'],

    admin: ['privilege.administrate.serviceCallTypes'],

    all: function () {
        return Ext.Array.merge(Sct.privileges.ServiceCallType.view);
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Sct.privileges.ServiceCallType.view);
    },

    canAdministrate: function() {
        return Uni.Auth.checkPrivileges(Sct.privileges.ServiceCallType.admin);
    }
});