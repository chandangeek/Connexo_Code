/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.model.Estimator', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],

    idProperty: 'implementation',

    fields: [
        {name: 'implementation', type: 'string'},
        {name: 'displayName', type: 'string'},
        {name: 'properties', type: 'auto', defaultValue: null}
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
