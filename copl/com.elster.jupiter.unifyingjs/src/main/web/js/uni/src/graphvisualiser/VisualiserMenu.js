Ext.define('Uni.graphvisualiser.VisualiserMenu', {
    extend: 'Ext.container.Container',
    alias: 'widget.visualisermenu',
    itemId: 'uni-visualiser-menu',
    floating: true,
    ui: 'visualiser',
    shadow: false,
    border: 1,
    objectType: Uni.I18n.translate('general.visulisation.search.objectType', 'UNI', 'device'),
    style: {
        'background-color': 'white',
        'border-color': '#cbcbcb',
        'border-radius': '10px',
        'border-style': 'solid'
    },

    initComponent: function(){
        this.items = [
            {
                xtype: 'combobox',
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                typeAhead: true,
                width: 250,
                emptyText: Ext.String.format(Uni.I18n.translate('general.visulisation.search.emptyText', 'UNI', 'Start typing to search for a {0}'),this.objectType),
                margin: '10 10 0 10',
                listeners: {
                    focus: function () {
                        if (this.value) {
                            var visualiser = Ext.ComponentQuery.query('visualiserpanel')[0];
                            visualiser.setSelection(this.value);
                            visualiser.highlightUpStreamFromNode(this.value+'');
                        }
                    },
                    select: function (combo, records) {
                        var visualiser = Ext.ComponentQuery.query('visualiserpanel')[0];
                        visualiser.setSelection(records[0].get('id'));
                        visualiser.highlightUpStreamFromNode(records[0].get('id')+'');
                    }
                }
            },
            {
                xtype: 'panel',
                ui: 'small',
                width: 270,
                itemId: 'uni-visualiser-menu-visulisation-panel',
                title: Uni.I18n.translate('general.visualisation', 'UNI', 'Visualisation'),
                collapsible: true,
                animCollapse: false,
                hideCollapseTool: true,
                tools: [
                    {
                        xtype: 'button',
                        ui: 'colexp',
                        iconCls: 'icon-circle-up2',
                        mystate: 'expanded',
                        handler: function(button) {
                            if (button.mystate==='expanded') {
                                button.up('#uni-visualiser-menu-visulisation-panel').collapse();
                                button.setIconCls('icon-circle-down2');
                                button.mystate = 'collapsed';
                            } else {
                                button.up('#uni-visualiser-menu-visulisation-panel').expand();
                                button.setIconCls('icon-circle-up2');
                                button.mystate = 'expanded';
                            }
                        }
                    }
                ],
                style: {
                    'background-color': 'white'
                },
                layout: {
                    type: 'vbox'
                },
                items: [
                    {
                        xtype: 'form',
                        title: Uni.I18n.translate('general.layers', 'UNI', 'Layers'),
                        itemId: 'uni-layer-section'
                    },
                    {
                        xtype: 'form',
                        margin: '15 0 0 0',
                        id: 'layer-options-section',
                        title: Uni.I18n.translate('general.options', 'UNI', 'Options'),
                        items: [
                            {
                                xtype: 'radiogroup',
                                margin: '-8 0 0 0',
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
            }
        ];
        this.callParent(arguments);
    }
});