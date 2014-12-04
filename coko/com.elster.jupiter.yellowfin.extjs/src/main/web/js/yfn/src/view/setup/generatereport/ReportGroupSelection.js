Ext.define('Yfn.view.setup.generatereport.ReportGroupSelection', {
    extend: 'Ext.container.Container',
    alias: 'widget.report-group-selection',
    config:{
        reportGroupTitle:'Device reports',
        reportGroupName:'',
        reportsList:[],
        reportGroupDisabled:false,
        reportGroupSelected:false

    },
    layout: 'fit',
    initComponent: function () {
        var me = this;

        var items = [];
        if (me.reportsList && me.reportsList.length) {
            for(var i=0;i<me.reportsList.length;i++){
                var report = me.reportsList[i];
                items.push({
                    xtype: 'radiofield',
                    boxLabel: report.reportName,
                    inputValue: report.reportUUID,
                    tooltip:report.reportDescription,
                    name: 'reportUUID',//report.subCategory,
                    checked: !me.disabled && report.checked,
                    disabled:me.reportGroupDisabled,
                    listeners:{
                        afterrender:function(component){
                            new Ext.ToolTip({
                                target: component.getEl(),
                                html: report.reportDescription
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
                        boxLabel: me.reportGroupTitle,
                        disabled : me.reportGroupDisabled,
                        checked : me.reportGroupSelected,
                        name: 'reportGroup'
                    },
                    {
                        xtype: 'radiogroup',
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
