/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Cal.privileges.Calendar
 *
 * Class that defines privileges for Calendar
 */

Ext.define('Cal.privileges.Calendar', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    admin: ['privilege.administrate.touCalendars'],

    all: function () {
        return Ext.Array.merge(Cal.privileges.Calendar.admin);
    },

    canAdministrate: function () {
        return Uni.Auth.checkPrivileges(Cal.privileges.Calendar.admin);
    }
});