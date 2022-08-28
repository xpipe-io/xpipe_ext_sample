package io.xpipe.ext.sample;

import io.xpipe.core.charsetter.StreamCharset;
import io.xpipe.core.data.node.DataStructureNodeAcceptor;
import io.xpipe.core.data.node.TupleNode;
import io.xpipe.core.source.StreamWriteConnection;
import io.xpipe.core.source.TableWriteConnection;
import io.xpipe.core.store.StreamDataStore;

public class SampleWriteConnection extends StreamWriteConnection implements TableWriteConnection {

    private final SampleSource source;

    public SampleWriteConnection(SampleSource source) {
        super(source.getStore(), source.getCharset());
        this.source = source;
    }

    @Override
    public DataStructureNodeAcceptor<TupleNode> writeLinesAcceptor() {
        return node -> {
            if (!node.isTuple()) {
                throw new IllegalArgumentException("Not a tuple");
            }
            if (node.size() != 2) {
                throw new IllegalArgumentException("Incompatible tuple size");
            }
            var first = node.getNodes().get(0);
            var second = node.getNodes().get(1);
            if (!first.isValue() || !second.isValue()) {
                throw new IllegalArgumentException("Not a tuple of values");
            }

            writer.append(first.asString()).append(source.getDelimiter()).append(second.asString());
            return true;
        };
    }
}
