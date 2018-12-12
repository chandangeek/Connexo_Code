/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.model.AvailableKPI', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'id', type: 'int', useNull: true},
        {name:'name', type: 'string'},
        {name:'frequency', type: 'auto', defaultValue: null}
    ]
});
