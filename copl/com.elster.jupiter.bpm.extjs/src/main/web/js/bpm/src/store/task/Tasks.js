Ext.define('Bpm.store.task.Tasks', {
    extend: 'Uni.data.store.Filterable',
    model: 'Bpm.model.task.Task',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/tasks',
        reader: {
            type: 'json',
            root: 'tasks'
        }
    }
});
