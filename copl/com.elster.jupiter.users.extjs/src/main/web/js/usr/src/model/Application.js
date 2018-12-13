/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.Application', {
    extend: 'Ext.data.Model',
    fields: [
        'componentName',
        'translatedName',
        'description',
        'selected',
        'sortingfield'
    ]
});