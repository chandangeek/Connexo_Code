/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.field.readingtypes.Controller', {
    extend: 'Ext.app.Controller',
    singleton: true,
    stores: [
        'Uni.store.ReadingTypes',
        'Uni.store.UnitsOfMeasure',
        'Uni.store.TimeOfUse',
        'Uni.store.Intervals'
    ]
});