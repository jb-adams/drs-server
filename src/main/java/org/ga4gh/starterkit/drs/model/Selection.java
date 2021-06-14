package org.ga4gh.starterkit.drs.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.ga4gh.starterkit.drs.utils.SerializeView;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonView(SerializeView.Always.class)
public class Selection {

    private List<String> selection;

    public Selection() {
        selection = new ArrayList<>();
    }

    public void setSelection(List<String> selection) {
        this.selection = selection;
    }

    public List<String> getSelection() {
        return selection;
    }
}
