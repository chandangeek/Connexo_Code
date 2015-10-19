Ext.define('Bpm.model.task.TaskEdit', {
    extend: 'Ext.data.Model',

    proxy: {
        type: 'rest',
        urlTpl: '/api/bpm/runtime/tasks/{taskId}',
        reader: {
            type: 'json'
        },
        setUrl: function (taskId) {
            this.url = this.urlTpl.replace('{taskId}', taskId);
        }
    }
});