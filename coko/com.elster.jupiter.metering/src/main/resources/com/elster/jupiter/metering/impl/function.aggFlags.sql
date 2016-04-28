CREATE OR REPLACE FUNCTION aggFlags (flagsArray IN Flags_Array)
RETURN number
AS
  type numbertab is varray(16) of number;
  sixteenbits numbertab := numbertab();
  retval       number := 0;
  allzeroes    number := 1;
BEGIN
  for indexnr in 1 .. flagsArray.count
  loop
    if flagsArray(indexnr) > 0 then
      /* At least one of the flags is set so we need to aggregate the overall result */
      if allzeroes = 1 then
        /* First occurrence of a set of flags that has at least one flag set: initialize the array */
        for bitnr in 1 .. sixteenbits.limit
        loop
          sixteenbits.extend;
          sixteenbits(bitnr) := 0;
        end loop;
        allzeroes := 0;
      end if;
      for bitnr in 1 .. sixteenbits.limit
      loop
        if sixteenbits(bitnr) = 0 then
          if bitand (flagsArray(indexnr), power(2, bitnr-1)) > 0 then
            sixteenbits(bitnr) := 1;
          end if;
        end if;
      end loop;
    else
      /* None of the flags are set, ignore the set */
      null;
    end if;
  end loop;
  if allzeroes = 0 then
    /* At least one of the flags was set */
    for bitnr in 1 .. sixteenbits.limit
    loop
      retval := retval + sixteenbits(bitnr) * power (2, bitnr-1);
    end loop;
  end if;
  return retval;
END;