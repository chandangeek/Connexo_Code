/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.column.Date
 */
Ext.define('Uni.grid.column.Date', {
    extend: 'Uni.grid.column.Default',
    xtype: 'uni-date-column',
    renderer: function (value, metaData) {
        var result = value ? Uni.DateTime.formatDateTimeShort(value) : '-';

        metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(result)) + '"';

        return result;
    }
});