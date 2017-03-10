/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.SelectedReadingTypesWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.validationSelectedReadingTypes',
    title: Uni.I18n.translate('readingType.window.selectedReadingtypes', 'CFG', 'No reading types selected'),
    closable: true,
    requires: [
        'Cfg.store.SelectedReadingTypes'
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
            store: "Cfg.store.SelectedReadingTypes",
            columns: [
                {
                    header: Uni.I18n.translate('general.readingType', 'CFG', 'Reading type'),
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