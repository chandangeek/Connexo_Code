/**
 * Created by david on 6/10/2016.
 */
Ext.define('Ddv.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'}
    ],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/val/kpis/kpigroups',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceGroups'
        }
    }
});