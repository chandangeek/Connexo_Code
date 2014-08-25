Ext.define('Mdc.widget.DateTimeField', {
    extend: 'Ext.form.FieldContainer',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.dateTimeField',
    layout: 'hbox',
    msgTarget: 'under',
    submitFormat: 'c',
    flex: 1,
    dateCfg: null,
    hourCfg:null,
    minuteCfg:null,

    initComponent: function () {
        var me = this;
        if (!me.dateCfg) {
            me.dateCfg = {};
        }
        if (!me.hourCfg) {
            me.hourCfg = {};
        }
        if (!me.minuteCfg) {
            me.minuteCfg = {};
        }
        me.buildField();
        me.callParent();
        me.dateField = me.down('#date');
        me.separator = me.down('#separator');
        me.hourField = me.down('#hourField');
        me.minuteField = me.down('#minuteField');
        me.initField();
    },

    //@private
    buildField: function () {
        var me = this;
        me.items = [
            Ext.apply({
                xtype: 'datefield',
                itemId: 'date',
                format: 'd/m/Y',
                altFormats: 'd.m.Y|d m Y',
                margin: '0 5 0 0',
                required: true,
                submitValue: false
            },me.dateCfg),
            Ext.apply({
                xtype: 'displayfield',
                itemId: 'separator',
                value: Uni.I18n.translate('datetimefield.at', 'UNI', 'at'),
                submitValue: false,
                msgTarget: 'none',
                margin: '0 5 0 10'
            }),
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'hourField',
//                valueToRaw: function (value)
//                {
//                    return (value < 10 ? '0' : '') + value;
//                },
                maxValue: 23,
                minValue: 0,
                submitValue: false,
                margin: '0 5 0 0'
            }, me.hourCfg),
            Ext.apply({
                xtype: 'displayfield',
                value: ':',
                submitValue: false,
                msgTarget: 'none',
                margin: '0 5 0 0'
            }),
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'minuteField',
//                valueToRaw: function (value)
//                {
//                    return (value < 10 ? '0' : '') + value;
//                },
                maxValue: 59,
                minValue: 0,
                submitValue: false,
                margin: '0 5 0 0'
            }, me.minuteCfg)

        ]
    },


    getValue: function () {
        if(this.dateField.getValue()!== null &&  this.dateField.getValue()!== undefined){
            var date =  this.dateField.getValue();
            date.setHours(this.hourField.getValue() || 0);
            date.setMinutes(this.minuteField.getValue() || 0);
            return date.getTime();
        }

    },

    setValue: function (value) {
        if(value !== undefined){
            var date = new Date(value);
            this.dateField.setValue(date);
            this.hourField.setValue(date.getHours());
            this.minuteField.setValue(date.getMinutes());
        } else {
            this.dateField.reset();
            this.hourField.reset();
            this.minuteField.reset();
        }
    },

    getSubmitData: function () {
        var me = this,
            data = null;
        if (!me.disabled && me.submitValue && !me.isFileUpload()) {
            data = {};
            value = me.getValue();
            data[me.getName()] = '' + value ? value : null;
        }
        return data;
    },

    markInvalid: function(fields){
        this.eachItem(function(field){
            field.markInvalid('');
        });
        this.items.items[0].markInvalid(fields);
    },

    eachItem: function(fn, scope) {
        if(this.items && this.items.each){
            this.items.each(fn, scope || this);
        }
    }
});

