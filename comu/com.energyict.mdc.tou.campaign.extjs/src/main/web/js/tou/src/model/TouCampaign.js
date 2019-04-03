/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.model.TouCampaign', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property',
    ],
    fields: [{
            name: 'name',
            defaultValue: null
        }, {
            name: 'deviceType',
            defaultValue: null,
            convert: function (value, record) {
                return value;
            }
        }, {
            name: 'deviceGroup',
            defaultValue: null,
            convert: function (value, record) {
                return value;
            }
        }, {
            name: 'activationStart',
            type: 'int',
            defaultValue: 64.800,
            convert: function (value, record) {
                return value * 1000;
            }
        }, {
            name: 'activationEnd',
            type: 'int',
            useNull: true,
            defaultValue: 82.800,
            convert: function (value, record) {
                return value * 1000;
            }
        }, {
            name: 'calendar',
            defaultValue: null
        }, {
            name: 'activationOption',
            defaultValue: null
        }, {
            name: 'activationDate',
            type: 'int',
            defaultValue: null
        }, {
            name: 'updateType',
            defaultValue: null
        }, {
            name: 'validationTimeout',
            type: 'int',
            defaultValue: null
        }, {
            name: 'status'
        }, {
            name: 'devices'
        }, {
            name: 'timeBoundary'
        }, {
            name: 'startedOn',
            type: 'int',
            timeFormat: 'date'
        }, {
            name: 'finishedOn',
            type: 'int',
            timeFormat: 'date'
        }, {
            name: 'withUniqueCalendarName',
            type: 'boolean',
            defaultValue: false
        }

    ],
    associations: [{
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
            read: '/api/tou/toucampaigns',
            create: '/api/tou/toucampaigns/create',
            update: '/api/tou/toucampaigns/{touCampaignId}/edit'
        },
        reader: {
            type: 'json'
        },
        setUpdateUrl: function (value) {
            this.api.update = this.api.update.replace('{touCampaignId}', value);
            return this.api.update;
        },
        resetUpdateUrl: function () {
            this.api.update = '/api/tou/toucampaigns/{touCampaignId}/edit';
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