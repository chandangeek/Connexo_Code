/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.model.ValidationTask', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id'},
        {name: 'name'},
        {name: 'nextRun', type: 'date', dateFormat: 'time'},
        {name: 'schedule', type: 'auto'},
        {name: 'metrologyContract', type: 'auto'},
        {name: 'metrologyConfiguration', type: 'auto'},
        {
            name: 'uniqueId',
            persist: false,
            type: 'string'
        }
    ],
    idProperty: 'uniqueId'
});
