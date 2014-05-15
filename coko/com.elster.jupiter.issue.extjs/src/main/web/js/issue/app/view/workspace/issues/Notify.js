Ext.define('Isu.view.workspace.issues.Notify', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.notify-user',
    deviceTypeId: null,
    content: [
        {
            items: [
                {
                    xtype: 'panel',
                    ui: 'large',
                    title: 'Notify user'
                },
                {
                    xtype: 'form',
                    width: '50%',
                    defaults: {
                        labelWidth: 160,
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            name: 'errors',
                            layout: 'hbox',
                            hidden: true,
                            defaults: {
                                xtype: 'container',
                                cls: 'isu-error-panel'
                            }
                        },
                        {
                            xtype: 'textarea',
                            name: 'recipients',
                            width: 500,
                            height: 150,
                            allowBlank: false,
                            labelSeparator: ' *',
                            fieldLabel: 'Recipients',
                            vtype: 'obisCode',
                            emptyText: 'user@example.com'
                        },
                        {
                            xtype: 'textarea',
                            name: 'email',
                            width: 500,
                            height: 150,
                            allowBlank: false,
                            labelSeparator: ' *',
                            fieldLabel: 'Email body',
//                            maskRe: /[\d.]+/,
//                            vtype: 'obisCode',
                            msgTarget: 'under',
                            maxLength: 200
                        }
                    ],
                    buttons: [
                        {
                            text: 'Notify',
                            action: 'notify',
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
    ],

    initComponent: function () {
        this.callParent(this);
        Ext.apply(Ext.form.VTypes, {
            obisCode: function (val, field) {
                var email = /^(")?(?:[^\."])(?:(?:[\.])?(?:[\w\-!#$%&'*+/=?^_`{|}~]))*\1@(\w[\-\w]*\.){1,5}([A-Za-z]){2,6}$/gm;
                return email.test(val);
            },
            obisCodeText: 'This field should be an e-mail address in the format "user@example.com"'
        });
    }
});



