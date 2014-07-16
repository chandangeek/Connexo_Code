Ext.define('Mdc.view.setup.comservercomports.AddComPortPool', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.addComPortPool',
    itemId: 'addComPortPoolToComPort',

    content: [
        {
            ui: 'large',
            title: 'Add communication port pool',
            buttonAlign: 'left',
            items: [
                {
                    xtype: 'radiogroup',
                    name: 'AllOrSelectedCommunicationPortPools',
                    columns: 1,
                    vertical: true,
                    submitValue: false,
                    defaults: {
                        padding: '0 0 30 0'
                    },
                    items: [
                        {
                            boxLabel: '<b>' +  Uni.I18n.translate('comServerComPorts.allComPortPools', 'MDC', 'All communication port pools') + '</b><br/>' +
                                '<span style="color: grey;"><i>' + Uni.I18n.translate('general.selectAllItems', 'MDC', 'Select all items (related to filters on previous screen)') + '</i></span>',
                            name: 'comPortPoolsRange',
                            checked: true,
                            inputValue: 'ALL'
                        },
                        {
                            boxLabel: '<b>' +  Uni.I18n.translate('comServerComPorts.selectedComPortPools', 'MDC', 'Selected communication port pools') + '</b><br/><span style="color: grey;"><i>' + Uni.I18n.translate('general.selectItemsInTable', 'MDC', 'Select items in table') + '</i></span>',
                            name: 'comPortPoolsRange',
                            inputValue: 'SELECTED'
                        }
                    ]
                },
                {
                    xtype: 'toolbar',
                    border: 0,
                    align: 'left',
                    items: [
                        {
                            xtype: 'container',
                            itemId: 'comPortPoolsCountContainer',
                            items: [
                                {
                                    xtype: 'container',
                                    html: Uni.I18n.translate('comServerComPorts.addPools.noSelectedCount', 'MDC', 'No communication port pools selected')
                                }
                            ]
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.uncheckall', 'MDC', 'Uncheck All'),
                            action: 'uncheckallcomportPools',
                            itemId: 'uncheckAllComPortPools',
                            ui: 'action',
                            margin: '0 0 0 10'
                        },
                        {
                            xtype: 'container',
                            flex: 1
                        },
                        {
                            xtype: 'button',
                            border: 0,
                            ui: 'link',
                            text: Uni.I18n.translate('comServerComPorts.addPools.,manageComPorts', 'MDC', 'Manage communication port pools')
                        }]
                },
                {
                    xtype: 'gridpanel',
                    itemId: 'addComPortPoolsGrid',
                    store: 'Mdc.store.OutboundComPortPools',
                    scroll: false,
                    viewConfig: {
                        style: { overflow: 'auto', overflowX: 'hidden' }
                    },
                    selType: 'checkboxmodel',
                    selModel: {
                        checkOnly: true,
                        enableKeyNav: false,
                        showHeaderCheckbox: false
                    },
                    columns: [
                        {
                            header: 'Communication port pool',
                            dataIndex: 'name',
                            menuDisabled:true,
                            flex: 3
                        },
                        {
                            header: 'Status',
                            dataIndex: 'active',
                            flex: 1,
                            renderer: function (value) {
                                if (value === true) {
                                    return Uni.I18n.translate('general.active', 'MDC', 'Active');
                                } else {
                                    return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                                }
                            }
                        },
                    ]
                },
                {
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            action: 'saveModel',
                            itemId: 'createEditButton',
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link',
                            itemId: 'cancelLink'
                        }
                    ]
                }
            ]
        }
    ]
});


