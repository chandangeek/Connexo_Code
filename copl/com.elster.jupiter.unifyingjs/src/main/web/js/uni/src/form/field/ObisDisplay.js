/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.ObisDisplay
 */
Ext.define('Uni.form.field.ObisDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'obis-displayfield',
    name: 'obisCode',
    cls: 'obisCode',
    fieldLabel: Uni.I18n.translate('obis.label', 'UNI', 'OBIS code'),
    emptyText: ''
});