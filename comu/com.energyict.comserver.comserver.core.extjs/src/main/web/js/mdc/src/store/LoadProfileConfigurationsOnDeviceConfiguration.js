/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Mdc.model.LoadProfileConfiguration',

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{0}/deviceconfigurations/{1}/loadprofileconfigurations',
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (deviceTypeId, deviceConfigurationId) {
            this.url = Ext.String.format(this.urlTpl, deviceTypeId, deviceConfigurationId);
        }
    }
});