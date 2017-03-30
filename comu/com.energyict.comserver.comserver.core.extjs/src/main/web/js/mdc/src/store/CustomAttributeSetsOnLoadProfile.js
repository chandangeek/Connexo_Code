/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CustomAttributeSetsOnLoadProfile', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.CustomAttributeSet',
    requires: [
        'Mdc.model.CustomAttributeSet'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/loadprofiletypes/custompropertysets',

        reader: {
            type: 'json',
            root: ''
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});