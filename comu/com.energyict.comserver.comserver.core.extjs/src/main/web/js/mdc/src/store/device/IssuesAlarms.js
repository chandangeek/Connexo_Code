/**
 * Created by H251853 on 9/4/2017.
 */
Ext.define('Mdc.store.device.IssuesAlarms', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.IssueAlarm'
    ],
    model: 'Mdc.model.IssueAlarm',
    // autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{deviceId}/history/issuesandalarms',
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }
});
