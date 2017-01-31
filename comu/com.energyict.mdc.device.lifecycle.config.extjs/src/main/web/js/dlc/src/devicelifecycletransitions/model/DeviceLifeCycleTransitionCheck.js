/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransitionCheck', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'key', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'category', type: 'auto'},
        {name: 'conflictGroup', type: 'auto'},
        {name: 'isRequired', type: 'boolean'},
        {name: 'checked', type: 'boolean'}
    ]
});