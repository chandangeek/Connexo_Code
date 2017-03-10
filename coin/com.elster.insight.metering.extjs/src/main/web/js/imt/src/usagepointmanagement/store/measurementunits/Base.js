/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.measurementunits.Base', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.MeasurementUnit',

    findUnit: function (data) {
        var me = this,
            index = me.findBy(function (record) {
                return data.unit === record.get('unit') && data.multiplier === record.get('multiplier');
            });

        return me.getAt(index);
    }
});