Ext.define('Mdc.view.setup.commandrules.CommandRuleActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.commandRuleActionMenu',

    ACTION_TOGGLE_ACTIVATION: 'toggleCommandRuleActivation',
    ACTION_EDIT_RULE: 'editCommandRule',
    ACTION_VIEW_PENDING_CHANGES: 'viewPendingChanges',
    ACTION_REMOVE_RULE: 'removeCommandRule',

    initComponent: function() {
        this.items = [
            {
                text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                action: this.ACTION_TOGGLE_ACTIVATION,
                privileges: Mdc.privileges.CommandLimitationRules.admin,
                itemId: 'mdc-command-rule-activation-toggle-menu-item',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: this.ACTION_EDIT_RULE,
                privileges: Mdc.privileges.CommandLimitationRules.admin,
                itemId: 'mdc-command-rule-edit-menu-item',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.viewPendingChanges', 'MDC', 'View pending changes'),
                action: this.ACTION_VIEW_PENDING_CHANGES,
                privileges: Mdc.privileges.CommandLimitationRules.view,
                itemId: 'mdc-command-rule-viewPendingChanges-menu-item',
                section: this.SECTION_VIEW
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: this.ACTION_REMOVE_RULE,
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