/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.store.UsedCalendars', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.timeofuse.model.AllowedCalendar'
    ],
    autoLoad: false,
    model: 'Mdc.timeofuse.model.AllowedCalendar',
    storeId: 'usedCalendarStore',
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