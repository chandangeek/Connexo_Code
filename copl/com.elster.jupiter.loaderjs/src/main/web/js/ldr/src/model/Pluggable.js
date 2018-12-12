/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ldr.model.Pluggable', {
    extend: 'Ext.data.Model',

    fields: [
        'name',
        'basePath',
        'startPage',
        'icon',
        'mainController',
        'scripts'
    ]
});