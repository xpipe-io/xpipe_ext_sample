package io.xpipe.ext.sample;


import com.fasterxml.jackson.annotation.JsonCreator;
import io.xpipe.core.charsetter.NewLine;
import io.xpipe.core.charsetter.StreamCharset;
import io.xpipe.core.impl.PreservingTableWriteConnection;
import io.xpipe.core.source.DataSource;
import io.xpipe.core.source.TableDataSource;
import io.xpipe.core.source.TableReadConnection;
import io.xpipe.core.source.TableWriteConnection;
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
    protected TableWriteConnection newWriteConnection() {
        return new SampleWriteConnection(this);
    }

    @Override
    protected TableWriteConnection newAppendingWriteConnection() {
        // Instead of creating our own write connection class for this purpose,
        // we can also make use of this utility class.
        // It stores the existing contents of this data source in a temporary file
        // and writes them back in combination with the appended input.
        // Obviously this is not very efficient,
        // however it always works.
        // Feel free to implement your own connection to handle this specific purpose
        return new PreservingTableWriteConnection(this, newWriteConnection(), true);
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
