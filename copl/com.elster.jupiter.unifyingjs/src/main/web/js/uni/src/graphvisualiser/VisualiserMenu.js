Ext.define('Uni.graphvisualiser.VisualiserMenu', {
    extend: 'Ext.container.Container',
    alias: 'widget.visualisermenu',
    itemId: 'visualiser-menu',
    //width: 300,
    //height: 400,
    floating: true,
    border: false,
    //collapsed: true,
    style: {
        'background-color': 'white'
    },
    //collapsible: true,
    //title: 'visualisation',
    // ui: 'small',
    items: [
        {
            xtype: 'form',
            ui: 'small',
            items: [
                {
                    xtype: 'combobox',
                    queryMode: 'local',
                    displayField: 'name',
                    valueField: 'id',
                    typeAhead: true,
                    listeners: {
                        focus: function () {
                            if (this.value) {
                                var visualiser = Ext.ComponentQuery.query('visualiserpanel')[0];
                                visualiser.upStreamFromNode(this.value);
                            }
                        },
                        select: function (combo, records) {
                            var visualiser = Ext.ComponentQuery.query('visualiserpanel')[0];
                            visualiser.upStreamFromNode(records[0].get('id'));
                        }
                    }
                }
            ]
        },
        {
            xtype: 'panel',
            ui: 'small',
            title: 'visualisation',
            collapsible: true,
            style: {
                'background-color': 'white'
            },
            layout: 'vbox',
            items: [
                {
                    xtype: 'form',
                    itemId: 'layer-section'
                },
                {
                    xtype: 'form',
                    title: ' options',
                    items: [
                        {
                            xtype: 'radiogroup',
                            fieldLabel: 'Lay out',
                            columns: 1,
                            vertical: true,
                            items: [
                                {boxLabel: 'Standard', name: 'rb', inputValue: '1', checked: true},
                                {boxLabel: 'Cluster', name: 'rb', inputValue: '2'},
                                {boxLabel: 'Radial', name: 'rb', inputValue: '3'},
                                {boxLabel: 'Tree', name: 'rb', inputValue: '4'}
                            ],
                            listeners: {
                                change: function (group, value) {
                                    var visualiser = Ext.ComponentQuery.query('visualiserpanel')[0];
                                    switch (value.rb) {
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
        }
    ]
});