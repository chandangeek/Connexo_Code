/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Yfn.store.DeviceGroupInfos
 */
Ext.define('Yfn.store.DeviceGroupInfos', {
    extend: 'Ext.data.Store',
    model: 'Yfn.model.DeviceGroupInfo',
    storeId: 'DeviceGroupInfos',
    autoLoad: false,
    requires:[
       'Yfn.model.FilterInfo'
    ],

    proxy: {
        type: 'ajax',
        url: '/api/yfn/cachegroups/list',
        reader: {
            type: 'json',
            root: 'groups'
        }
    }
});