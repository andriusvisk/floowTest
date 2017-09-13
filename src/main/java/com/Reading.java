package com;

import com.entit.Chunk;
import org.apache.commons.io.LineIterator;

import java.util.*;

/**
 * Created by andrius on 10/09/2017.
 */
public class Reading {

    private LineIterator it;
    private Long currentLine;
    private boolean hasMoreLines;

    private List<BufferChunk> buffer;

    public Reading(LineIterator it) {
        this.it = it;
        buffer = new ArrayList<>();
        hasMoreLines = true;
        currentLine = 0L;
    }

    // master read
    public BufferChunk readNextChunkForMaster(final Parameters parameters, boolean masterReadForHimself, String executorUUID) {
        return readChunk(currentLine + 1, parameters, masterReadForHimself, executorUUID);
    }

    // slave read
    public BufferChunk readChunkForSlave(Chunk chunk, final Parameters parameters) {
        return readChunk(chunk.getFromLineNbr(), parameters, false, chunk.getRunnerUUID());
    }

    private BufferChunk readChunk(Long lineFrom, final Parameters parameters, boolean masterReadForHimself, String executorUUID) {
        // slave has empty buffer
        BufferChunk bufferObj = buffer.stream().filter(x -> x.getChunk().getFromLineNbr().equals(lineFrom)).findFirst().orElse(null);
        if (bufferObj != null) {
            bufferObj.getChunk().setRunnerUUID(executorUUID); // reassign
            return bufferObj;
        } else {
            return readChunkFromFile(new Chunk(lineFrom, executorUUID), parameters, masterReadForHimself);
        }
    }

    private BufferChunk readChunkFromFile(Chunk chunk, final Parameters parameters, boolean putToBuffer) {
        // forward up to required line
        while ((currentLine < chunk.getFromLineNbr() - 1) && (hasMoreLines)) {
            if (it.hasNext()) {
                it.nextLine();
                ++currentLine;
            } else {
                hasMoreLines = false;
            }
        }
        if (hasMoreLines) {
            String lineBuffer = "";
            Long startLine = new Long(currentLine) + 1;
            for (int i = 0; i < parameters.chunkOfLinesSize; i++) {
                if (it.hasNext()) {
                    ++currentLine;
                    lineBuffer = lineBuffer + " " + it.nextLine();
                }
            }
            chunk.setToLineNbr(currentLine);
            BufferChunk bufferChunk = new BufferChunk(lineBuffer, chunk);
            if (putToBuffer) {
                buffer.add(bufferChunk);
            }
            return bufferChunk;
        } else {
            return null;
        }
    }

    public LineIterator getIt() {
        return it;
    }

    public List<BufferChunk> getBuffer() {
        return buffer;
    }

    public void setBuffer(List<BufferChunk> buffer) {
        this.buffer = buffer;
    }

    public Long getCurrentLine() {
        return currentLine;
    }

    public boolean isHasMoreLines() {
        return hasMoreLines;
    }
}
