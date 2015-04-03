Ext.define('Cfg.view.validation.VersionActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.version-action-menu',
    plain: true,
    border: false,
    itemId: 'version-action-menu',
    shadow: false,
    items: [
        {
            itemId: 'editVersion',
            text: Uni.I18n.translate('validation.edit', 'CFG', 'Edit'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'editVersion'
        },     
        {
            itemId: 'deleteVersion',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            action: 'deleteVersion'
        }
    ]
});

