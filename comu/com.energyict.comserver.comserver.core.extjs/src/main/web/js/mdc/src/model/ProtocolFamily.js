/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ProtocolFamily', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'protocolFamilyCode', type: 'int', useNull: true},
        'protocolFamilyName'
    ]
});
