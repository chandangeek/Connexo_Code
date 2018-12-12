/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.UsagePointGroup', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'mRID', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'dynamic', type: 'boolean'},
        {name: 'criteria'}
    ]
});