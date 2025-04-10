package com.scoring.model.response;

import lombok.Data;

import java.util.ArrayList;

@Data

public class ScoringResponse {
    public ArrayList<Object> links;
    public int version;
    public String moduleId;
    public String stepId;
    public String executionState;
    public Metadata metadata;
//    public ArrayList<Output> outputs;

    @Data
    public static class Output{
        public String name;
//        public Map<String, Object> value;
    }

    @Data
    public static class Metadata{
        public String module_id;
        public String step_id;
    }
}
