/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.MenuItem
 */
Ext.define('Uni.model.MenuItem', {
    extend: 'Ext.data.Model',
    fields: [
        'text',
        'portal',
        'href',
        'glyph',
        'index',
        'hidden'
    ],
    proxy: {
        type: 'memory'
    }
});