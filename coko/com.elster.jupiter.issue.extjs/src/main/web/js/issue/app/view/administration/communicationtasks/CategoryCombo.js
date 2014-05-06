Ext.define('Isu.view.administration.communicationtasks.CategoryCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.communication-tasks-category-combo',
    name: 'category',
    fieldLabel: 'Category',
    labelSeparator: ' *',
    store: 'Isu.store.CommunicationTasksCategories',
    queryMode: 'local',
    displayField: 'name',
    valueField: 'id',
    allowBlank: false,
    editable: false,
    emptyText: 'Choose a category'
});