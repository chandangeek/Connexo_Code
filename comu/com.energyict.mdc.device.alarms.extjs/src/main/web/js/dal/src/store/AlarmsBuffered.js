Ext.define('Dal.store.AlarmsBuffered', {
    extend: 'Dal.store.Alarms',
    buffered: true,
    pageSize: 200,
    remoteFilter: true
});
