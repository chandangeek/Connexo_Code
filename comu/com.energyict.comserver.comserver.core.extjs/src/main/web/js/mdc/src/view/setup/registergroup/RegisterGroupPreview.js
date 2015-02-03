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
                            itemId: 'register-groups-register-types-grid',
                            withPaging: false,
                            bottomPagging: false,
                            withActions: false,
                            store: 'AvailableRegisterTypesForRegisterGroup'
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
                                    privileges: ['privilege.administrate.masterData'],
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