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
            privileges: !Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'activateRule'
        },
        {
            itemId: 'deactivate',
            text: Uni.I18n.translate('validation.deactivate', 'CFG', 'Deactivate'),
            privileges: !Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'deactivateRule'
        },
        {
            itemId: 'editRule',
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            privileges: !Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'editRule'
        },
        {
            itemId: 'deleteRule',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            privileges: !Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'deleteRule'
        }
    ]
});

