/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceConflictingMapping', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'fromConfiguration', type: 'string'},
        {name: 'isSolved'},
        {name: 'toConfiguration', type: 'string'},
        {name: 'solved', type: 'auto'}
    ]
});
