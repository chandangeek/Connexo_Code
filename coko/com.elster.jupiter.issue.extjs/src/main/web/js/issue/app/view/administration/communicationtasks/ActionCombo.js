Ext.define('Isu.view.administration.communicationtasks.ActionCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.communication-tasks-action-combo',
    name: 'action',
    fieldLabel: 'Action',
    labelSeparator: ' *',
    store: 'Isu.store.CommunicationTasksActions',
    queryMode: 'local',
    displayField: 'name',
    valueField: 'id',
    allowBlank: false,
    editable: false,
    emptyText: 'Choose an action'
});