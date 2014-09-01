Ext.define('Dsh.store.ConnectionCurrentStates', {
    extend: 'Ext.data.Store',
    storeId: 'ConnectionCurrentStates',
    model: 'Dsh.model.ConnectionCurrentState',
    data: [
        {
            name: 'busy',
            localizedValue: 'Busy'
        },
        {
            name: 'inactive',
            localizedValue: 'Inactive'
        },
        {
            name: 'failed',
            localizedValue: 'Failed'
        },
        {
            name: 'neverCompleted',
            localizedValue: 'Never completed'
        },
        {
            name: 'pending',
            localizedValue: 'Pending'
        },
        {
            name: 'retrying',
            localizedValue: 'Retrying'
        },
        {
            name: 'waiting',
            localizedValue: 'Waiting'
        }
    ]
});
