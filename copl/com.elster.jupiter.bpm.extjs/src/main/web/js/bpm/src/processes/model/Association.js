/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.model.Association', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property',
    ],
    fields: [
        {name: 'type', type: 'string'},
        {name: 'name', type: 'string'}
    ],
    associations: [
        {
            type: 'hasMany',
            name: 'properties',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        },
    ]
});