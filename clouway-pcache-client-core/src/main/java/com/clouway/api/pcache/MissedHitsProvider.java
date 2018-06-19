package com.clouway.api.pcache;

import java.util.List;

public interface MissedHitsProvider<T> {
    List<T> get(List<String> missingKeys);
}
