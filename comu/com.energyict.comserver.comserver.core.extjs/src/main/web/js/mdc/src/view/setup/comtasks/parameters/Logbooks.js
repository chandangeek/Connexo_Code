Ext.define('Mdc.view.setup.comtasks.parameters.Logbooks', {
    extend: 'Ext.form.field.ComboBox',
    requires: [
        'Mdc.util.ComboSelectedCount'
    ],
    plugins: ['selectedCount'],
    alias: 'widget.communication-tasks-logbookscombo',
    name: 'logbooks',
    queryMode: 'local',
    multiSelect: true,
    fieldLabel: 'Logbook type',
    labelWidth: 200,
    required: true,
    width: 400,
    store: 'Mdc.store.LogbookTypes',
    displayField: 'name',
    valueField: 'id',
    allowBlank: true,
    editable: false,
    validateOnBlur: false,
    validateOnChange: false
});
