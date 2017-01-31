/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ExecutionLevel', {
    extend: 'Uni.model.ParentVersion',
    fields: [
        {name: 'id',type:'string',useNull:true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'userRoles'}
    ],
    requires: [
        'Mdc.model.UserRole'
    ],
    associations: [
    {name: 'userRoles', type: 'hasMany', model: 'Mdc.model.UserRole', associationKey: 'userRoles',
        getTypeDiscriminator: function (node) {
            return 'Mdc.model.UserRole';
        }
    }
]
});
