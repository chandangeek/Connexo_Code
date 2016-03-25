Ext.define('Dxp.view.tasks.AddReadingTypesToTaskSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.AddReadingTypesToTaskSetup',
    itemId: 'AddReadingTypesToTaskSetup',
    overflowY: true,

    requires: [
        'Dxp.view.tasks.AddReadingTypesToTaskFilter',
        'Dxp.view.tasks.AddReadingTypesToTaskBulk',
        'Dxp.store.LoadedReadingTypes',
        'Dxp.store.UnitsOfMeasure',
        'Dxp.store.TimeOfUse',
        'Dxp.store.Intervals',
        'Dxp.view.tasks.AddReadingTypesNoItemsFoundPanel'
    ],

    content: [
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
                        //xtype: 'no-items-found-panel',
                        //margin: '15 0 20 0',
                        //title: Uni.I18n.translate('validation.readingType.empty.title', 'DES', 'No reading types found.'),
                        //reasons: [
                        //    Uni.I18n.translate('validation.readingType.empty.list.item1', 'DES', 'No reading types have been added yet.'),
                        //    Uni.I18n.translate('validation.readingType.empty.list.item2', 'DES', 'No reading types comply to the filter.'),
                        //    Uni.I18n.translate('dataExportTasks.readingType.empty.list.item3', 'DES', 'All reading types have already been added to the data export task.')
                        //]
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
                    itemId: 'dxp-view-tasks-addreadingtypes-filter-panel-top'
                }
            ]
        }
    ]
});
