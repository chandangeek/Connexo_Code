/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerDecoderMappedCollection.java $
 * Version:     
 * $Id: BerDecoderMappedCollection.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 15:54:06
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValue;
import java.util.HashMap;
import java.util.Map;
import static com.elster.ber.coding.BerIds.*;

/**
 * Decoder which uses an id map to decode collections.<P>
 * The default constructor adds the "universal" type to this map. Special mappings
 * can be added by {@link #addMapping(com.elster.ber.types.BerId, com.elster.ber.coding.BerDecoderBase)}
 *
 * @author osse
 */
public class BerDecoderMappedCollection extends BerDecoderCollection
{
  private BerDecoder defaultCoder = null; // = new BerDecoderUnknown();
  private BerDecoder defaultDecoderConstructed = null; // = new BerCodingDefaultCollection();
  private final Map<BerId, BerDecoder> idMap;

  private static Map<BerId, BerDecoder> createUniversalMap()
  {
    Map<BerId, BerDecoder> result =
            new HashMap<BerId, BerDecoder>();

    result.put(ID_BITSTRING, new BerDecoderBitString());
    result.put(ID_GRAPHICSTRING, new BerDecoderGraphicString());
    result.put(ID_INT, new BerDecoderInt());
    result.put(ID_OCTETSTRING, new BerDecoderOctetString());
    result.put(ID_OID, new BerDecoderObjectIdentifer());

    return result;
  }

  private static BerDecoderMappedCollection createDefaultConstructedCoder()
  {
    BerDecoderMappedCollection result = new BerDecoderMappedCollection(new BerDecoderUnknown(), null,
                                                                       createUniversalMap());
    result.defaultDecoderConstructed = result;
    return result;
  }

  private BerDecoderMappedCollection(BerDecoder defaultCoder, BerDecoder defaultCoderConstructed,
                                     Map<BerId, BerDecoder> idMap)
  {
    super();
    this.defaultCoder = defaultCoder;
    this.defaultDecoderConstructed = defaultCoderConstructed;
    this.idMap = idMap;
  }

  /**
   * Sole constructor<P>
   * Constructs the collection with universal mappings and an default constructed decoder.
   *
   */
  public BerDecoderMappedCollection()
  {
    this(true, true);
  }

  /**
   * Constructor.
   * 
   * @param addUniversalTypes Add the universal types to the map.
   * @param addDefaultDecoders Add the default decoders.
   */
  public BerDecoderMappedCollection(boolean addUniversalTypes, boolean addDefaultDecoders)
  {
    super();
    if (addUniversalTypes)
    {
      this.idMap = createUniversalMap();
    }
    else
    {
      this.idMap = new HashMap<BerId, BerDecoder>();
    }
    if (addDefaultDecoders)
    {
      this.defaultCoder = new BerDecoderUnknown();
      this.defaultDecoderConstructed = createDefaultConstructedCoder();
    }
  }

  @Override
  protected BerDecoder getDecoder(BerId id)
  {
    BerDecoder codingBase = idMap.get(id);
    if (codingBase == null)
    {
      if (id.isConstructed() && defaultDecoderConstructed != null)
      {
        codingBase = defaultDecoderConstructed;
      }
      else
      {
        codingBase = defaultCoder;
      }
    }
    return codingBase;
  }

  /**
   * Decoder for id's which cannot be found in the map.
   * <P>
   * If the id is "constructed" and the {@code defaultCoderConstructed} is
   * not {@code null} the {@code defaultCoderConstructed} will be used instead.
   *
   * @return The default decoder.
   */
  public BerDecoder getDefaultDecoder()
  {
    return defaultCoder;
  }

  /**
   * See {@link #getDefaultDecoder()}
   *
   * @param defaultDecoder the new default decoder.
   */
  public void setDefaultDecoder(BerDecoder defaultDecoder)
  {
    this.defaultCoder = defaultDecoder;
  }

  /**
   * Decoder for constructed id's which cannot be found in the map.
   * <P>
   * If the id is "constructed" and the {@code defaultCoderConstructed} is
   * not {@code null} the {@code defaultCoderConstructed} will be used instead.
   *
   * @return The default decoder.
   */
  public BerDecoder getDefaultDecoderConstructed()
  {
    return defaultDecoderConstructed;
  }

  /**
   * See {@link #getDefaultDecoderConstructed()}
   *
   * @param defaultDecoderConstructed The new default decoder.
   */
  public void setDefaultCoderConstructed(BerDecoder defaultDecoderConstructed)
  {
    this.defaultDecoderConstructed = defaultDecoderConstructed;
  }

  public void addMapping(BerId id, BerDecoderBase<? extends BerValue> coder)
  {
    idMap.put(id, coder);
  }

}
