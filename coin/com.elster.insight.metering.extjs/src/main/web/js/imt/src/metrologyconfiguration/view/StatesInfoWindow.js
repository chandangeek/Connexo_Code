/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.StatesInfoWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.states-info-window',
    closable: true,
    width: 800,
    height: 425,
    modal: true,
    layout: 'fit',
    closeAction: 'destroy',
    floating: true,
    statesStore: null,
    title: Uni.I18n.translate('general.usagePointStates', 'IMT', 'Usage point states'),

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'grid',
                itemId: 'states-info-window-grid',
                margin: '10 0 0 10',
                store: me.statesStore,
                columns: [
                    {
                        header: Uni.I18n.translate('general.state', 'IMT', 'State'),
                        dataIndex: 'name',
                        flex: 1
                    },
                    {
                        header: Uni.I18n.translate('general.stage', 'IMT', 'Stage'),
                        dataIndex: 'stage',
                        flex: 1
                    },
                    {
                        header: Uni.I18n.translate('general.usagePointLifeCycle', 'IMT', 'Usage point life cycle'),
                        dataIndex: 'usagePointLifeCycleName',
                        flex: 1
                    }
                ]
            }
        ];

        me.dockedItems = [
            {
                xtype: 'toolbar',
                dock: 'top',
                padding: '10 0 0 10',
                store: me.statesStore,
                items: [
                    {
                        xtype: 'tbtext',
                        itemId: 'states-info-window-grid-count',
                        text: Uni.I18n.translate('general.usagePointStatesCount', 'IMT', '{0} usage point states(s)', me.statesStore.totalCount),
                    }
                ]
            },
            {
                xtype: 'toolbar',
                dock: 'bottom',
                items: [
                    '->',
                    {
                        xtype: 'button',
                        itemId: 'states-info-window-close-btn',
                        text: Uni.I18n.translate('general.close', 'UNI', 'Close'),
                        handler: function(){me.destroy()}
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});