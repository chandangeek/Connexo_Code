Ext.define('Dcs.view.scheduling.DataCollectionScheduleList', {
    extend: 'Ext.grid.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.dataCollectionScheduleList',
    itemId: 'dataCollectionScheduleList',
    store: 'DataCollectionSchedules',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],





    columns: {
        items: [
            { header: Uni.I18n.translate('scheduling.status', 'DCS', 'Status'), dataIndex: 'running', flex: 0.04, sortable: false, fixed: true,
                renderer: function(value, metaData, record, rowIndex, colIndex, store) {
                    	             switch (record.data.running) {
                        	                 case true:
                                                 return '<img src="../dcs/resources/images/Play.png">';
                            	              case false:
                                                 return '<img src="../dcs/resources/images/Pause.png">';
                            	             }


            }},
            { header: Uni.I18n.translate('scheduling.name', 'DCS', 'Name'), dataIndex: 'name', flex: 0.3, sortable: false, fixed: true},
            { header: Uni.I18n.translate('scheduling.deviceGroup', 'DCS', 'Device group'), dataIndex: 'deviceGroupName', flex: 0.15, sortable: false, fixed: true  },
            { header:Uni.I18n.translate('scheduling.schedule', 'DCS', 'Schedule'), dataIndex: 'schedule', flex: 0.15, sortable: false, fixed: true },
            { header:Uni.I18n.translate('scheduling.plannedDate', 'DCS', 'Planned date'), dataIndex: 'plannedDate', flex: 0.2, sortable: false, fixed: true },
            {
                xtype:'actioncolumn',
                fixed: true,
                sortable: false,
                align: 'center',
                header: Uni.I18n.translate('scheduling.actions', 'DCS', 'Actions'),
                flex: 0.05,
                items: [{
                    icon: '../dcs/resources/images/gear-16x16.png',
                    handler: function(grid, rowIndex, colIndex,item,e) {
                        var menu = Ext.widget('menu', {
                            items: [{
                                xtype: 'menuitem',
                                text: Uni.I18n.translate('general.edit', 'DCS', 'Edit'),
                                listeners: {
                                    click: {
                                        element: 'el',
                                        fn: function(){
                                            this.fireEvent('edit',grid.getSelectionModel().getSelection());
                                        },
                                        scope: this
                                    }
                                }
                            },
                            {
                                    xtype: 'menuseparator'
                            },
                            {
                                xtype: 'menuitem',
                                text: Uni.I18n.translate('general.delete', 'DCS', 'Delete'),
                                listeners: {
                                    click: {
                                        element: 'el',
                                        fn: function(){
                                            console.log('delete');
                                            this.fireEvent('delete',grid.getSelectionModel().getSelection());
                                        },
                                        scope: this
                                    }
                                }
                            }]
                        });
                        menu.showAt(e.getXY());
                    }
                }]
            }
        ]
    },



    initComponent: function () {

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                displayMsg: '{0} - {1} of {2} data collection schedules',
                dock: 'top',
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        text: Uni.I18n.translate('scheduling.createDataCollectionSchedule', 'DCS', 'Create schedule'),
                        itemId: 'newDataCollectionSchedule',
                        xtype: 'button',
                        href: '#administration_dcs/validation/createdatacollectionschedule',
                        hrefTarget: '_self'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                itemsPerPageMsg: 'Data collection schedules per page',
                dock: 'bottom'
            }];

        this.callParent(arguments);
    }
});
