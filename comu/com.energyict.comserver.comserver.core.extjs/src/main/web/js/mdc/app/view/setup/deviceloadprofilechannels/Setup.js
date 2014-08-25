Ext.define('Mdc.view.setup.deviceloadprofilechannels.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelsSetup',
    itemId: 'deviceLoadProfileChannelsSetup',

    mRID: null,
    loadProfileId: null,
    router: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceloadprofiles.SubMenuPanel',
        'Mdc.view.setup.deviceloadprofilechannels.Grid',
        'Mdc.view.setup.deviceloadprofilechannels.Preview'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'deviceLoadProfilesSubMenuPanel',
                router: me.router
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceloadprofiles.channels', 'MDC', 'Channels'),
            items: [
                {
                    xtype: 'form',
                    itemId: 'deviceLoadProfileChannelsIntervalAndLastReading',
                    defaults: {
                        labelWidth: 200
                    },
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                            name: 'interval_formatted'
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
                        }
                    ]
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'deviceLoadProfileChannelsGrid',
                        mRID: me.mRID,
                        loadProfileId: me.loadProfileId,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceloadprofilechannels.empty.title', 'MDC', 'No channels found'),
                        reasons: [
                            Uni.I18n.translate('deviceloadprofilechannels.empty.list.item1', 'MDC', 'No channels have been defined yet.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'deviceLoadProfileChannelsPreview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});