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

    // Internal clock
    private final LogicalClock clock;
    // Buffer for writing clock value
    private final LongBuffer buffer;

    /**
     * Build a shared clock given a name
     * @param name Unique name of the shared clock
     * @throws IOException
     */
    public SharedClock(String name) throws IOException {
        clock = new LogicalClock();

        // Create memory mapped file
        final File f = new File(name);
        final FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        final MappedByteBuffer b = channel.map(FileChannel.MapMode.READ_WRITE, 0, 8);
        buffer = b.asLongBuffer();
        buffer.put(0, clock.getValue());
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
     * Synchronize clock with another value
     * @param clock Clock to synchronize with
     * @return Clock value
     */
    @Override
    public long sync(long clock) {
        this.clock.sync(clock);
        long value = this.clock.getValue();
        buffer.put(0, value);
        return value;
    }

}
