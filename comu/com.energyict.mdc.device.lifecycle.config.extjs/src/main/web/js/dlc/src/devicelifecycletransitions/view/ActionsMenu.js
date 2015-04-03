Ext.define('Dlc.devicelifecycletransitions.view.ActionsMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.transitions-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-transition',
            text: Uni.I18n.translate('general.edit', 'DLC', 'Edit'),
            action: 'editTransition'
        }
    ]
});


