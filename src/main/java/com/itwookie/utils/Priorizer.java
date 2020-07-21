package com.itwookie.utils;

import java.util.Comparator;

public class Priorizer implements Comparator<Priorized> {
    @Override
    public int compare(Priorized o1, Priorized o2) {
        return o1.compareTo(o2);
    }
}
