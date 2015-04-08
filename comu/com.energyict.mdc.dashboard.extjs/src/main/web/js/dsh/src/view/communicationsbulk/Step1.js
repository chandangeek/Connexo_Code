Ext.define('Dsh.view.communicationsbulk.Step1', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.view.grid.BulkSelection'
    ],
    alias: 'widget.communications-bulk-step1',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'step1-error-message',
                width: 400,
                hidden: true
            },
            {
                xtype: 'bulk-selection-grid',
                itemId: 'communications-bulk-selection-grid',
                store: 'Dsh.store.CommunicationTasksBuffered',

                counterTextFn: function (count) {
                    return Uni.I18n.translatePlural(
                        'communication.bulk.counterText',
                        count,
                        'DSH',
                        '{0} communications selected'
                    );
                },

                allLabel: Uni.I18n.translate('communication.bulk.allLabel', 'DSH', 'All communications'),
                allDescription: Uni.I18n.translate('communication.bulk.allDescription', 'DSH', 'Select all communications (related to filters and grouping on the communications  screen)'),

                selectedLabel: Uni.I18n.translate('communication.bulk.selectedLabel', 'DSH', 'Selected communications'),
                selectedDescription: Uni.I18n.translate('communication.bulk.selectedDescription', 'DSH', 'Select communications in table'),

                bottomToolbarHidden: true,

                radioGroupName: 'selected-communications',

                columns: [
                    {
                        itemId: 'name',
                        text: Uni.I18n.translate('communication.widget.details.commmunication', 'DSH', 'Communication'),
                        dataIndex: 'name',
                        flex: 2
                    },
                    {
                        itemId: 'device',
                        text: Uni.I18n.translate('communication.widget.details.device', 'DSH', 'Device'),
                        dataIndex: 'device',
                        flex: 1,
                        renderer: function (val) {
                            return val.name ? val.name : '';
                        }
                    },
                    {
                        itemId: 'currentState',
                        text: Uni.I18n.translate('communication.widget.details.currentState', 'DSH', 'Current state'),
                        dataIndex: 'currentState',
                        flex: 1,
                        renderer: function (val) {
                            return val.displayValue ? val.displayValue : '';
                        }
                    },
                    {
                        itemId: 'latestResult',
                        text: Uni.I18n.translate('connection.widget.details.latestResult', 'DSH', 'Latest result'),
                        dataIndex: 'latestResult',
                        flex: 1,
                        renderer: function (val) {
                            return val.displayValue ? val.displayValue : '';
                        }
                    },
                    {
                        itemId: 'nextCommunication',
                        text: Uni.I18n.translate('communication.widget.details.nextCommunication', 'DSH', 'Next communication'),
                        dataIndex: 'nextCommunication',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                        },
                        flex: 2
                    },
                    {
                        itemId: 'startTime',
                        text: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Started on'),
                        dataIndex: 'startTime',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                        },
                        flex: 2
                    },
                    {
                        itemId: 'successfulFinishTime',
                        text: Uni.I18n.translate('communication.widget.details.finishedOn', 'DSH', 'Finished successfully on'),
                        dataIndex: 'successfulFinishTime',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                        },
                        flex: 2
                    }
                ]
            },
            {
                xtype: 'component',
                itemId: 'selection-grid-error',
                cls: 'x-form-invalid-under',
                margin: '-30 0 0 0',
                html: Uni.I18n.translate('communication.bulk.selectionGridError', 'DSH', 'Select at least one communication'),
                hidden: true
            }
        ];

        me.callParent(arguments);
    }
});