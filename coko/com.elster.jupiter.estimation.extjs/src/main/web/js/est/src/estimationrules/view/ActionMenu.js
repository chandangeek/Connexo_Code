Ext.define('Est.estimationrules.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.estimation-rules-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.activate', 'EST', 'Activate'),
            action: 'toggleActivation'
        },
        {
            text: Uni.I18n.translate('general.edit', 'EST', 'Edit'),
            action: 'edit'
        },
        {
            text: Uni.I18n.translate('general.remove', 'EST', 'Remove'),
            action: 'remove'
        }
    ],
    listeners: {
        show: {
            fn: function (menu) {
                if (menu.record) {
                    menu.down('[action=toggleActivation]').setText(menu.record.get('active')
                        ? Uni.I18n.translate('general.deactivate', 'EST', 'Deactivate')
                        : Uni.I18n.translate('general.activate', 'EST', 'Activate'));
                }
            }
        }
    }
});