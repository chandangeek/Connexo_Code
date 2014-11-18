Ext.define('Cfg.view.validation.AddReadingTypesToRuleSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.AddReadingTypesToRuleSetup',
    itemId: 'addReadingTypesToRuleSetup',
    overflowY: true,

    requires: [
        'Uni.component.filter.view.FilterTopPanel',
        'Cfg.view.validation.SideFilter',
        'Cfg.view.validation.AddReadingTypesBulk'
    ],

    side: [
        {
            xtype: 'cfg-side-filter'
        }
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('validation.addReadingTypes', 'CFG', 'Add reading types'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    title: Uni.I18n.translate('general.filter', 'CFG', 'Filter'),
                    xtype: 'filter-top-panel',
                    itemId: 'filterReadingTypes',
                    margin: '0 0 20 0',
                    name: 'filter',
                    emptyText: Uni.I18n.translate('general.none', 'CFG', 'None')
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        itemId: 'addReadingTypesGrid',
                        xtype: 'addReadingTypesBulk'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        margin: '0 0 20 0',
                        title: Uni.I18n.translate('validation.readingType.empty.title', 'CFG', 'No reading types found.'),
                        reasons: [
                            Uni.I18n.translate('validation.readingType.empty.list.item1', 'CFG', 'No reading types have been added yet.'),
                            Uni.I18n.translate('validation.readingType.empty.list.item2', 'CFG', 'No reading types comply to the filter.'),
                            Uni.I18n.translate('validation.readingType.empty.list.item3', 'CFG', 'All reading types have been already added to rule.')
                        ]
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

                            text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                            name: 'add',
                            ui: 'action'
                        },
                        {
                            name: 'cancel',
                            text: Uni.I18n.translate('general.add', 'CFG', 'Cancel'),
                            ui: 'link'
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
