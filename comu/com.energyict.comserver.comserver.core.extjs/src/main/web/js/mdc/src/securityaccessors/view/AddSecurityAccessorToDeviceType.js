/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.AddSecurityAccessorToDeviceType', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.security-accessor-add-to-device-type-form',
    overflowY: true,
    deviceTypeId: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.securityaccessors.view.AvailableSecurityAccessorsGrid'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('securityaccessors.addSecurityAccessors', 'MDC', 'Add security accessors'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    selectByDefault: false,
                    grid: {
                        itemId: 'available-security-accessors-grd',
                        xtype: 'available-security-accessors-grd',
                        plugins: {
                            ptype: 'bufferedrenderer'
                        }
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        margin: '15 0 20 0',
                        title: Uni.I18n.translate('securityaccessors.empty.title', 'MDC', 'No security accessors found'),
                        reasons: [
                            Uni.I18n.translate('securityaccessors.empty.list.item2', 'MDC', 'No security accessors have been defined yet.')
                        ]
                    }
                },
                {
                    xtype: 'container',
                    itemId: 'buttonsContainer',
                    defaults: {
                        xtype: 'button'
                    },
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            name: 'add',
                            itemId: 'btn-add-security-accessors',
                            ui: 'action'
                        },
                        {
                            name: 'cancel',
                            itemId: 'btn-cancel-add-security-accessors',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }
            ]
        }
    ]
});