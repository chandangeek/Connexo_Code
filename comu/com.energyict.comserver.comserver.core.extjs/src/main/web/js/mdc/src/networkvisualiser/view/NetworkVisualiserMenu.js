Ext.define('Mdc.networkvisualiser.view.NetworkVisualiserMenu', {
    extend: 'Uni.graphvisualiser.VisualiserMenu',
    alias: 'widget.networkvisualisermenu',
    models: [
        'Uni.graphvisualiser.model.GraphModel'
    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);


        me.down('#uni-layer-section').add([
            {
                xtype: 'checkboxgroup',
                margin: '-7 0 0 0',
                columns: 1,
                items: [
                    {
                        xtype: 'checkboxfield',
                        boxLabel: Uni.I18n.translate('general.layer.deviceTypes', 'MDC', 'Device types'),
                        itemId: 'mdc-visualiser-layer-device-types',
                        name: 'rb',
                        inputValue: '1'
                    },
                    {
                        xtype: 'checkboxfield',
                        boxLabel: Uni.I18n.translate('general.layer.issuesAndAlarms', 'MDC', 'Issues/Alarms'),
                        itemId: 'mdc-visualiser-layer-issues-alarms',
                        name: 'rb',
                        inputValue: '2'
                    },
                    {
                        xtype: 'checkboxfield',
                        boxLabel: Uni.I18n.translate('general.layer.hops', 'MDC', 'Amount of hops'),
                        itemId: 'mdc-visualiser-layer-hops',
                        name: 'rb',
                        inputValue: '3'
                    },
                    {
                        xtype: 'checkboxfield',
                        boxLabel: Uni.I18n.translate('general.layer.quality', 'MDC', 'Network/link quality'),
                        itemId: 'mdc-visualiser-layer-quality',
                        name: 'rb',
                        inputValue: '4'
                    },
                    {
                        xtype: 'checkboxfield',
                        boxLabel: Uni.I18n.translate('general.layer.lifeCycleStatus', 'MDC', 'Status of device life cycle'),
                        itemId: 'mdc-visualiser-layer-life-cycle-status',
                        name: 'rb',
                        inputValue: '5'
                    },
                    {
                        xtype: 'checkboxfield',
                        boxLabel: Uni.I18n.translate('general.layer.communicationStatus', 'MDC', 'Communication status'),
                        itemId: 'mdc-visualiser-layer-communication-status',
                        name: 'rb',
                        inputValue: '6'
                    },
                    {
                        xtype: 'checkboxfield',
                        boxLabel: Uni.I18n.translate('general.layer.map', 'MDC', 'Map'),
                        itemId: 'mdc-visualiser-layer-maps',
                        name: 'rb',
                        inputValue: '7',
                        hidden: true
                    }
                ],
                listeners: {
                    change: {
                        fn: this.checkboxHandler,
                        scope: this.visualiser
                    }
                }
            }
        ]);

        Ext.Ajax.request({
            url: '/api/dtg/topology/configuration',
            method: 'GET',

            success: function (option) {
                var response = Ext.JSON.decode(option.responseText);
                me.down('#mdc-visualiser-layer-maps').setVisible(response.geolocationEnabled);
                geoLocationlayer = response.geolocationTileLayer;
                geoLocationZoom = response.maxZoom,
                    geoLocationType = response.geolocationType;
            }
        });
    },

    checkboxHandler: function (field, newValues, oldValues) {
        var me = this, // = VisualiserPanel
            oldFilters = [],
            filters = [],
            deviceTypeCheckBox = field.down('#mdc-visualiser-layer-device-types'),
            issuesAlarmsCheckBox = field.down('#mdc-visualiser-layer-issues-alarms'),
            hopsCheckBox = field.down('#mdc-visualiser-layer-hops'),
            qualityCheckBox = field.down('#mdc-visualiser-layer-quality'),
            lifeCylceStatusCheckBox = field.down('#mdc-visualiser-layer-life-cycle-status'),
            commStatusCheckBox = field.down('#mdc-visualiser-layer-communication-status'),
            mapsCheckBox = field.down('#mdc-visualiser-layer-maps'),
            optionsMenu = Ext.ComponentQuery.query('#uni-visualiser-menu')[0].down('#layer-options-section');


        if (!newValues.rb) {
            filters.concat(newValues.rb);
        } else if (typeof newValues.rb === 'string') {
            filters[0] = newValues.rb;
        } else {
            filters = newValues.rb;
        }
        if (!oldValues.rb) {
            oldFilters.concat(oldValues.rb);
        } else if (typeof oldValues.rb === 'string') {
            oldFilters[0] = oldValues.rb;
        } else {
            oldFilters = oldValues.rb;
        }

        if (filters.length > oldFilters.length) { // An extra layer has been selected
            var result = Ext.Array.difference(filters, oldFilters); // Determine the added layer
            switch (result[0]) {
                case '1':
                    hopsCheckBox.setDisabled(true);
                    lifeCylceStatusCheckBox.setDisabled(true);
                    me.addLayer(me.showDeviceType);
                    break;
                case "2":
                    me.addLayer(me.showIssuesAndAlarms);
                    break;
                case "3": // amount of hops
                    deviceTypeCheckBox.setDisabled(true);
                    lifeCylceStatusCheckBox.setDisabled(true);
                    me.addLayer(me.showHopLevel);
                    break;
                case "4":
                    me.addLayer(me.showLinkQuality);
                    break;
                case "5":
                    deviceTypeCheckBox.setDisabled(true);
                    hopsCheckBox.setDisabled(true);
                    me.addLayer(me.showDeviceLifeCycleStatus);
                    break;
                case "6":
                    me.addLayer(me.showCommunicationStatus);
                    break;
                case "7":
                    optionsMenu.hide();
                    me.showMap();
                    me.addLayer(me.showMap);
                    break;
            }

            if (result[0] === '3') { // amount of hops layer added (this one doesn't need extra back-end data)
                me.clearAllLegendItems();
                me.setDefaultStyle();
                me.showLayers();
            } else {
                var layersToQuery = [];
                if (deviceTypeCheckBox.getValue()) {
                    layersToQuery.push('topology.GraphLayer.DeviceType');
                }
                if (issuesAlarmsCheckBox.getValue()) {
                    layersToQuery.push('topology.GraphLayer.IssuesAndAlarms');
                }
                if (qualityCheckBox.getValue()) {
                    layersToQuery.push('topology.GraphLayer.linkQuality');
                }
                if (lifeCylceStatusCheckBox.getValue()) {
                    layersToQuery.push('topology.GraphLayer.DeviceLifeCycleStatus');
                }
                if (commStatusCheckBox.getValue()) {
                    layersToQuery.push('topology.GraphLayer.CommunicationStatus');
                }
                if (mapsCheckBox.getValue()) {
                    layersToQuery.push('topology.GraphLayer.DeviceGeoCoordinatesLayer');
                }
                me.store.getProxy().setExtraParam('filter', Ext.encode([
                    {
                        property: 'layers',
                        value: layersToQuery
                    },
                    {
                        property: 'refresh',
                        value: false
                    }
                ]));
                me.refreshLayers(qualityCheckBox.getValue());
            }
        } else if (filters.length < oldFilters.length) { // A layer has been removed
            var result = Ext.Array.difference(oldFilters, filters); // Determine the removed layer
            switch (result[0]) {
                case '1':
                    hopsCheckBox.setDisabled(false);
                    lifeCylceStatusCheckBox.setDisabled(false);
                    me.removeLayer(me.showDeviceType);
                    break;
                case "2":
                    me.removeLayer(me.showIssuesAndAlarms);
                    break;
                case "3":
                    deviceTypeCheckBox.setDisabled(false);
                    lifeCylceStatusCheckBox.setDisabled(false);
                    me.removeLayer(me.showHopLevel);
                    break;
                case "4":
                    me.removeLayer(me.showLinkQuality);
                    break;
                case "5":
                    deviceTypeCheckBox.setDisabled(false);
                    hopsCheckBox.setDisabled(false);
                    me.removeLayer(me.showDeviceLifeCycleStatus);
                    break;
                case "6":
                    me.removeLayer(me.showCommunicationStatus);
                    break;
                case "7":
                    optionsMenu.show();
                    me.hideMap();
                    me.removeLayer(me.showMap);
                    break;
            }
            me.clearAllLegendItems();
            me.setDefaultStyle();
            me.showLayers();
        }
    }
});