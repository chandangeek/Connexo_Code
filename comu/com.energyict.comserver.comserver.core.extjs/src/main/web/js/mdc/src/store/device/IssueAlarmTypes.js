/**
 * Created by H251853 on 9/26/2017.
 */
Ext.define('Mdc.store.device.IssueAlarmTypes', {
    extend: 'Isu.store.IssueTypes',
    listeners: {
        load: function (store, records, successful, eOpts) {


            store.insert(0, {
                uid: 'devicealarm',
                name: 'Device alarm'
            })
        }
    }

});