/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.column.search.DeviceType
 */
Ext.define('Uni.grid.column.search.DeviceType', {
    extend: 'Ext.grid.column.Column',
    xtype: 'uni-grid-column-search-devicetype',

    columnTpl: '<a href="../multisense/index.html#/administration/devicetypes/{deviceTypeId}">{value}</a>',

    renderer: function (value, metaData, record) {
        var me = metaData.column,
            deviceTypeId = record.raw.deviceTypeId,
            result = me.columnTpl;

        result = result.replace('{value}', Ext.htmlEncode(value));
        result = result.replace('{deviceTypeId}', deviceTypeId);

        metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';

        return result || '-';
    }
});