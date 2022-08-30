package io.xpipe.ext.sample;

import io.xpipe.core.data.node.DataStructureNodeAcceptor;
import io.xpipe.core.data.node.TupleNode;
import io.xpipe.core.data.node.ValueNode;
import io.xpipe.core.data.type.TupleType;
import io.xpipe.core.data.type.ValueType;
import io.xpipe.core.source.StreamReadConnection;
import io.xpipe.core.source.TableReadConnection;

import java.io.BufferedReader;
import java.util.List;

public class SampleReadConnection extends StreamReadConnection implements TableReadConnection {

    private final SampleSource source;

    public SampleReadConnection(SampleSource source) {
        super(source.getStore(), source.getCharset());
        this.source = source;
    }

    @Override
    public TupleType getDataType() {
        // In the case of this simple sample format,
        // the data type is fixed.
        // For a more dynamic format, you would have to implement a data type detection for it.
        return TupleType.of(List.of(ValueType.of(), ValueType.of()));
    }

    @Override
    public int getRowCount() throws Exception {
        // Some data sources contain header information about the
        // amount of table rose included in the file without having to read the complete file.
        // However, as this is not the case with this simple format,
        // we just return -1 instead.
        return -1;
    }

    @Override
    public void withRows(DataStructureNodeAcceptor<TupleNode> lineAcceptor) throws Exception {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }

                var split = line.split("" + source.getDelimiter());
                if (split.length != 2) {
                    throw new IllegalArgumentException("Not a valid row");
                }
                lineAcceptor.accept(TupleNode.of(List.of(ValueNode.immutable(split[0], false), ValueNode.immutable(split[1], false))));
            }
        }
    }
}
