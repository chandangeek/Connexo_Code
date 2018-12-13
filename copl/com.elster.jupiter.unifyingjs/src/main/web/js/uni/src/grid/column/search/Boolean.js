/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.column.search.Boolean
 */
Ext.define('Uni.grid.column.search.Boolean', {
    extend: 'Ext.grid.column.Column',
    xtype: 'uni-grid-column-search-boolean',

    renderer: function (value, metaData, record) {
        var result = value ? Uni.I18n.translate('general.yes', 'UNI', 'Yes') :
            Uni.I18n.translate('general.no', 'UNI', 'No')

        metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(result)) + '"';

        return result;
    }
});