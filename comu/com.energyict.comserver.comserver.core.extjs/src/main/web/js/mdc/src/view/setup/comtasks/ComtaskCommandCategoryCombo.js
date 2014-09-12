Ext.define('Mdc.view.setup.comtasks.ComtaskCommandCategoryCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.comtaskCommandCategoryCombo',
    name: 'category',
    fieldLabel: 'Category',
    labelWidth: 200,
    labelSeparator: '*',
    store: 'Mdc.store.CommunicationTasksCategories',
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