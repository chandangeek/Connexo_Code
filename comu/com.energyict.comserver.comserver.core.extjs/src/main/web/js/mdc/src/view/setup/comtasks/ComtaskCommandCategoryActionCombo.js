/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskCommandCategoryActionCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.comtaskCommandCategoryActionCombo',
    name: 'action',
    fieldLabel: Uni.I18n.translate('communicationtasks.commands.action', 'MDC', 'Action'),
    labelWidth: 200,
    required: true,
    width: 400,
    store: 'Mdc.store.CommunicationTasksActions',
    queryMode: 'local',
    displayField: 'name',
    valueField: 'id',
    allowBlank: false,
    editable: false,
    msgTarget:'under',
    emptyText: Uni.I18n.translate('communicationtasks.commands.action.emptyMessage', 'MDC', 'Choose an action'),
    validateOnBlur: false,
    validateOnChange: false
});