Ext.define('Mdc.view.setup.comtasks.ComtaskCommandCategoryCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.comtaskCommandCategoryCombo',
    name: 'category',
    fieldLabel: Uni.I18n.translate('communicationtasks.commands.Category', 'MDC', 'Category'),
    labelWidth: 200,
    required: true,
    width: 400,
    store: 'Mdc.store.CommunicationTasksCategories',
    queryMode: 'local',
    displayField: 'name',
    valueField: 'id',
    allowBlank: false,
    editable: false,
    msgTarget:'under',
    emptyText: Uni.I18n.translate('communicationtasks.commands.Category.EmptyMessage', 'MDC', 'Choose a category'),
    validateOnBlur: false,
    validateOnChange: false
});