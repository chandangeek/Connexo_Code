/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.EstimationRule', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property',
        'Mdc.model.EstimationReadingType'
    ],

    fields: [
        'id',
        {name: 'active', type: 'boolean'},
        {name: 'deleted', type: 'boolean'},
        {name: 'implementation', type: 'string', defaultValue: null, useNull: true},
        {name: 'displayName', type: 'string'},
        {name: 'name', type: 'string', defaultValue: null, useNull: true, convert: function (value) {return value === '' ? null : value}},
        {name: 'properties', type: 'auto', defaultValue: null},
        {name: 'readingTypes', type: 'auto', defaultValue: null},
        {name: 'ruleSet', type: 'auto', defaultValue: null}
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
        },
        {
            type: 'hasMany',
            model: 'Mdc.model.EstimationReadingType',
            associationKey: 'readingTypes',
            name: 'readingTypes'
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/est/estimation/{ruleSetId}/rules',
        reader: {
            type: 'json'
        },

        setUrl: function (ruleSetId) {
            this.url = this.urlTpl.replace('{ruleSetId}', ruleSetId);
        }
    }
});
