/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.view.AddWebServicesToStateGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.AddWebServicesToStateGrid',
    xtype: 'add-web-services-to-state-selection-grid',
    requires: [
        'Dlc.devicelifecyclestates.store.AvailableWebServiceEndpoints'
    ],
    plugins: {
        ptype: 'bufferedrenderer'
    },
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfWebServiceEndpoints.selected', count, 'DLC',
            'No web service endpoints selected', '{0} web service endpoints selected', '{0} web service endpoints selected'
        );
    },
    bottomToolbarHidden: true,
    columns: {
        items: [
            {
                header: Uni.I18n.translate('geeral.name', 'DLC', 'Name'),
                dataIndex: 'name',
                sortable: false,
                menuDisabled: true,
                ascSortCls: Ext.baseCSSPrefix,          //No arrow
                flex: 1
            }
        ]
    }
});