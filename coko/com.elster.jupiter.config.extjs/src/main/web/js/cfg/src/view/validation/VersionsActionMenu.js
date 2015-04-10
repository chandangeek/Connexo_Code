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
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'editVersion'
        },
        {
            itemId: 'cloneVersion',
            text: Uni.I18n.translate('validation.clone', 'CFG', 'Clone'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.versions.administrate.validationConfiguration'),
            action: 'cloneVersion'
        },
        {
            itemId: 'deleteVersion',
            text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'deleteVersion'
        }
    ]
});
