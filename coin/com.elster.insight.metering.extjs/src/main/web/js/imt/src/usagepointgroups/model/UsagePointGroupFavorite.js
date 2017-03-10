/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.model.UsagePointGroupFavorite', {
    extend: 'Uni.model.Favorite',

    proxy: {
        type: 'rest',
        url: '/api/udr/favorites/usagepointgroups',
        reader: {
            type: 'json'
        }
    }
});