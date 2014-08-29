Ext.define('Mdc.view.setup.deviceloadprofiles.DataPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfilesDataPreview',
    itemId: 'deviceLoadProfilesDataPreview',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.DataActionMenu',
        'Uni.form.field.IntervalFlagsDisplay'
    ],
    layout: 'fit',
    frame: true,

    channels: null,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'deviceLoadProfilesDataActionMenu'
            }
        }
    ],

    initComponent: function () {
        var me = this,
            channelsFields = [];

        Ext.Array.each(me.channels, function (channel) {
            channelsFields.push({
                xtype: 'fieldcontainer',
                fieldLabel: channel.name,
                layout: 'hbox',
                items: [
                    {
                        xtype: 'displayfield',
                        name: 'channelData',
                        renderer: function (data) {
                            var result = '',
                                unitOfMeasureField = this.nextSibling('displayfield');
                            if (data[channel.id]) {
                                result = data[channel.id];
                                unitOfMeasureField.show();
                            } else {
                                unitOfMeasureField.hide();
                            }
                            return result;
                        }
                    },
                    {
                        xtype: 'displayfield',
                        hidden: true,
                        renderer: function (data) {
                            return (channel && channel.unitOfMeasure) ? '&nbsp;' + channel.unitOfMeasure.localizedValue : '';
                        }
                    }
                ]
            });
        });

        me.items = {
            xtype: 'form',
            itemId: 'deviceLoadProfilesDataPreviewForm',
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5
            },
            items: [
                {
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                            name: 'interval_formatted'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.readingTime', 'MDC', 'Reading time'),
                            name: 'readingTime_formatted'
                        },
                        {
                            xtype: 'interval-flags-displayfield'
                        }
                    ]
                }
            ],
            loadRecord: function (record) {
                var me = this,
                    form = this,
                    fields = form.query('[isFormField=true]');

                Ext.Array.each(fields, function (field) {
                    var value = record.get(field.name);

                    value && field.setValue(value);
                });
            }
        };

        me.callParent(arguments);
    }
});
