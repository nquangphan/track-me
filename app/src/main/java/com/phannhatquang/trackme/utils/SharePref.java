package com.phannhatquang.trackme.utils;

import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * @author hanhnguyen
 */
@SharedPref(SharedPref.Scope.UNIQUE)
public interface SharePref {

    String currentSessionID();

    int currentState();

}
