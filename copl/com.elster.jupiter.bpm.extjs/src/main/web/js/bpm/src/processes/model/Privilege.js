/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.model.Privilege', {
    extend: 'Ext.data.Model',
    requires: [
        'Bpm.processes.model.UserRole'
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
            model: 'Bpm.processes.model.UserRole',
            associationKey: 'userRoles',
            getTypeDiscriminator: function (node) {
                return 'Bpm.processes.model.UserRole';
            }
        }
    ]

});