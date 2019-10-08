/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.store.PropertyEndDeviceGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.property.model.PropertyEndDeviceGroup'
    ],
    model: 'Uni.property.model.PropertyEndDeviceGroup',
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