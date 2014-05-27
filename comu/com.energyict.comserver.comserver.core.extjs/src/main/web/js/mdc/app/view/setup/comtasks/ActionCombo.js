Ext.define('Mdc.view.setup.comtasks.ActionCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.communication-tasks-actioncombo',
    name: 'action',
    fieldLabel: 'Action',
    labelWidth: 200,
    labelSeparator: '*',
    store: 'Mdc.store.CommunicationTasksActions',
    queryMode: 'local',
    displayField: 'name',
    valueField: 'id',
    allowBlank: false,
    editable: false,
    emptyText: 'Choose an action',
    validateOnBlur: false,
    validateOnChange: false
});