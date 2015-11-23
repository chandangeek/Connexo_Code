Ext.define('Sam.view.componentslist.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.components-overview',
    requires: [
        'Sam.view.componentslist.Grid',
        'Sam.view.componentslist.Filter'
    ],

    content: [
        {
            title: Uni.I18n.translate('general.componentsList', 'SAM', 'Components list'),
            ui: 'large',
            items: [
                {
                    xtype: 'components-filter',
                    itemId: 'components-filter'
                },
                {
                    xtype: 'emptygridcontainer',
                    grid: {
                        xtype: 'components-list',
                        itemId: 'components-list'
                    },
                    emptyComponent: {
                        itemId: 'components-no-items-found-panel',
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('components.empty.title', 'SAM', 'No components found'),
                        reasons: [
                            Uni.I18n.translate('components.empty.list.item1', 'SAM', 'The filter is too narrow')
                        ]
                    }
                }
            ]
        }
    ]
});