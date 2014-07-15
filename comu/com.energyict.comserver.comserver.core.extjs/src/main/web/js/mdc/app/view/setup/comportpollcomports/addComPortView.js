Ext.define('Mdc.view.setup.comportpollcomports.addComPortView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.addComportToComportPoolView',
    itemId: 'addComportToComportPoolView',

    side: {
        xtype: 'panel',
        ui: 'medium',
        title: Uni.I18n.translate('', 'MDC', 'Communication port pools'),
        width: 350,
        items: [{
            xtype: 'comportpoolsubmenu',
            itemId: 'comportpoolsubmenu'
        }]
    },
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('comPortPoolComPort.addComPort', 'MDC', 'Add communication port'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'radiogroup',
                    name: 'AllOrSelectedCommunicationPorts',
                    columns: 1,
                    vertical: true,
                    submitValue: false,
                    defaults: {
                        padding: '0 0 30 0'
                    },
                    items: [
                        {
                            boxLabel: '<b>' +  Uni.I18n.translate('comPortPoolComPort.allComPorts', 'MDC', 'All communication ports') + '</b><br/>' +
                                '<span style="color: grey;">' + Uni.I18n.translate('general.selectAllItems', 'MDC', 'Select all items (related to filters on previous screen)') + '</span>',
                            name: 'comPortsRange',
                            checked: true,
                            inputValue: 'ALL'
                        },
                        {
                            boxLabel: '<b>' +  Uni.I18n.translate('comPortPoolComPort.selectedComPorts', 'MDC', 'Selected communication ports') + '</b><br/><span style="color: grey;">' + Uni.I18n.translate('general.selectItemsInTable', 'MDC', 'Select items in table') + '</span>',
                            name: 'comPortsRange',
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
                            itemId: 'comPortsCountContainer',
                            items: [
                                {
                                    xtype: 'container',
                                    html: Uni.I18n.translate('comPortPoolComPorts.addPorts.noPortsSelected', 'MDC', 'No communication ports selected')
                                }
                            ]
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.uncheckall', 'MDC', 'Uncheck All'),
                            action: 'uncheckallcomports',
                            itemId: 'uncheckAllComPorts',
                            ui: 'action'
                        }]
                },
                {
                    xtype: 'addComportToComportPoolGrid'
                },
                {
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'button',
                            name: 'addcomportstocomportpool',
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link',
                            handler: function (button, event) {
                                Ext.History.back();
                            }
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});