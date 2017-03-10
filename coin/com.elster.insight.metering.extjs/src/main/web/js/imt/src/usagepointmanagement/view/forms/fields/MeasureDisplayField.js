/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.fields.MeasureDisplayField', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.measuredisplayfield',
    unitType: null,
    unitStoresMap: {
        "voltage": 'Imt.usagepointmanagement.store.measurementunits.Voltage',
        "amperage": 'Imt.usagepointmanagement.store.measurementunits.Amperage',
        "power": 'Imt.usagepointmanagement.store.measurementunits.Power',
        "pressure": 'Imt.usagepointmanagement.store.measurementunits.Pressure',
        "pressureExtended": 'Imt.usagepointmanagement.store.measurementunits.Pressure',
        "volume": 'Imt.usagepointmanagement.store.measurementunits.Volume',
        "capacity": 'Imt.usagepointmanagement.store.measurementunits.Capacity',
        "estimationLoad": 'Imt.usagepointmanagement.store.measurementunits.EstimationLoad'
    },


    renderer: function (data) {
        var me = this, store, record;
        if (data) {
            store = Ext.getStore(me.unitStoresMap[me.unitType]);
            record = store.findUnit(data);
            if(record){
                return data.value + "&nbsp" + record.get('displayValue');
            } else {
                return '-';
            }
        } else {
            return '-';
        }
    }
});