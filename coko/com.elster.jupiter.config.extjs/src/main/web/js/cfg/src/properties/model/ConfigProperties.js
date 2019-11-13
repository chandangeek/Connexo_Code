/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.properties.model.ConfigProperties', {
    extend: 'Ext.data.Model',
    requires: [
        'Cfg.properties.model.Properties'
    ],

    fields: [
        {name: 'type', type: 'auto'},
    ],

    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Apr.model.TaskProperties',
            associationKey: 'properties',
            foreignKey: 'properties',
            getterName: 'getProperties',
            setterName: 'setProperties',
            getTypeDiscriminator: function (node) {
                return 'Cfg.properties.model.Properties';
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/mtr/cfgprops',
        reader: {
            type: 'json'
        }
    }
});