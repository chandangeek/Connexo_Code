/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idv.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Clipboard',
        'Idv.store.NonEstimatedDataStore',
        'Mdc.store.Estimators'
    ],

    models: [
        'Idv.model.Issue',
        'Idv.model.DeviceChannelDataSaveEstimate'
    ],

    views: [
        'Idv.view.Detail',
        'Idv.view.NonEstimatedDataGrid',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.devicechannels.ReadingEstimationWindow'
    ],

    constructor: function () {
        var me = this;

        me.refs = [
            {
                ref: 'page',
                selector: 'data-validation-issue-detail'
            },
            {
                ref: 'detailForm',
                selector: 'data-validation-issue-detail data-validation-issue-detail-form'
            },
            {
                ref: 'commentsPanel',
                selector: 'data-validation-issue-detail #data-validation-issue-comments'
            },
            {
                ref: 'readingEstimationWindow',
                selector: '#reading-estimation-window'
            },
            {
                ref: 'noEstimatedDataGrid',
                selector: '#validation-no-estimated-data-grid'
            },
            {
                ref: 'issueDetailForm',
                selector: 'data-validation-issue-detail #issue-detail-form'
            },
            {
                ref: 'actionMenu',
                selector: 'data-validation-issue-detail #issues-action-menu'
            }
        ];
        me.callParent(arguments);
    },

    itemUrl: '/api/isu/issues/',

    init: function () {
        this.control({
            'data-validation-issue-detail #data-validation-issue-comments #issue-comments-add-comment-button': {
                click: this.showCommentFormValidation
            },
            'data-validation-issue-detail #data-validation-issue-comments #empty-message-add-comment-button': {
                click: this.showCommentFormValidation
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-comment-cancel-adding-button': {
                click: this.hideCommentForm
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-comment-save-button': {
                click: this.addCommentValidation
            },
            'data-validation-issue-detail #data-validation-issue-comments #issue-add-comment-area': {
                change: this.validateCommentForm
            },
            'data-validation-issue-detail #issue-detail-action-menu': {
                click: this.chooseAction
            },
            'no-estimated-data-grid uni-actioncolumn': {
                viewData: function(record) {
                    var me = this;
                    if (record.get('registerId')) {
                        this.getController('Uni.controller.history.Router').getRoute('devices/device/registers/registerdata').forward({
                            deviceId: me.getDetailForm().getRecord().get('device').name,
                            channelId: record.get('registerId')
                        });
                    } else if(record.get('channelId')) {
                        this.getController('Uni.controller.history.Router').getRoute('devices/device/channels/channelvalidationblocks').forward(
                            {
                                deviceId: me.getDetailForm().getRecord().get('device').name,
                                channelId: record.get('channelId'),
                                issueId: record.getId()
                            },
                            {
                                validationBlock: record.get('startTime')
                            }
                        );
                    }
                },
                estimateValues: this.estimateValue
            },
            '#reading-estimation-window #estimate-reading-button': {
                click: this.estimateReading
            }
        });
    },

    estimateValue: function (record) {
        var me = this,
            bothSuspected = false;

        me.getStore('Mdc.store.Estimators').load(function () {
            me.getPage().setLoading(false);
            Ext.widget('reading-estimation-window', {
                itemId: 'reading-estimation-window',
                record: record,
                bothSuspected: bothSuspected
            }).show();
        });
    },

    estimateReading: function () {
        var me = this,
            propertyForm = me.getReadingEstimationWindow().down('#property-form'),
            model = Ext.create('Idv.model.DeviceChannelDataSaveEstimate'),
            estimateBulk = false,
            record = me.getReadingEstimationWindow().record,
            intervalsArray = [];

        !me.getReadingEstimationWindow().down('#form-errors').isHidden() && me.getReadingEstimationWindow().down('#form-errors').hide();
        !me.getReadingEstimationWindow().down('#error-label').isHidden() && me.getReadingEstimationWindow().down('#error-label').hide();
        propertyForm.clearInvalid();

        model.beginEdit();
        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            model.set('estimatorImpl', me.getReadingEstimationWindow().down('#estimator-field').getValue());
            model.propertiesStore = propertyForm.getRecord().properties();
        }
        if (!me.getReadingEstimationWindow().down('#value-to-estimate-radio-group').isHidden()) {
            estimateBulk = me.getReadingEstimationWindow().down('#value-to-estimate-radio-group').getValue().isBulk;
        } else {
            if (!Ext.isArray(record)) {
                estimateBulk = record.get('readingType') && (record.get('readingType').isCumulative);
            } else {
                Ext.Array.findBy(record, function (item) {
                    estimateBulk = item.get('readingType') && (item.get('readingType').isCumulative);
                    return estimateBulk;
                });
            }
        }
        if (!Ext.isArray(record)) {
            intervalsArray.push({
                start: record.get('startTime'),
                end: record.get('endTime')
            });
        } else {
            Ext.Array.each(record, function (item) {
                intervalsArray.push({
                    start: record.get('startTime'),
                    end: record.get('endTime')
                });
            });
        }
        model.set('estimateBulk', estimateBulk);
        model.set('intervals', intervalsArray);
        model.set('readingType', record.get('readingType'));
        model.endEdit();
        me.saveChannelDataEstimateModel(model, record);
    },

    saveChannelDataEstimateModel: function (record, readings) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        record.getProxy().extraParams = {
            deviceId: me.getDetailForm().getRecord().get('device').name,
            channelId: readings.get('channelId')
        };

        me.getReadingEstimationWindow().setLoading();
        Ext.Ajax.suspendEvent('requestexception');
        record.save({
            callback: function (rec, operation, success) {
                Ext.Ajax.resumeEvent('requestexception');
                var responseText = Ext.decode(operation.response.responseText, true);
                Ext.suspendLayouts();

                if (success) {
                    me.getReadingEstimationWindow().destroy();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.estimateSucceeded', 'IDV', 'Estimate values succeeded'));
                    me.refreshGrid(me.getPage());
                } else {
                    me.getReadingEstimationWindow().setLoading(false);
                    if (responseText) {
                        if (responseText.message) {
                            me.getReadingEstimationWindow().down('#error-label').show();
                            me.getReadingEstimationWindow().down('#error-label').setText('<div style="color: #EB5642">' + responseText.message + '</div>', false);
                        } else if (responseText.errors) {
                            me.getReadingEstimationWindow().down('#form-errors').show();
                            me.getReadingEstimationWindow().down('#property-form').markInvalid(responseText.errors);
                        }
                    }
                    else {
                        me.getReadingEstimationWindow().down('#error-label').show();
                        me.getReadingEstimationWindow().down('#error-label').setText('<div style="color: #EB5642">' +
                            Uni.I18n.translate('devicechannels.saveEstimationErrorMessage', 'IDV', 'Could not estimate with {0}',
                                me.getReadingEstimationWindow().down('#estimator-field').getRawValue().toLowerCase()) + '</div>', false);

                    }

                }
                Ext.resumeLayouts(true);
            }
        });
    }
});