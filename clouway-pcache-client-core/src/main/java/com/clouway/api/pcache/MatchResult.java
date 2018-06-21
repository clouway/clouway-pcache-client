package com.clouway.api.pcache;

import java.util.List;
import java.util.Objects;

public class MatchResult <T> {
    private List<T> hits;
    private List<String> missedKeys;

    public MatchResult(List<T> hits, List<String> missedKeys) {
        this.hits = hits;
        this.missedKeys = missedKeys;
    }

    public List<T> getHits() {
        return this.hits;
    }

    public List<String> getMissedKeys() {
        return this.missedKeys;
    }

    public Boolean hasMissedKeys() {
        return !missedKeys.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchResult that = (MatchResult) o;
        return Objects.equals(missedKeys, that.missedKeys) &&
                Objects.equals(hits, that.hits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(missedKeys, hits);
    }

    @Override
    public String toString() {
        return "MatchResult{" +
                "missedKeys=" + missedKeys +
                ", hits=" + hits +
                '}';
    }
}