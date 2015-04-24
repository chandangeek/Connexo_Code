Ext.define('Est.estimationtasks.store.EstimationTasks', {
    extend: 'Ext.data.Store',
    requires: ['Est.estimationtasks.model.EstimationTask'],
    model: 'Est.estimationtasks.model.EstimationTask',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/est/estimation/tasks',
        //url: '/apps/est/src/estimationtasks/fakedata/estimationtasks.json',
//        url: '/apps/est/src/estimationtasks/fakedata/estimationtasksempty.json',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'estimationTasks'
        }
    }
});
