Ext.define('Dsh.store.CommunicationTasksBuffered', {
    extend: 'Dsh.store.CommunicationTasks',
    buffered: true,
    pageSize: 50
});