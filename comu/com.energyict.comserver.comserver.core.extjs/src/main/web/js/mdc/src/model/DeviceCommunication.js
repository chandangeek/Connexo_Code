Ext.define('Mdc.model.DeviceCommunication', {
    extend: 'Mdc.model.DeviceCommunicationTask',

    fields: [
        'latestResult',
        'lastCommunicationStart',
        'isOnHold'
    ],

    run: function(callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/run'.replace('{id}', this.get('comTask').id),
            success: callback
        });
    },

    runNow: function(callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/runnow'.replace('{id}', this.get('comTask').id),
            success: callback
        });
    },

    activate: function(callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/activate'.replace('{id}', this.get('comTask').id),
            success: callback
        });
    },

    deactivate: function(callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/deactivate'.replace('{id}', this.get('comTask').id),
            success: callback
        });
    },

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/comtasks',
        reader: {
            type: 'json',
            root: 'comTasks'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});