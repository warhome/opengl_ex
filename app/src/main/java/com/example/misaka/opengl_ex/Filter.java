package com.example.misaka.opengl_ex;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    private String name;
    private List<Float> params = new ArrayList<>();

    Filter(String name) {
        this.name = name;
    }

    Filter(String name, float param1) {
        this.name = name;
        params.add(param1);
    }

    Filter(String name, float param1, float param2) {
        this.name = name;
        params.set(0, param1);
        params.set(1, param2);
    }

    public String getName() {
        return name;
    }

    List<Float> getParams() {
        return params;
    }
}
