Ext.define('Imt.purpose.store.EstimationTasks', {
    extend: 'Ext.data.Store',
    model: 'Est.estimationtasks.model.EstimationTask',

    proxy: {
        type: 'rest',
        tplUrl: '/api/udr/usagepoints/{id}/estimationtasks',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'dataEstimationTasks'
        },
        setUrl: function (id) {
            this.url = this.tplUrl.replace('{id}', encodeURIComponent(id))
        }
    }
});