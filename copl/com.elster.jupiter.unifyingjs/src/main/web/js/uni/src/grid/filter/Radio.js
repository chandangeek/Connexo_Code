/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.filter.Radio
 */
Ext.define('Uni.grid.filter.Radio', {
    extend: 'Uni.grid.filter.Checkbox',
    xtype: 'uni-grid-filter-radio',

    fieldLabel: Uni.I18n.translate('grid.filter.radio.label', 'UNI', 'Radio'),

    defaultType: 'radiofield'
});