Ext.define('Mdc.view.setup.searchitems.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Ext.grid.plugin.BufferedRenderer'
    ],
    alias: 'widget.searchitems-bulk-step1',
    name: 'selectDevices',
    ui: 'large',
    title: Uni.I18n.translate('searchItems.bulk.step1title', 'MDC', 'Bulk action - step 1 of 5: Select devices'),
    items: [
        {
            xtype: 'toolbar',
            layout: {
                type: 'vbox',
                align: 'left'
            },
            width: '100%',
            items: [
                {
                    itemId: 'step1-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true,
                    text: Uni.I18n.translate('searchItems.bulk.devicesError', 'MDC', 'It is required to select one or more devices to go to the next step')
                },
                {
                    itemId: 'deviceSelectionRange',
                    xtype: 'radiogroup',
                    name: 'selectionMode',
                    columns: 1,
                    vertical: true,
                    submitValue: false,
                    defaults: {
                        padding: '0 0 30 0'
                    },
                    items: [
                        {
                            itemId: 'allDevices',
                            boxLabel: '<b>' + Uni.I18n.translate('searchItems.bulk.allDevices', 'MDC', 'All devices') + '</b><br/>' +
                                '<span style="color: grey;">' +
                                Uni.I18n.translate('searchItems.bulk.selectMsg', 'MDC', 'Select all devices (related to filters on previous screen)') +
                                '</span>',
                            name: 'deviceRange',
                            inputValue: 'ALL',
                            checked: true
                        },
                        {
                            itemId: 'selectedDevices',
                            boxLabel: '<b>' + Uni.I18n.translate('searchItems.bulk.selectedDevices', 'MDC', 'Selected devices') + '</b></br>' +
                                '<span style="color: grey;">' + Uni.I18n.translate('searchItems.bulk.selectedDevicesInTable', 'MDC', 'Select devices in table') +
                                '</span>',
                            name: 'deviceRange',
                            inputValue: 'SELECTED'
                        }
                    ]
                },
                {
                    itemId: 'selected-devices',
                    xtype: 'container',
                    name: 'selected-devices',
                    width: '100%',
                    layout: {
                        type: 'hbox',
                        align: 'middle'
                    },
                    items: [
                        {
                            itemId: 'devices-qty-txt',
                            xtype: 'container',
                            name: 'devices-qty-txt',
                            html: '<span style="color: grey;">' + Uni.I18n.translate('searchItems.bulk.noDeviceSelected', 'MDC', 'No device selected') +
                                '</span>'
                        },
                        {
                            itemId: 'uncheck-all',
                            xtype: 'button',
                            name: 'uncheck-all-btn',
                            text: 'Uncheck all',
                            margin: '0 0 0 16'
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'gridpanel',
            itemId: 'devicesgrid',
            store: 'Mdc.store.DevicesBuffered',
            height: 355,
            plugins: {
                ptype: 'bufferedrenderer'
            },
            selType: 'checkboxmodel',
            selModel: {
                checkOnly: true,
                enableKeyNav: false,
                showHeaderCheckbox: false
            },
            columns: {
                items: [
                    {
                        itemId: 'MRID',
                        header: Uni.I18n.translate('searchItems.bulk.mrid', 'MDC', ' MRID'),
                        dataIndex: 'mRID',
                        flex: 1,
                        renderer: function (value) {
                            return '<a href="#devices/' + value + '">' + value + '</a>';
                        }
                    },
                    {
                        itemId: 'serialNumber',
                        header: Uni.I18n.translate('searchItems.bulk.serialNumber', 'MDC', ' Serial number'),
                        dataIndex: 'serialNumber',
                        flex: 1
                    },
                    {
                        itemId: 'deviceType',
                        header: Uni.I18n.translate('searchItems.bulk.deviceType', 'MDC', 'Device type'),
                        dataIndex: 'deviceTypeName',
                        flex: 1
                    },
                    {
                        itemId: 'deviceConfiguration',
                        header: Uni.I18n.translate('searchItems.bulk.deviceConfig', 'MDC', 'Device configuration'),
                        dataIndex: 'deviceConfigurationName',
                        flex: 1
                    }
                ]
            }
        }
    ]
});