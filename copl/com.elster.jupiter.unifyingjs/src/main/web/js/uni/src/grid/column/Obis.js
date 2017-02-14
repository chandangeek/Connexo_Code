/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.column.Obis
 */
Ext.define('Uni.grid.column.Obis', {
    extend: 'Ext.grid.column.Column',
    xtype: 'obis-column',
    header: Uni.I18n.translate('obis.label', 'UNI', 'OBIS code'),
    minWidth: 120,
    align: 'left'
});