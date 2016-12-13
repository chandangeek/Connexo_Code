Ext.define('Imt.purpose.view.registers.RegisterReadingActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.purpose-register-readings-data-action-menu',
    router: null,
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'confirm-value',
            hidden: true,
            text: Uni.I18n.translate('general.confirm', 'IMT', 'Confirm'),
            action: 'confirmValue'
        },
        {
            itemId: 'edit-value',
            text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
            action: 'editValue'
        },
        {
            itemId: 'reset-value',
            hidden: true,
            text: Uni.I18n.translate('general.reset', 'IMT', 'Reset'),
            action: 'resetValue'
        }
    ]
});
