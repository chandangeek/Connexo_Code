/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        {name: 'id', type: 'integer'},
        {name: 'name', type: 'string'},
        {name: 'customPropertySetId', type: 'string'},
        {name: 'editable', type: 'boolean'},
        {name: 'timesliced', type: 'boolean'},
        {name: 'isActive', type: 'boolean'},
        {name: 'startTime', dateFormat: 'time', type: 'date'},
        {name: 'endTime', dateFormat: 'time', type: 'date'},
        {name: 'versionId', type: 'integer'},
        {name: 'objectTypeId', type: 'integer'},
        {name: 'objectTypeVersion', type: 'integer'}
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
