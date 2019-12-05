/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.application.services.action;

/**
 * DLMS action result.
 *
 * @author osse
 */
public enum ActionResult
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
  LONG_ACTION_ABORTED(15, "long-action-aborted"),
  NO_LONG_ACTION_IN_PROGRESS(16, "no-long-action-in-progress"),
  OTHER_REASON(250, "other-reason");

  private final int id;
  private final String name;

  private ActionResult(int id, String name)
  {
    this.id = id;
    this.name = name;
  }

  public int getId()
  {
    return id;
  }

  public String getName()
  {
    return name;
  }

  static public ActionResult findById(int id)
  {

    //shortcut
    if (id == 0)
    {
      return SUCCESS;
    }

    //simple binary search:
    ActionResult[] all = values();

    int l = 0;
    int h = all.length - 1;

    while (l <= h)
    {
      int m = (l + h) >>> 1;
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
    }
    return null;
  }

  @Override
  public String toString()
  {
    return id+" " + name;
  }




}
