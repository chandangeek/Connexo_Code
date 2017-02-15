/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.filter.Radio
 */
Ext.define('Uni.grid.filtertop.Radio', {
    extend: 'Uni.grid.filtertop.Checkbox',
    xtype: 'uni-grid-filtertop-radio',

    emptyText: Uni.I18n.translate('grid.filter.radio.label', 'UNI', 'Radio'),

    defaultType: 'radiofield'
});