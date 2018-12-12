/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.model.UsagePointFavorite', {
    extend: 'Uni.model.Favorite',

    proxy: {
        type: 'rest',
        url: '/api/udr/favorites/usagepoints',
        reader: {
            type: 'json'
        }
    }
});