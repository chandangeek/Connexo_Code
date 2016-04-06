Ext.define('Uni.view.calendar.TimeOfUsePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.timeOfUsePreview',
    record: null,
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
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.period', 'UNI', 'Period'),
                    itemId: 'periodField'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.dayTypes', 'UNI', 'Day types'),
                    itemId: 'dayTypesField'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.tariffs', 'UNI', 'Tariffs'),
                    itemId: 'tariffsField'
                }
            ]
        };
        me.callParent(arguments);
    },

    fillFieldContainers: function (record) {
        var me = this;
        Ext.suspendLayouts();

        me.setTitle(record.get('name'));

        me.down('#periodField').removeAll();
        record.periods().each(function (record) {
            me.down('#periodField').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: record.get('name'),
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
                    value: record.get('name') + '(' + record.get('code') + ')',
                    margin: '0 0 -10 0'
                }
            );
        });
        me.doComponentLayout();
        Ext.resumeLayouts(true);
        me.updateLayout();
        me.doLayout();
    }

});