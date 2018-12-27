/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.model.TouCampaign', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property',
    ],
    fields: [
        {name: 'name', defaultValue: null},
        {name: 'deviceType', defaultValue: null, convert: function (value, record) {
            return value;
        }},
        {name: 'deviceGroup', defaultValue: null, convert: function (value, record) {
            return value;
        }},
        {name: 'activationStart', type: 'date', dateFormat: 'time',type: 'int', useNull: true, defaultValue: 64800},
        {name: 'activationEnd', type: 'date', dateFormat: 'time',type: 'int',useNull: true, defaultValue: 82800},
        {name: 'calendar', defaultValue: null},
        {name: 'activationDate'},
        {name: 'updateType',  defaultValue: null},
        {name: 'timeValidation', type: 'date', dateFormat: 'time', persist: false}

    ],
    associations: [
        {
            type: 'hasMany',
            name: 'properties',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties'
        }
    ],
    proxy: {
        type: 'rest',
        api: {
             read: '/api/tou/touCampaigns',
             create: '/api/tou/touCampaigns/create',
             update: '/api/tou/touCampaigns'
        },
        reader: {
            type: 'json'
        }
    },
    convertObjectField: function (value) {
        if (Ext.isObject(value) || value === null) {
            return value
        } else {
            return {
                id: value
            }
        }
    }
});