/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.LoadProfileTypeOnDeviceType', {
    extend: 'Uni.model.ParentVersion',
    fields: [
        {name:'id', type: 'int', useNull: true},
        {name:'name', type: 'string'},
        {name:'obisCode', type: 'string'},
        {name:'timeDuration', type: 'auto', useNull: true},
        {name:'registerTypes', type: 'auto'},
        {name:'isLinkedToActiveDeviceConf', type: 'boolean'},
        {name:'customPropertySet', type: 'auto', defaultValue: null}
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/loadprofiletypes',
        reader: {
            type: 'json'
        },

        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', encodeURIComponent(deviceTypeId));
        }
    }
});