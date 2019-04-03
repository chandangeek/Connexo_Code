/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.model.AllowedDeviceTypeOption', {
    extend: 'Ext.data.Model',
    requires: [
        'Tou.model.AllowedCalendar', 'Tou.model.DeviceType'
    ],
    fields: [{
            name: 'withActivationDate',
            type: 'boolean'
        }, {
            name: 'fullCalendar',
            type: 'boolean'
        }, {
            name: 'specialDays',
            type: 'boolean'
        },

    ],

    associations: [{
            name: 'calendars',
            type: 'hasMany',
            model: 'Tou.model.AllowedCalendar',
            getterName: 'getCalendar',
            associationKey: 'calendars',
            foreignKey: 'calendars',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.timeofuse.Calendar';
            }
        }, {
            name: 'deviceType',
            type: 'hasOne',
            model: 'Tou.model.DeviceType',
            associationKey: 'deviceType'
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '../../api/tou/toucampaigns/getoptions?type={deviceTypeId}',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }

});