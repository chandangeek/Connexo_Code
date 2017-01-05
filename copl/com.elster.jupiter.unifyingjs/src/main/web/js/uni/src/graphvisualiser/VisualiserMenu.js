Ext.define('Uni.graphvisualiser.VisualiserMenu', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.visualisermenu',
    itemId: 'uni-visualiser-menu',
    //width: 300,
    //height: 400,
    floating: true,
    //collapsed: true,
    style: {
        'background-color': 'white'
    },
    collapsible: true,
    title: Uni.I18n.translate('general.visualisation', 'UNI', 'Visualisation'),
    ui: 'small',
    items: [{
        layout: 'vbox',
        items: [
            {
                xtype: 'form',
                title: Uni.I18n.translate('general.layers', 'UNI', 'Layers'),
                itemId: 'uni-layer-section'
            },
            {
                xtype: 'form',
                margin: '15 0 0 0',
                title: Uni.I18n.translate('general.options', 'UNI', 'Options'),
                items: [
                    {
                        xtype: 'radiogroup',
                        margin: '0 0 0 0',
                        columns: 1,
                        vertical: true,
                        items: [
                            {
                                boxLabel: Uni.I18n.translate('general.layout.standard', 'UNI', 'Standard'),
                                name: 'rb',
                                inputValue: '1',
                                checked: true
                            },
                            {
                                boxLabel: Uni.I18n.translate('general.layout.cluster', 'UNI', 'Cluster'),
                                name: 'rb',
                                inputValue: '2'
                            },
                            {
                                boxLabel: Uni.I18n.translate('general.layout.radial', 'UNI', 'Radial'),
                                name: 'rb',
                                inputValue: '3'
                            },
                            {
                                boxLabel: Uni.I18n.translate('general.layout.tree', 'UNI', 'Tree'),
                                name: 'rb',
                                inputValue: '4'
                            }
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