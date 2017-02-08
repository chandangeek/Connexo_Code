/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.model.task.OpenTask', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'status', 'action'
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
    ],
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/taskcontent',
        reader: {
            type: 'json'
        }
    }
});