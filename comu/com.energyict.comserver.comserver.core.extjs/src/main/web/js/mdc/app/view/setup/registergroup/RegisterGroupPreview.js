Ext.define('Mdc.view.setup.registergroup.RegisterGroupPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    alias: 'widget.registerGroupPreview',
    itemId: 'registerGroupPreview',

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
                    {
                        xtype: 'component',
                        html: '<b>' + Uni.I18n.translate('registerGroup.previewTitle', 'MDC', 'Selected register group preview') + '</b>',
                        itemId: 'registerGroupPreviewTitle'
                    },
                    '->',
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
                        iconCls: 'x-uni-action-iconD',
                        menu: {
                            xtype: 'register-group-action-menu'
                        }
                    }
                ],
                items: [
                    {
                        xtype: 'emptygridcontainer',
                        itemId: 'registerTypeEmptyGrid',
                        grid: {
                            xtype: 'registerTypeGrid',
                            withPaging: false,
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