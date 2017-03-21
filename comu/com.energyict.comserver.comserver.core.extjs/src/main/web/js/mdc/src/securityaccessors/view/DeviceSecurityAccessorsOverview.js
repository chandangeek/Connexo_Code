/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.DeviceSecurityAccessorsOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-security-accessors-overview',

    device: null,

    requires: [
        'Mdc.securityaccessors.view.DeviceSecurityAccessorsGrid',
        'Mdc.securityaccessors.view.DeviceSecurityAccessorPreview',
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],

    initComponent: function () {
        var me = this,
            securityKeysStore = Ext.getStore('Mdc.securityaccessors.store.DeviceSecurityKeys'),
            securityCertificatesStore = Ext.getStore('Mdc.securityaccessors.store.DeviceSecurityCertificates');

        securityKeysStore.getProxy().setExtraParam('deviceId', me.device.get('name'));
        securityCertificatesStore.getProxy().setExtraParam('deviceId', me.device.get('name'));
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'securityAccessorsLink'
                    }
                ]
            }
        ];

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'mdc-device-security-accessors-panel',
                title: Uni.I18n.translate('general.securityAccessors', 'MDC', 'Security accessors'),

                items: [
                    {
                        xtype: 'tabpanel',
                        margin: '15 0 0 0',
                        itemId: 'mdc-device-security-accessors-tab-panel',
                        activeTab: me.activeTab,
                        width: '100%',
                        items: [
                            {
                                title: Uni.I18n.translate('general.keys', 'MDC', 'Keys'),
                                padding: '8 16 16 0',
                                itemId: 'mdc-device-accessors-keys-tab',
                                items: [
                                    {
                                        xtype: 'preview-container',
                                        itemId: 'mdc-device-security-accessors-keys-previewContainer',
                                        grid: {
                                            xtype: 'device-security-accessors-grid',
                                            itemId: 'mdc-device-accessors-keys-grid',
                                            deviceId: encodeURIComponent(me.device.get('name')),
                                            store: securityKeysStore
                                        },
                                        emptyComponent: me.getEmptyComponent(),
                                        previewComponent: {
                                            xtype: 'device-security-accessor-preview',
                                            itemId: 'mdc-device-accessors-key-preview'
                                        }
                                    }
                                ]
                            },
                            {
                                title: Uni.I18n.translate('general.certificates', 'MDC', 'Certificates'),
                                padding: '8 16 16 0',
                                itemId: 'mdc-device-accessors-certificates-tab',
                                items: [
                                    {
                                        xtype: 'preview-container',
                                        itemId: 'mdc-device-security-accessors-certificates-previewContainer',
                                        grid: {
                                            xtype: 'device-security-accessors-grid',
                                            itemId: 'mdc-device-accessors-certificates-grid',
                                            deviceId: encodeURIComponent(me.device.get('name')),
                                            store: securityCertificatesStore
                                        },
                                        emptyComponent: me.getEmptyComponent(),
                                        previewComponent: {
                                            xtype: 'device-security-accessor-preview',
                                            itemId: 'mdc-device-accessors-certificate-preview'
                                        }
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    getEmptyComponent: function () {
        return  {
            xtype: 'uni-form-empty-message',
            text: Uni.I18n.translate('general.noSecurityAccessors', 'MDC', 'No security accessors have been defined yet.')
        };
    }

});
