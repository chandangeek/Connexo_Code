Ext.define('Isu.view.workspace.issues.NotifySend', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.notify-user',
    content: [
        {
            items: [
                {
                    xtype: 'panel',
                    ui: 'large',
                    itemId: 'notifyPanel'
                },
                {
                    xtype: 'form',
                    width: '40%',
                    defaults: {
                        labelWidth: 160,
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            itemId: 'errors',
                            name: 'errors',
                            layout: 'hbox',
                            margin: '0 0 20 100',
                            hidden: true,
                            defaults: {
                                xtype: 'container',
                                cls: 'isu-error-panel'
                            }
                        }
                    ],
                    buttons: [
                        {
                            itemId: 'notifySend',
                            action: 'notifySend',
                            ui: 'action'
                        },
                        {
                            text: 'Cancel',
                            action: 'cancel',
                            ui: 'link',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#/workspace/datacollection/issues';
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



