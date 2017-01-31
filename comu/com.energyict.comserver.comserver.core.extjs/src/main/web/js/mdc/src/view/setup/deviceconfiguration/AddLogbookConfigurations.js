/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconfiguration.AddLogbookConfigurations', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-logbook-configurations',
    requires: [
        'Uni.grid.column.Obis',
        'Uni.util.FormEmptyMessage',
        'Mdc.store.LogbookConfigurations',
        'Mdc.view.setup.deviceconfiguration.AddLogbookConfigurationsGrid'
    ],
    deviceTypeId: null,
    deviceConfigurationId: null,

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('deviceconfiguration.addLogbookConfiguration', 'MDC', 'Add logbook configuration'),
            items: [
                {
                    xtype: 'preview-container',
                    selectByDefault: false,
                    grid: {
                        xtype: 'add-logbook-configurations-grid',
                        itemId: 'add-logbook-configurations-grid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        itemId: 'add-logbook-configurations-empty-grid',
                        text: Uni.I18n.translate('logbooktype.empty.list.item1', 'MDC', 'No logbook types have been defined yet.')
                    }
                },
                {
                    layout: 'hbox',
                    margin: '10 0 0 0',
                    defaults: {
                        xtype: 'button'
                    },
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            disabled: true,
                            action: 'add',
                            itemId: 'logbookConfAdd',
                            ui: 'action'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            action: 'cancel',
                            ui: 'link',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#/administration/devicetypes/' + this.up('add-logbook-configurations').deviceTypeId + '/deviceconfigurations/' + this.up('add-logbook-configurations').deviceConfigurationId + '/logbookconfigurations';
                                    }
                                }
                            }
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});

