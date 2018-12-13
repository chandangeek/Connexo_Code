/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.column.Default
 */
Ext.define('Uni.grid.column.Default', {
    extend: 'Ext.grid.column.Column',
    xtype: 'uni-default-column',

    header: Uni.I18n.translate('general.default', 'UNI', 'Default'),
    minWidth: 120,
    align: 'left',

    renderer: function (value, metadata) {
        if (value) {
            return '<div class="' + Uni.About.baseCssPrefix + 'default-column-icon'
                + ' default">&nbsp;</div>';
        } else {
            return '-';
        }
    }
});