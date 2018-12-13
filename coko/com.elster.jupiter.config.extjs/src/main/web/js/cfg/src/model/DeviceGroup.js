/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.model.DeviceGroup', {
    extend: 'Ext.data.Model',
    proxy: 'memory',
    fields: [
        { name: 'id', type: 'int'},
        { name: 'mRID', type: 'string'},
        { name: 'displayValue', type: 'string'},
        { name: 'dynamic', type: 'boolean'},
        { name: 'criteria'}
    ]
});
