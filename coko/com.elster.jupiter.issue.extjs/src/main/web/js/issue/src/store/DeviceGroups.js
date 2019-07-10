/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Isu.model.DeviceGroup'
    ],
    model: 'Isu.model.DeviceGroup',
    autoLoad: false,
    storeId: 'IsuDeviceGroups',
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    sortOnLoad: true,

    proxy: {
        type: 'rest',
        urlTpl: '../../api/ddr/devicegroups/filtered',
        url: '../../api/ddr/devicegroups/filtered',
        reader: {
            type: 'json',
            root: 'devicegroups'
        },
        
        setExcludedGroups: function (exclGroupsIds) {
            if (exclGroupsIds) {
                this.url = this.urlTpl.concat('?exclude=', exclGroupsIds);
            } else {
                this.url = this.urlTpl;
            }
        }
    }
});
