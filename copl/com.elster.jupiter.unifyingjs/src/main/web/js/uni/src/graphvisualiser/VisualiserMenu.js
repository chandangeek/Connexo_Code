Ext.define('Uni.graphvisualiser.VisualiserMenu', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.visualisermenu',
    itemId: 'visualiser-menu',
    //width: 300,
    //height: 400,
    floating: true,
    //collapsed: true,
    style: {
        'background-color': 'white'
    },
    collapsible: true,
    title: 'visualisation',
    //html: 'Test Panel',
    ui: 'small',
    initComponent: function(){
        var me = this;
        this.items = [{
            layout: 'vbox',
            items: [
                {
                    xtype: 'form',
                    items: [
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
                                change: this.checkboxHandler
                            }
                        },
                        {
                            xtype: 'button',
                            text: 'clear',
                            handler: function(){
                                Ext.ComponentQuery.query('visualiserpanel')[0].clearFilters();
                            }
                        }
                    ]
                    //{
                    //    xtype: 'button',
                    //    text: 'Link quality',
                    //    handler: function () {
                    //        Ext.ComponentQuery.query('visualiserpanel')[0].showLinkQuality();
                    //    }
                    //},
                    //{
                    //    xtype: 'button',
                    //    text: 'Device type',
                    //    handler: function () {
                    //        Ext.ComponentQuery.query('visualiserpanel')[0].showDeviceType();
                    //    }
                    //},
                    //{
                    //    xtype: 'button',
                    //    text: 'Alarms',
                    //    handler: function () {
                    //        Ext.ComponentQuery.query('visualiserpanel')[0].showAlarms();
                    //    }
                    //}
                    // ]
                },
                {
                    xtype: 'form',
                    title:' options',
                    items: [
                        {
                            xtype: 'radiogroup',
                            fieldLabel: 'Lay out',
                            columns: 1,
                            vertical: true,
                            items: [
                                { boxLabel: 'Standard', name: 'rb', inputValue: '1', checked: true },
                                { boxLabel: 'Cluster', name: 'rb', inputValue: '2' },
                                { boxLabel: 'Radial', name: 'rb', inputValue: '3' },
                                { boxLabel: 'Tree', name: 'rb', inputValue: '4' }
                            ],
                            listeners: {
                                change: function(group,value){
                                    var visualiser = Ext.ComponentQuery.query('visualiserpanel')[0];
                                    switch(value.rb){
                                        case '1':
                                            visualiser.doLayout('standard');
                                            break;
                                        case '2':
                                            visualiser.doLayout('structural');
                                            break;
                                        case '3':
                                            visualiser.doLayout('radial');
                                            break;
                                        case '4':
                                            visualiser.doLayout('hierarchy');
                                            break;
                                    }
                                }
                            }
                        }
                    ]
                }
            ]
        }];
        me.callParent(arguments);
    },

    checkboxHandler: function(field, values){
        var filters = [];
        if(!values.rb){
            filters.concat(values.rb);
        }else if(typeof  values.rb === 'string'){
            filters[0] = values.rb;
        } else {
            filters = values.rb;
        }
        var visualiser = Ext.ComponentQuery.query('visualiserpanel')[0];
        visualiser.clearFilters();
        Ext.each(filters, function(filter){
            switch(filter) {
                case '1':
                    visualiser.showDeviceType();
                    break;
                case "2":
                    visualiser.showAlarms();
                    break;
                case "3":
                    visualiser.showHopLevel();
                    break;
                case "4":
                    visualiser.showLinkQuality();
                    break;

            }
        });
    }
});