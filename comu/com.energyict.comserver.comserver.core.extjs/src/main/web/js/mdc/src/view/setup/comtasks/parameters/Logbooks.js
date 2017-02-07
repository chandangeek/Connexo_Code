/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.parameters.Logbooks', {
    extend: 'Mdc.view.setup.comtasks.parameters.ComboWithToolbar',
    alias: 'widget.communication-tasks-logbookscombo',
    name: 'logbooks',    
    fieldLabel: Uni.I18n.translate('general.logbookType', 'MDC', 'Logbook type'),
    itemId: 'checkLogbookTypes',
    store: 'Mdc.store.LogbookTypes'
});
