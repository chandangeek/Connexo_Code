Ext.define('Mdc.view.setup.devicetopology.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTopologySetup',
    itemId: 'deviceTopologySetup',
    router: null,
    device: null,
    hasgateway: false,

    requires: [
        'Mdc.view.setup.devicetopology.Grid',
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.devicechannels.DeviceTopologiesTopFilter',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    stores: [
        'Mdc.store.TopologyOfDevice',
        'Mdc.store.MasterDeviceCandidates'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'topologyLink'
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceCommunicationTopology.topologyTitle', 'MDC', 'Communication topology'),
            items: [
                {
                    xtype: 'preview-container',
                    hidden: !me.hasgateway,
                    grid: {
                        xtype: 'deviceTopologyGrid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceCommunicationTopology.empty.title', 'MDC', 'No slave devices found'),
                        reasons: [
                            Uni.I18n.translate('deviceCommunicationTopology.empty.list.item1', 'MDC', 'The gateway contains no slave devices.'),
                            Uni.I18n.translate('deviceCommunicationTopology.empty.list.item2', 'MDC', 'No slave devices comply with the filter.')
                        ]
                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'form',
                            margins: '15 0 10 0',
                            items: {
                                xtype: 'fieldcontainer',
                                itemId: 'mdc-topology-master-container',
                                layout: 'vbox',
                                fieldLabel: Uni.I18n.translate('deviceCommunicationTopology.master', 'MDC', 'Master'),
                                items: me.getMasterContainerViewItems()
                            },
                            hidden: me.device.get('isDirectlyAddressed')
                        },
                        {
                            xtype: 'container',
                            html: '<h2>' + Uni.I18n.translate('deviceCommunicationTopology.slaves', 'MDC', 'Slaves') + '</h2>',
                            hidden: !me.hasgateway
                        },
                        {
                            xtype: 'mdc-view-setup-devicechannels-topologiestopfilter',
                            hidden: !me.hasgateway
                        }
                    ]
                }
            ]
        };
        me.callParent(arguments);
    },

    addMasterContainerViewItems: function() {
        var me = this,
            masterContainer = me.down('#mdc-topology-master-container');

        masterContainer.removeAll();
        masterContainer.add(me.getMasterContainerViewItems());
    },

    getMasterContainerViewItems: function() {
        var me = this;
        return [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'displayfield',
                        renderer: function() {
                            var masterId = me.device.get('masterDeviceName');

                            if (masterId) {
                                return Ext.String.format(
                                    '<a href="{0}">{1}</a>',
                                    me.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(masterId)}),
                                    Ext.String.htmlEncode(masterId)
                                );
                            } else {
                                return '-';
                            }
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'mdc-topology-edit-master-btn',
                        margins: '7 0 0 10',
                        ui: 'blank',
                        text: '<span class="icon-pencil3" style="display:inline-block; font-size:16px;"></span>',
                        tooltip: Uni.I18n.translate('deviceCommunicationTopology.editMaster.tooltip', 'MDC', 'Edit master')
                    },
                    {
                        xtype: 'button',
                        itemId: 'mdc-topology-remove-master-btn',
                        margins: '7 0 0 0',
                        ui: 'blank',
                        text: '<span class="icon-cancel-circle2" style="display:inline-block; font-size:16px;"></span>',
                        tooltip: Uni.I18n.translate('deviceCommunicationTopology.removeMaster.tooltip', 'MDC', 'Remove master'),
                        hidden: Ext.isEmpty(me.device.get('masterDeviceName'))
                    }
                ]
            }
        ];
    },

    addMasterContainerEditItems: function() {
        var me = this,
            masterContainer = me.down('#mdc-topology-master-container');

        masterContainer.removeAll();
        masterContainer.add(
            {
                xtype: 'combobox',
                itemId: 'mdc-topology-masterCandidatesCombo',
                store: 'MasterDeviceCandidates',
                displayField: 'name',
                valueField: 'id',
                queryMode: 'remote',
                queryParam: 'search',
                queryCaching: false,
                minChars: 1,
                forceSelection: false,
                width: 250,
                emptyText: Uni.I18n.translate('general.selectMaster', 'MDC', 'Start typing to select a master...'),
                msgTarget: 'under',
                editable: true,
                typeAhead: true,
                listeners: {
                    expand: {
                        fn: me.comboLimitNotification
                    }
                }
            },
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'mdc-topology-edit-master-save-btn',
                        ui: 'action',
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save')
                    },
                    {
                        xtype: 'button',
                        itemId: 'mdc-topology-edit-master-cancel-btn',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        ui: 'link'
                    }
                ]
            }
        );
        me.setLoading(true);
        var combo = masterContainer.down('#mdc-topology-masterCandidatesCombo'),
            masterStore = combo.getStore();
        masterStore.getProxy().setExtraParam('excludeDeviceName', me.device.get('name'));
        masterStore.load({
            callback: function () {
                if (me.device.get('masterDeviceId')) {
                    combo.setValue(me.device.get('masterDeviceId'));
                }
                me.setLoading(false);
                combo.focus();
                combo.selectText();
            }
        });
    },

    comboLimitNotification: function (combo) {
        var picker = combo.getPicker(),
            fn = function (view) {
                var store = view.getStore();
                if (store.getTotalCount() > store.getCount()) {
                    var el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');
                    el.appendChild({
                        tag: 'li',
                        html: Uni.I18n.translate('general.combo.limitNotification', 'MDC', 'Keep typing to narrow down'),
                        cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification'
                    });
                }
            };

        picker.on('refresh', fn);
        picker.on('beforehide', function () {
            picker.un('refresh', fn);
        }, combo, {single: true});
    }
});