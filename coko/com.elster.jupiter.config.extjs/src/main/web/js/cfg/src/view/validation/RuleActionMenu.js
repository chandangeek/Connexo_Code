Ext.define('Cfg.view.validation.RuleActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.validation-rule-action-menu',
    plain: true,
    border: false,
    itemId: 'rule-action-menu',
    shadow: false,
    items: [
        {
            itemId: 'activate',
            text: Uni.I18n.translate('validation.activate', 'CFG', 'Activate'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'activateRule'
        },
        {
            itemId: 'deactivate',
            text: Uni.I18n.translate('validation.deactivate', 'CFG', 'Deactivate'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'deactivateRule'
        },
        {
            itemId: 'view',
            text: Uni.I18n.translate('general.view', 'CFG', 'View'),
            action: 'view'
        },
        {
            itemId: 'editRule',
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'editRule'
        },
        {
            itemId: 'deleteRule',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'deleteRule'
        }
    ]
});

