/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.EstimationRuleOnChannel', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property',
        'Mdc.model.EstimationReadingType'
    ],

    fields: [
        'id',
        {name: 'active', type: 'boolean'},
        {name: 'deleted', type: 'boolean'},
        {name: 'estimatorImpl', type: 'string', defaultValue: null, useNull: true},
        {name: 'displayName', type: 'string'},
        {name: 'name', type: 'string', defaultValue: null, useNull: true, convert: function (value) {return value === '' ? null : value}},
        {name: 'properties', type: 'auto', defaultValue: null},
        {name: 'ruleSetId', type: 'auto'}
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
