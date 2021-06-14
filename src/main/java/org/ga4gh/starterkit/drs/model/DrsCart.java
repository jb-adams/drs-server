package org.ga4gh.starterkit.drs.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.ga4gh.starterkit.drs.utils.SerializeView;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonView(SerializeView.Always.class)
public class DrsCart {

    private DrsCartSummary summary;
    private Map<String, DrsObject> drsObjects;
    private Map<String, Integer> unloadedDrsObjects;

    public DrsCart() {
        summary = new DrsCartSummary();
        drsObjects = new HashMap<>();
        unloadedDrsObjects = new HashMap<>();
    }

    public void setSummary(DrsCartSummary summary) {
        this.summary = summary;
    }

    public DrsCartSummary getSummary() {
        return summary;
    }

    public void setDrsObjects(Map<String, DrsObject> drsObjects) {
        this.drsObjects = drsObjects;
    }

    public Map<String, DrsObject> getDrsObjects() {
        return drsObjects;
    }

    public void setUnloadedDrsObjects(Map<String, Integer> unloadedDrsObjects) {
        this.unloadedDrsObjects = unloadedDrsObjects;
    }

    public Map<String, Integer> getUnloadedDrsObjects() {
        return unloadedDrsObjects;
    }
}
