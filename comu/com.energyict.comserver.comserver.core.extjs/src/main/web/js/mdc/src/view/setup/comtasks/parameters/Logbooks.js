Ext.define('Mdc.view.setup.comtasks.parameters.Logbooks', {
    extend: 'Mdc.view.setup.comtasks.parameters.ComboWithToolbar',
    alias: 'widget.communication-tasks-logbookscombo',
    name: 'logbooks',    
    fieldLabel: Uni.I18n.translate('general.logbookType', 'MDC', 'Logbook type'),
    store: 'Mdc.store.LogbookTypes'
});
