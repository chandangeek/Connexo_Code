/**
 * @class Uni.form.field.Coordinates
 */
Ext.define('Uni.form.field.Coordinates', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.coordinates',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    layout: 'vbox',
    requires: [
        'Ext.container.Container'
    ],

    initComponent: function () {
        var me = this,
            noCoordinate = {
                xtype: 'displayfield',
                itemId: 'no-coordinate',
                value: Uni.I18n.translate('coordinate.noInput', 'UNI', 'No coordinates input'),
                fieldStyle: {
                    fontStyle: 'italic',
                    color: '#999'
                },
                width: '100%'
            },
            displayCoordinateLat = {
                xtype: 'displayfield',
                itemId: 'coordinate-lat',
                width: '40%'
            },
            displayCoordinateLong = {
                xtype: 'displayfield',
                itemId: 'coordinate-long',
                width: '40%'
            },
            displayCoordinateElev = {
                xtype: 'displayfield',
                itemId: 'coordinate-elev',
                width: '20%'
            },
            displayContainer = {
                xtype: 'container',
                itemId: 'ctn-coordinate',
                hidden: true,
                width: '100%',
                layout: {
                    type: 'hbox',
                    align: 'middle'
                },
                items: [displayCoordinateLat, displayCoordinateLong, displayCoordinateElev]
            },
            textCoordinateLat = {
                xtype: 'numberfield',
                hideTrigger: true,
                itemId: 'txt-coordinate-lat',
                width: '35%',
                decimalPrecision: 4,
                minValue: -90,
                maxValue: 90,
                listeners: {
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    },
                    blur: {
                        fn: me.recurrenceNumberFieldValidation,
                        scope: me
                    }
                }
            },
            textCoordinateLong = {
                xtype: 'numberfield',
                hideTrigger: true,
                itemId: 'txt-coordinate-long',
                width: '35%',
                decimalPrecision: 4,
                minValue: -180,
                maxValue: 180,
                listeners: {
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    },
                    blur: {
                        fn: me.recurrenceNumberFieldValidation,
                        scope: me
                    }
                }
            },
            textCoordinateElev = {
                xtype: 'numberfield',
                hideTrigger: true,
                itemId: 'txt-coordinate-elev',
                width: '20%',
                decimalPrecision: 3,
                minValue: -10000,
                maxValue: 10000,
                listeners: {
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    },
                    blur: {
                        fn: me.recurrenceNumberFieldValidation,
                        scope: me
                    }
                }
            },
            coordinateSeparator = {
                xtype: 'component',
                html: ':',
                width: '4%',
                margin: '5 0 0 6'
            },
            textNote = {
                xtype: 'displayfield',
                value: Uni.I18n.translate('coordinate.note', 'UNI', 'Edited coordinate values will be displayed also in degrees, minutes, seconds format'),
                fieldStyle: {
                    fontStyle: 'italic',
                    color: '#999'
                },
                width: '100%'
            },
            textContainer = {
                xtype: 'container',
                width: '100%',
                layout: {
                    type: 'hbox',
                    align: 'top'
                },
                items: [textCoordinateLat, coordinateSeparator, textCoordinateLong, coordinateSeparator, textCoordinateElev]
            };
        me.items = [noCoordinate, displayContainer, textContainer, textNote];

        me.callParent(arguments);
        if (me.value) {
            me.setValue(me.value);
        }
    },

    onItemChange: function (field, newValue, oldValue) {
        var me = this,
            latTextField = me.down('#txt-coordinate-lat'),
            longTextField = me.down('#txt-coordinate-long'),
            elevTextField = me.down('#txt-coordinate-elev'),
            latField = me.down('#coordinate-lat'),
            longField = me.down('#coordinate-long'),
            elevField = me.down('#coordinate-elev'),
            isNoCoordinateVisible = (latTextField.getValue() == null) && (longTextField.getValue() == null) && (elevTextField.getValue() == null);

        me.down('#no-coordinate').setVisible(isNoCoordinateVisible);
        me.down('#ctn-coordinate').setVisible(!isNoCoordinateVisible);
        if (field.getValue() != null) {
            if (field.itemId == 'txt-coordinate-lat') {
                latField.setValue(me.convertDDToDMS(field.getValue(), 'lat'));
            }
            else if (field.itemId == 'txt-coordinate-long') {
                longField.setValue(me.convertDDToDMS(field.getValue(), 'long'));
            }
            else if (field.itemId == 'txt-coordinate-elev') {
                elevField.setValue(Ext.String.format('{0} {1}', field.getValue(), Uni.I18n.translate('coordinates.elevationUnit', 'UNI', 'm')));
            }
        }
        else {
            if (field.itemId == 'txt-coordinate-lat') {
                latField.setValue('');
            }
            else if (field.itemId == 'txt-coordinate-long') {
                longField.setValue('');
            }
            else if (field.itemId == 'txt-coordinate-elev') {
                elevField.setValue('');
            }
        }
    },

    setValue: function (value) {
        var me = this,
            latTextField = me.down('#txt-coordinate-lat'),
            longTextField = me.down('#txt-coordinate-long'),
            elevTextField = me.down('#txt-coordinate-elev'),
            values;

        if ((value == null) || (value.spatialCoordinates == null) || (value.spatialCoordinates.length == 0)) {
            me.down('#ctn-coordinate').setVisible(false);
            me.down('#no-coordinate').setVisible(true);

            return;
        }
        else {
            me.down('#ctn-coordinate').setVisible(true);
            me.down('#no-coordinate').setVisible(false);
        }
        values = value.spatialCoordinates.split(':');
        if (values.length > 0) {
            latTextField.setValue(values[0]);
        }

        if (values.length > 1) {
            longTextField.setValue(values[1]);
        }

        if (values.length > 1) {
            elevTextField.setValue(values[2]);
        }
    },

    getValue: function () {
        var me = this,
            latTextField = me.down('#txt-coordinate-lat'),
            longTextField = me.down('#txt-coordinate-long'),
            elevTextField = me.down('#txt-coordinate-elev');

        return {
            spatialCoordinates: (latTextField.getValue() == null && longTextField.getValue() == null && elevTextField.getValue() == null) ? null :
                Ext.String.format('{0}:{1}:{2}', latTextField.getValue(), longTextField.getValue(), elevTextField.getValue()),
            coordinatesDisplay: null
        };
    },

    convertDDToDMS: function (value, type) {
        var sign = 1, Abs = 0;
        var days, minutes, secounds, direction;

        if (value < 0) {
            sign = -1;
        }
        Abs = Math.abs(Math.round(value * 1000000.));

        if (type == "lat" && Abs > (90 * 1000000)) {
            return false;
        } else if (type == "long" && Abs > (180 * 1000000)) {
            return false;
        }

        days = Math.floor(Abs / 1000000);
        minutes = Math.floor(((Abs / 1000000) - days) * 60);
        secounds = ( Math.floor((( ((Abs / 1000000) - days) * 60) - minutes) * 100000) * 60 / 100000 );
        secounds = Math.round(secounds * 100) / 100
        days = days * sign;

        if (type == 'lat') {
            direction = days < 0 ? 'S' : 'N';
        }
        if (type == 'long') {
            direction = days < 0 ? 'W' : 'E'
        }

        if (isNaN(days) || isNaN(minutes) || isNaN(secounds)) {
            return '';
        }
        return (days * sign) + 'º ' + minutes + "' " + secounds + "'' " + direction;
    },

    markInvalid: function (fields) {
        this.down('#txt-coordinate-long').markInvalid('');
        this.down('#txt-coordinate-elev').markInvalid('');
        this.down('#txt-coordinate-lat').markInvalid(fields);
    },

    recurrenceNumberFieldValidation: function (field) {
        var value = field.getValue();

        if (value < field.minValue) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    }
});