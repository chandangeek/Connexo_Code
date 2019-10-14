/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.autoclosureexclusions.AutoclosureExclusionsPreview', {
    extend: 'Ext.panel.Panel',
    requires: [
    ],
    alias: 'widget.autoclosure-exclusion-item-preview',
    title: ' ',
    itemId: 'autoclosure-exclusion-item-preview',
    frame: true,
    items: {
        xtype: 'form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('creationRule.ruleName', 'MDC', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('creationRule.issueType', 'MDC', 'Issue type'),
                        name: 'issueType_name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('creationRule.ruleTemplate', 'MDC', 'Rule template'),
                        name: 'template_name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('creationRule.issueReason', 'MDC', 'Issue reason'),
                        name: 'reason_name'
                    },

                    {
                        fieldLabel: Uni.I18n.translate('creationRule.dueIn', 'MDC', 'Due in'),
                        name: 'dueIn',
                        renderer: function (value) {
                            var result = '-';

                            if (value && value.number) {
                                switch (value.type) {
                                    case 'days':
                                        result = Uni.I18n.translatePlural('general.timeUnit.days', value.number, 'MDC', '{0} days', '{0} day', '{0} days');
                                        break;
                                    case 'weeks':
                                        result = Uni.I18n.translatePlural('general.timeUnit.weeks', value.number, 'MDC', '{0} weeks', '{0} week', '{0} weeks');
                                        break;
                                    case 'months':
                                        result = Uni.I18n.translatePlural('general.timeUnit.months', value.number, 'MDC', '{0} months', '{0} month', '{0} months');
                                        break;
                                }
                            }

                            return result;
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('creationRule.status', 'MDC', 'Status'),
                        name: 'active',
                        renderer: function (value) {
                            return value ? Uni.I18n.translate('creationRules.active', 'MDC', 'Active')
                                : Uni.I18n.translate('creationRules.inactive', 'MDC', 'Inactive');
                        }
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('creationRule.created', 'MDC', 'Created'),
                        name: 'creationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('creationRule.lastModified', 'MDC', 'Last modified'),
                        name: 'modificationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    }
                ]
            }
        ]
    }
});