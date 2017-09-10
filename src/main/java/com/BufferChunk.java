package com;

import com.entit.Chunk;

/**
 * Created by andrius on 10/09/2017.
 */
public class BufferChunk {

    private String text;
    private Chunk chunk;

    public BufferChunk(String text, Chunk chunk) {
        this.text = text;
        this.chunk = chunk;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }
}
