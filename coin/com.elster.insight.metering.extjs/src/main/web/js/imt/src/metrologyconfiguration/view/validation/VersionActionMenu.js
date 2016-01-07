Ext.define('Imt.metrologyconfiguration.view.validation.VersionActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.validation-version-actionmenu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.view', 'IMT', 'View'),
            itemId: 'viewVersion',
            action: 'viewVersion'
        }
    ]
});
