/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.LoadProfileTypesOnDeviceType', {
    extend: 'Uni.model.ParentVersion',
    fields: [
        {name:'id', type: 'int', useNull: true},
        {name:'name', type: 'string'},
        {name:'obisCode', type: 'string'},
        {name:'timeDuration', type: 'auto', useNull: true},
        {name:'registerTypes', type: 'auto'},
        {name:'isLinkedToActiveDeviceConf', type: 'boolean'}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceType}/loadprofiletypes',
        reader: {
            type: 'json'
        },
        setUrl: function (deviceType) {
            this.url = this.urlTpl.replace('{deviceType}', encodeURIComponent(deviceType));
        }
    }
});