Ext.define('Bpm.monitorissueprocesses.store.AlarmProcesses', {
    extend: 'Ext.data.Store',
    model: 'Bpm.monitorissueprocesses.model.IssueProcess',
    autoLoad: false,
    proxy: 'memory',

    proxy: {
        type: 'rest',
        urlTpl: '/api/dal/alarms/{alarmId}/processes?variableid=alarmId&variablevalue={Id}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'processes'
        },
        setUrl: function (alarmId) {
            this.url = this.urlTpl.replace('{alarmId}', alarmId)
                .replace('{Id}', alarmId);
        }
    }
});
