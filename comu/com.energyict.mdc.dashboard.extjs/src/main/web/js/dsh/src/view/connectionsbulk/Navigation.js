Ext.define('Dsh.view.connectionsbulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.connections-bulk-navigation',
    jumpForward: false,
    jumpBack: true,
    items: [
        {
            itemId: 'cnbn-select-connections',
            action: 'select-connections',
            text: Uni.I18n.translate('connection.bulk.selectConnections', 'DSH', 'Select connections')
        },
        {
            itemId: 'cnbn-select-action',
            action: 'select-action',
            text: Uni.I18n.translate('general.selectAction', 'DSH', 'Select action')
        },
        {
            itemId: 'cnbn-action-details',
            action: 'action-details',
            text: Uni.I18n.translate('general.actionDetails', 'DSH', 'Action details')
        },
        {
            itemId: 'cnbn-confirmation',
            action: 'confirmation',
            text: Uni.I18n.translate('general.confirmation', 'DSH', 'Confirmation')
        },
        {
            itemId: 'cnbn-status',
            action: 'status',
            text: Uni.I18n.translate('general.status', 'DSH', 'Status')
        }
    ],

    makeStepXCompletedAndUnclickable: function(stepX) {
        var item = this.items.getAt(stepX -1);
        if (item) {
            item.removeCls(['step-completed', 'step-active', 'step-non-completed', 'not-a-clickable']);
            item.addCls(['step-completed', 'not-a-clickable']);
        }
    }
});