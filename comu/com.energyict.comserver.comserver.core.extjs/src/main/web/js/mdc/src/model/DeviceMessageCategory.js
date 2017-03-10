/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceMessageCategory', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        'deviceMessageSpecs'
    ],
    hasMany: {
        model: 'Mdc.model.DeviceCommand',
        name: 'deviceMessageSpecs',
        foreignKey: 'deviceMessageSpecs'
    }
});