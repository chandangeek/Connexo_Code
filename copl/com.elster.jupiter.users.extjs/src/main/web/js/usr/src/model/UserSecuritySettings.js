/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.UserSecuritySettings', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'id', type: 'int'},
        {name: 'lockAccountOption', type: 'boolean'},
        {name: 'failedLoginAttempts', type: 'int'},
        {name: 'lockOutMinutes', type: 'int'},
    ],
    proxy: {
        type: 'rest',
        url: '/api/usr/userSecuritySettings',
        reader1: {
            type: 'json',

        }
    }
});