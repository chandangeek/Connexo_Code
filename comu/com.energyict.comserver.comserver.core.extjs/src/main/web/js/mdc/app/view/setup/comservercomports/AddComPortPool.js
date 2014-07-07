Ext.define('Mdc.view.setup.comservercomports.AddComPortPool', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.addComPortPool',
    required: [
    ],
    content: [
        {
            ui: 'large',
            title: 'Add communication port pool',
            buttonAlign: 'left',
            items: [
                {
                    xtype: 'grid',
                    width: '60%',
                    hideHeaders: true,
                    itemId: 'addComPortPoolsGrid',
                    store: 'Mdc.store.OutboundComPortPools',
                    selType: 'checkboxmodel',
                    selModel: {
                        checkOnly: true,
                        enableKeyNav: false
                    },
                    buttonAlign: 'left',
                    columns: [
                        {
                            header: 'Communication port pool',
                            dataIndex: 'name',
                            flex: 3
                        },
                        {
                            header: 'status',
                            dataIndex: 'type',
                            flex: 1
                        }
                    ],
                    tbar: [
                        {
                            xtype: 'label',
                            text: 'No communication port pools selected',
                            itemId: 'comPortPoolsGridSelection'
                        },
                        {
                            xtype: 'button',
                            text: 'Uncheck all',
                            action: 'uncheckAll'
                        },
                        {
                            xtype: 'container',
                            flex: 1
                        },
                        {
                            xtype: 'button',
                            ui: 'link',
                            text: 'Manage comport pools'
                        }
                    ],
                    buttons: [
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            action: 'saveModel',
                            itemId: 'createEditButton'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            href: ''
                        }
                    ]
                }
            ]
        }
    ]
});


