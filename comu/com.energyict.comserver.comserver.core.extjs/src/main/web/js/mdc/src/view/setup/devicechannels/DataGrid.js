/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfileChannelDataGrid',
    itemId: 'deviceLoadProfileChannelDataGrid',
    store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Mdc.view.setup.devicechannels.DataActionMenu',
        'Uni.grid.column.IntervalFlags',
        'Uni.grid.column.Edited',
        'Uni.view.toolbar.PagingTop',
        'Uni.grid.column.Action',
        'Mdc.view.setup.devicechannels.DataBulkActionMenu',
        'Uni.grid.plugin.CopyPasteForGrid'
    ],
    viewConfig: {
        loadMask: false,
        enableTextSelection: true,
        doFocus: Ext.emptyFn // workaround to avoid page jump during row selection
    },
    selModel: {
        mode: 'MULTI'
    },
    channelRecord: null,
    router: null,

    initComponent: function () {
        var me = this,
            readingType = me.channelRecord.get('readingType'),
            unitOfCollectedValues = readingType && readingType.names ? readingType.names.unitOfMeasure : undefined,
            calculatedReadingType = me.channelRecord.get('calculatedReadingType'),
            unitOfCalculatedValues = calculatedReadingType && calculatedReadingType.names ? calculatedReadingType.names.unitOfMeasure : undefined;

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
                onSpecialKey: Ext.emptyFn, // workaround to fix CXO-6274
                listeners: {
                    'beforeedit': function (e, f) {
                        return !f.record.get('slaveChannel');
                    }
                }
            }
        ];

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                renderer: function (value, metaData, record) {
                    var readingQualitiesPresent = !Ext.isEmpty(record.get('readingQualities')),
                        text = value
                            ? Uni.DateTime.formatDateTimeShort(new Date(value))
                            : '-',
                        tooltipContent = '',
                        icon = '';

                    if (readingQualitiesPresent) {
                        Ext.Array.forEach(record.get('readingQualities'), function (readingQualityName) {
                            tooltipContent += (readingQualityName + '<br>');
                        });
                        if (tooltipContent.length > 0) {
                            tooltipContent += '<br>';
                            tooltipContent += Uni.I18n.translate('general.deviceQuality.tooltip.moreMessage', 'MDC', 'View reading quality details for more information.');

                            icon = '<span class="icon-price-tags" style="margin-left:10px; position:absolute;" data-qtitle="'
                                + Uni.I18n.translate('general.deviceQuality', 'MDC', 'Device quality') + '" data-qtip="'
                                + tooltipContent + '"></span>';
                        }
                    }
                    return text + icon;
                },
                flex: 1
            },
            {
                header: unitOfCalculatedValues
                    ? Uni.I18n.translate('general.calculated', 'MDC', 'Calculated') + ' (' + unitOfCalculatedValues + ')'
                    : Uni.I18n.translate('general.collected', 'MDC', 'Collected') + ' (' + unitOfCollectedValues + ')',
                dataIndex: 'value',
                align: 'right',
                renderer: function (v, metaData, record) {
                    return me.formatColumn(v, metaData, record, 'mainValidationInfo');
                },
                editor: {
                    xtype: 'textfield',
                    stripCharsRe: /[^-0-9\.]/,
                    selectOnFocus: true,
                    validateOnChange: true,
                    fieldStyle: 'text-align: right'
                },
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions,
                width: 200
            },
            {
                header: unitOfCalculatedValues
                    ? Uni.I18n.translate('general.calculated', 'MDC', 'Calculated') + ' (' + unitOfCalculatedValues + ')'
                    : Uni.I18n.translate('general.collected', 'MDC', 'Collected') + ' (' + unitOfCollectedValues + ')',
                dataIndex: 'value',
                align: 'right',
                renderer: function (v, metaData, record) {
                    return me.formatColumn(v, metaData, record, 'mainValidationInfo');
                },
                hidden: Mdc.dynamicprivileges.DeviceState.canEditData(),
                width: 200
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'mainModificationState',
                width: 30,
                emptyText: ' '
            },
            {
                header: Uni.I18n.translate('general.collected', 'MDC', 'Collected') + (unitOfCollectedValues ? ' (' + unitOfCollectedValues + ')' :''),
                dataIndex: 'collectedValue',
                flex: 1,
                align: 'right',
                hidden: Ext.isEmpty(calculatedReadingType),
                renderer: function (v, metaData, record) {
                    return me.formatColumn(v, metaData, record, 'bulkValidationInfo');
                }
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'bulkModificationState',
                width: 30,
                emptyText: ' '
            },
            {
                header: Uni.I18n.translate('device.channelData.lastUpdate', 'MDC', 'Last update'),
                dataIndex: 'reportedDateTime',
                flex: 0.5,
                renderer: function(value){
                    if (value) {
                        var date = new Date(value);
                        return Uni.DateTime.formatDateTimeShort(date)
                    } else {
                        return '-';
                    }
                }
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                itemId: 'channel-data-grid-action-column',
                menu: {
                    xtype: 'deviceLoadProfileChannelDataActionMenu',
                    itemId: 'channel-data-grid-action-menu'
                },
                isDisabled: function(view, rowIndex, colIndex, item, record) {
                    return !Ext.isEmpty(record.get('slaveChannel'));
                }
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                isFullTotalCount: true,
                noBottomPaging: true,
                displayMsg: ' ',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'pre-validate-button',
                        text: Uni.I18n.translate('general.preValidate', 'MDC', 'Pre-validate'),
                        privileges: Mdc.privileges.Device.administrateDeviceData,
                        disabled: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'save-changes-button',
                        text: Uni.I18n.translate('general.saveChanges', 'MDC', 'Save changes'),
                        privileges: Mdc.privileges.Device.administrateDeviceData,
                        disabled: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'undo-button',
                        text: Uni.I18n.translate('general.undo', 'MDC', 'Undo'),
                        privileges: Mdc.privileges.Device.administrateDeviceData,
                        disabled: true
                    },
                    {
                        xtype: 'button',
                        itemId: 'device-channel-data-bulk-action-button',
                        text: Uni.I18n.translate('general.bulkAction', 'MDC', 'Bulk action'),
                        privileges: Mdc.privileges.Device.administrateDeviceData,
                        menu: {
                            xtype: 'channel-data-bulk-action-menu',
                            itemId: 'channel-data-bulk-action-menu'
                        }
                    }
                ]
            }
        ];

        me.getSelectionModel().suppressMultiSelEvent = true;
        me.callParent(arguments);
    },

    formatColumn: function (v, metaData, record, validationInfo) {
        var me = this,
            validationFlag = validationInfo === 'mainValidationInfo' ? 'mainValidationInfo' : validationInfo === 'bulkValidationInfo' ? 'bulkValidationInfo' : null,
            validationInfo = validationFlag ? record.get(validationInfo) : validationInfo,
            status = validationInfo.validationResult ? validationInfo.validationResult.split('.')[1] : '',
            icon = '',
            validationRules = validationFlag === 'mainValidationInfo' ? record.get('validationRules') : record.get('bulkValidationRules'),
            app,
            date,
            tooltipText,
            formattedDate,
            estimationComment = null,
            value = Ext.isEmpty(v)
                ? '-'
                : Uni.Number.formatNumber(
                v.toString(),
                me.channelRecord && !Ext.isEmpty(me.channelRecord.get('overruledNbrOfFractionDigits')) ? me.channelRecord.get('overruledNbrOfFractionDigits') : -1
            );

        if (validationFlag && validationInfo && validationInfo.commentValue) {
            estimationComment = validationInfo.commentValue;
        }

        if (status === 'notValidated') {
            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('general.notValidated', 'MDC', 'Not validated') + '"></span>';
        } else if (validationInfo.confirmedNotSaved) {
            metaData.tdCls = 'x-grid-dirty-cell';
        } else if (status === 'suspect') {
            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:red;" data-qtip="'
                + Uni.I18n.translate('general.suspect', 'MDC', 'Suspect') + '"></span>';
        }

        if (validationInfo.estimatedByRule && status !== 'suspect') {
            date = Ext.isDate(record.get('readingTime')) ? record.get('readingTime') : new Date(record.get('readingTime'));
            formattedDate = Uni.DateTime.formatDateTimeLong(date);
            tooltipText = validationInfo.estimatedNotSaved ? Uni.I18n.translate('general.estimatedNotSaved', 'MDC', 'Estimated.') : Uni.I18n.translate('general.estimatedOnX', 'MDC', 'Estimated on {0}', formattedDate);
            if (estimationComment) {
                tooltipText += ' ' + Uni.I18n.translate('general.estimationCommentWithComment', 'MDC', 'Estimation comment: {0}', estimationComment);
            }
            icon = '<span class="icon-flag5" style="margin-left:10px; position:absolute; color:#33CC33;" data-qtip="'
                + tooltipText + '"></span>';
        } else if ((validationInfo.isConfirmed || validationInfo.confirmedNotSaved) && !record.isModified('value')) {
            icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.confirmed', 'MDC', 'Confirmed') + '"></span>';
        }
        if ((record.get('potentialSuspect') || record.get('bulkPotentialSuspect')) && validationRules.length) {
            icon = this.addPotentialSuspectFlag(icon, record, validationRules);
        }
        return '<div style=" display:inline;">' + value + '</div>' + icon + '<span style=" display:inline;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>';
    },

    addPotentialSuspectFlag: function (icon, record, validationRules) {
        var validationRulesToken = '',
            tooltip;

        if (validationRules.length === 1) {
            validationRulesToken = validationRules[0].name;
        } else {
            validationRulesToken = validationRules.reduce(function(previousValue, currentValue) {
                return (Ext.isObject(previousValue) ? previousValue.name : previousValue) + ', ' + currentValue.name;
            });
        }
        tooltip = Uni.I18n.translate('general.potentialSuspect', 'MDC', 'Potential suspect') + '.<br>' + Uni.I18n.translate('general.validationRules', 'MDC', 'Validation rules') + ': ' + validationRulesToken;
        icon += '<span class="icon-flag6" style="margin-left:27px; color: red; position:absolute;" data-qtip="' + tooltip + '"></span>';
        return icon;
    }
});