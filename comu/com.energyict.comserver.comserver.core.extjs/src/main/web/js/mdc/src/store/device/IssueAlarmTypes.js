/**
 * Created by H251853 on 9/26/2017.
 */
Ext.define('Mdc.store.device.IssueAlarmTypes', {
    extend: 'Isu.store.IssueTypes',
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    listeners: {
        load: function (store, records, successful, eOpts) {

            Mdc.privileges.Device.canViewAdminAlarm() && store.insert(0, {
                uid: 'devicealarm',
                name: Uni.I18n.translate('issueTypes.devicealarm', 'MDC', 'Device alarm')
            })
        }
    }

});