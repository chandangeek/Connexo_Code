Ext.define('Mdc.view.setup.executionlevels.ExecutionLevelActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.execution-level-action-menu',
    plain: true,
    border: false,
    itemId: 'execution-level-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'deleteExecutionLevel',
            action: 'deleteExecutionLevel'

        }
    ]
});
