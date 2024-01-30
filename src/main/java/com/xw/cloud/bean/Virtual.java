package com.xw.cloud.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@ToString
public class Virtual {
    @JsonProperty(value = "id")
    private int id;
    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "state")
    private String state;
    @JsonProperty(value = "maxMem")
    private long maxMem;
    @JsonProperty(value = "cpuNum")
    private int cpuNum;
    @JsonProperty(value = "usemem")
    private Double useMem;
    @JsonProperty(value = "usecpu")
    private Double usecpu;
//    @JsonProperty(value = "CurrentBandWidth")
//    private long bandwidth;
    @JsonProperty(value = "ipaddr")
    private String ipaddr;
    @JsonProperty(value = "upBandWidth")
    private Integer upBW;
    @JsonProperty(value = "downBandWidth")
    private Integer downBW;

}
