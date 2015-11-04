Ext.define('Bpm.model.task.Priority', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'value',
        'label'
    ],
    idProperty: 'name'
});