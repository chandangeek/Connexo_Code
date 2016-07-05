Ext.define('Mdc.view.setup.devicetopology.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTopologySetup',
    itemId: 'deviceTopologySetup',
    router: null,
    device: null,

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
                            html: '<h2>' + Uni.I18n.translate('deviceCommunicationTopology.slaves', 'MDC', 'Slaves') + '</h2>'
                        },
                        {
                            xtype: 'mdc-view-setup-devicechannels-topologiestopfilter'
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
                            var masterMRID = me.device.get('masterDevicemRID');

                            if (masterMRID) {
                                return Ext.String.format(
                                    '<a href="{0}">{1}</a>',
                                    me.router.getRoute('devices/device').buildUrl({mRID: encodeURIComponent(masterMRID)}),
                                    Ext.String.htmlEncode(masterMRID)
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
                        text: '<span class="icon-minus-circle2" style="display:inline-block; font-size:16px;"></span>',
                        tooltip: Uni.I18n.translate('deviceCommunicationTopology.removeMaster.tooltip', 'MDC', 'Remove master'),
                        hidden: Ext.isEmpty(me.device.get('masterDevicemRID'))
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
                minChars: 0,
                forceSelection: true,
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
        var masterStore = masterContainer.down('#mdc-topology-masterCandidatesCombo').getStore();
        masterStore.getProxy().setExtraParam('excludeDeviceMRID', me.device.get('mRID'));
        masterStore.load({
            callback: function () {
                if (me.device.get('masterDeviceId')) {
                    masterContainer.down('#mdc-topology-masterCandidatesCombo').setValue(me.device.get('masterDeviceId'));
                }
                me.setLoading(false);
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
    },

    applyMasterDevice: function () {
        var me = this,
            masterCombo = me.down('#mdc-topology-masterCandidatesCombo');
        if (me.device && !Ext.isEmpty(masterCombo.getValue())) {
            this.updateDevice(
                {
                    masterDeviceId: masterCombo.getValue(),
                    masterDevicemRID: masterCombo.getRawValue()
                }
            );
        }
    },

    updateDevice: function (data) {
        var me = this;

        me.setLoading(true);
        me.device.set('masterDeviceId', data.masterDeviceId);
        me.device.set('masterDevicemRID', data.masterDevicemRID);
        me.device.save({
            isNotEdit: true,
            success: function (deviceData) {
                Ext.ModelManager.getModel('Mdc.model.Device').load(deviceData.get('mRID'), {
                    success: function (device) {
                        me.addMasterContainerViewItems();
                    },
                    callback: function () {
                        me.setLoading(false);
                    }
                });
            },
            failure: function(record, operation) {
                me.setLoading(false);
            }
        });
    },

    removeMasterDevice: function() {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('comTopologyWidget.removeMasterConfirmation.title', 'MDC', "Remove '{0}' as master device?", me.device.get('masterDevicemRID')),
            msg: Uni.I18n.translate('comTopologyWidget.removeMasterConfirmation.message', 'MDC', "This device will no longer be the master of '{0}'", me.device.get('mRID'), false),
            fn: function (action) {
                if (action === 'confirm') {
                    me.updateDevice(
                        {
                            masterDeviceId: null,
                            masterDevicemRID: null
                        }
                    );
                }
            }
        });
    }
});