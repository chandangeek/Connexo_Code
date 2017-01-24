Ext.define('Mdc.metrologyconfiguration.view.ActionsMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.metrology-configuration-actions-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                privileges: Mdc.privileges.MetrologyConfiguration.canAdmin(),
                action: 'toggleActivation',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.MetrologyConfiguration.canAdmin(),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                privileges: Mdc.privileges.MetrologyConfiguration.canAdmin(),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function () {
            var me = this,
                isActive = me.record.get('status').id === 'active',
                removeItem = me.down('[action=remove]'),
                toggleActivationItem = me.down('[action=toggleActivation]');

            Ext.suspendLayouts();
            if (removeItem) {
                removeItem.setDisabled(isActive);
            }
            if (toggleActivationItem) {
                toggleActivationItem.setText(isActive
                    ? Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate')
                    : Uni.I18n.translate('general.activate', 'MDC', 'Activate'));
            }
            Ext.resumeLayouts(true);
        }
    }
});