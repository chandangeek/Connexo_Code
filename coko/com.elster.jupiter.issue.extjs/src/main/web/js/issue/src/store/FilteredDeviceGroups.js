/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.FilteredDeviceGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Isu.model.FilteredDeviceGroup'
    ],
    model: 'Isu.model.FilteredDeviceGroup',
    autoLoad: false,
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