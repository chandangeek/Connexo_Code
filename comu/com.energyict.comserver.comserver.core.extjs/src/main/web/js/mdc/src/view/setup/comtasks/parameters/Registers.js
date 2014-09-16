Ext.define('Mdc.view.setup.comtasks.parameters.Registers', {
    extend: 'Ext.form.field.ComboBox',
    requires: [
        'Mdc.util.ComboSelectedCount'
    ],
    plugins: ['selectedCount'],
    queryMode: 'local',
    multiSelect: true,
    alias: 'widget.communication-tasks-registerscombo',
    name: 'registers',
    fieldLabel: 'Register groups',
    labelWidth: 200,
    required: true,
    width: 400,
    store: 'Mdc.store.RegisterGroups',
    displayField: 'name',
    valueField: 'id',
    allowBlank: false,
    editable: false,
    validateOnBlur: false,
    validateOnChange: false
});
