package com.xw.cloud.resp;

import java.util.List;

public class SelectResp {

    private String label;
    private String value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SelectResp{" +
                "label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
