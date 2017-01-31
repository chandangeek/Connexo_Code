/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.group.privilege.ApplicationList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.applicationList',
    itemId: 'applicationList',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Usr.store.Applications',
        'Usr.view.group.privilege.ApplicationActionMenu',
        'Uni.grid.column.Action'
    ],

    store: 'Usr.store.Applications',

    initComponent: function () {
        this.columns = {
            defaults: {
                flex: 1,
                sortable: false,
                hideable: false,
                fixed: true
            },
            items: [
                {
                    header: Uni.I18n.translate('privilege.application', 'USR', 'Application'),
                    dataIndex: 'translatedName',
                    menuDisabled: true,
                    flex: 2,
                    renderer: function (value, metadata, record) {
                        switch (record.get('selected')) {
                            case 0:
                                metadata.tdCls = 'uni-icon-drop-no';
                                break;
                            case 1:
                                metadata.tdCls = 'uni-no-icon';
                                break;
                            case 2:
                                metadata.tdCls = 'uni-icon-drop-yes';
                                break;
                        }
                        return record.get('translatedName');
                    }
                },
                {
                    header: Uni.I18n.translate('general.description', 'USR', 'Description'),
                    dataIndex: 'description',
                    flex: 7
                },
                {
                    xtype: 'uni-actioncolumn',
                    items: 'Usr.view.group.privilege.ApplicationActionMenu'
                }
            ]
        };

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('privilege.application.top', 'USR', 'Applications'),
                items: [
                    {
                        text: Uni.I18n.translate('privilege.noAccess', 'USR', 'No access'),
                        icon: '../sky/build/resources/images/grid/drop-no.png',
                        itemId: 'privilegesNoAccess',
                        action: 'privilegesNoAccess'
                    },
                    {
                        text: Uni.I18n.translate('privilege.fullControl', 'USR', 'Full control'),
                        icon: '../sky/build/resources/images/grid/drop-yes.png',
                        itemId: 'privilegesFullControl',
                        action: 'privilegesFullControl'
                    }
                ]
            }
        ];

        this.callParent();
    }
});