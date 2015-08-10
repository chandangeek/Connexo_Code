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
            privileges: Cfg.privileges.Validation.admin,
            action: 'activateRule'
        },
        {
            itemId: 'deactivate',
            text: Uni.I18n.translate('validation.deactivate', 'CFG', 'Deactivate'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'deactivateRule'
        },
        {
            itemId: 'editRule',
            text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'editRule'
        },
        {
            itemId: 'deleteRule',
            text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'deleteRule'
        }
    ]
});

