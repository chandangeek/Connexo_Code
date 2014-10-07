Ext.define('Mdc.view.setup.deviceloadprofilechannels.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelOverview',
    itemId: 'deviceLoadProfileChannelOverview',

    requires: [
        'Mdc.view.setup.deviceloadprofilechannels.SubMenuPanel',
        'Mdc.view.setup.deviceloadprofilechannels.ValidationOverview'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                items: [
                    {
                        xtype: 'form',
                        itemId: 'deviceLoadProfileChannelsOverviewForm',
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.general', 'MDC', 'General'),
                                labelAlign: 'top',
                                layout: 'vbox',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 200
                                },
                                items: [
                                    {
                                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.name', 'MDC', 'Name'),
                                        name: 'name'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                                        name: 'interval_formatted'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.unitOfMeasure', 'MDC', 'Unit of measure'),
                                        name: 'unitOfMeasure_formatted'
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.lastReading', 'MDC', 'Last reading'),
                                        layout: 'hbox',
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                name: 'lastReading_formatted',
                                                margin: '3 0 0 0',
                                                renderer: function (value) {
                                                    this.nextSibling('button').setVisible(value ? true : false);
                                                    return value;
                                                }
                                            },
                                            {
                                                xtype: 'button',
                                                tooltip: Uni.I18n.translate('deviceloadprofiles.tooltip.lastreading', 'MDC', 'The moment when the data was read out for the last time.'),
                                                iconCls: 'icon-info-small',
                                                ui: 'blank',
                                                itemId: 'lastReadingHelp',
                                                shadow: false,
                                                margin: '6 0 0 10',
                                                width: 16
                                            }
                                        ]
                                    },
                                    {
                                        xtype: 'reading-type-displayfield',
                                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels.readingType', 'MDC', 'Reading type'),
                                        name: 'cimReadingType'
                                    },
                                    {
                                        xtype: 'obis-displayfield',
                                        name: 'obisCode'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.multiplier', 'MDC', 'Multiplier'),
                                        name: 'multiplier'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.overflowValue', 'MDC', 'Overflow value'),
                                        name: 'overflowValue'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm.nbrOfFractionDigits', 'MDC', 'Number of fraction digits'),
                                        name: 'nbrOfFractionDigits'
                                    }
                                ]
                            },
                            {
                                xtype: 'deviceloadprofilechannelsoverview-validation',
                                router: me.router
                            }
                        ]
                    }
                ]
            }
        ];

        me.side = {
            xtype: 'deviceLoadProfileChannelSubMenuPanel',
            router: me.router
        };

        me.callParent(arguments);
    }
});