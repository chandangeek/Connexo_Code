/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.Textarea', {
  extend: 'Ext.panel.Panel',
  xtype: 'uni-search-internal-textarea',
  layout: 'fit',
  separator: ';',

  fieldMaxWidth: 600,
  lineHeight: 16,
  lines: 10,
  maxLength: 1900000,

  requires: [
    'Ext.form.field.TextArea'
  ],

  maxCount: 5000,

  getField: function() {
    return this.down('#filter-input');
  },

  isValid: function() {
      return this.getField().isValid();
  },

  validate: function() {
      return this.getField().validate();
  },

  setValue: function(value) {
    var me = this;

    if (!value) {
      value = [];
    }

    if(value instanceof Array) {
      this.getField().setValue(
          value.join(me.separator + ' ')
      );
    } else {
      this.getField().setValue(value);
    }
  },

  getValue: function() {
    var me = this;
    return this.getField().getValue().split(me.separator)
      .map(function(piece) {
        return piece.trim();
      });
  },

  reset: function() {
    this.getField().reset();
    this.fireEvent('reset', this);
  },

  onChange: function() {
    if (this.validateOnChange) {
        this.getField().validate();
    }

    this.fireEvent('change', this, this.getValue());
  },

  onPaste: function(event, el) {
    var me = this;
    var value = el.value;

    var paste = event.browserEvent.clipboardData
      .getData('text/plain')
      .trim()
      .replace(/\t/g, me.separator)
      .replace(/\r\n/g, me.separator)
      .replace(/\n/g, me.separator)
      .split(me.separator)
    ;

    paste.unshift(value.substring(0, el.selectionStart))
    paste.push(value.substring(el.selectionEnd, el.value.length))

    me.setValue(paste
      .map(function(piece) {
        return piece.trim();
      })
      .filter(Boolean)
    );

    const measure = Ext.util.TextMetrics.measure(
      el,
      paste.join(me.separator + ' '),
      me.fieldMaxWidth - 28
    );

    el.scrollTop = measure.height;

    event.preventDefault();
    event.stopPropagation();
  },

  initComponent: function() {
    var me = this;

    me.addEvents(
      "change",
      "reset"
    );

    me.items = {
      xtype: 'textarea',
      itemId: 'filter-input',
      minWidth: me.fieldMaxWidth,
      height: (me.lines * me.lineHeight) + 8, // 8 - padding
      grow: false,
      maxLength: me.maxLength,
      maxLengthText: Uni.I18n.translate('search.field.internal.textarea.maxSymbLimit','UNI','Maximum symbol limit reached ({0})', [me.maxLength]),
      allowBlank: !me.isFilterField,
      validateOnBlur: false,
      validator: function() {
        if (me.getValue().length < me.maxCount) {
          return true
        }

        return Uni.I18n.translate(
          'search.field.internal.textarea.maxCount',
          'UNI',
          'Maximum item limit reached ({0})',
          [me.maxCount]
        );
      },
      listeners: {
        change: {
            fn: me.onChange,
            scope: me
        }
      }
    };

    me.dockedItems = {
      xtype: 'toolbar',
      dock: 'right',
      width: 24,
      items: [{
        xtype: 'button',
        layout: 'fit',
        tooltip: {
          title: Uni.I18n.translate(
            'search.field.internal.textarea.header',
            'UNI',
            'Search field formatting'
          ),
          text: Uni.I18n.translate(
            'search.field.internal.textarea.text',
            'UNI',
            'Multiple values can be inserted, divided by separator<br /> {0} - Item separator',
            [me.separator]
          ),
          maxWidth: 150
        },
        iconCls: 'uni-icon-info-small',
        cls: 'uni-btn-transparent',
        margin: '5 10 5 5'
      }]
    }

    me.callParent(arguments);

    me.on('afterrender', function() {
      me.mon(me.getField().inputEl, 'paste', me.onPaste, me);
    })
  },
});