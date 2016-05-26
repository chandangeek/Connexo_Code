Ext.define('Mdc.metrologyconfiguration.view.ActionsMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.metrology-configuration-actions-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            privileges: Mdc.privileges.MetrologyConfiguration.canAdmin(),
            action: 'remove'
        }
    ],
    listeners: {
        beforerender: function () {
            var me = this,
                isActive = me.record.get('status').id === 'active',
                removeItem = me.down('[action=remove]');

            if (removeItem) {
                removeItem.setDisabled(isActive);
            }
        }
    }
});