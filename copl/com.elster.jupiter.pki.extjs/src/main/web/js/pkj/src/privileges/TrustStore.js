/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.privileges.TrustStore', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: [/*'privilege.administrate.trustStores', 'privilege.view.trustStores'*/],
    admin: [/*'privilege.administrate.trustStores'*/],

    all: function () {
        return Ext.Array.merge(Pkj.privileges.TrustStore.view);
    },
    canView: function () {
        return true; // Uni.Auth.checkPrivileges(Sct.privileges.ServiceCallType.view);
    },

    canAdministrate: function() {
        return true; //Uni.Auth.checkPrivileges(Sct.privileges.ServiceCallType.admin);
    }
});