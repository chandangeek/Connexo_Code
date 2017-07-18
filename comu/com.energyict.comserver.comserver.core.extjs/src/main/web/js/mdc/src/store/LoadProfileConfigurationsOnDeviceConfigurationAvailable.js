/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.LoadProfileConfigurationsOnDeviceConfigurationAvailable', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LoadProfileType'
    ],
    autoLoad: false,
    model: 'Mdc.model.LoadProfileType',
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{0}/deviceconfigurations/{1}/loadprofileconfigurations/available',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (deviceTypeId, deviceConfigurationId) {
            this.url = Ext.String.format(this.urlTpl, deviceTypeId, deviceConfigurationId);
        }
    }

});