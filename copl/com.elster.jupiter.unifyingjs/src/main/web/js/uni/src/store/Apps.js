/**
 * @class Uni.store.Apps
 */
Ext.define('Uni.store.Apps', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.App',
    storeId: 'apps',
    singleton: true,
    autoLoad: true,
    isLoaded: false,

    listeners: {
        load: function (store) {
            store.isLoaded = true;
        }
    },

    proxy: {
        type: 'ajax',
        url: '/api/apps/apps',
        reader: {
            type: 'json',
            root: ''
        }
    },

    sorters: [
        {
            property: 'name',
            direction: 'ASC'
        }
    ],

    checkApp: function(appName){
        var record = this.findRecord('name', appName);
        return !!record;
    },

    getAppUrl: function(appName){
        var url = null,
        record = this.findRecord('name', appName);
        return record ? record.get('url') : this.findRecord('name', "Admin");
    }
});