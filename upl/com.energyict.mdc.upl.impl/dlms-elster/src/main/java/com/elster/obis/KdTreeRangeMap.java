/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/obis/KdTreeRangeMap.java $
 * Version:     
 * $Id: KdTreeRangeMap.java 4769 2012-07-02 16:59:57Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.07.2010 14:57:12
 */
package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Node of an (immutable) KD-Tree for OBIS Code definitions.<P>
 * The KD-Tree provides an (very) fast access to OBIS Code definitions for an OBIS-Code.<P>
 * The children will be build on demand (or complete by calling {@link #propagateAll()})
 *
 * @author osse
 */
public class KdTreeRangeMap<T> implements IRangeMap<T>
{
  private static final int PROPAGATION_THRESHOLD = 2;
  private int groupIndex;
  private int splitValue;
  private boolean propagationDone = false;
  private List<Pair<T>> pairs;
  private KdTreeRangeMap<T> left = null;
  private KdTreeRangeMap<T> right = null;

  /**
   * Creates the node (tree) with the list of defs.
   *
   * @param source
   */
  public KdTreeRangeMap(final IRangeMap<T> source)
  {
    this.pairs = new ArrayList<Pair<T>>(source.getPairs());
  }

  /**
   * Creates the node (tree) with the list of defs.
   *
   * @param source
   */
  public KdTreeRangeMap(final List<Pair<T>> source)
  {
    this.pairs = new ArrayList<Pair<T>>(source);
  }

  /**
   * Creates the node (tree) with the list of defs.
   *
   * @param source
   * @param internal
   */
  protected KdTreeRangeMap(final List<Pair<T>> source, final boolean internal)
  {
    if (!internal)
    {
      throw new AssertionError("Only for internal use");
    }
    this.pairs = source;
  }

  public T find(final ObisCode obisCode)
  {
    if (!propagationDone)
    {
      propagate();
    }

    if (pairs != null)
    {
      for (Pair<T> p : pairs)
      {
        if (p.getRange().contains(obisCode))
        {
          return p.getItem();
        }
      }
      return null;
    }

    if (obisCode.getValueGroup(groupIndex) <= splitValue)
    {
      return left.find(obisCode);
    }
    else
    {
      return right.find(obisCode);
    }
  }

  private void findAll(final ObisCode obisCode, final List<Pair<T>> results)
  {
    if (!propagationDone)
    {
      propagate();
    }

    if (pairs == null)
    {
      if (obisCode.getValueGroup(groupIndex) <= splitValue)
      {
        left.findAll(obisCode, results);
      }
      else
      {
        right.findAll(obisCode, results);
      }
    }
    else
    {
      for (Pair<T> p : pairs)
      {
        if (p.getRange().contains(obisCode))
        {
          results.add(p);
        }
      }

    }
  }

  /**
   * Propagates the complete tree.<P>
   * Normaly the tree will be propagated on demand.
   *
   */
  public synchronized void propagateAll()
  {
    propagate();

    if (left != null)
    {
      left.propagateAll();
    }
    if (right != null)
    {
      right.propagateAll();
    }
  }
// <editor-fold defaultstate="collapsed" desc="old version of propagate">
/*
   private void propagate2()
   {
   if (propagationDone)
   {
   return;
   }


   propagationDone = true;
   int quality = propagationThreshold / 2;
   boolean qualityOk = false;

   if (defs.size() >= propagationThreshold)
   {
   List<ObisCodeDef> leftDefs = null;
   List<ObisCodeDef> rightDefs = null;

   for (int i = 0; i < 6; i++)
   {


   List<Integer> splitCandiates = new ArrayList<Integer>();

   for (ObisCodeDef def : defs)
   {
   splitCandiates.add(def.getMaxGroupValue(groupIndex));
   }

   Collections.sort(splitCandiates);
   splitValue = splitCandiates.get(splitCandiates.size() / 2);

   leftDefs = new ArrayList<ObisCodeDef>();
   rightDefs = new ArrayList<ObisCodeDef>();

   for (ObisCodeDef def : defs)
   {
   if (def.getMinGroupValue(groupIndex) <= splitValue)
   {
   leftDefs.add(def);
   }
   if (def.getMaxGroupValue(groupIndex) > splitValue)
   {
   rightDefs.add(def);
   }
   }

   qualityOk = !(leftDefs.size() < quality || rightDefs.size() < quality
   || defs.size() - leftDefs.size() < quality
   || defs.size() - rightDefs.size() < quality);

   if (qualityOk)
   {
   break;
   }
   else
   {
   groupIndex = (groupIndex + 1) % 6;
   }
   }

   if (qualityOk)
   {
   left = new KdTreeNodeObisCodeDef(leftDefs);
   right = new KdTreeNodeObisCodeDef(rightDefs);
   defs = null;
   }
   }
   }
   */// </editor-fold>

  private void propagate()
  {
    //Diese Methode findet eine gute Stelle zum Aufsplitten durch einfaches Probieren aller Möglichkeiten.
    //Da dieses Methode nur ein einziges Mal pro Knoten aufgerufen wird, wird auf eine Optimierung verzichtet.
    if (propagationDone)
    {
      return;
    }
    propagationDone = true;

    if (pairs.size() >= PROPAGATION_THRESHOLD)
    {
      int score = Integer.MIN_VALUE;
      splitValue = 0;

      final int optSize = pairs.size() / 2; //optimal size

      final int minSplitSize = 1; //propagationThreshold / 2;
      boolean doSplit = false;

      for (int i = 0; i < 6; i++)
      {
        final SortedSet<Integer> splitCandiates = new TreeSet<Integer>();
        for (Pair<T> pair : pairs)
        {
          splitCandiates.add(pair.getRange().getGroupRange(i).getMaxGroupValue());
        }

        for (int candidate : splitCandiates)
        {

          int leftSize = 0;
          int rightSize = 0;

          for (Pair<T> pair : pairs)
          {
            if (pair.getRange().getGroupRange(i).getMinGroupValue() <= candidate)
            {
              leftSize++;
            }
            if (pair.getRange().getGroupRange(i).getMaxGroupValue() > candidate)
            {
              rightSize++;
            }
          }

          final int newScore = -((leftSize - optSize) * (leftSize - optSize)
                                 + (rightSize - optSize) * (rightSize - optSize)); // -(dl^2+dr^2)
          if (newScore > score)
          {
            score = newScore;
            splitValue = candidate;
            groupIndex = i;
            doSplit = (leftSize >= minSplitSize && rightSize >= minSplitSize && pairs.size() - leftSize
                                                                                >= minSplitSize
                       && pairs.size() - rightSize >= minSplitSize);
          }
        }
      }

      if (doSplit)
      {
        final List<Pair<T>> leftDefs = new ArrayList<Pair<T>>();
        final List<Pair<T>> rightDefs = new ArrayList<Pair<T>>();

        for (Pair<T> pair : pairs)
        {
          if (pair.getRange().getGroupRange(groupIndex).getMinGroupValue() <= splitValue)
          {
            leftDefs.add(pair);
          }
          if (pair.getRange().getGroupRange(groupIndex).getMaxGroupValue() > splitValue)
          {
            rightDefs.add(pair);
          }
        }

        left = new KdTreeRangeMap<T>(leftDefs, true);
        right = new KdTreeRangeMap<T>(rightDefs, true);
        pairs = null;
      }
    }
  }

  /**
   * Analyses the tree (for internal proposes).
   *
   * @param leaves Infos about leaves
   * @param innerNodes Infos about inner nodes
   */
  public void analyse(final List<String> leaves, final List<String> innerNodes)
  {
    analyse(leaves, innerNodes, 0);
  }

  private int analyse(final List<String> leaves, final List<String> innerNodes, final int level)
  {
    if (!propagationDone)
    {
      propagate();
    }

    if (pairs == null)
    {
      final int sl = left.analyse(leaves, innerNodes, level + 1);
      final int sr = right.analyse(leaves, innerNodes, level + 1);

      innerNodes.add("L " + level + " Left " + sl + " Right " + sr);
      return sl + sr;
    }
    else
    {
      leaves.add("L " + level + " " + pairs.size() + " Elements");
      return pairs.size();
    }
  }

  public List<T> findAll(final ObisCode obisCode)
  {
    final List<Pair<T>> pairs = new ArrayList<Pair<T>>();
    findAll(obisCode, pairs);

    final List<T> result = new ArrayList<T>(pairs.size());

    for (Pair<T> p : pairs)
    {
      result.add(p.getItem());
    }

    return result;
  }

  public List<Pair<T>> findAllPairs(ObisCode obisCode)
  {
    final List<Pair<T>> pairs = new ArrayList<Pair<T>>();
    findAll(obisCode, pairs);
    return pairs;
  }

  private void collectPairs(final List<Pair<T>> results)
  {
    if (pairs == null)
    {
      left.collectPairs(results);
      right.collectPairs(results);
    }
    else
    {
      results.addAll(pairs);
    }
  }

  public List<Pair<T>> getPairs()
  {
    final List<Pair<T>> result = new ArrayList<Pair<T>>();
    collectPairs(result);
    return result;
  }

}
