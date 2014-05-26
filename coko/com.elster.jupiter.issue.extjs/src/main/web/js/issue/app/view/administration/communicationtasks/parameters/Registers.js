Ext.define('Isu.view.administration.communicationtasks.parameters.Registers', {
    extend: 'Ext.form.field.ComboBox',
    requires: [
        'Isu.util.ComboSelectedCount'
    ],
    plugins: ['selectedCount'],
    queryMode: 'local',
    multiSelect: true,
    alias: 'widget.communication-tasks-registerscombo',
    name: 'registers',
    fieldLabel: 'Register groups',
    labelWidth: 200,
    labelSeparator: '*',
    store: 'Isu.store.CommunicationTasksRegisters',
    displayField: 'name',
    valueField: 'id',
    allowBlank: false,
    editable: false,
    validateOnBlur: false,
    validateOnChange: false
});
