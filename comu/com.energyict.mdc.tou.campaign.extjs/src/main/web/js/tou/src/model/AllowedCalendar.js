/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.model.AllowedCalendar', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.model.timeofuse.Calendar'
    ],
    fields: [
        {name: 'name', type: 'string'},
        {name: 'ghost', type: 'boolean'},
        {
            name: 'status',
            persist: false,
            mapping: function (data) {
                if (data.ghost) {
                    return 'Ghost';
                } else {
                    return 'Final';
                }
            }
        }
    ],


    associations: [
        {
            name: 'calendar',
            type: 'hasOne',
            model: 'Uni.model.timeofuse.Calendar',
            getterName: 'getCalendar',
            associationKey: 'calendar',
            foreignKey: 'calendar',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.timeofuse.Calendar';
            }
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '../../api/dtc/devicetypes/{deviceTypeId}/timeofuse',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }

});