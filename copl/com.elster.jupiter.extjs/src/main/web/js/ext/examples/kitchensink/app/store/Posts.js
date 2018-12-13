/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('KitchenSink.store.Posts', {
    extend: 'Ext.data.TreeStore',

    requires: [
        'KitchenSink.data.Posts',
        'Ext.ux.ajax.JsonSimlet',
        'Ext.ux.ajax.SimManager'
    ],

    model: 'KitchenSink.model.tree.Post',

    proxy: {
        type: 'ajax',
        reader: 'json',
        url: '/KitchenSink/Posts'
    },
    lazyFill: false
}, function() {
    Ext.ux.ajax.SimManager.init({
        defaultSimlet: null
    }).register({
        '/KitchenSink/Posts': {
            data: KitchenSink.data.Posts.data,
            stype: 'json'
        }
    });
});