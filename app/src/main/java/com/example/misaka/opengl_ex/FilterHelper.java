package com.example.misaka.opengl_ex;

import java.util.Iterator;
import java.util.List;

class FilterHelper {
    void deleteElement(List<Filter> filters, String name) {
        for (Iterator<Filter> iterator = filters.listIterator(); iterator.hasNext(); ) {
            Filter f = iterator.next();
            if (f.getName().equals(name)) iterator.remove();
        }
    }

    boolean isContains(List <Filter> filters, String name) {
        for (Filter f : filters) {
            if (f.getName().equals(name)) return true;
        }
        return false;
    }
}