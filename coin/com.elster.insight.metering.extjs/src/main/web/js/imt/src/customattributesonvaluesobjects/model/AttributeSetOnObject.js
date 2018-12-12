/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetOnObject', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        {name: 'id', type: 'integer'},
        {name: 'customPropertySetId', type: 'string'},
        {name: 'parent', type: 'auto', defaultValue: null},
        {name: 'name', type: 'string'},
        {name: 'isEditable', type: 'boolean'},
        {name: 'isVersioned', type: 'boolean'},
        {name: 'isActive', type: 'boolean'},
        {name: 'startTime', type: 'auto', defaultValue: null},
        {name: 'endTime', type: 'auto', defaultValue: null},
        {name: 'versionId', type: 'integer'}
    ],

    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ]
});
