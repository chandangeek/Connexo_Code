/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.DataTable', {
  extend: 'Ext.form.field.TextArea',
  xtype: 'uni-search-internal-datatable',
  // layout: 'fit',
  minWidth: 500,
  grow: true,
  minGrow: 2,
  maxGrow: 10,
  separator: ';',
  maxLength: 500,

  getField: function() {
    return this;
  },

  isValid: function() {
    return this.callParent(arguments);
  },

  validate: function() {
    return this.callParent(arguments);
  },

  setValue: function(value) {
    this.callParent([value]);
  },

  getValue: function() {
    const value = this.callParent(arguments);

    return value.split(';');
  },

  reset: function() {
    // this.reset();
    this.fireEvent('reset', this);
  },

  onChange: function() {
    if (this.validateOnChange) {
      this.validate();
    }

    this.fireEvent('change', this, this.getValue());
  },

// initComponent: function () {
//     var me = this;
//     me.items = [
//         Ext.apply({
//             xtype: 'numberfield',
//             itemId: 'filter-input',
//             width: 180,
//             maxValue: Number.MAX_SAFE_INTEGER,
//             minValue: 0,
//             maxLength: 15,
//             allowBlank: !me.isFilterField,
//             allowExponential: false,
//             enforceMaxLength: true,
//             validateOnBlur: false,
//             margin: '0 5 0 0',
//             listeners: {
//                 change:{
//                     fn: me.onChange,
//                     scope: me
//                 }
//             }
//         }, me.itemsDefaultConfig)
//     ];

//     me.callParent(arguments);
// }

  onPaste: function(event) {
    var me = this;

    var paste = event.browserEvent.clipboardData
      .getData('text/plain')
      .trim()
      .replace(/\t/g, me.separator)
      .replace(/\r\n/g, me.separator)
      .replace(/\n/g, me.separator)
      .split(me.separator)
      .map(function(piece) {
        return piece.trim();
      })
      .join(me.separator)
    ;

    this.setValue(paste);
    event.preventDefault();
    event.stopPropagation();
  },

  initComponent: function() {
    var me = this;

    me.addEvents(
      "change",
      "reset"
    );

    me.callParent(arguments);

    me.on('afterrender', function() {
      me.mon(me.inputEl, 'paste', me.onPaste, me);
    })
  },
});