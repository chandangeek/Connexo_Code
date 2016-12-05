Ext.define('Mdc.view.setup.executionlevels.ExecutionLevelActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.execution-level-action-menu',
    itemId: 'execution-level-action-menu',
    items: [
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'deleteExecutionLevel',
            action: 'deleteExecutionLevel'
        }
    ]
});
