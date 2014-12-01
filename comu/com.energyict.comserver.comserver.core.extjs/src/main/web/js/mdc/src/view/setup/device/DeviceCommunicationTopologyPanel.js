Ext.define('Mdc.view.setup.device.DeviceCommunicationTopologyPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationTopologyPanel',
    overflowY: 'auto',
    itemId: 'devicecommicationtopologypanel',
    mRID: null,
    ui: 'tile',

    initComponent: function () {
        this.callParent(arguments);
    },

    addItemToForm: function (container, html, link) {
        var me = this,
            element = {
                tag: 'a',
                html: html
            };
        if (link) {
            element.href = link;
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
            showFullTopologyLink,
            slavesContainer,
            masterContainer,
            form,
            grid;

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
                            return '<a href="' + href + '">' + value + '</a>'
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
                                return Uni.I18n.formatDate('deviceloadprofiles.dateFormat', new Date(value), 'MDC', 'M d, Y H:i');
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

            if (device.get('masterDeviceId')) {
                me.addItemToForm(masterContainer, device.get('masterDevicemRID'), me.router.getRoute('devices/device').buildUrl({mRID: device.get('masterDevicemRID')}));
            } else {
                me.addItemToForm(masterContainer, Uni.I18n.translate('general.none', 'MDC', 'None'));
            }
            if (slavesCount) {
                slavesStore.each(function (slave) {
                    me.addItemToForm(slavesContainer, slave.get('mRID'), me.router.getRoute('devices/device').buildUrl({mRID: slave.get('mRID')}));
                });
            } else {
                me.addItemToForm(slavesContainer, Uni.I18n.translate('general.none', 'MDC', 'None'));
            }

        }
    }
});

