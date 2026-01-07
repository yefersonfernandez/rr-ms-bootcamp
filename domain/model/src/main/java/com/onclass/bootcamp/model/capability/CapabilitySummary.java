package com.onclass.bootcamp.model.capability;

import com.onclass.bootcamp.model.technology.TechnologySummary;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class CapabilitySummary {
    private Long id;
    private String name;
    private List<TechnologySummary> technologies;
}
