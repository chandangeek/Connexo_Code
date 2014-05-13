Ext.define('Mdc.view.setup.communicationschedule.CommunicationSchedulesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationSchedulesSetup',
    itemId: 'CommunicationSchedulesSetup',
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview',
        'Uni.view.breadcrumb.Trail'
    ],
    cls: 'content-container',
    side: [

    ],
    content: [
        {
            xtype: 'component',
            html: '<h1>' + Uni.I18n.translate('communicationschedule.communicationSchedules', 'MDC', 'Communication schedules') + '</h1>',
            margins: '10 10 10 10'
        },
        {
            xtype: 'communicationSchedulesGrid'
        },
        {
            xtype: 'component',
            height: 25
        },
        {
            xtype: 'communicationSchedulePreview'
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});


