package io.xpipe.ext.sample;


import com.fasterxml.jackson.annotation.JsonCreator;
import io.xpipe.core.charsetter.NewLine;
import io.xpipe.core.charsetter.StreamCharset;
import io.xpipe.core.impl.PreservingTableWriteConnection;
import io.xpipe.core.source.*;
import io.xpipe.core.store.DataStore;
import io.xpipe.core.store.StreamDataStore;
import io.xpipe.extension.util.NamedCharacter;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Optional;

@SuperBuilder
@Jacksonized
@Getter
@FieldDefaults(
        makeFinal = true,
        level = AccessLevel.PRIVATE
)
public class SampleSource extends TableDataSource<StreamDataStore> {

    @UtilityClass
    public class Delimiter {

        public static final List<NamedCharacter> CHARS = List.of(
                new NamedCharacter(',', List.of("comma"), "csv.comma"),
                new NamedCharacter(';', List.of("semicolon"), "csv.semicolon"),
                new NamedCharacter(':', List.of("colon"), "csv.colon"),
                new NamedCharacter('|', List.of("pipe"), "csv.pipe"),
                new NamedCharacter('=', List.of("equals"), "csv.equals"),
                new NamedCharacter(' ', List.of("space"), "csv.space"),
                new NamedCharacter('\t', List.of("tab"), "csv.tab")
        );
    }

    public static SampleSource defaultSource(DataStore input) {
        return SampleSource.builder().store(input.asNeeded()).charset(StreamCharset.UTF8).newLine(NewLine.LF).delimiter(' ').build();
    }

    StreamCharset charset;
    NewLine newLine;
    Character delimiter;

    @Override
    protected TableWriteConnection newWriteConnection(WriteMode mode) {
        if (mode == WriteMode.REPLACE) {
            return new SampleWriteConnection(this);
        }

        // Instead of creating our own write connection class for the purposes of pretending and appending data,
        // we can also make use of this utility class. It stores the existing contents of this data source in a temporary file
        // and writes them back in combination with the appended input. Obviously this is not very efficient,
        // however it always works.
        // You can of course also implement your own more efficient write connection to handle this specific purpose.
        if (mode == WriteMode.APPEND || mode == WriteMode.PREPEND) {
            return new PreservingTableWriteConnection(this, new SampleWriteConnection(this), mode == WriteMode.APPEND);
        }

        // Unsupported write mode
        return null;
    }

    @Override
    protected TableReadConnection newReadConnection() {
        return new SampleReadConnection(this);
    }

    @Override
    public Optional<String> determineDefaultName() {
        return super.determineDefaultName();
    }
}
