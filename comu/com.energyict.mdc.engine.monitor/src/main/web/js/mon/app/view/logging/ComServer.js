Ext.define('CSMonitor.view.logging.ComServer', {
    extend: 'Ext.panel.Panel',
    xtype: 'comServer',
    border: false,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'component',
            margins: '0 0 0 10', // top, right, bottom, left
            html: '<h2>Communication server logging</h2>'
        },
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            defaults: { flex: 1, margins: '0, 10, 0, 10' }, // top, right, bottom, left
            items: [
                {
                    xtype: 'container',
                    itemId: 'generalLoggingContainer',
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
                            },
                            click: function(event, target) {
                                window.open('#/logging/general');
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
                            xtype: 'image',
                            src: 'resources/images/general.png',
                            margins: '5,0,0,0',
                            width: 48,
                            height: 48
                        },
                        {
                            xtype: 'component',
                            html: '<b>General</b>'
                        },
                        {
                            xtype: 'component',
                            margins: '15,0,0,0',
                            html: 'Opens a new browser tab containing logging about:<ul><li>Communication server changes (eg. a port was added/removed)</li><li>Database</li><li>Network</li></ul>'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    itemId: 'storageLoggingContainer',
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
                            },
                            click: function(event, target) {
                                window.open('#/logging/data');
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
                            xtype: 'image',
                            src: 'resources/images/datastorage.png',
                            margins: '5,0,0,0',
                            width: 48,
                            height: 48
                        },
                        {
                            xtype: 'component',
                            html: '<b>Data storage</b>'
                        },
                        {
                            xtype: 'component',
                            margins: '15,0,0,0',
                            html: 'Opens a new browser tab containing logging about:<ul><li>Data storage</li></ul>'
                        }
                    ]
                }
            ]
        }
    ]
});