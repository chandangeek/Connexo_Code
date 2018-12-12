/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.deviceconfigurationestimationrules.model.EstimationRule', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name:'active', type: 'boolean'},
        {name:'deleted', type: 'boolean'},
        {name:'implementation', type: 'string'},
        {name:'displayName', type: 'string'},
        {name:'name', type: 'string'},
        {name:'readingTypes', type: 'auto'},
        {name:'ruleSet', type: 'auto'}
    ],

    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function () {
                return 'Uni.property.model.Property';
            }
        }
    ]
});
