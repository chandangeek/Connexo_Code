/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskCommandCategoryCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.comtaskCommandCategoryCombo',
    name: 'category',
    fieldLabel: Uni.I18n.translate('communicationtasks.commands.category', 'MDC', 'Category'),
    labelWidth: 250,
    required: true,
    width: 400,
    store: 'Mdc.store.CommunicationTasksCategories',
    queryMode: 'local',
    displayField: 'name',
    valueField: 'id',
    allowBlank: false,
    editable: false,
    msgTarget:'under',
    emptyText: Uni.I18n.translate('communicationtasks.commands.category.emptyMessage', 'MDC', 'Choose a category'),
    validateOnBlur: false,
    validateOnChange: false
});