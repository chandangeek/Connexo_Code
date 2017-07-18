/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceFirmwareHistory', {
    extend: 'Ext.data.Model',
    fields: [
        'firmwareVersion',
        'firmwareType',
        'activationDate'
    ]
});
