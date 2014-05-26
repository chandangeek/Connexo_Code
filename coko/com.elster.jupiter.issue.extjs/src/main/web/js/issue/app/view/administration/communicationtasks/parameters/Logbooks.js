Ext.define('Isu.view.administration.communicationtasks.parameters.Logbooks', {
    extend: 'Ext.form.field.ComboBox',
    requires: [
        'Isu.util.ComboSelectedCount'
    ],
    plugins: ['selectedCount'],
    alias: 'widget.communication-tasks-logbookscombo',
    name: 'logbooks',
    queryMode: 'local',
    multiSelect: true,
    fieldLabel: 'Logbook type',
    labelWidth: 200,
    labelSeparator: '*',
    store: 'Isu.store.CommunicationTasksLogbooks',
    displayField: 'name',
    valueField: 'id',
    allowBlank: false,
    editable: false,
    validateOnBlur: false,
    validateOnChange: false
});
