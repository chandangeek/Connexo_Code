Ext.define('Dbp.startprocess.store.AvailableProcesses', {
    extend: 'Ext.data.Store',
    model: 'Dbp.startprocess.model.AvailableProcess',

    autoLoad: false,
    pageSize: 1000,
    proxy: {
        type: 'rest',
        urlTpl: '/api/bpm/runtime/activeprocesses?devicestateid={deviceState}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'processes'
        },
        setUrl: function (stateId) {
            this.url = this.urlTpl.replace('{deviceState}', stateId);
        }
    }
});