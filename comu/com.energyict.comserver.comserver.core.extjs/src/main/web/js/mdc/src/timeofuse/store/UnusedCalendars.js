/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.store.UnusedCalendars', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.model.timeofuse.Calendar'
    ],
    model: 'Uni.model.timeofuse.Calendar',
    storeId: 'unusedCalendarStore',
    proxy: {
        type: 'rest',
        urlTpl: "../../api/dtc/devicetypes/{deviceTypeId}/unusedcalendars",
        reader: {
            type: 'json',
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});