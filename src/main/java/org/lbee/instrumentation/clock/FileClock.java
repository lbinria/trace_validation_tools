package org.lbee.instrumentation.clock;

import java.io.File;
import java.io.IOException;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

/**
 * A named clock that can be shared through multiple processes. Clock value is
 * stored as a memory file map and can be accessed by different processes on the
 * same hardware.
 */
class FileClock implements InstrumentationClock {
    // Buffer storing the clock value
    private final LongBuffer buffer;

    /**
     * Build a shared clock given a name
     * 
     * @param name Unique name of the shared clock
     * @throws IOException
     */
    public FileClock(String name) throws IOException {
        // Create memory mapped file
        final File f = new File(name);
        final FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
        final MappedByteBuffer b = channel.map(FileChannel.MapMode.READ_WRITE, 0, 8);
        buffer = b.asLongBuffer();
    }
   
    /**
     * Get clock value
     * 
     * @return Clock value
     */
    private long getValue() {
        return buffer.get(0);
    }

    /**
     * Set clock value
     * 
     * @param value Value
     */
    private void setValue(long value) {
        buffer.put(0, value);
    }

    /**
     * Synchronize clock with another value
     * 
     * @param clock clock to synchronize with
     * @return clock value
     */
    @Override
    public synchronized long getNextTime(long clock) {
        final long value = this.getValue();
        final long newValue = Math.max(value, clock) + 1;
        this.setValue(newValue);
        System.out.println("###### Clock value: " + newValue + "(was " + value + ")");
        return newValue;
    }
}
