Ext.define('Dxp.view.tasks.AddReadingTypesToTaskSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.AddReadingTypesToTaskSetup',
    itemId: 'AddReadingTypesToTaskSetup',
    overflowY: true,

    requires: [
        'Uni.component.filter.view.FilterTopPanel',
        'Dxp.view.tasks.AddReadingTypesToTaskBulk',
        'Dxp.view.tasks.SideFilter'
    ],

    side: [
        {
            xtype: 'rt-side-filter'
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
                    name: 'filter'
                },
                {
                    xtype: 'container',
                    itemId: 'AddReadingTypesToTaskBulk'
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
                            ui: 'action'
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
