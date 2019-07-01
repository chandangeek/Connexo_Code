/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.data.reader.StringArray', {
  extend: 'Ext.data.reader.Json',
  alias : 'reader.stringArray',

  createFieldAccessExpression: function(field, fieldVarName, dataName) {
    var result;

    if (field.mapping && typeof field.mapping === 'function') {
        result = fieldVarName + '.mapping(' + dataName + ', this)';
    } else {
        result = dataName;
    }

    return result;
  }
});
