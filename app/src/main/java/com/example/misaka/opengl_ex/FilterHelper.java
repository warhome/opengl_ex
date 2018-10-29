package com.example.misaka.opengl_ex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class FilterHelper {

    boolean isContains(List <Filter> filters, String name) {
        for (Filter f : filters) {
            if (f.getName().equals(name)) return true;
        }
        return false;
    }

    Filter findFilterId(List<Filter> filters, String filterName) {
            final Collection<Filter> list = new ArrayList<>(filters);
            list.removeIf(value -> !value.getName().equals(filterName));
            return ((ArrayList<Filter>)list).get(0);
    }

    void deleteElement(List<Filter> filters, String name) {
        filters.removeIf(f -> f.getName().equals(name));
    }
}