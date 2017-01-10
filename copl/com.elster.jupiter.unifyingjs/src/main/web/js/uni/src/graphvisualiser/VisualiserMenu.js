Ext.define('Uni.graphvisualiser.VisualiserMenu', {
    extend: 'Ext.container.Container',
    alias: 'widget.visualisermenu',
    itemId: 'uni-visualiser-menu',
    floating: true,
    border: false,
    objectType: Uni.I18n.translate('general.visulisation.search.objectType', 'UNI', 'device'),
    style: {
        'background-color': 'white'
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
                            visualiser.highlightUpStreamFromNode(this.value);
                        }
                    },
                    select: function (combo, records) {
                        var visualiser = Ext.ComponentQuery.query('visualiserpanel')[0];
                        visualiser.setSelection(records[0].get('id'));
                        visualiser.highlightUpStreamFromNode(records[0].get('id'));
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
                hideCollapseTool: true,
                tools: [
                    {
                        xtype: 'button',
                        ui: 'colexp',
                        tooltip: Uni.I18n.translate('general.collapse', 'UNI', 'Collapse'),
                        iconCls: 'icon-circle-up2',
                        mystate: 'expanded',
                        handler: function(button) {
                            if (button.mystate==='expanded') {
                                button.up('#uni-visualiser-menu-visulisation-panel').collapse();
                                button.setIconCls('icon-circle-down2');
                                button.mystate = 'collapsed';
                                button.setTooltip(Uni.I18n.translate('general.expand', 'UNI', 'Expand'));
                            } else {
                                button.up('#uni-visualiser-menu-visulisation-panel').expand();
                                button.setIconCls('icon-circle-up2');
                                button.mystate = 'expanded';
                                button.setTooltip(Uni.I18n.translate('general.collapse', 'UNI', 'Collapse'));
                            }
                        }
                    }
                ],
                style: {
                    'background-color': 'white'
                },
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
                                //  fieldLabel: Uni.I18n.translate('general.layout', 'UNI', 'Layout'),
                                //   labelWidth: 50,
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
        ]
        this.callParent(arguments);
    }
});