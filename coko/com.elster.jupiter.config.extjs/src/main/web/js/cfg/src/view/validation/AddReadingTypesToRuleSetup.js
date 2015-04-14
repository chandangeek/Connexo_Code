Ext.define('Cfg.view.validation.AddReadingTypesToRuleSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.AddReadingTypesToRuleSetup',
    itemId: 'addReadingTypesToRuleSetup',
    overflowY: true,

    requires: [
        'Uni.component.filter.view.FilterTopPanel',
        'Cfg.view.validation.SideFilter'
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
                    name: 'filter'/*,
                    emptyText: Uni.I18n.translate('general.none', 'CFG', 'None')*/
                },
                {
                    xtype: 'container',
                    itemId: 'bulkReadingTypesContainer'
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
                            itemId: 'btn-add-reading-types',
                            ui: 'action',
                            disabled: true
                        },
                        {
                            name: 'cancel',
                            itemId: 'lnk-cancel-add-reading-types',
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
