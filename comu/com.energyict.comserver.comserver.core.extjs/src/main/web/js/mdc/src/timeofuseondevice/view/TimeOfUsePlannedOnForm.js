Ext.define('Mdc.timeofuseondevice.view.TimeOfUsePlannedOnForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.device-tou-planned-on-form',
    layout: {
        type: 'column'
    },

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            defaults: {
                labelWidth: 250
            },
            items: [
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('timeofuse.timeOfUseCalendar', 'MDC', 'Time of use calendar'),
                    name: 'name'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('timeofuse.releaseDate', 'MDC', 'Release date (command)'),
                    name: 'releaseDateDisplayField'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('timeofuse.activationDate', 'MDC', 'Activation date'),
                    name: 'activationDateDisplayField'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                    name: 'status'
                }
            ]
        };
        me.callParent(arguments);
    }

});