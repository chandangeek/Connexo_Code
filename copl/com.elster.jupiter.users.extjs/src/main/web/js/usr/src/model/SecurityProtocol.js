/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.SecurityProtocol', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'value'
    ],
    idProperty: 'name'
});