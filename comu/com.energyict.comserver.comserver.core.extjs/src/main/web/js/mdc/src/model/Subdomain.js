/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.Subdomain', {
    extend: 'Ext.data.Model',
    fields: [
        'subDomain',
        'localizedValue'
    ],
    idProperty: 'subDomain'
});