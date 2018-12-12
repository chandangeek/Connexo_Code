/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.types.basic.ObisCode;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
public interface IReadWriteObject
{
  ObisCode getObisCode();
  void write(CosemApplicationLayer layer, Object data[]) throws IOException;
  Object read(CosemApplicationLayer layer) throws IOException;
}
