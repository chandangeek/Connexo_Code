/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.FlaggedDevice', {
    extend: 'Ext.data.Model',
    idProperty: 'name',

    requires: ['Mdc.model.DeviceLabel'],

    fields: [
        { name: 'name', type: 'string'},
        { name: 'serialNumber', type: 'string'},
        { name: 'deviceTypeName', type: 'string'}
    ],

    hasOne: {
        model: 'Mdc.model.DeviceLabel',
        name: 'deviceLabelInfo',
        associationKey: 'deviceLabelInfo',
        getterName: 'getLabel'
    }
});