Ext.define('Dal.store.AlarmWorkgroupAssignees', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.AlarmWorkgroupAssignee',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dal/workgroups',
        reader: {
            type: 'json',
            root: 'workgroups'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});