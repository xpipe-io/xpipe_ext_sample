package io.xpipe.ext.sample;

import io.xpipe.core.data.node.DataStructureNodeAcceptor;
import io.xpipe.core.data.node.TupleNode;
import io.xpipe.core.data.type.TupleType;
import io.xpipe.core.data.type.ValueType;
import io.xpipe.core.impl.StreamWriteConnection;
import io.xpipe.core.source.TableMapping;
import io.xpipe.core.source.TableWriteConnection;

import java.util.List;
import java.util.Optional;

public class SampleWriteConnection extends StreamWriteConnection implements TableWriteConnection {

    private final SampleSource source;

    public SampleWriteConnection(SampleSource source) {
        super(source.getStore(), source.getCharset());
        this.source = source;
    }

    @Override
    public Optional<TableMapping> createMapping(TupleType inputType) {
        // For this example format, the data type is fixed as it is just a tuple of two values
        var outputType = TupleType.of(List.of(ValueType.of(), ValueType.of()));
        return TableMapping.createBasic(inputType, outputType);
    }

    @Override
    public DataStructureNodeAcceptor<TupleNode> writeLinesAcceptor(TableMapping mapping) {
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

            writer.append(first.asString()).append(source.getDelimiter()).append(second.asString()).append(source.getNewLine().getNewLineString());
            return true;
        };
    }
}
