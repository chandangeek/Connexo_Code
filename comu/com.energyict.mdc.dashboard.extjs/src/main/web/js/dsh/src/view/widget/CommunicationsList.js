/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.CommunicationsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communications-list',
    store: 'Dsh.store.CommunicationTasks',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Yfn.privileges.Yellowfin',
        'Dsh.view.widget.CommunicationsActionMenu'
    ],
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'name',
                text: Uni.I18n.translate('communication.widget.details.commmunicationTask', 'DSH', 'Communication task'),
                dataIndex: 'name',
                flex: 2
            },
            {
                itemId: 'device',
                text: Uni.I18n.translate('general.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 2,
                renderer: function (val) {
                    return val.name ? Ext.String.htmlEncode(val.name) : '-';
                }
            },
            {
                itemId: 'latestResult',
                text: Uni.I18n.translate('general.lastResult', 'DSH', 'Last result'),
                dataIndex: 'latestResult',
                flex: 1,
                renderer: function (val) {
                    //CONM-2537
                    return val.displayValue ? Ext.String.htmlEncode(val.displayValue) : 'Never Started';
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('general.status', 'DSH', 'Status'),
                dataIndex: 'currentState',
                flex: 1,
                renderer: function (val) {
                    //CONM-2537
                    return val.displayValue ? Ext.String.htmlEncode(val.displayValue) : 'Never Started';
                }
            },
            {
                text: Uni.I18n.translate('general.sharedCommunicationSchedule', 'DSH', 'Shared communication schedule'),
                itemId: 'comScheduleName',
                dataIndex: 'comScheduleName',
                flex: 2
            },
            {
                itemId: 'startTime',
                text: Uni.I18n.translate('general.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                },
                flex: 2
            },
            {
                itemId: 'nextCommunication',
                text: Uni.I18n.translate('general.nextCommunication', 'DSH', 'Next communication'),
                dataIndex: 'nextCommunication',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                },
                flex: 2
            },
            {
                itemId: 'successfulFinishTime',
                text: Uni.I18n.translate('communication.widget.details.finishedOn', 'DSH', 'Finished successfully on'),
                dataIndex: 'successfulFinishTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                },
                flex: 2
            },
            {
                itemId: 'location',
                text: Uni.I18n.translate('communication.widget.details.location', 'DSH', 'Location'),
                dataIndex: 'device',
                renderer: function (value) {
                    return value ? value.location ? value.location.name : "-" : "-";
                },
                flex: 2
            },
            {
                itemId: 'communicationsGridActionMenu',
                xtype: 'uni-actioncolumn',
                width: 120,
                menu: {
                    xtype: 'communications-action-menu'
                }
            }
        ]
    },
    initComponent: function () {
        var me = this;
        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                needFilteredCustomExporter: true,
                displayMsg: Uni.I18n.translate('communication.widget.details.displayMsg', 'DSH', '{0} - {1} of {2} communications'),
                displayMoreMsg: Uni.I18n.translate('communication.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} communications'),
                emptyMsg: Uni.I18n.translate('communication.widget.details.emptyMsg', 'DSH', 'There are no communications to display'),
                items: [
                    {
                        xtype:'button',
                        itemId:'generate-report',
                        hidden: !Uni.store.Apps.checkApp('Facts'),
                        privileges: Yfn.privileges.Yellowfin.view,
                        text: Uni.I18n.translate('generatereport.generateReportButton', 'DSH', 'Generate report')
                    },
                    {
                        xtype: 'button',
                        itemId: 'btn-communications-bulk-action',
                        privileges: Mdc.privileges.Device.viewOrAdministrateOrOperateDeviceCommunication,
                        text: Uni.I18n.translate('general.bulkAction', 'DSH', 'Bulk action')
                    }
                ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                deferLoading: true,
                needExtendedData: true,
                itemsPerPageMsg: Uni.I18n.translate('communication.widget.details.itemsPerPage', 'DSH', 'Communications per page')
            }
        ];
        me.callParent(arguments);
    },
    initActions: function () {
        var me = this,
            bulkActionBtn = me.down("#btn-communications-bulk-action");
        bulkActionBtn.on('click', me.applyFilters, me);
    },
    applyFilters: function () {
        var me = this,
            pagingToolbarTop = Ext.Array.findBy(Ext.ComponentQuery.query('pagingtoolbartop'), function (toolbar) {
                return toolbar.store.$className === me.store.$className;
            }),
            pagingToolbarBottom = Ext.Array.findBy(Ext.ComponentQuery.query('pagingtoolbarbottom'), function (toolbar) {
                return toolbar.store.$className === me.store.$className;
            });

        if (pagingToolbarTop) {
            pagingToolbarTop.resetPaging();
        }
        if (pagingToolbarBottom) {
            pagingToolbarBottom.resetPaging();
        }
        if (Ext.isDefined(me.store)) {
            me.store.load();
        }
    }
});

