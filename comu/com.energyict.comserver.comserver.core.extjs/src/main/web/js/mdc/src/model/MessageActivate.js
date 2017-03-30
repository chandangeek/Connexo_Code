/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.MessageActivate', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'messageIds', type: 'auto'},
        {name: 'privileges', type: 'auto'},
        {name: 'deviceConfiguration', type: 'auto', defaultValue: null}
    ],
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/devicemessageenablements',
        reader: {
            type: 'json',
            root: 'categories'
        }
    }
});