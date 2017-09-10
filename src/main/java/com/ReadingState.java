package com;

import org.apache.commons.io.LineIterator;

import java.util.List;
import java.util.Map;

/**
 * Created by andrius on 10/09/2017.
 */
public class ReadingState {
    private LineIterator it;
    private Long lineFromNbr;
    private int counter;

    // <beginingLineNbr, ...>
    private Map<Long, BufferChunk> buffer;

    public ReadingState() {
    }

    public LineIterator getIt() {
        return it;
    }

    public void setIt(LineIterator it) {
        this.it = it;
    }

    public Long getLineFromNbr() {
        return lineFromNbr;
    }

    public void setLineFromNbr(Long lineFromNbr) {
        this.lineFromNbr = lineFromNbr;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public Map<Long, BufferChunk> getBuffer() {
        return buffer;
    }

    public void setBuffer(Map<Long, BufferChunk> buffer) {
        this.buffer = buffer;
    }
}
