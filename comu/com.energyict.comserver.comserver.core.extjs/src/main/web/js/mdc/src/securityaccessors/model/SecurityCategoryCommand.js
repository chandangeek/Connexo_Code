/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.securityaccessors.model.SecurityCategoryCommand', {
    extend: 'Ext.data.Model',
    requires: [

    ],
    fields: [
        {name: 'id', type: 'string'},
        {name: 'name', type: 'string'},
        'properties',
        'serviceProperties'
    ],
    hasMany: [{
        name: 'properties',
        model: 'Uni.property.model.Property'
    },
    {
            name: 'serviceProperties',
            model: 'Uni.property.model.Property'
    }]
});