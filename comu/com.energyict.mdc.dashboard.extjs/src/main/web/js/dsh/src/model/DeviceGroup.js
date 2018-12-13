/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.DeviceGroup', {
    extend: 'Uni.model.ParentVersion',
    proxy: 'memory',
    fields: [
        { name: 'id', type: 'int'},
        { name: 'mRID', type: 'string'},
        { name: 'name', type: 'string'},
        { name: 'dynamic', type: 'boolean'},
        { name: 'favorite', type: 'boolean'},
        { name: 'criteria'}
    ]
});