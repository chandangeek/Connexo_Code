/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.logging.Communication', {
    extend: 'Ext.container.Container',
    xtype: 'communication',
    layout: {
        type: 'vbox',
        align: 'stretch',
        pack: 'center',
    },
    items: [ {
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
                align: 'stretch',
                pack: 'center'
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox',
                        pack: 'center',
                        align: 'middle'
                    },
                    items: [
                        {
                            xtype: 'image',
                            src: 'resources/images/inbound.png',
                            margins: '5,0,0,5',
                            itemId: 'inboundImage',
                            width: 48,
                            height: 48
                        },
                        {
                            xtype: 'image',
                            src: 'resources/images/outbound.png',
                            margins: '5,0,0,0',
                            itemId: 'outboundImage',
                            width: 48,
                            height: 48
                        }
                    ]
                },
                {
                    xtype: 'container',
                    layout: {
                        type: 'vbox',
                        align: 'center'
                    },
                    items: [
                        {
                            xtype: 'component',
                            itemId: 'inboundOutboundTxt',
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
                            itemId: 'inboundOutboundInfo',
                            html: 'Opens a new browser tab containing logging about:<ul><li>Scheduler</li><li>Inbound/outbound connections</li><li>Communication tasks</li></ul>'
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'criteria',
            bodyStyle: 'background:transparent;'
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