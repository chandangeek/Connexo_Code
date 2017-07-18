/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsOnDevice', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-history-custom-attribute-sets-versions',

    margin: '20 0 0 0',

    requires: [
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsSetup'
    ],

    items: [
        {
            title: Uni.I18n.translate('general.versions', 'MDC', 'Versions'),
            ui: 'medium',
            type: 'device',
            padding: '8 16 16 0',
            xtype: 'custom-attribute-set-versions-setup',
            store: 'Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice'
        }
    ]
});

