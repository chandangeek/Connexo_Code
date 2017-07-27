/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.model.Command', {
    extend: 'Mdc.model.DeviceCommand',
    fields: [
        {
            name:'deviceType'
        },
        {
            name:'deviceTypeAndConfiguration',
            persist: false,
            mapping: function (data) {
                var res = {};
                res.deviceType = data.deviceType;
                res.deviceConfiguration = data.deviceConfiguration;
                return res;
            }
        }
    ]
});