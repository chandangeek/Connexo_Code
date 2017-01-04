Ext.define('Mdc.networkvisualiser.view.NetworkVisualiserMenu', {
    extend: 'Uni.graphvisualiser.VisualiserMenu',
    alias: 'widget.networkvisualisermenu',

    initComponent: function(){
        var me = this;
        me.callParent(arguments);
        me.down('#uni-layer-section').add([
            {
                xtype: 'checkboxgroup',
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
                    }
                ],
                listeners: {
                    change: {
                        fn: this.checkboxHandler,
                        scope: this.visualiser
                    }
                }
            },
            //{
            //    xtype: 'button',
            //    text: 'clear',
            //    handler: function(){
            //        Ext.ComponentQuery.query('visualiserpanel')[0].clearLayers();
            //    }
            //}
        ])
    },

    checkboxHandler: function(field, values){
        var me = this,
            filters = [],
            deviceTypeCheckBox = field.down('#mdc-visualiser-layer-device-types'),
            issuesAlarmsCheckBox = field.down('#mdc-visualiser-layer-issues-alarms'),
            hopsCheckBox = field.down('#mdc-visualiser-layer-hops'),
            qualityCheckBox = field.down('#mdc-visualiser-layer-quality'),
            lifeCylceStatusCheckBox = field.down('#mdc-visualiser-layer-life-cycle-status'),
            commStatusCheckBox = field.down('#mdc-visualiser-layer-communication-status');

        deviceTypeCheckBox.setDisabled(false);
        issuesAlarmsCheckBox.setDisabled(false);
        hopsCheckBox.setDisabled(false);
        qualityCheckBox.setDisabled(false);
        lifeCylceStatusCheckBox.setDisabled(false);
        commStatusCheckBox.setDisabled(false);
        if(!values.rb) {
            filters.concat(values.rb);
        } else if(typeof values.rb === 'string'){
            filters[0] = values.rb;
        } else {
            filters = values.rb;
        }
        this.clearLayers();
        Ext.each(filters, function(filter){
            switch(filter) {
                case '1':
                    hopsCheckBox.setDisabled(true);
                    lifeCylceStatusCheckBox.setDisabled(true);
                    me.addLayer(me.showDeviceType);
                    break;
                case "2":
                    me.addLayer(me.showAlarms);
                    break;
                case "3":
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
                    break;
            }
        });
        this.showLayers();
    }
});