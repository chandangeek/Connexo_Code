/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskPrivilegesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comtaskPrivilegesGrid',
    store: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.grid.column.RemoveAction',
        'Mdc.view.setup.comtasks.PrivilegesInfoPanel'
    ],
    router: null,
    forceFit: true,
    autoScroll: false,
    enableColumnHide: false,
    autoHeight: true,

    initComponent: function () {
        var me = this,
        	layoutNeedsUpdate = true;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                sortable: false,
                menuDisabled: true,
                dataIndex: 'name',
                ascSortCls: null, // no sort indication
                descSortCls: null, // no sort indication
                flex: 1,
                renderer: function (value, metaData, record) {
                    var id = Ext.id();

                    Ext.defer(function () {
                        new Ext.button.Button({
                            renderTo: Ext.query('#' + id)[0],
                            name: 'comTaskPrivilegesInfoIcon',
                            text: '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                            ui: 'blank',
                            record: record
                        });
                        if (layoutNeedsUpdate) {
                        	Ext.defer(function () {
                        		me.updateLayout();
                        	}, 10);
                        	layoutNeedsUpdate = false;
                        }
                    }, 10);

                    return value + Ext.String.format('<span style="margin-left:7px; padding:0;" id="{0}"></span>', id);
                }
            },
            {
                xtype: 'uni-actioncolumn-remove',
                privileges: Mdc.privileges.Communication.admin,
                handler: function (grid, rowIndex, colIndex, item, e, record) {
                    this.fireEvent('removePrivilege', grid, rowIndex, record);
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                border: false,
                items: [
                    {
                        xtype: 'button',
                        itemId: 'add-privilege-action',
                        disabled: true,
                        text: Uni.I18n.translate('comtask.privileges.add', 'MDC', 'Add privileges'),
                        privileges: Mdc.privileges.Communication.admin,
                        //action: 'createcommunicationtaskprivilege',
                        href: me.router.getRoute('administration/communicationtasks/view/privileges/add').buildUrl()
                    }
                ],
                updateInfo: function() {
                    this.child('#displayItem').setText(
                        Uni.I18n.translatePlural('general.xPrivileges', this.store.getCount(), 'MDC',
                            'No privileges', '{0} privilege', '{0} privileges')
                    );
                }
            }
        ];
        me.callParent(arguments);
    }
});
