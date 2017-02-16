/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceLifeCycle', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name',
        'version',
        'containsCommunicationActions'
    ]
});
