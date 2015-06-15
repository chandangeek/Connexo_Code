Ext.define('Mdc.view.setup.validation.VersionActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.validation-version-actionmenu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.view', 'MDC', 'View'),
            itemId: 'viewVersion',
            action: 'viewVersion'
        }
    ]
});
