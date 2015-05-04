Ext.define('Dcs.view.scheduling.DataCollectionScheduleBrowse', {
    extend: 'Uni.view.container.ContentContainer',
    border: false,
    alias: 'widget.dataCollectionScheduleBrowse',
    overflowY: 'auto',
    requires: [
        'Dcs.view.scheduling.DataCollectionScheduleList',
        'Dcs.view.scheduling.DataCollectionSchedulePreview',
        'Uni.view.breadcrumb.Trail'
    ],
    region: 'center',


    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            border: false,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' +  Uni.I18n.translate('scheduling.dataCollectionSchedules', 'DCS', 'Data collection schedules') +'</h1>',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'dataCollectionScheduleList'
                },
                {
                    xtype: 'component',
                    height : 50
                },
                {
                    xtype: 'dataCollectionSchedulePreview'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
