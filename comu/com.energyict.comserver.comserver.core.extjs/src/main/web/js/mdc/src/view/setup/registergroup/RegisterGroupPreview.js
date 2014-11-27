Ext.define('Mdc.view.setup.registergroup.RegisterGroupPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    alias: 'widget.registerGroupPreview',
    itemId: 'registerGroupPreview',
    title: Uni.I18n.translate('registerGroup.previewTitle', 'MDC', 'Selected register group preview'),
    ui: 'medium',
    requires: [
        'Mdc.model.RegisterGroup',
        'Mdc.view.setup.registergroup.RegisterGroupActionMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    padding: '20 0 0 0',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                border: false,
                itemId: 'registerGroupPreviewForm',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                tbar: [
                ],
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'registerTypeEmptyGrid',
                        grid: {
                            xtype: 'registerTypeGrid',
                            withPaging: true,
                            topPagging: {
                                xtype: 'pagingtoolbartop',
                                store: 'AvailableRegisterTypesForRegisterGroup',
                                dock: 'top',
                                displayMsg: Uni.I18n.translate('registerTypes.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register types'),
                                displayMoreMsg: Uni.I18n.translate('registerTypes.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register types'),
                                emptyMsg: Uni.I18n.translate('registerTypes.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register types to display')
                            },
                            bottomPagging: false,
                            withActions: false,
                            store: 'AvailableRegisterTypesForRegisterGroup',
                            maxHeight: 650
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('registerGroupPreview.empty.title', 'MDC', 'No register types found'),
                            reasons: [
                                Uni.I18n.translate('registerGroupPreview.empty.list.item1', 'MDC', 'No register types are associated to this register group.')
                            ],
                            stepItems: [
                                {
                                    itemId: 'editEmptyPreviewButton',
                                    text: Uni.I18n.translate('registerGroup.edit', 'MDC', 'Edit register group'),
                                    action: 'editRegisterGroup'
                                }
                            ]
                        }
                    },
                    {
                        xtype: 'registerTypePreview',
                        withActions: false
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});