/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.AddReadingTypesToTaskSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.AddReadingTypesToTaskSetup',
    itemId: 'AddReadingTypesToTaskSetup',
    overflowY: true,
    defaultFilters: null,

    requires: [
        'Dxp.view.tasks.AddReadingTypesToTaskFilter',
        'Dxp.view.tasks.AddReadingTypesToTaskBulk',
        'Dxp.store.LoadedReadingTypes',
        'Dxp.store.UnitsOfMeasure',
        'Dxp.store.TimeOfUse',
        'Dxp.store.Intervals',
        'Dxp.view.tasks.AddReadingTypesNoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.addReadngTypes', 'DES', 'Add reading types'),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'preview-container',
                        selectByDefault: false,
                        grid: {
                            itemId: 'addReadingTypesGrid',
                            xtype: 'AddReadingTypesToTaskBulk',
                            store: 'Dxp.store.LoadedReadingTypes',
                            height: 600,
                            plugins: {
                                ptype: 'bufferedrenderer'
                            }
                        },
                        emptyComponent: {
                            xtype: 'addReadingTypesNoItemsFoundPanel'
                        }
                    },
                    {
                        xtype: 'container',
                        itemId: 'buttonsContainer',
                        defaults: {
                            xtype: 'button'
                        },
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'DES', 'Add'),
                                name: 'add',
                                itemId: 'btn-add-reading-types',
                                action: 'addReadingTypes',
                                ui: 'action'
                            },
                            {
                                name: 'cancel',
                                itemId: 'lnk-cancel-add-reading-types',
                                text: Uni.I18n.translate('general.cancel', 'DES', 'Cancel'),
                                ui: 'link'
                            }
                        ]
                    }
                ],
                dockedItems: [
                    {
                        xtype: 'dxp-view-tasks-addreadingtypestotaskfilter',
                        itemId: 'dxp-view-tasks-addreadingtypes-filter-panel-top',
                        defaultFilters: me.defaultFilters
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
