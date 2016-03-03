Ext.define('Idc.view.InboundIssueDetailsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.inbound-issue-details-form',
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'inbound-issue-details-container',
                xtype: 'data-collection-details-container'
            },
            {
                itemId: 'inbound-issue-details-panel',
                title: Uni.I18n.translate('general.connectionDetails', 'IDC', 'Connection details'),
                ui: 'medium',
                layout: 'column',
                items: [
                    {
                        xtype: 'container',
                        layout: 'form',
                        columnWidth: 0.5,
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                itemId: 'inbound-issue-device',
                                fieldLabel: Uni.I18n.translate('general.title.device', 'IDC', 'Device'),
                                name: 'deviceMRID',
                                renderer: function (value) {
                                    return value ? Ext.String.htmlEncode(value) : '';
                                }
                            },
                            {
                                itemId: 'inbound-issue-number-of-connections',
                                fieldLabel: Uni.I18n.translate('general.numberOfConnections', 'IDC', 'Number of connections'),
                                name: 'connectionAttemptsNumber'
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: 'form',
                        columnWidth: 0.5,
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                itemId: 'inbound-issue-first-connection',
                                fieldLabel: Uni.I18n.translate('general.firstConnection', 'IDC', 'First connection'),
                                name: 'firstConnectionAttempt',
                                renderer: function (value) {
                                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                }
                            },
                            {
                                itemId: 'inbound-issue-last-connection',
                                fieldLabel: Uni.I18n.translate('general.lastConnection', 'IDC', 'Last connection'),
                                name: 'lastConnectionAttempt',
                                renderer: function (value) {
                                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
