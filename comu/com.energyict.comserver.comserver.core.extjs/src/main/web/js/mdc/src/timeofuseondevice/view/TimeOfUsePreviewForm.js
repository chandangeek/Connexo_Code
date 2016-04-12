Ext.define('Mdc.timeofuseondevice.view.TimeOfUsePreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.device-tou-preview-form',
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
                    fieldLabel: Uni.I18n.translate('general.lastVerifief', 'MDC', 'Last verified'),
                    name: 'lastVefirifiedDisplayField'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('timeofuse.periods', 'MDC', 'Periods'),
                    itemId: 'periodField'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('timeofuse.dayTypes', 'MDC', 'Day types'),
                    itemId: 'dayTypesField'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('timeofuse.tariffs', 'MDC', 'Tariffs'),
                    itemId: 'tariffsField'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('timeofuse.passiveCalendars', 'MDC', 'Passive calendar(s)'),
                    itemId: 'passiveField'
                }
            ]
        };
        me.callParent(arguments);
    },

    fillFieldContainers: function (record) {
        var me = this;
        Ext.suspendLayouts();

        me.down('#periodField').removeAll();
        record.periods().each(function (record) {
            me.down('#periodField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + ' (' + Uni.I18n.translate('general.fromX', 'MDC', 'from {0}', [me.calculateDate(record.get('fromMonth'), record.get('fromDay'))]) + ')',
                    margin: '0 0 -10 0'
                }
            );
        });
        me.down('#dayTypesField').removeAll();
        record.dayTypes().each(function (record) {
            me.down('#dayTypesField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name'),
                    margin: '0 0 -10 0'
                }
            );
        });

        this.down('#tariffsField').removeAll();
        record.events().each(function (record) {
            me.down('#tariffsField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name') + ' (' + record.get('code') + ')',
                    margin: '0 0 -10 0'
                }
            );
        });

        this.down('#passiveField').removeAll();
        record.passivecalendars().each(function (record) {
            me.down('#passiveField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name'),
                    margin: '0 0 -10 0'
                }
            );
        });
        me.doComponentLayout();
        Ext.resumeLayouts(true);
        me.updateLayout();
        me.doLayout();
    },

    calculateDate: function (month, day) {
        var date = new Date();
        date.setMonth(month);
        date.setDate(day);

        return Ext.util.Format.date(date, 'j F')
    }

});