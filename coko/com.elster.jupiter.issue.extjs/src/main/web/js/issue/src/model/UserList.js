/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.UserList', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'authenticationName',
            type: 'auto'
        },
        {
            name: 'name'
        },
        {
            name: 'description',
            type: 'auto'
        },
        {
            name: 'version',
            type: 'auto'
        },
        {
            name: 'groups',
            type: 'auto'
        }
    ]
});