/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.parameters.Registers', {
    extend: 'Mdc.view.setup.comtasks.parameters.ComboWithToolbar',
    alias: 'widget.communication-tasks-registerscombo',
    name: 'registers',
    fieldLabel: Uni.I18n.translate('comtask.register.groups','MDC','Register groups'),
    itemId: 'checkRegisterGroups',
    store: 'Mdc.store.RegisterGroups'
});
