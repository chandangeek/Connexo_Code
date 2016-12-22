Ext.define('Mdc.networkvisualiser.view.NetworkVisualiserMenu', {
    extend: 'Uni.graphvisualiser.VisualiserMenu',
    alias: 'widget.networkvisualisermenu',
    initComponent: function(){
        var me = this;
        me.callParent(arguments);
        me.down('#layer-section').add([
            {
                xtype: 'checkboxgroup',
                columns: 1,
                items: [
                    { xtype: 'checkboxfield', boxLabel: 'Device types', name: 'rb', inputValue: '1'},
                    { xtype: 'checkboxfield', boxLabel: 'Issues alarms', name: 'rb', inputValue: '2'},
                    { xtype: 'checkboxfield', boxLabel: 'Amount of hops', name: 'rb', inputValue: '3'},
                    { xtype: 'checkboxfield', boxLabel: 'Network/link quality', name: 'rb', inputValue: '4'},
                    { xtype: 'checkboxfield', boxLabel: 'Status of device life cycle', name: 'rb', inputValue: '5'},
                    { xtype: 'checkboxfield', boxLabel: 'Communication status', name: 'rb', inputValue: '6'}
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
        var me=this,filters = [];
        if(!values.rb){
            filters.concat(values.rb);
        }else if(typeof  values.rb === 'string'){
            filters[0] = values.rb;
        } else {
            filters = values.rb;
        }
        this.clearLayers();
        Ext.each(filters, function(filter){
            switch(filter) {
                case '1':
                    me.addLayer(me.showDeviceType);
                    break;
                case "2":
                    me.addLayer(me.showAlarms);
                    break;
                case "3":
                    me.addLayer(me.showHopLevel);
                    break;
                case "4":
                    me.addLayer(me.showLinkQuality);
                    break;

            }
        });
        this.showLayers();
    }
});