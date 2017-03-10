/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.form.field.readingtypes.SelectedReadingTypesWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.selected-reading-types-window',
    closable: true,
    width: 800,
    height: 425,
    autoShow: true,
    modal: true,
    layout: 'fit',
    closeAction: 'destroy',
    floating: true,
    selectedReadingTypesStore: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'grid',
                margin: '10 0 0 0',
                store: me.selectedReadingTypesStore,
                columns: [
                    {
                        header: Uni.I18n.translate('general.readingType', 'UNI', 'Reading type'),
                        dataIndex: 'fullAliasName',
                        flex: 1
                    }
                ]
            }
        ]

        me.callParent(arguments);
    }
});