Ext.define('Cfg.view.validation.RuleSetActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.ruleset-action-menu',
    itemId: 'ruleset-action-menu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            itemId: 'editRuleSet',
            text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'editRuleSet'
        },
        {
            itemId: 'deleteRuleSet',
            text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'deleteRuleSet'
        }
    ]
});
