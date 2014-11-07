Ext.define('Cfg.view.validation.RuleSetActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.ruleset-action-menu',
    itemId: 'ruleset-action-menu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            itemId: 'viewRuleSet',
            text: Uni.I18n.translate('general.view', 'CFG', 'View'),
            action: 'viewRuleSet'
        },
        {
            itemId: 'editRuleSet',
            text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'editRuleSet'
        },
        {
            itemId: 'deleteRuleSet',
            text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'deleteRuleSet'
        }
    ]
});
