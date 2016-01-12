Ext.define('CSMonitor.view.logging.Communication', {
    extend: 'Ext.panel.Panel',
    xtype: 'communication',
    border: false,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'component',
            margins: '0 0 0 10',
            html: '<h2>Communication logging</h2>'
        },
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            defaults: { flex: 1, margins: '5, 15, 15, 15' }, // top, left, bottom, right
            items: [
                {
                    xtype: 'container',
                    itemId: 'communicationLoggingContainer',
                    listeners: {
                        el: {
                            mouseover: function(event, target) {
                                target.style.cursor = 'pointer';
                            },
                            mouseenter: function(event, target) {
                                target.style.backgroundColor = '#f9f9f9';
                            },
                            mouseleave: function(event, target) {
                                target.style.backgroundColor = '#ffffff';
                            }
                        }
                    },
                    style: {
                        backgroundColor: '#ffffff'
                    },
                    layout: {
                        type: 'vbox',
                        align: 'center'
                    },
                    items: [
                        {
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'stretch'
                            },
                            items: [
                                {
                                    xtype: 'image',
                                    src: 'resources/images/inbound.png',
                                    margins: '5,0,0,0',
                                    width: 48,
                                    height: 48
                                },
                                {
                                    xtype: 'container',
                                    width: 5
                                },
                                {
                                    xtype: 'image',
                                    src: 'resources/images/outbound.png',
                                    margins: '5,0,0,0',
                                    width: 48,
                                    height: 48
                                }
                            ]
                        },
                        {
                            xtype: 'component',
                            html: '<b>Inbound/outbound communication <sup>*</sup></b>'
                        },
                        {
                            xtype: 'component',
                            itemId: 'defineCriteriaMsg',
                            style: {
                                color: '#000000'
                            },
                            html: '(<sup>*</sup> Define at least one criterion beneath)'
                        },
                        {
                            xtype: 'component',
                            margins: '15,0,0,0',
                            html: 'Opens a new browser tab containing logging about:<ul><li>Scheduler</li><li>Inbound/outbound connections</li><li>Communication tasks</li></ul>'
                        }
                    ]
                },
                {
                    xtype: 'container'
                }
            ]
        }
    ],

    warnForEmptyCriteria: function() {
        var me = this,
            taskSetWhiteAgain = new Ext.util.DelayedTask(function() {
                me.setMsgColor('black');
            });
        this.setMsgColor('red');
        taskSetWhiteAgain.delay(1000);
    },

    setMsgColor: function(color) {
        this.down('#defineCriteriaMsg').getEl().setStyle('color', color);
    },

    openWindow: function(url) {
        window.open(url);
    }

});