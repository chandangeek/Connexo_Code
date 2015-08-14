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
            text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'editVersion'
        },     
        {
            itemId: 'deleteVersion',
            text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
            privileges: Cfg.privileges.Validation.admin,
            action: 'deleteVersion'
        }
    ]
});

