Ext.define('Mdc.view.setup.deviceloadprofilechannels.DataPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfileChannelDataPreview',
    itemId: 'deviceLoadProfileChannelDataPreview',
    requires: [
        'Mdc.view.setup.deviceloadprofilechannels.DataActionMenu'
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
                xtype: 'deviceLoadProfileChannelDataActionMenu'
            }
        }
    ],

    items: {
        xtype: 'form',
        itemId: 'deviceLoadProfileChannelDataPreviewForm',
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
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                name: 'value'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.multiplier', 'MDC', 'Multiplier'),
                name: 'multiplier'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.intervalFlags', 'MDC', 'Interval flags'),
                layout: 'hbox',
                items: [
                    {
                        xtype: 'displayfield',
                        name: 'intervalFlags',
                        margin: '3 0 0 0',
                        renderer: function (data) {
                            var result = '',
                                tooltip = '',
                                icon = this.nextSibling('button');
                            if (Ext.isArray(data) && data.length) {
                                result = data.length;
                                Ext.Array.each(data, function (value, index) {
                                    index++;
                                    tooltip += Uni.I18n.translate('deviceloadprofiles.flag', 'MDC', 'Flag') + ' ' + index + ': ' + value + '<br>';
                                });
                                icon.setTooltip(tooltip);
                                icon.show();
                            } else {
                                icon.hide();
                            }
                            return result;
                        }
                    },
                    {
                        xtype: 'button',
                        tooltip: '',
                        iconCls: 'icon-info-small',
                        ui: 'blank',
                        itemId: 'intervalFlagsHelp',
                        shadow: false,
                        margin: '6 0 0 10',
                        width: 16
                    }
                ]
            }
        ]
    }
});
