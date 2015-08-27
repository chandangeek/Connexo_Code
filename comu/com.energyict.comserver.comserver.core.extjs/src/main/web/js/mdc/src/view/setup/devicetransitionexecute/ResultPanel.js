Ext.define('Mdc.view.setup.devicetransitionexecute.ResultPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.transition-result-panel',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    router: null,
    response: null,

    addErrorItems: function (checks) {
        var me = this,
            errorsContainer = me.down('#errors-container');

        Ext.each(checks, function (check) {
            errorsContainer.add({
                xtype: 'container',
                margin: '10 0 0 30',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'container',
                        html: '-' + check.id + '?'

                    },
                    {
                        xtype: 'button',
                        tooltip: check.name,
                        iconCls: 'uni-icon-info-small',
                        cls: 'uni-btn-transparent',
                        style: {
                            display: 'inline-block',
                            "text-decoration": 'none !important'
                        }
                    }
                ]
            })
        })


    },

    initComponent: function () {
        var me = this;

        if (me.response.result) {
            me.items = [
                {
                    xtype: 'panel',
                    title: new Date(me.response.effectiveTimestamp)<new Date() ?
                        Uni.I18n.translatePlural('devicetransitionexecute.wizard.step2successNow', me.response.targetState, 'MDC', "Successfully changed device state to '{0}'") :
                        Uni.I18n.translatePlural('devicetransitionexecute.wizard.step2success', Uni.DateTime.formatDateTimeLong(new Date(me.response.effectiveTimestamp)), 'MDC', "Device state will change at {0}")
                }
            ];
        } else {
            me.items = [
                {
                    xtype: 'panel',
                    style: {
                        iconCls: 'isu-icon-ok isu-msg-success-icon',
                        colorCls: 'isu-msg-success'
                    }
                },
                {
                    xtype: 'panel',
                    title: Uni.I18n.translatePlural('devicetransitionexecute.wizard.step2fail',  me.response.targetState, 'MDC', "Unable to change device state to '{0}'"),
                    items: [
                        {
                            xtype: 'container',
                            html: me.response.message + ':'
                        },
                        {
                            xtype: 'container',
                            itemId: 'errors-container'
                        }
                    ]
                }
            ];
        }

        me.bbar = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.finish', 'MDC', 'Finish'),
                itemId: 'finishButton',
                style: {
                    'background-color': '#71adc7'
                },
                handler: function () {
                    if(me.deviceRemoved === true){
                        me.router.getRoute('search').forward();
                    } else {
                        me.router.getRoute('devices/device').forward();
                    }
                }
            }
        ];

        this.callParent(arguments);

        Ext.suspendLayouts();
        if (!me.response.result) {
            me.addErrorItems(me.response.microChecks);
        }
        Ext.resumeLayouts(true);
    }

});
