/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ConnectionFunction', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'localizedValue',
        {name: 'alreadyUsed', type: 'boolean', useNull: true}
    ]
});