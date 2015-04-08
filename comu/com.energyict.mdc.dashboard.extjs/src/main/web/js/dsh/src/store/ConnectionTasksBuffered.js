Ext.define('Dsh.store.ConnectionTasksBuffered', {
    extend: 'Dsh.store.ConnectionTasks',
    buffered: true,
    pageSize: 200
});