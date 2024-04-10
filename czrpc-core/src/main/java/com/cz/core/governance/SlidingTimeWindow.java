package com.cz.core.governance;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * SlidingTimeWindow implement based on RingBuffer and TS(timestamp).
 * Use TS/1000->SecondNumber to mapping an index slot in a RingBuffer.
 *
 * @author Zjianru
 */
@Getter
@ToString
@Slf4j
public class SlidingTimeWindow {

    public static final int DEFAULT_SIZE = 30;

    private final int size;
    private final RingBuffer ringBuffer;
    private int sum = 0;
    private int currMark = -1;
    private long startTs = -1L;
    private long currTs = -1L;

    public SlidingTimeWindow() {
        this(DEFAULT_SIZE);
    }

    public SlidingTimeWindow(int size) {
        this.size = size;
        this.ringBuffer = new RingBuffer(this.size);
    }

    /**
     * record current ts millis.
     *
     * @param millis millis
     */
    public synchronized void record(long millis) {
        log.debug("window before: {}", this);
        log.debug("window.record({})", millis);
        long ts = millis / 1000;
        if (startTs == -1L) {
            initRing(ts);
        } else {
            // TODO  Prev 是否需要考虑
            log.debug("window ts:{}, curr_ts:{}, size:{}", ts, currTs, size);
            if (ts == currTs) {
                this.ringBuffer.incr(currMark, 1);
            } else if (ts > currTs && ts < currTs + size) {
                int offset = (int) (ts - currTs);
                log.debug("window ts:{}, curr_ts:{}, size:{}, offset:{}", ts, currTs, size, offset);
                this.ringBuffer.reset(currMark + 1, offset);
                this.ringBuffer.incr(currMark + offset, 1);
                currTs = ts;
                currMark = (currMark + offset) % size;
            } else if (ts >= currTs + size) {
                this.ringBuffer.reset();
                initRing(ts);
            }
        }
        this.sum = this.ringBuffer.sum();
        log.debug("window after: {}", this);
    }

    private void initRing(long ts) {
        log.debug("window initRing ts:{}", ts);
        this.startTs = ts;
        this.currTs = ts;
        this.currMark = 0;
        this.ringBuffer.incr(0, 1);
    }

    public int calcSum() {
        long ts = System.currentTimeMillis() / 1000;
        log.debug("calc sum for window ts:{}, curr_ts:{}, size:{}", ts, currTs, size);
        if (ts > currTs && ts < currTs + size) {
            int offset = (int) (ts - currTs);
            log.debug("=========>>>calc sum for window ts:{}, curr_ts:{}, size:{}, offset:{}", ts, currTs, size, offset);
            this.ringBuffer.reset(currMark + 1, offset);
            currTs = ts;
            currMark = (currMark + offset) % size;
        } else if (ts >= currTs + size) {
            this.ringBuffer.reset();
            initRing(ts);
        }
        log.debug("calc sum for window:{}", this);
        return ringBuffer.sum();
    }


}