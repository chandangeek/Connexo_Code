/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.store.ComTasksForValidate', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.firmwarecampaigns.model.ComTaskForValidate'
    ],
    autoLoad: false,
    model: 'Fwc.firmwarecampaigns.model.ComTaskForValidate',
    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/field/comtasks?type={deviceTypeId}',
        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        },
        reader: {
            type: 'json'
        }
    }
});