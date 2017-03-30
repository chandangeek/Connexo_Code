/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuseondevice.store.CalendarContracts', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.timeofuseondevice.model.CalendarContract'
    ],
    model: 'Mdc.timeofuseondevice.model.CalendarContract',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: "/api/ddr/field/contracts",
        reader: {
            type: 'json',
            root: 'contracts'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});