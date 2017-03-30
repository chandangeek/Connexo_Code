/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.register.RegisterMappingAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerMappingAdd',
    //  store: 'AvailableRegisterTypes',

    requires: [
        'Mdc.view.setup.register.RegisterMappingAddGrid',
        'Mdc.view.setup.register.RegisterMappingsAddFilter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    deviceTypeId: null,

    content: {
            xtype: 'panel',
            ui: 'large',
            itemId: 'addRegisterTypePanel',
            title: Uni.I18n.translate('registerMapping.addRegisterMapping', 'MDC', 'Add register types'),
            layout: {
                type: 'vbox',
                align: 'left'
            },
            items: [
                {
                    itemId: 'add-register-type-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true
                },
                {
                    xtype: 'preview-container',
                    width: '100%',
                    selectByDefault: false,
                    grid: {
                        xtype: 'registerMappingAddGrid',
                        itemId: 'register-mapping-add-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('registermappings.empty.title', 'MDC', 'No register types found'),
                        reasons: [
                            Uni.I18n.translate('registermappings.empty.list.item1', 'MDC', 'No register types are defined yet'),
                            Uni.I18n.translate('registermappings.empty.list.item2', 'MDC', 'All register types are already added to the device type.')
                        ]
                    }
                },
                {
                    xtype: 'container',
                    itemId: 'add-register-type-selection-error',
                    hidden: true,
                    html: '<span style="color: #eb5642">' + Uni.I18n.translate('registertypes.no.registertypes.selected', 'MDC', 'Select at least 1 register type') + '</span>'
                },
                {
                    xtype: 'toolbar',
                    fieldLabel: '&nbsp',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    width: '100%',
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            xtype: 'button',
                            action: 'addRegisterMappingAction',
                            ui: 'action',
                            itemId: 'addButton'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            action: 'cancel',
                            xtype: 'button',
                            itemId: 'cancelButton',
                            ui: 'link',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#/administration/devicetypes/' + this.up('registerMappingAdd').deviceTypeId + '/registertypes'
                                    }
                                }
                            }
                        }
                    ]
                }
            ]
    },

    side: [

    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
