Ext.define('Mdc.timeofuseondevice.view.SendCalendarForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tou-device-send-cal-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.timeofuseondevice.store.AllowedCalendars'
    ],
    layout: {
        type: 'form'
    },
    defaults: {
        labelWidth: 250
    },
    deviceTypeId: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                defaults: {
                    labelWidth: 250
                },
                ui: 'large',
                width: '100%',
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true,
                        width: 500
                    },
                    {
                        xtype: 'combo',
                        fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseCalendar', 'MDC', 'Time of use calendar'),
                        required: true,
                        displayField: 'name',
                        valueField: 'id',
                        store: 'Mdc.timeofuseondevice.store.AllowedCalendars',
                        emptyText: Uni.I18n.translate('timeofuse.selectCalendar', 'MDC', 'Select a time of use calendar...'),
                        width: 500
                    }

                ]
            }
        ];

        me.callParent(arguments);
    }
});
