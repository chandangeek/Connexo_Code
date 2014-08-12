Ext.define('Mdc.view.setup.deviceloadprofiles.Data', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfilesData',
    itemId: 'deviceLoadProfilesData',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.SubMenuPanel',
        'Mdc.view.setup.deviceloadprofiles.TableView',
        'Mdc.view.setup.deviceloadprofiles.GraphView'
    ],

    router: null,
    channels: null,

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('deviceloadprofiles.loadProfileData', 'MDC', 'Load profile data'),
            tools: [
                {
                    xtype: 'button',
                    itemId: 'deviceLoadProfilesTableViewBtn',
                    text: Uni.I18n.translate('deviceloadprofiles.tableView', 'MDC', 'Table view'),
                    action: 'showTableView',
                    ui: 'link'
                },
                {
                    xtype: 'tbtext',
                    text: '|'
                },
                {
                    xtype: 'button',
                    itemId: 'deviceLoadProfilesGraphViewBtn',
                    text: Uni.I18n.translate('deviceloadprofiles.graphView', 'MDC', 'Graph view'),
                    action: 'showGraphView',
                    disabled: true,
                    ui: 'link'
                }
            ],

            tbar: {
                xtype: 'toolbar',
                items: [
                    {
                        xtype: 'container',
                        itemId: 'readingsCountOnLoadProfile',
                        hidden: true,
                        flex: 1
                    }
                    // will not be shown on a demo (out of scope)
//                    {
//                        xtype: 'button',
//                        text: Uni.I18n.translate('deviceloadprofiles.flashback', 'MDC', 'Flashback'),
//                        action: 'flashback',
//                        itemId: 'deviceLoadProfilesDataFlashbackBtn'
//                    },
//                    {
//                        xtype: 'button',
//                        text: Uni.I18n.translate('deviceloadprofiles.configure', 'MDC', 'Configure'),
//                        action: 'configure',
//                        itemId: 'deviceLoadProfilesDataConfigureBtn'
//                    }
                ]
            },
            items: [
                {
                    xtype: 'deviceLoadProfilesTableView',
                    hidden: true,
                    channels: me.channels
                },
                {
                    xtype: 'deviceLoadProfilesGraphView'
                }
            ]
        };

        me.side = {
            xtype: 'panel',
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'deviceLoadProfilesSubMenuPanel',
                    router: me.router
                },
                {
                    xtype: 'panel',
                    ui: 'medium',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    items: {
                        xtype: 'form',
                        itemId: 'deviceLoadProfileDataFilterForm',
                        title: Uni.I18n.translate('general.filter', 'MDC', 'Filter'),
                        ui: 'filter',
                        items: [
                            {
                                xtype: 'component',
                                html: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval')
                            },
                            {
                                xtype: 'datefield',
                                name: 'intervalStart',
                                fieldLabel: 'Between',
                                labelAlign: 'top',
                                format: 'd M Y',
                                maxValue: new Date(),
                                anchor: '100%',
                                editable: false
                            },
                            {
                                xtype: 'datefield',
                                name: 'intervalEnd',
                                fieldLabel: 'and',
                                labelAlign: 'top',
                                format: 'd M Y',
                                maxValue: new Date(),
                                anchor: '100%',
                                editable: false
                            }
                        ],
                        dockedItems: [
                            {
                                xtype: 'toolbar',
                                dock: 'bottom',
                                items: [
                                    {
                                        itemId: 'deviceLoadProfileDataFilterApplyBtn',
                                        ui: 'action',
                                        text: Uni.I18n.translate('general.apply', 'MDC', 'Apply'),
                                        action: 'filter'
                                    },
                                    {
                                        itemId: 'deviceLoadProfileDataFilterResetBtn',
                                        text: Uni.I18n.translate('general.clearAll', 'MDC', 'Clear all'),
                                        action: 'reset'
                                    }
                                ]
                            }
                        ]
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

