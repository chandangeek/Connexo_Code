/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.model.SystemPropsInfo', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
            {
                name: 'id',
                type: 'int'
            },
    ],

    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/sp/systemproperties',
        reader: {
            type: 'json'
        }
    }
});
