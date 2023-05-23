package org.lbee.instrumentation.clock;

import java.io.File;
import java.io.IOException;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

/**
 * A named clock that can be shared through multiple process.
 * Clock value is stored as a memory file map and can be accessed by different programs on the same hardware.
 */
public class SharedClock implements InstrumentationClock {

    // Buffer for writing clock value
    private final LongBuffer buffer;

    /**
     * Build a shared clock given a name
     * @param name Unique name of the shared clock
     * @throws IOException
     */
    public SharedClock(String name) throws IOException {
        // Create memory mapped file
        final File f = new File(name);
        final FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        final MappedByteBuffer b = channel.map(FileChannel.MapMode.READ_WRITE, 0, 8);
        buffer = b.asLongBuffer();
    }

    /**
     * Get a shared clock by its name
     * @param name Name of shared clock you want
     * @return A shared clock
     * @throws IOException
     */
    public static SharedClock get(String name) throws IOException {
        return new SharedClock(name);
    }

    /**
     * Get clock value
     * @return Clock value
     */
    @Override
    public long getValue() {
        return buffer.get(0);
    }

    /**
     * Set clock value
     * @param value Value
     */
    private void setValue(long value) {
        buffer.put(0, value);
    }

    /**
     * Reset shared clock (value=0)
     */
    public void reset() {
        setValue(0);
    }

    /**
     * Synchronize clock with another value
     * @param clock Clock to synchronize with
     * @return Clock value
     */
    @Override
    public synchronized long sync(long clock) {
        final long value = getValue();
        final long newValue = Math.max(value, clock) + 1;
        setValue(newValue);
        return newValue;
    }

    public String toString() {
        return Long.toString(getValue());
    }

}
