/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.main.view.SelectedReadingTypesWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.estimationSelectedReadingTypes',
    title: Uni.I18n.translate('readingType.window.noSelectedReadingtypes', 'EST', 'No reading types selected'),
    closable: true,
    requires: [
        'Est.main.store.SelectedReadingTypes'
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
            store: 'Est.main.store.SelectedReadingTypes',
            columns: [
                {
                    header: Uni.I18n.translate('general.readingType', 'EST', 'Reading type'),
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