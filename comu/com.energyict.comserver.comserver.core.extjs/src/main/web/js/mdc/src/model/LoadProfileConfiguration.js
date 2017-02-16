/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.LoadProfileConfiguration', {
    extend: 'Uni.model.ParentVersion',
    fields: [
        {name:'id', type: 'int', useNull: true},
        {name:'name', type: 'string'},
        {name:'obisCode', type: 'string'},
        {name:'overruledObisCode', type: 'string'},
        {name:'timeDuration', type: 'auto', defaultValue: null},
        {name:'channels', type: 'auto', defaultValue: null},
        {name: 'fakeId', persist: false, defaultValue: null}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{0}/deviceconfigurations/{1}/loadprofileconfigurations',
        reader: {
            type: 'json'
        },
        setUrl: function (deviceTypeId, deviceConfigurationId) {
            this.url = Ext.String.format(this.urlTpl, deviceTypeId, deviceConfigurationId);
        }
    }
});