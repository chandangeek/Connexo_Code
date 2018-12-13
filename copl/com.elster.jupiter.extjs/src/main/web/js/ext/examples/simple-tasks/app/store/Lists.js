/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('SimpleTasks.store.Lists', {
    extend: 'Ext.data.TreeStore',
    model: 'SimpleTasks.model.List',

    root: {
        expanded: true,
        id: -1,
        name: 'All Lists'
    }

});