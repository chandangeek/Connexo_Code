/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.properties.model.Properties', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'name', 'displayName', 'properties'
    ],
    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ]
});
