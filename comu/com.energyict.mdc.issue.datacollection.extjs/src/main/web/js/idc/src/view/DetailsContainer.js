Ext.define('Idc.view.DetailsContainer', {
    extend: 'Ext.container.Container',
    alias: 'widget.data-collection-details-container',
    layout: 'column',
    initComponent: function () {
        var me = this;

        me.items = [
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
                        itemId: 'data-collection-issue-detail-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'IDC', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : '';
                        }
                    },
                    {
                        itemId: 'issue-type-field',
                        fieldLabel: Uni.I18n.translate('general.type', 'IDC', 'Type'),
                        name: 'issueType',
                        renderer: function (value) {
                            return value ? value.name : '';
                        }
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
                        itemId: 'data-collection-issue-detail-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'IDC', 'Status'),
                        name: 'status',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : '';
                        }
                    },
                    {
                        itemId: 'data-collection-issue-detail-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'IDC', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '';
                        }
                    },
                    {
                        itemId: 'data-collection-issue-detail-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.assignee', 'IDC', 'Assignee'),
                        name: 'assignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.none', 'IDC', 'None');
                        }
                    },
                    {
                        itemId: 'data-collection-issue-detail-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'IDC', 'Creation date'),
                        name: 'creationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});