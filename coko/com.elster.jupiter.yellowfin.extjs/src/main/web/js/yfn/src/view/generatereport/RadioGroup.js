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
        groupSelected:false

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
                    listeners:{
                        afterrender:function(component){
                            new Ext.ToolTip({
                                target: component.getEl(),
                                html: item.tooltip
                            });

                        },
                        change:function(field, newValue, oldValue, eOpt){
                            if(newValue) {
                                field.up('radiogroup').previousSibling('radiofield').setValue(newValue);
                            }
                        }
                    }
                });
            }


            Ext.applyIf(me, {
                items: [
                    {
                        xtype: 'radiofield',
                        boxLabel: me.groupLabel,
                        disabled : me.groupDisabled,
                        checked : me.groupSelected,
                        inputValue:me.groupValue,
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
