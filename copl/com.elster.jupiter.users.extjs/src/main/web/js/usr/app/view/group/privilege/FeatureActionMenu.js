Ext.define('Usr.view.group.privilege.FeatureActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.feature-action-menu',
    plain: true,
    border: false,
    itemId: 'feature-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('privilege.allow', 'USM', 'Allow'),
            itemId: 'privilegeAllow',
            action: 'privilegeAllow'
        },
        {
            text: Uni.I18n.translate('privilege.deny', 'USM', 'Deny'),
            itemId: 'privilegeDeny',
            action: 'privilegeDeny'
        }
    ]
});
