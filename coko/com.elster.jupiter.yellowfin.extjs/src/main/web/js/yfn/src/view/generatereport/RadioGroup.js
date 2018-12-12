/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Yfn.view.generatereport.RadioGroup', {
    extend: 'Ext.container.Container',
    alias: 'widget.radio-group',
    ui:'tile',
    config:{
        groupLabel:'Device reports',
        groupValue:false,
        groupName:undefined,
        groupItems:[],
        groupDisabled:false,
        groupSelected:false,
        allowBlank:false

    },
    layout: 'fit',
    initComponent: function () {
        var me = this;

        var items = [];
        if (me.groupItems && me.groupItems.length) {
            for(var i=0;i<me.groupItems.length;i++){
                var item = me.groupItems[i];
                items.push({
                    xtype: 'radiofield',
                    boxLabel: item.boxLabel,
                    inputValue: item.inputValue,
                    name: item.name,
                    checked: !me.disabled && item.checked,
                    disabled:me.groupDisabled,
                    record:item.record,
                    tooltip:item.tooltip,
                    fieldType:item.fieldType,
                    listeners:{
                        afterrender:function(component){
                            var tooltip = component.tooltip;// || (component.record && component.record.get('description')) ;

                            new Ext.ToolTip({
                                target: component.getEl(),
                                html: tooltip
                            });

                        }
                    }
                });
            }


            Ext.applyIf(me, {
                items: [
                    {
                        xtype: 'label',
                        text: me.groupLabel,
                        disabled : me.groupDisabled,
                        name: me.groupName
                    },
                    {
                        xtype: 'radiogroup',
                        layout:'fit',
                        padding: '0 0 0 20',
                        columns: 1,
                        items: items
                    }
                ]
            });
        }

        this.callParent(arguments);
    }
});
