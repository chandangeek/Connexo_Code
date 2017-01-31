/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.MeterActivation', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'start', type: 'date', dateFormat: 'time'},
        {name: 'end', type: 'date', dateFormat: 'time'},
        {name: 'active', type: 'boolean'},
        {name: 'deviceConfiguration', type: 'auto'},
        {name: 'usagePoint', type: 'auto'},
        {name: 'multiplier', type: 'auto', defaultValue: null, useNull: true}
    ]
});