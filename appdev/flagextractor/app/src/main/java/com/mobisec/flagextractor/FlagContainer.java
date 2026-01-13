package com.mobisec.flagextractor;

import java.io.Serializable;
import java.util.ArrayList;

public class FlagContainer implements Serializable {
    public String[] parts;
    public ArrayList<Integer> perm;
}