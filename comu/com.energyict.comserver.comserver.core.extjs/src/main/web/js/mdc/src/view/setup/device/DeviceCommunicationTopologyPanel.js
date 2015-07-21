Ext.define('Mdc.view.setup.device.DeviceCommunicationTopologyPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationTopologyPanel',
    requires: [
        'Mdc.store.MasterDeviceCandidates'
    ],
    overflowY: 'auto',
    itemId: 'devicecommicationtopologypanel',
    mRID: null,
    device: null,
    ui: 'tile',

    initComponent: function () {
        this.callParent(arguments);
    },

    addItemToForm: function (container, html, link, tag, style) {
        var me = this,
            element = {
                tag: tag ? tag : 'a',
                html: html
            };
        if (link) {
            element.href = link;
        }
        if (style) {
            element.style = style;
        }
        container.add(
            {
                xtype: 'component',
                cls: 'x-form-display-field',
                autoEl: element
            });
    },

    setRecord: function (device) {
        var me = this,
            slavesStore = device.slaveDevices(),
            slavesCount = slavesStore.getCount(),
            showFullTopologyLink, slavesContainer,
            masterContainer, form, grid;

        me.device = device;
        me.removeAll(true);

        if (device.get('gatewayType') === 'LAN') {

            grid = {
                xtype: 'gridpanel',
                margin: '5 0 0 0',
                itemId: 'communication-topology-grid',
                columns: [
                    {
                        header: Uni.I18n.translate('deviceCommunicationTopology.mRID', 'MDC', 'MRID'),
                        dataIndex: 'mRID',
                        flex: 1,
                        renderer: function (value, meta, record) {
                            var href = me.router.getRoute('devices/device').buildUrl({mRID: record.get('mRID')});
                            return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
                        }
                    },
                    {
                        header: Uni.I18n.translate('deviceCommunicationTopology.type', 'MDC', 'Type'),
                        dataIndex: 'deviceTypeName',
                        flex: 1
                    },
                    {
                        header: Uni.I18n.translate('deviceCommunicationTopology.configuration', 'MDC', 'Configuration'),
                        dataIndex: 'deviceConfigurationName',
                        flex: 1
                    },
                    {
                        header: Uni.I18n.translate('deviceCommunicationTopology.addedOn', 'MDC', 'Added on'),
                        dataIndex: 'creationTime',
                        flex: 1,
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '';
                        }
                    }
                ]
            };

            showFullTopologyLink = {
                xtype: 'container',
                html: '<a href="' + me.router.getRoute('devices/device/topology').buildUrl({mRID: me.router.arguments.mRID}) + '">' + Uni.I18n.translate('deviceCommunicationTopology.showFullCommunicationTopology', 'MDC', 'Show full communication topology') + '</a>'
            };

            me.setTitle(Uni.I18n.translate('deviceCommunicationTopology.communicationTopologyTitleRecentlyAdded', 'MDC', 'Communication topology: most recently added'));

            if (slavesCount) {
                me.add(grid, showFullTopologyLink);
                me.down('#communication-topology-grid').reconfigure(slavesStore);

            } else {
                me.add({
                    xtype: 'container',
                    margin: '10 0 0 20',
                    html: "<a style='font-size: 12pt'>" + Uni.I18n.translate('deviceCommunicationTopology.communicationTopologyTitleHasNoSlaves', 'MDC', 'This gateway has no slaves') + "</a>"
                });
            }

        } else {
            me.setTitle(Uni.I18n.translate('deviceCommunicationTopology.topologyTitle', 'MDC', 'Communication topology'));

            form = {
                xtype: 'form',
                itemId: 'deviceCommunicationTopologyForm',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    labelWidth: 150,
                    labelAlign: 'right',
                    xtype: 'fieldcontainer',
                    layout: {
                        type: 'vbox'
                    }
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('deviceCommunicationTopology.master', 'MDC', 'Master'),
                        itemId: 'masterDeviceContainer'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('deviceCommunicationTopology.slave', 'MDC', 'Slave(s)'),
                        itemId: 'slaveDevicesContainer'
                    }
                ]
            };

            me.add(form);

            slavesContainer = me.down('#slaveDevicesContainer');
            masterContainer = me.down('#masterDeviceContainer');

            Ext.suspendLayouts();
            if (!device.get('isDirectlyAddressed')) {
                if (device.get('masterDeviceId')) {
                    me.addItemToForm(masterContainer, device.get('masterDevicemRID'), me.router.getRoute('devices/device').buildUrl({mRID: device.get('masterDevicemRID')}));
                    me.renderActionButtonsToMasterField(masterContainer, true, true, false, false);
                } else {
                    me.addItemToForm(masterContainer, Uni.I18n.translate('general.none', 'MDC', 'None'), null, 'span');
                    me.renderActionButtonsToMasterField(masterContainer, true, false, false, false);
                }
            } else {
                me.addItemToForm(masterContainer, Uni.I18n.translate('general.na', 'MDC', 'N/A'));
                me.addItemToForm(masterContainer, Uni.I18n.translate('deviceCommunicationTopology.isDirectlyAddressed', 'MDC', 'The device is directly addressable.<br/>It is not possible to set master device.'), null, 'span',
                    {
                        top: '2em !important',
                        fontStyle: 'italic',
                        color: '#999'
                    }
                );
            }

            if (device.get('isGateway')) {
                if (slavesCount) {
                    slavesStore.each(function (slave) {
                        me.addItemToForm(slavesContainer, slave.get('mRID'), me.router.getRoute('devices/device').buildUrl({mRID: slave.get('mRID')}));
                    });
                } else {
                    me.addItemToForm(slavesContainer, Uni.I18n.translate('general.none', 'MDC', 'None'), null, 'span');
                }
            } else {
                me.addItemToForm(slavesContainer, Uni.I18n.translate('general.na', 'MDC', 'N/A'));
                me.addItemToForm(slavesContainer, Uni.I18n.translate('deviceCommunicationTopology.isNotGateway', 'MDC', 'The device is not a gateway.<br/>It has no slaves.'), null, 'span',
                    {
                        top: '2em !important',
                        fontStyle: 'italic',
                        color: '#999'
                    }
                );
            }
            Ext.resumeLayouts();
        }
    },

    renderActionButtonsToMasterField: function (container, edit, clear, apply, cancel) {
        var me = this;

        Ext.each(container.el.query('.masterfield-btn'), function (elm) {
            elm.parentNode.removeChild(elm);
        });

        Ext.defer(function () {
            var btnsLeftOffset = container.el.down('.x-box-target').getWidth(),
                btnsStyle = {
                    display: 'inline-block',
                    textDecoration: 'none !important',
                    position: 'absolute',
                    top: '8px'
                },
                btnsLeftOffsetListener;

            if (btnsLeftOffset === 0) {
                btnsLeftOffset = 120;
            }

            btnsLeftOffsetListener = function (btn) {
                btn.el.dom.style.left = btnsLeftOffset + 'px';
            };

            // Edit button
            if (edit) {
                btnsLeftOffset += 20;
                new Ext.button.Button({
                    renderTo: container.el.down('.x-form-item-body'),
                    icon: '../mdc/resources/images/pencil.png',
                    cls: 'uni-btn-transparent masterfield-btn',
                    style: btnsStyle,
                    scope: me,
                    handler: me.editMasterDevice,
                    listeners: {
                        afterrender: btnsLeftOffsetListener
                    }
                });
            }

            // Clear button
            if (clear) {
                btnsLeftOffset += 20;
                new Ext.button.Button({
                    renderTo: container.el.down('.x-form-item-body'),
                    icon: '../mdc/resources/images/cancel.png',
                    cls: 'uni-btn-transparent masterfield-btn',
                    style: btnsStyle,
                    scope: me,
                    handler: me.clearMasterDevice,
                    listeners: {
                        afterrender: btnsLeftOffsetListener
                    }
                });
            }

            // Apply button
            if (apply) {
                btnsLeftOffset += 20;
                new Ext.button.Button({
                    renderTo: container.el.down('.x-form-item-body'),
                    icon: '../mdc/resources/images/apply.png',
                    cls: 'uni-btn-transparent masterfield-btn',
                    style: btnsStyle,
                    scope: me,
                    handler: me.applyMasterDevice,
                    listeners: {
                        afterrender: btnsLeftOffsetListener
                    }
                });
            }

            // Cancel button
            if (cancel) {
                btnsLeftOffset += 20;
                new Ext.button.Button({
                    renderTo: container.el.down('.x-form-item-body'),
                    icon: '../mdc/resources/images/remove.png',
                    cls: 'uni-btn-transparent masterfield-btn',
                    style: btnsStyle,
                    scope: me,
                    handler: me.cancelMasterDevice,
                    listeners: {
                        afterrender: btnsLeftOffsetListener
                    }
                });
            }
        }, 100);
    },

    editMasterDevice: function () {
        var me = this, params = {},
            masterContainer = me.down('#masterDeviceContainer');

        masterContainer.removeAll(true);
        masterContainer.add(
            {
                xtype: 'combobox',
                itemId: 'masterCandidatesCombo',
                store: 'MasterDeviceCandidates',
                displayField: 'name',
                valueField: 'id',
                queryMode: 'local',
                forceSelection: true,
                width: 165
            }
        );

        params.excludeDeviceMRID = me.device.get('mRID');

        me.setLoading(true);

        masterContainer.down('#masterCandidatesCombo').store.load({
            params: params,
            callback: function () {
                if (me.device.get('masterDeviceId')) {
                    masterContainer.down('#masterCandidatesCombo').setValue(me.device.get('masterDeviceId'));
                }
                me.setLoading(false);
            }
        });

        me.renderActionButtonsToMasterField(masterContainer, false, false, true, true);
    },

    clearMasterDevice: function () {
        var me = this;
        Ext.create('Uni.view.window.Confirmation').show({
            title: Ext.String.format(Uni.I18n.translate('comTopologyWidget.removeMasterConfirmation.title', 'MDC', 'Remove \'{0}\' as master device?'), me.device.get('masterDevicemRID')),
            msg: Ext.String.format(Uni.I18n.translate('comTopologyWidget.removeMasterConfirmation.message', 'MDC', 'This device will no longer be the master of  \'{0}\''), me.device.get('mRID')),
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
    },

    applyMasterDevice: function () {
        var masterCombo = this.down('#masterCandidatesCombo');
        if (this.device && !Ext.isEmpty(masterCombo.getValue())) {
            this.updateDevice(
                {
                    masterDeviceId: masterCombo.getValue(),
                    masterDevicemRID: masterCombo.getRawValue()
                }
            );
        }
    },

    cancelMasterDevice: function () {
        var me = this;

        me.setLoading(true);

        Ext.defer(function () {
            me.setRecord(me.device);
            me.setLoading(false);
        }, 10);
    },

    updateDevice: function (data) {
        var me = this;
        me.setLoading(true);
        me.device.set('masterDeviceId', data.masterDeviceId);
        me.device.set('masterDevicemRID', data.masterDevicemRID);
        me.device.save({
            success: function (deviceData) {
                Ext.ModelManager.getModel('Mdc.model.Device').load(deviceData.get('mRID'), {
                    success: function (device) {
                        me.setRecord(device);
                    },
                    callback: function () {
                        me.setLoading(false);
                    }
                });
            }
        });
    }
});