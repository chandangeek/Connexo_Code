/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.creationrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.alarms-creation-rules-list',
    store: 'Dal.store.CreationRules',
    columns: {
        items: [
            {
                itemId: 'Name',
                header: Uni.I18n.translate('general.title.name', 'DAL', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                itemId: 'templateColumn',
                header: Uni.I18n.translate('general.title.ruleTemplate', 'DAL', 'Rule template'),
                dataIndex: 'template_name',
                flex: 1
            },
            {   itemId: 'action',
                xtype: 'uni-actioncolumn',
                privileges: Dal.privileges.Alarm.createAlarmRule,
                menu: { xtype: 'alarm-creation-rule-action-menu' }
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('administration.alarmCreationRules.pagingtoolbartop.displayMsg', 'DAL', '{0} - {1} of {2} alarm creation rules'),
                displayMoreMsg: Uni.I18n.translate('administration.alarmCreationRules.pagingtoolbartop.displayMoreMsg', 'DAL', '{0} - {1} of more than {2} alarm creation rules'),
                emptyMsg: Uni.I18n.translate('administration.alarmCreationRules.pagingtoolbartop.emptyMsg', 'DAL', 'There are no alarm creation rules to display'),
                items: [
                    {
                        itemId: 'createRule',
                        xtype: 'button',
                        text: Uni.I18n.translate('administration.alarmCreationRules.add', 'DAL', 'Add rule'),
                        privileges: Dal.privileges.Alarm.createAlarmRule,
                        href: '#/administration/alarmcreationrules/add',
                        action: 'create'
                    }
                ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('administration.alarmCreationRules.pagingtoolbarbottom.itemsPerPage', 'DAL', 'Alarm creation rules per page')
            }
        ];

        this.callParent(arguments);
    }
});