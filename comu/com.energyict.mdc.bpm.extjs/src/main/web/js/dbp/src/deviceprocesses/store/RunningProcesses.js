Ext.define('Dbp.deviceprocesses.store.RunningProcesses', {
    extend: 'Ext.data.Store',
    model: 'Dbp.deviceprocesses.model.RunningProcess',

    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/bpm/runtime/runningprocesses?variableid={variableid}&variablevalue={variablevalue}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'processes'
        },
        setUrl: function (variableid, variablevalue) {
            this.url = this.urlTpl.replace('{variableid}', variableid)
                .replace('{variablevalue}', encodeURIComponent(variablevalue));
        }
    }
});
