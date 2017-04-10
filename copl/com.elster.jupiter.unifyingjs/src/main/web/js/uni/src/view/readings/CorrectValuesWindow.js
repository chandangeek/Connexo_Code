/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.readings.CorrectValuesWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.correct-values-window',
    modal: true,
    title: Uni.I18n.translate('correct.window.correct', 'UNI', 'Correct'),
    /**
     * @cfg {Object/Array}
     *
     * Selected readings.
     */
    record: null,

    /**
     * @cfg {Boolean}
     *
     * True to show info message on top.
     */
    showInfoMessage: false,
    /**
     * @cfg {String}
     *
     * Text of info message.
     */
    infoMessageText: Uni.I18n.translate('correct.window.info.message', 'UNI', 'The correction will be applied to calculated value'),

    requires: [
        'Uni.util.FormEmptyMessage',
        'Uni.model.readings.ReadingCorrection',
        'Uni.util.ReadingEditor'
    ],


    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'correct-values-window-form',
            padding: 0,
            defaults: {
                width: 503,
                labelWidth: 200
            },
            items: [
                {
                    xtype: 'uni-form-empty-message',
                    text: me.infoMessageText,
                    hidden: !me.showInfoMessage,
                    margin: '7 0 16 0'
                },
                {
                    xtype: 'radiogroup',
                    fieldLabel: Uni.I18n.translate('correct.window.type', 'UNI', 'Type'),
                    itemId: 'correct-values-window-form-radiogroup',
                    required: true,
                    name: 'type',
                    vertical: true,
                    columns: 1,
                    items: [
                        {
                            boxLabel: Uni.I18n.translate('correct.window.multiply', 'UNI', 'Multiply'),
                            name: 'type',
                            itemId: 'multiply-radio',
                            checked: true,
                            inputValue: 'MULTIPLY'
                        },
                        {
                            boxLabel: Uni.I18n.translate('correct.window.add', 'UNI', 'Add'),
                            name: 'type',
                            itemId: 'add-radio',
                            inputValue: 'ADD'
                        },
                        {
                            boxLabel: Uni.I18n.translate('correct.window.subtract', 'UNI', 'Subtract'),
                            name: 'type',
                            itemId: 'substract-radio',
                            inputValue: 'SUBTRACT'
                        }
                    ],
                    listeners: {
                        change: function (rdg, newValue, oldValue, eOpts) {
                            switch (newValue.type){
                                //TODO: add map?
                                case 'MULTIPLY': {
                                    me.getAmountField().setValue(1);
                                } break;
                                case 'ADD': {
                                    me.getAmountField().setValue(0);
                                } break;
                                case 'SUBTRACT': {
                                    me.getAmountField().setValue(0);
                                } break;
                            }
                        }
                    }
                },
                {
                    xtype: 'numberfield',
                    fieldLabel: Uni.I18n.translate('correct.window.correctionAmount', 'UNI', 'Correction amount'),
                    required: true,
                    name: 'amount',
                    value: 1,
                    minValue: 0,
                    itemId: 'correction-amount-number-fld'
                },
                {
                    xtype: 'checkbox',
                    fieldLabel: Uni.I18n.translate('correct.window.limitValues', 'UNI', 'Limit values'),
                    boxLabel: Uni.I18n.translate('correct.window.correctionAmount', 'UNI', 'Only suspect and estimated values'),
                    itemId: 'limit-checkbox',
                    name: 'onlySuspectOrEstimated'
                },
                {
                    xtype: 'checkbox',
                    fieldLabel: Uni.I18n.translate('correct.window.projectedValues', 'UNI', 'Projected value'),
                    boxLabel: Uni.I18n.translate('correct.window.projectedValues.markValues', 'UNI', 'Mark values as projected'),
                    itemId: 'projected-checkbox',
                    name: 'projected',
                    disabled: true
                },
                {
                    xtype: 'combobox',
                    fieldLabel: Uni.I18n.translate('correct.window.ectimationComment', 'UNI', 'Estimation comment'),
                    name: 'estimationComment',
                    disabled: true
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'correct-reading-button',
                            text: Uni.I18n.translate('correct.window.correct', 'UNI', 'Correct'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-button',
                            text: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
                            ui: 'link',
                            handler: function () {
                                me.close();
                            }
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    },

    getAmountField: function(){
        return this.down('#correction-amount-number-fld');
    },

    updateRecord: function(record){
        this.down('#correct-values-window-form').updateRecord(record);
    },

    calculateValues: function(correctionModel){


        Ext.Array.each(correctionModel.get('intervals'), function(interval){
            interval.value = Uni.util.ReadingEditor.valueCorrector(interval.value, correctionModel.get('type'), correctionModel.get('amount'))
        });
        // return correctionModel.get('intervals').map(function(interval){
        //     return Uni.util.ReadingEditor.valueCorrector(interval.value, correctionModel.get('type'), correctionModel.get('amount'));
        // });
    }
});