Ext.define('Uni.property.view.property.Time', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'container',
            layout: 'hbox',
            name: this.getName(),
            width: me.width,
            required: me.required,
            items: [
                {
                    xtype: 'numberfield',
                    itemId: me.key + 'hourField',
                    readOnly: me.isReadOnly,
                    width: me.width / 2 - 5,
                    valueToRaw: function (value) {
                        if (!Ext.isDefined(value)) {
                            return null;
                        }
                        value = value || 0;
                        return (value < 10 ? '0' : '') + value;
                    },
                    maxValue: 23,
                    minValue: 0,
                    allowDecimals: false,
                    submitValue: false,
                    margin: '0 3 0 0'
                },
                {
                    xtype: 'label',
                    text: ':',
                    margin: '5 0 0 0'
                },
                {
                    xtype: 'numberfield',
                    itemId: me.key + 'minuteField',
                    readOnly: me.isReadOnly,
                    width: me.width / 2 - 5,
                    valueToRaw: function (value) {
                        if (!Ext.isDefined(value)) {
                            return null;
                        }
                        value = value || 0;
                        return (value < 10 ? '0' : '') + value;
                    },
                    maxValue: 59,
                    minValue: 0,
                    allowDecimals: false,
                    submitValue: false,
                    margin: '0 0 0 3'
                }
            ]
        };
    },

    getHoursField: function () {
        return this.down('#' + this.key + 'hourField');
    },

    getMinutesField: function () {
        return this.down('#' + this.key + 'minuteField');
    },

    getField: function () {
        return this;
    },

    setValue: function (value) {
        var hours = 0, minutes = 0, doSet = false;
        if (value === '') {
            doSet = true;
        } else if (value !== null && value !== '') {
            value = new Date(value * 1000);
            hours = value.getHours();
            minutes = value.getMinutes();
            doSet = true;
        }
        if (doSet) {
            if (!this.isEdit) {
                this.callParent([(hours < 10 ? '0' : '') + hours + ':' + (minutes < 10 ? '0' : '') + minutes ]);
            } else {
                this.getHoursField().setValue(hours);
                this.getMinutesField().setValue(minutes);
            }
        }
    },

    getValue: function () {
        var hourValue = this.getHoursField().getValue(),
            minValue = this.getMinutesField().getValue();
        if (hourValue !== null && hourValue !== '' && minValue !== null && minValue !== '') {
            var newDate = new Date(1970, 0, 1, hourValue, minValue, 0, 0);
            return newDate.getTime() / 1000;
        }
        return null;
    }
    ,
    initListeners: function () {
        var me = this;
        var hoursField = me.getHoursField();
        var minutesField = me.getMinutesField();

        if (hoursField) {
            hoursField.on('change', function () {
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
                if (hoursField.getValue() === null || hoursField.getValue() === '') {
                    me.getProperty().set('hasValue', false);
                    me.getProperty().set('propertyHasValue', false);
                }
                me.customHandlerLogic();
            });
            hoursField.on('blur', function () {
                if (!hoursField.hasNotValueSameAsDefaultMessage && hoursField.getValue() !== '' &&
                    !me.getProperty().get('isInheritedOrDefaultValue') && hoursField.getValue() === me.getProperty().get('default')) {
                    me.showPopupEnteredValueEqualsInheritedValue(hoursField, me.getProperty());
                }
                if (hoursField.getValue() === ''  && hoursField.getValue() === me.getProperty().get('default')) {
                    debugger;
                    me.getProperty().set('isInheritedOrDefaultValue', true);
                    me.updateResetButton();
                }
                me.customHandlerLogic();
            })
        }

        if (minutesField) {
            minutesField.on('change', function () {
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
                if (minutesField.getValue() === null || minutesField.getValue() === '') {
                    me.getProperty().set('hasValue', false);
                    me.getProperty().set('propertyHasValue', false);
                }
                me.customHandlerLogic();
            });
            minutesField.on('blur', function () {
                if (!minutesField.hasNotValueSameAsDefaultMessage && minutesField.getValue() !== '' &&
                    !me.getProperty().get('isInheritedOrDefaultValue') && minutesField.getValue() === me.getProperty().get('default')) {
                    me.showPopupEnteredValueEqualsInheritedValue(minutesField, me.getProperty());
                }
                if (minutesField.getValue() === ''  && minutesField.getValue() === me.getProperty().get('default')) {
                    debugger;
                    me.getProperty().set('isInheritedOrDefaultValue', true);
                    me.updateResetButton();
                }
                me.customHandlerLogic();
            })
        }
        this.getResetButton().setHandler(this.restoreDefault, this);
    }

});