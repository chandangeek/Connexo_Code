Ext.define('Bpm.model.task.Assign', {
    extend: 'Ext.data.Model',
    proxy: {
        type: 'rest',
        urlTpl: '/api/bpm/runtime/tasks/{taskId}/assign',
        reader: {
            type: 'json'
        },
        setUrl: function (taskId) {
            this.url = this.urlTpl.replace('{taskId}', taskId);
        }
    }
});