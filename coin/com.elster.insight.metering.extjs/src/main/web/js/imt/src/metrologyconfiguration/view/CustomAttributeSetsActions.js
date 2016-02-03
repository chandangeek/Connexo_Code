Ext.define('Imt.metrologyconfiguration.view.CustomAttributeSetsActions', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.custom-attribute-sets-actions',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'remove',
            text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
//            privileges: Cfg.privileges.Validation.validateManual,
            action: 'removeCustomAttributeSet'
        }
    ]
});
