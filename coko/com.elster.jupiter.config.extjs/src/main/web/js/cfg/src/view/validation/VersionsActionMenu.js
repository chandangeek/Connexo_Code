Ext.define('Cfg.view.validation.VersionsActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.versions-action-menu',
    itemId: 'ruleset-versions-action-menu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            itemId: 'editVersion',
            text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'editVersion'
        },
        {
            itemId: 'cloneVersion',
            text: Uni.I18n.translate('validation.clone', 'CFG', 'Clone'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'cloneVersion'
        },
        {
            itemId: 'deleteVersion',
            text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'deleteVersion'
        }
    ]
});
