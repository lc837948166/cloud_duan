package com.xw.cloud.resp;

import java.util.List;

public class CasResp {
    private String label;
    private String value;
    private List<CasResp> children;

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

    public List<CasResp> getChildren() {
        return children;
    }

    public void setChildren(List<CasResp> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "CasResp{" +
                "label='" + label + '\'' +
                ", value='" + value + '\'' +
                ", children=" + children +
                '}';
    }
}
