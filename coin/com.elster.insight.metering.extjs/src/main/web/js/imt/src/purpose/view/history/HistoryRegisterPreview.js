/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Imt.purpose.view.history.HistoryRegisterPreview', {
    extend: 'Imt.purpose.view.registers.RegisterDataPreview',
    alias: 'widget.history-register-preview',

    requires: [
        'Imt.purpose.view.history.HistoryValidationPreview'
    ],

    /**
     * @private
     * @override
     */
    getGeneralItems: function () {
        var me = this;

        return [
            me.getTime(),
            {
                fieldLabel: Uni.I18n.translate('general.lastUpdate', 'IMT', 'Changed On'),
                name: 'reportedDateTime',
                itemId: 'history-preview-changedOn-field',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                }
            },
            {
                fieldLabel: Uni.I18n.translate('historyGrid.changedBy', 'IMT', 'Changed by'),
                name: 'userName',
                itemId: 'history-preview-changedBy-field',
                htmlEncode: false
            },
            {
                fieldLabel: Uni.I18n.translate('general.value', 'IMT', 'Value'),
                name: 'value',
                itemId: 'register-preview-value-field',
                renderer: function(value){
                    return me.renderValueAndUnit(value);
                }
            },
            {
                fieldLabel: Uni.I18n.translate('general.formula', 'IMT', 'Formula'),
                itemId: 'register-preview-formula-field'
            }
        ];
    },


    /**
     * @override
     */
    getValidationItems: function () {
        return [
            {
                xtype:'history-validation-preview',
                router: this.router,
                fieldLabel: ''
            }
        ];
    },


    /**
     * @private
     */
    getTime: function(){
        var me = this;

        if ((me.output.get('deliverableType') === 'numerical' || me.output.get('deliverableType') === 'billing') &&
            (me.output.get('isCummulative') || me.output.get('isBilling'))) {
            return {
                fieldLabel: Uni.I18n.translate('general.measurementPeriod', 'IMT', 'Measurement period'),
                name: 'interval',
                itemId: 'register-preview-measurementPeriod-field',
                renderer: Imt.purpose.util.DataFormatter.formatIntervalLong
            };
        } else if (!me.output.get('hasEvent')) {
            return {
                fieldLabel: Uni.I18n.translate('general.measurementTime', 'IMT', 'Measurement time'),
                name: 'timeStamp',
                itemId: 'register-preview-measurementTime-field',
                renderer: Imt.purpose.util.DataFormatter.formatDateLong
            };
        }
    }
});