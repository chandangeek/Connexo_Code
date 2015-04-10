Ext.define('Dlc.devicelifecyclestates.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-life-cycle-states-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'DLC', 'Edit'),
            itemId: 'edit-action',
            action: 'edit'
        },
        {
            text: Uni.I18n.translate('deviceLifeCycleStates.setAsInitial', 'DLC', 'Set as initial state'),
            itemId: 'initialAction',
            action: 'setAsInitial'
        }
    ]
});