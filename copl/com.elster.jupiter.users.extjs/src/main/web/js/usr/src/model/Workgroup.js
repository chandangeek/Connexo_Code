/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.Workgroup', {
    extend: 'Ext.data.Model',
    requires: [
        'Usr.model.WorkgroupUser'
    ],
    fields: [
        'id',
        'name',
        'description',
        'version',
        'users'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Usr.model.WorkgroupUser',
            associationKey: 'users',
            name: 'users'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/usr/workgroups',
        reader1: {
            type: 'json',
            root: 'workGroups'
        }
    }
});