Ext.define('Isu.view.administration.communicationtasks.CategoryCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.communication-tasks-categorycombo',
    name: 'category',
    fieldLabel: 'Category',
    labelWidth: 200,
    labelSeparator: '*',
    store: 'Isu.store.CommunicationTasksCategories',
    queryMode: 'local',
    displayField: 'name',
    valueField: 'id',
    allowBlank: false,
    editable: false,
    msgTarget:'under',
    emptyText: 'Choose a category',
    validateOnBlur: false,
    validateOnChange: false
});