package org.dfhu.vpodplayer.sqlite;

import java.util.List;

interface ConsumeHydrator<T> {
    void consume(ColumnCursor cc, List<T> items);
}
