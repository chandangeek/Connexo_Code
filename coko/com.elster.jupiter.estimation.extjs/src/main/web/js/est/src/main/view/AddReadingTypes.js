/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.main.view.AddReadingTypes', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Est.main.view.ReadingTypesGrid',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Est.main.view.ReadingTypeTopFilter',
        'Est.main.view.AddReadingTypesNoItemsFoundPanel'
    ],
    alias: 'widget.add-reading-types',
    itemId: 'add-reading-types',
    returnLink: undefined,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                title: Uni.I18n.translate('general.addReadingTypes', 'EST', 'Add reading types'),
                items: [
                    {
                        xtype: 'est-main-view-readingtypetopfilter'
                    },
                    {
                        xtype: 'preview-container',
                        selectByDefault: false,
                        grid: {
                            xtype: 'reading-types-grid',
                            itemId: 'reading-types-grid',
                            store: 'Est.main.store.ReadingTypes',
                            height: 600,
                            plugins: {
                                ptype: 'bufferedrenderer'
                            }
                        },
                        emptyComponent:{
                            xtype: 'addReadingTypesNoItemsFoundPanel'
                        }
                    }
                ],
                dockedItems: [
                    {
                        xtype: 'toolbar',
                        dock: 'bottom',
                        items: [
                            {
                                itemId: 'add-reading-types-button',
                                text: Uni.I18n.translate('general.add', 'EST', 'Add'),
                                ui: 'action',
                                action: 'addReadingTypes',
                                disabled: true
                            },
                            {
                                itemId: 'cancel-add-reading-types-button',
                                text: Uni.I18n.translate('general.cancel', 'EST', 'Cancel'),
                                ui: 'link',
                                action: 'cancelAddReadingTypes',
                                href: me.returnLink
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});