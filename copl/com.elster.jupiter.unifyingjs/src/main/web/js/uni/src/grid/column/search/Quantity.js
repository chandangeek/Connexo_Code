/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.column.search.Quantity
 */
Ext.define('Uni.grid.column.search.Quantity', {
    extend: 'Ext.grid.column.Column',
    xtype: 'uni-grid-column-search-quantity',


    renderer: function (value, metaData, record) {
        var result = '-';

        if (value) {
            if (value.multiplier == 0)
                result = value.value + ' ' + value.unit;
            else
                result = value.value + '*10<sup style="vertical-align: top; position: relative; top: -0.5em;">' + value.multiplier + '</sup> ' + value.unit;

        }

        metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(result)) + '"';

        return result;
    }
});