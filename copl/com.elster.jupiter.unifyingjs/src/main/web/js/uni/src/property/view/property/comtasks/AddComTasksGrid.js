/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
 
 Ext.define('Uni.property.view.property.comtasks.AddComTasksGrid', {
    extend: 'Ext.grid.Panel',
    overflowY: 'auto',
    alias: 'widget.uni-comtasks-selection-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
		'Uni.property.model.PropertyCommunicationTask',
		'Uni.property.store.PropertyCommunicationTasks'
    ],

    selType: 'checkboxmodel',
    
    selModel: {
        mode: 'MULTI',
        checkOnly: true,
    },
    
    store: 'Uni.property.store.PropertyCommunicationTasks',

    initComponent: function () {
        var me = this;
        
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'UNI', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.systemTask', 'UNI', 'Is system ComTask'),
                dataIndex: 'systemTask',
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('general.yes', 'UNI', 'Yes')
                    } else {
                        return Uni.I18n.translate('general.no', 'UNI', 'No')
                    }
                },
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                usesExactCount: true,
                dock: 'top',
                displayMsg: Uni.I18n.translate('comtask.pagingtoolbartop.displayMsg', 'UNI', '{0} - {1} of {2} communication tasks'),
                displayMoreMsg: Uni.I18n.translate('comtask.pagingtoolbartop.displayMoreMsg', 'UNI', '{0} - {1} of more than {2} communication tasks'),
                emptyMsg: Uni.I18n.translate('comtask.pagingtoolbartop.emptyMsg', 'UNI', 'There are no communication tasks to display'),
                items: []
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                isSecondPagination: true,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('comtask.pagingtoolbarbottom.itemsPerPage', 'UNI', 'Communication tasks per page')
            }
        ];

        me.callParent();
    }
});