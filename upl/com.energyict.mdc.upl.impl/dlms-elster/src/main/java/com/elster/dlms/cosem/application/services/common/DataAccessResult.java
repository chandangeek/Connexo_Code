package com.elster.dlms.cosem.application.services.common;

/**
 * DLMS DataAccessResult<P>
 * Used by the COSEM, GET and SET services and for the optional return parameters of the COSEM ACTION service
 *
 * @author osse
 */
public enum DataAccessResult
{
  SUCCESS(0, "success"),
  HARDWARE_FAULT(1, "hardware-fault"),
  TEMPORARY_FAILURE(2, "temporary-failure"),
  READ_WRITE_DENIED(3, "read-write-denied"),
  OBJECT_UNDEFINED(4, "object-undefined"),
  OBJECT_CLASS_INCONSISTENT(9, "object-class-inconsistent"),
  OBJECT_UNAVAILABLE(11, "object-unavailable"),
  TYPE_UNMATCHED(12, "type-unmatched"),
  SCOPE_OF_ACCESS_VIOLATED(13, "scope-of-access-violated"),
  DATA_BLOCK_UNAVAILABLE(14, "data-block-unavailable"),
  LONG_GET_ABORTED(15, "long-get-aborted"),
  NO_LONG_GET_IN_PROGRESS(16, "no-long-get-in-progress"),
  LONG_SET_ABORTED(17, "long-set-aborted"),
  NO_LONG_SET_IN_PROGRESS(18, "no-long-set-in-progress"),
  DATA_BLOCK_NUMBER_INVALID(19, "data-block-number-invalid"),
  OTHER_REASON(250, "other-reason");
  private final int id;
  private final String name;

  private DataAccessResult(final int id,final String name)
  {
    this.id = id;
    this.name = name;
  }

  /**
   * The id (used by the protocol).
   * <P>
   * See GB ed.7 p.211
   *
   * @return The id
   */
  public int getId()
  {
    return id;
  }

  /**
   * The name as mentioned in the GB.
   * <P>
   * See GB ed.7 p.211
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the {@code DataAccessResult} for the specified ID.
   *
   * @param id The id.
   * @return The {@code DataAccessResult} for the id or {@code null}
   */
  static public DataAccessResult findById(final int id)
  {

    //shortcut
    if (id == 0)
    {
      return SUCCESS;
    }

    //simple binary search:
    DataAccessResult[] all = values();

    int l = 0;
    int h = all.length - 1;

    int m = id;

    while (l <= h)
    {
      if (m > h || m < l)
      {
        m = (l + h) >>> 1;
      }
      int val = all[m].getId();
      if (val < id)
      {
        l = m + 1;
      }
      else if (val > id)
      {
        h = m - 1;
      }
      else
      {
        return all[m];
      }

      m = m + (id - val);
    }
    return null;
  }

  @Override
  public String toString()
  {
    return name+"("+id+")";
  }

}
