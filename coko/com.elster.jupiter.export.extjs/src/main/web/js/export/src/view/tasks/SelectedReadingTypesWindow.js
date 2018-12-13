/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.SelectedReadingTypesWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.dataExportSelectedReadingTypes',
    title: Uni.I18n.translate('readingType.window.noSelectedReadingtypes', 'DES', 'No reading types selected'),
    closable: true,
    requires: [
        'Dxp.store.SelectedReadingTypes'
    ],
    width: 800,
    height: 425,
    autoShow: true,
    modal: true,
    layout: 'fit',
    closeAction: 'destroy',
    floating: true,
    items: [
        {
            xtype: 'grid',
            margin: '10 0 0 0',
            store: 'Dxp.store.SelectedReadingTypes',
            columns: [
                {
                    header: Uni.I18n.translate('general.readingType', 'DES', 'Reading type'),
                    dataIndex: 'readingType',
                    flex: 1,
                    renderer: function(data){
                        return data.fullAliasName;
                    }
                }
            ]
        }
    ]
});