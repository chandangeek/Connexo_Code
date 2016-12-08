Ext.define('Mdc.view.setup.commandrules.CommandRuleActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.commandRuleActionMenu',
    initComponent: function() {
        this.items = [
            {
                text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                action: 'toggleCommandRuleActivation',
                privileges: Mdc.privileges.CommandLimitationRules.admin,
                itemId: 'mdc-command-rule-activation-toggle-menu-item',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editCommandRule',
                privileges: Mdc.privileges.CommandLimitationRules.admin,
                itemId: 'mdc-command-rule-edit-menu-item',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'removeCommandRule',
                privileges: Mdc.privileges.CommandLimitationRules.admin,
                itemId: 'mdc-command-rule-remove-menu-item',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function(menu) {
            var isActive = menu.record.get('active'),
                toggleActivationMenuItem = menu.down('#mdc-command-rule-activation-toggle-menu-item');

            Ext.suspendLayouts();
            if (toggleActivationMenuItem) {
                toggleActivationMenuItem.setText(isActive
                    ? Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate')
                    : Uni.I18n.translate('general.activate', 'MDC', 'Activate'));
            }
            Ext.resumeLayouts(true);
        }
    }
});