package com.example.misaka.opengl_ex;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    private String name;
    List<Float> params = new ArrayList<>();

    Filter(String name) {
        this.name = name;
        params.add(0f);
        params.add(0f);
    }

    Filter(String name, float param1) {
        this.name = name;
        params.add(param1);
        params.add(0f);
    }

    Filter(String name, float param1, float param2) {
        this.name = name;
        params.add(param1);
        params.add(param2);
    }

    public String getName() {
        return name;
    }

    List<Float> getParams() {
        return params;
    }

    void setParams(List<Float> params) {
        this.params = params;
    }
}
