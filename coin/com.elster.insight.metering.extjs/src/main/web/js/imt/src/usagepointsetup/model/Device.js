/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointsetup.model.Device', {
    extend: 'Ext.data.Model',
    fields: [
        'mRID',
        'id',
        'version',
        'name',
        {name: 'meterActivations', type: 'auto'}
    ],
    idProperty: 'name'
});