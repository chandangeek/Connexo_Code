/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.store.AllowedCalendars', {
    extend: 'Ext.data.Store',
    requires: [
        'Tou.model.AllowedCalendar'
    ],
    autoLoad: false,
    model: 'Mdc.timeofuse.model.AllowedCalendar',
    storeId: 'allowedCalendarStore',
    proxy: {
        type: 'rest',
        urlTpl: '../../api/tou/touCampaigns/getoptions?type={deviceTypeId}',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});