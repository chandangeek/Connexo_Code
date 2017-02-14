/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.DeviceGroup', {
    extend: 'Ext.data.Model',
    proxy: 'memory',
    fields: [
        { name: 'id', type: 'int'},
        { name: 'mRID', type: 'string'},
        { name: 'name', type: 'string'},
        { name: 'dynamic', type: 'boolean'},
        { name: 'criteria'}
    ]
});
