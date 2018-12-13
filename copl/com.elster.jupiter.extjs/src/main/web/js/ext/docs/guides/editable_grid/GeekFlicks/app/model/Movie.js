/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('GeekFlicks.model.Movie', {
    extend: 'Ext.data.Model',
    fields: [{
        name: 'title',
        type: 'string'
    }, {
        name: 'year',
        type: 'int'
    }]
});
