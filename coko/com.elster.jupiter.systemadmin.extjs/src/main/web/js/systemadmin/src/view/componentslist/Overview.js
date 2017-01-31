/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.componentslist.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.components-overview',
    requires: [
        'Sam.view.componentslist.Grid',
        'Sam.view.componentslist.Filter',
        'Uni.util.FormEmptyMessage'
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
                        xtype: 'form',
                        items: {
                            xtype: 'uni-form-empty-message',
                            text: Uni.I18n.translate('components.empty', 'SAM', 'No components comply with the filter.')
                        }
                    }
                }
            ]
        }
    ]
});