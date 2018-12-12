/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.model.task.TaskForm', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [

        'status',
        'action',
        'id',
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
    ],
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/validateform{id}',
        reader: {
            type: 'json'
        },
        setUrl: function (newUrl) {
            this.url = newUrl;
        }
    }
});