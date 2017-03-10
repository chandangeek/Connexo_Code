/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.view.form.OverlapGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.custom-attribute-set-versions-overlap-grid',
    itemId: 'custom-attribute-set-versions-overlap-grid-id',
    requires: [
        'Imt.customattributesonvaluesobjects.view.form.OverlapGridActionMenu',
        'Uni.grid.column.Action',
        'Uni.form.field.DateTime'
    ],

    plugins: {
        ptype: 'cellediting',
        initEditTriggers: function () {
            return null;
        }
    },

    initComponent: function () {
        var me = this,
            centerContainer,
            undoBtn;

        me.columns = {
            defaults: {
                editor: {
                    xtype: 'date-time',
                    layout: 'hbox',
                    selectOnFocus: true,
                    dateConfig: {
                        width: 120
                    },
                    hoursConfig: {
                        width: 75
                    },
                    minutesConfig: {
                        width: 75
                    },
                    dateTimeSeparatorConfig: {
                        html: '<span style="color: #686868;">' + Uni.I18n.translate('general.lowercase.at', 'IMT', 'at') + '</span>'
                    }
                }
            },
            items: [
                {
                    header: Uni.I18n.translate('general.from', 'IMT', 'From'),
                    dataIndex: 'startTime',
                    flex: 3,
                    renderer: function (value, meta, record, index, column) {
                        if (record.get('editable')) {
                            meta.style = 'font-weight: bold';
                        }
                        if (record.get('editable') && value) {
                            var icon = '<span id="edit-column-icon-' + column + '" class="icon-calendar2" style="cursor: pointer; display: inline-block; width: 20px; height: 20px; float: right; font-size: 20px"></span>';

                            return '<span style="margin: 0px 50px 0px 0px;">' + Uni.DateTime.formatDateTimeShort(new Date(value)) + '</span>' + icon;
                        } else {
                            var timeString,
                                valueToShow;

                            if (value) {
                                timeString = Uni.DateTime.formatDateTimeShort(new Date(value));
                            } else {
                                timeString = Uni.I18n.translate('general.infinite', 'IMT', 'Infinite');
                            }

                            if (record.get('conflictAtStart') || record.get('conflictType') === 'RANGE_OVERLAP_DELETE') {
                                valueToShow = '<span style="color: #eb5642;">' + timeString + '</span>';
                            } else {
                                valueToShow = timeString;
                            }

                            return valueToShow;
                        }
                    }
                },
                {
                    header: Uni.I18n.translate('general.until', 'IMT', 'Until'),
                    dataIndex: 'endTime',
                    flex: 3,
                    renderer: function (value, meta, record, index, column) {
                        if (record.get('editable')) {
                            meta.style = 'font-weight: bold';
                        }
                        if (record.get('editable') && value) {
                            var icon = '<span id="edit-column-icon-' + column + '" class="icon-calendar2" style="cursor: pointer; display: inline-block; width: 20px; height: 20px; float: right; font-size: 20px"></span>';

                            return '<span style="margin: 0px 50px 0px 0px;">' + Uni.DateTime.formatDateTimeShort(new Date(value)) + '</span>' + icon;
                        } else {
                            var timeString,
                                valueToShow;

                            if (value) {
                                timeString = Uni.DateTime.formatDateTimeShort(new Date(value));
                            } else {
                                timeString = Uni.I18n.translate('general.infinite', 'IMT', 'Infinite');
                            }

                            if (record.get('conflictAtEnd') || record.get('conflictType') === 'RANGE_OVERLAP_DELETE') {
                                valueToShow = '<span style="color: #eb5642;">' + timeString + '</span>';
                            } else {
                                valueToShow = timeString;
                            }

                            return valueToShow;
                        }
                    }
                },
                {
                    header: Uni.I18n.translate('general.remark', 'IMT', 'Remark'),
                    dataIndex: 'message',
                    flex: 2,
                    renderer: function (value, meta, record) {
                        meta.style = 'color: #eb5642;';
                        if (record.get('editable')) {
                            meta.style += 'font-weight: bold;';
                        }
                        return value;
                    }
                },

                {
                    xtype: 'uni-actioncolumn',
                    itemId: 'custom-attribute-set-versions-grid-action-column',
                    privileges: Imt.privileges.MetrologyConfig.admin,
                    menu: {
                        xtype: 'versions-overlap-grid-action-menu',
                        store: me.getStore()
                    },
                    isDisabled: function(view, rowIndex, callIndex, item, record) {
                        item.menu.record = record;
                        //todo: code like this should be generalized in common component
                        var hasItems = item.menu.items.items.map(function(i){return i.isVisible()}).filter(function(i){return !!i}).length;
                        return !record.get('editable') || !hasItems;
                    }
                }
            ]
        };

        me.dockedItems = [
            {
                xtype: 'container',
                dock: 'top',
                margin: '0 0 10 0',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'container',
                        margin: '10 0 0 20',
                        flex: 1,
                        html: '<span style="color: #eb5642;">' + Uni.I18n.translate('customattributesetsversions.conflictedtoptext', 'IMT', 'There are conflicting versions') + '</span>'
                    },
                    {
                        text: Uni.I18n.translate('general.undo', 'IMT', 'Undo'),
                        itemId: 'overlap-panel-undo-btn',
                        xtype: 'button',
                        margin: 0,
                        hidden: true
                    }
                ]
            },
            {
                xtype: 'container',
                margin: '10 0 -10 20',
                dock: 'bottom',
                html: '<span style="color: #eb5642;">' + Uni.I18n.translate('customattributesetsversions.overlappanelbottomtext', 'IMT', 'Changes, made here, will take effect to other versions') + '</span>'
            }
        ];

        me.callParent(arguments);
        undoBtn = me.down('#overlap-panel-undo-btn');
        me.getStore().on('load', function () {
            me.getSelectionModel().select(0);
            if (me.getEl()) {
                if (me.getEl().down('#edit-column-icon-0')) {
                    me.getEl().down('#edit-column-icon-0').on('click', function () {
                        me.plugins[0].startEdit(me.recordToEdit, 0);
                        me.down('date-time[dataIndex=startTime]').down('#date-time-field-minutes').focus();
                    });
                }
                if (me.getEl().down('#edit-column-icon-1')) {
                    me.getEl().down('#edit-column-icon-1').on('click', function () {
                        me.plugins[0].startEdit(me.recordToEdit, 1);
                        me.down('date-time[dataIndex=endTime]').down('#date-time-field-minutes').focus();
                    });
                }
            }
        });
        me.on('select', function (selectionModel, record) {
            var id = record.get('startTime'),
                centerContainer = Ext.ComponentQuery.query('#centerContainer')[0],
                previewPanel = centerContainer.down('custom-attribute-set-versions-preview');

            if (!record.get('editable')) {
                previewPanel.show();
                if (!id) {
                    id = 0;
                }
                centerContainer.versionModel.load(id, {
                    success: function (record) {
                        previewPanel.loadRecord(record);
                    }
                });
            } else {
                previewPanel.hide();
            }
        });
        me.on('alignleft', function (timestamp) {
            var centerContainer = me.up('#centerContainer'),
                startDateField = centerContainer.down('#custom-attribute-set-version-start-date-field');

            Ext.suspendLayouts();
            centerContainer.suspendCheckVersion = true;
            startDateField.setValue(timestamp);
            centerContainer.suspendCheckVersion = false;
            undoBtn.show();
            Ext.resumeLayouts(true);
            centerContainer.checkRecord();
        });
        me.on('alignright', function (timestamp) {
            var centerContainer = me.up('#centerContainer'),
                endDateField = centerContainer.down('#custom-attribute-set-version-end-date-field');

            Ext.suspendLayouts();
            centerContainer.suspendCheckVersion = true;
            endDateField.setValue(timestamp);
            centerContainer.suspendCheckVersion = false;
            undoBtn.show();
            Ext.resumeLayouts(true);
            centerContainer.checkRecord();
        });
        me.on('edit', function (editor, e) {
            var centerContainer = me.up('#centerContainer'),
                startDateField = centerContainer.down('#custom-attribute-set-version-start-date-field'),
                endDateField = centerContainer.down('#custom-attribute-set-version-end-date-field');

            Ext.suspendLayouts();
            centerContainer.suspendCheckVersion = true;
            startDateField.setValue(e.record.get('startTime'));
            endDateField.setValue(e.record.get('endTime'));
            centerContainer.suspendCheckVersion = false;
            undoBtn.show();
            Ext.resumeLayouts(true);
            centerContainer.checkRecord();
        });
        undoBtn.on('click', function () {
            var centerContainer = me.up('#centerContainer'),
                startDateField = centerContainer.down('#custom-attribute-set-version-start-date-field'),
                endDateField = centerContainer.down('#custom-attribute-set-version-end-date-field');

            Ext.suspendLayouts();
            centerContainer.suspendCheckVersion = true;
            startDateField.setValue(centerContainer.savedStartDate);
            endDateField.setValue(centerContainer.savedEndDate);
            centerContainer.suspendCheckVersion = false;
            undoBtn.hide();
            Ext.resumeLayouts(true);
            centerContainer.checkRecord();
        });
    }
});
