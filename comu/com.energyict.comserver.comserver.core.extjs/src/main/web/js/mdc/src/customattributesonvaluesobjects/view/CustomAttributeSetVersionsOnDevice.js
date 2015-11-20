Ext.define('Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsOnDevice', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-history-custom-attribute-sets-versions',

    margin: '20 0 0 20',

    requires: [
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsSetup'
    ],

    items: [
        {
            title: Uni.I18n.translate('general.versions', 'MDC', 'Versions'),
            ui: 'medium',
            type: 'device',
            xtype: 'custom-attribute-set-versions-setup',
            store: 'Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice'
        }
    ]
});

