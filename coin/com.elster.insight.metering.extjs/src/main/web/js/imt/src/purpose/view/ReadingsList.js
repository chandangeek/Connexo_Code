/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.ReadingsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.readings-list',
    itemId: 'readings-list',
    requires: [
        'Imt.purpose.store.Readings',
        'Uni.view.toolbar.PagingTop',
        'Imt.purpose.view.SingleReadingActionMenu',
        'Imt.purpose.view.MultipleReadingsActionMenu',
        'Uni.grid.column.Edited',
        'Imt.purpose.util.TooltipRenderer',
        'Uni.grid.plugin.CopyPasteForGrid'
    ],
    selModel: {
        mode: 'MULTI'
    },
    viewConfig: {
        loadMask: false,
        enableTextSelection: true,
        doFocus: Ext.emptyFn // workaround to avoid page jump during row selection
    },
    store: 'Imt.purpose.store.Readings',

    initComponent: function () {
        var me = this,
            readingType = me.output.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : undefined;

        me.plugins = [
            {
                ptype: 'gridviewcopypaste',
                editColumnDataIndex: 'value'
            },
            {
                ptype: 'bufferedrenderer',
                trailingBufferZone: 12,
                leadingBufferZone: 24
            },
            {
                ptype: 'cellediting',
                clicksToEdit: 1,
                pluginId: 'cellplugin',
                onSpecialKey: Ext.emptyFn // workaround to fix CXO-6274
            }
        ];
        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'IMT', 'End of interval'),
                dataIndex: 'interval',
                renderer: function (interval, metaData, record) {
                    var text = interval.end
                        ? Uni.DateTime.formatDateTimeShort(new Date(interval.end))
                        : '-';

                    return text + Imt.purpose.util.TooltipRenderer.prepareIcon(record);
                },
                flex: 1
            },
            {
                header: unit
                    ? Uni.I18n.translate('general.valueOf', 'IMT', 'Value ({0})', [unit])
                    : Uni.I18n.translate('general.value.empty', 'IMT', 'Value'),
                width: 210,
                renderer: me.formatColumn,
                editor: {
                    xtype: 'textfield',
                    stripCharsRe: /[^0-9\.]/,
                    selectOnFocus: true,
                    validateOnChange: true,
                    fieldStyle: 'text-align: right',
                },
                align: 'right',
                dataIndex: 'value'
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'modificationState',
                width: 30,
                emptyText: ' '
            },
            {
                header: Uni.I18n.translate('device.readingData.lastUpdate', 'IMT', 'Last update'),
                dataIndex: 'reportedDateTime',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                }
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                itemId: 'channel-data-grid-action-column',
                privileges: Imt.privileges.UsagePoint.admin,
                menu: {
                    xtype: 'purpose-readings-data-action-menu',
                    itemId: 'purpose-readings-data-action-menu'
                },
                isDisabled: function(grid, rowIndex, colIndex, clickedItem, record) {
                    return record.get('partOfTimeOfUseGap');
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                noBottomPaging: true,
                usesExactCount: true,
                isFullTotalCount: true,
                displayMsg: Uni.I18n.translate('reading.pagingtoolbartop.displayMsg', 'IMT', '{1} reading(s)'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'pre-validate-button',
                        text: Uni.I18n.translate('general.preValidate', 'IMT', 'Pre-validate'),
                        disabled: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'save-changes-button',
                        text: Uni.I18n.translate('general.saveChanges', 'IMT', 'Save changes'),
                        disabled: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'undo-button',
                        text: Uni.I18n.translate('general.undo', 'IMT', 'Undo'),
                        disabled: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'readings-bulk-action-button',
                        text: Uni.I18n.translate('general.bulkAction', 'IMT', 'Bulk action'),
                        disabled: true,
                        menu: {
                            xtype: 'purpose-bulk-action-menu',
                            itemId: 'purpose-bulk-action-menu'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    addProjectedFlag: function (icon) {
        icon += '<span style="margin-left:42px; position:absolute; font-weight:bold; cursor: default" data-qtip="'
            + Uni.I18n.translate('reading.estimated.projected', 'IMT', 'Projected') + '">P</span>';
        return icon;
    },

    formatColumn: function (v, metaData, record) {
        var status = record.get('validationResult') ? record.get('validationResult').split('.')[1] : '',
            value = Ext.isEmpty(v) ? '-' : v,
            estimatedByRule = record.get('estimatedByRule'),
            estimationComment = record.get('commentValue')
                ? ' ' + Uni.I18n.translate('general.estimationCommentWithComment', 'IMT', 'Estimation comment: {0}', record.get('commentValue'))
                : '',
            icon = '';
        if (record.get('confirmedNotSaved') || record.isModified('isProjected')) {
            metaData.tdCls = 'x-grid-dirty-cell';
        }
        if (record.get('partOfTimeOfUseGap')) {
            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.tou.gap', 'IMT', 'Data not calculated, calendar \''+ record.get('calendarName') +'\' only uses data specified in the formula.') + '"></span>';
        } else if (status === 'notValidated') {
            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.notvalidated', 'IMT', 'Not validated') + '"></span>';
        } else if (status === 'suspect') {
            icon = '<span class="icon-flag5" style="margin-left:10px; color:red; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.suspect', 'IMT', 'Suspect') + '"></span>';
        } else if (status === 'ok' && record.get('action') == 'WARN_ONLY') {
            icon = '<span class="icon-flag5" style="margin-left:10px; color: #dedc49; position:absolute;" data-qtip="'
                + Uni.I18n.translate('validationStatus.informative', 'IMT', 'Informative') + '"></span>';
        }
        if ((!Ext.isEmpty(estimatedByRule)) && !record.get('removedNotSaved') &&  (!record.isModified('value') || record.isModified('isProjected')) && status !== 'suspect') {
            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:#33CC33;" data-qtip="'
                + Uni.I18n.translate('reading.estimated', 'IMT', 'Estimated on {0} at {1}', [
                    Uni.DateTime.formatDateLong(new Date(estimatedByRule.when)),
                    Uni.DateTime.formatTimeLong(new Date(estimatedByRule.when))
                ], false) + estimationComment + '"></span>';
            if (record.get('isProjected')) {
                icon = this.addProjectedFlag(icon);
            }
        } else if (record.get('estimatedNotSaved') && record.get('ruleId') > 0 && status !== 'suspect') {
            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:#33CC33;" data-qtip="' + Uni.I18n.translate('reading.estimatedNotSaved', 'IMT', 'Estimated.') + ' ' + estimationComment + '"></span>';
            if (record.get('isProjected')) {
                icon = this.addProjectedFlag(icon);
            }
        } else if ((record.get('isConfirmed') || record.get('confirmedNotSaved')) && !record.isModified('value')) {
            icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.confirmed', 'IMT', 'Confirmed') + '"></span>';
            if (Ext.isEmpty(v) && record.data.calculatedValue) {
                value = record.data.value = record.data.calculatedValue;
            }
        } else if ((record.get('modificationFlag') && record.get('modificationDate') || record.isModified('value')) && record.get('isProjected')) {
            icon = this.addProjectedFlag(icon);
        }
        if (record.get('estimatedCommentNotSaved')) {
            record.modified.value = record.get('value');
        }
        if (record.get('potentialSuspect')) {
            icon = this.addPotentialSuspectFlag(icon, record);
        }
        return '<div style=" display:inline;">' + value + '</div>' + icon + '<span style=" display:inline;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>';
    },

    addPotentialSuspectFlag: function (icon, record) {
        var validationRules = record.get('validationRules'),
            validationRulesToken = '',
            tooltip;

        if (validationRules.length === 1) {
            validationRulesToken = validationRules[0].name;
        } else {
            validationRulesToken = validationRules.reduce(function(previousValue, currentValue) {
                return (Ext.isObject(previousValue) ? previousValue.name : previousValue) + ', ' + currentValue.name;
            });
        }
        tooltip = Uni.I18n.translate('general.potentialSuspect', 'IMT', 'Potential suspect') + '.<br>' + Uni.I18n.translate('general.validationRules', 'IMT', 'Validation rules') + ': ' + validationRulesToken;
        icon += '<span class="icon-flag6" style="margin-left:27px; color: red; position:absolute;" data-qtip="' + tooltip + '"></span>';
        return icon;
    }
});