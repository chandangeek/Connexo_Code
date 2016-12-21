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
    ui: 'small',
    items: [{
        layout: 'vbox',
        items: [
            {
                xtype: 'form',
                itemId: 'layer-section'
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
    }]
});