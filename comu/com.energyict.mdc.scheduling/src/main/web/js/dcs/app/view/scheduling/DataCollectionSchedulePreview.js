Ext.define('Dcs.view.scheduling.DataCollectionSchedulePreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.dataCollectionSchedulePreview',
    itemId: 'dataCollectionSchedulePreview',
    requires: [
        'Dcs.model.DataCollectionSchedule'
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },


    items: [
        {
            xtype: 'panel',
            border: false,
            padding: '0 10 0 10',
            tbar: [
                {
                    xtype: 'component',
                    html: '<H4>' + Uni.I18n.translate('scheduling.noDataCollectionScheduleSelected', 'DCS', 'No data collection schedule selected') + '</H4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<H5>' + Uni.I18n.translate('scheduling.selectDataCollectionSchedule', 'DCS', 'Select a data collection schedule to see its details') + '</H5>'
                }
            ]

        },

        {
            xtype: 'form',
            border: false,
            itemId: 'dataCollectionScheduleForm',
            padding: '0 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>' + Uni.I18n.translate('scheduling.dataCollectionSchedules', 'DCS', 'Data collection schedules')  + '</h4>',
                    itemId: 'dataCollectionSchedulePreviewTitle'
                },
                '->',
                {
                    icon: 'resources/images/gear-16x16.png',
                    text: Uni.I18n.translate('scheduling.actions', 'DCS', 'Actions'),
                    menu:{
                        items:[
                            {
                                text: Uni.I18n.translate('general.edit', 'DCS', 'Edit'),
                                itemId: 'editDataCollectionSchedule',
                                action: 'editDataCollectionSchedule'

                            },
                            {
                                xtype: 'menuseparator'
                            },
                            {
                                text: Uni.I18n.translate('general.delete', 'DCS', 'Delete'),
                                itemId: 'deleteDataCollectionSchedule',
                                action: 'deleteDataCollectionSchedule'

                            }
                        ]
                    }
                }],

            items: [
                {
                    xtype: 'displayfield',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('scheduling.name', 'DCS', 'Name'),
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'schedule',
                    fieldLabel: Uni.I18n.translate('scheduling.schedule', 'DCS', 'Schedule'),
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'devieGroupName',
                    fieldLabel: Uni.I18n.translate('scheduling.deviceGroup', 'DCS', 'Device Group'),
                    labelWidth:	250
                },
                {
                    xtype: 'displayfield',
                    name: 'status',
                    fieldLabel:  Uni.I18n.translate('scheduling.status', 'DCS', 'Status'),
                    labelWidth:	250
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
