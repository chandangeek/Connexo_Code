Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.load-profile-type-action-menu',
    plain: true,
    border: false,
    itemId: 'load-profile-type-action-menu',
    shadow: false,
    items: [

        {
            text: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
            action: 'editloadprofiletype'
        },
        {
            text: Uni.I18n.translate('general.remove', 'USM', 'Remove'),
            action: 'deleteloadprofiletype'
        }

    ]
});
