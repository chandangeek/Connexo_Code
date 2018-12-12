/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.model.Privilege', {
    extend: 'Ext.data.Model',
    requires: [
        'Dbp.processes.model.UserRole'
    ],
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'applicationName',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'userRoles'
        }
    ],
    associations: [
        {
            name: 'userRoles',
            type: 'hasMany',
            model: 'Dbp.processes.model.UserRole',
            associationKey: 'userRoles',
            getTypeDiscriminator: function (node) {
                return 'Dbp.processes.model.UserRole';
            }
        }
    ]

});