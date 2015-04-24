Ext.define('Est.main.view.AddReadingTypes', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Est.main.view.ReadingTypesSideFilter',
        'Est.main.view.ReadingTypesGrid',
        'Uni.component.filter.view.FilterTopPanel',
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    alias: 'widget.add-reading-types',
    itemId: 'add-reading-types',
    returnLink: undefined,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                title: Uni.I18n.translate('general.filter', 'EST', 'Filter'),
                ui: 'medium',
                width: 250,
                items: [
                    {
                        xtype: 'reading-types-side-filter',
                        itemId: 'reading-types-side-filter',
                        ui: 'filter'
                    }
                ]
            }
        ];

        me.content = [
            {
                ui: 'large',
                title: Uni.I18n.translate('general.addReadingTypes', 'EST', 'Add reading types'),
                items: [
                    {
                        xtype: 'filter-top-panel',
                        itemId: 'add-reading-types-filter-toolbar'
                    },
                    {
                        xtype: 'emptygridcontainer',
                        itemId: 'reading-types-empty-grid-container',
                        grid: {
                            xtype: 'reading-types-grid',
                            itemId: 'reading-types-grid',
                            listeners: {
                                selectionchange: {
                                    fn: Ext.bind(me.onSelectionChange, me)
                                }
                            }
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('general.readingType.empty.title', 'EST', 'No reading types found.'),
                            reasons: [
                                Uni.I18n.translate('general.readingType.empty.list.item1', 'EST', 'No reading types have been added yet.'),
                                Uni.I18n.translate('general.readingType.empty.list.item2', 'EST', 'No reading types comply to the filter.'),
                                Uni.I18n.translate('general.readingType.empty.list.item3', 'EST', 'All reading types have been already added to rule.')
                            ]
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
    },

    onSelectionChange: function (selectionModel, selected) {
        this.down('[action=addReadingTypes]').setDisabled(!selected.length);
    }
});