/**
 * @class Uni.store.Apps
 */
Ext.define('Uni.store.Apps', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.App',
    storeId: 'apps',
    singleton: true,
    autoLoad: false,
    storeLoaded: false,


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

    listeners : {
        load: function(){
            this.storeLoaded = true;
        }
    },

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