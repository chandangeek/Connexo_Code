Ext.define('Imt.usagepointlifecyclestates.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.usagepoint-life-cycle-states-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
            itemId: 'edit-action',
            action: 'edit'
        },
        {
            text: Uni.I18n.translate('usagePointLifeCycleStates.setAsInitial', 'IMT', 'Set as initial state'),
            itemId: 'initialAction',
            action: 'setAsInitial'
        },
        {
            text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
            itemId: 'remove-state',
            action: 'removeState'
        }
    ]
});