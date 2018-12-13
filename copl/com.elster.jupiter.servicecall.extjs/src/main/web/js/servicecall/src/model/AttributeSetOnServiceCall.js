/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.model.AttributeSetOnServiceCall', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        {name: 'id', type: 'integer'},
        {name: 'name', type: 'string'},
        {name: 'editable', type: 'boolean'},
        {name: 'version', type: 'number', persist: false},
        {name: 'parent', type: 'auto', persist: false}
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
