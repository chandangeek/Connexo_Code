Ext.define('Imt.purpose.store.EstimationTasks', {
    extend: 'Ext.data.Store',
    model: 'Est.estimationtasks.model.EstimationTask',

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/estimationtasks',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'dataEstimationTasks'
        }
    }
});