package io.xpipe.ext.sample;

import io.xpipe.core.charsetter.Charsetter;
import io.xpipe.core.charsetter.NewLine;
import io.xpipe.core.charsetter.StreamCharset;
import io.xpipe.core.dialog.Dialog;
import io.xpipe.core.impl.TextSource;
import io.xpipe.core.source.DataSource;
import io.xpipe.core.source.DataSourceType;
import io.xpipe.core.store.DataStore;
import io.xpipe.core.store.StreamDataStore;
import io.xpipe.extension.*;
import io.xpipe.extension.util.DialogHelper;
import io.xpipe.extension.util.DynamicOptionsBuilder;
import io.xpipe.extension.util.NamedCharacter;
import io.xpipe.extension.util.SimpleFileDataSourceProvider;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;

import java.io.BufferedReader;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SampleSourceProvider implements SimpleFileDataSourceProvider<SampleSource>, DataSourceProvider<SampleSource> {

    @Override
    public String getId() {
        return "sampleSource";
    }

    @Override
    public String getDisplayIconFileName() {
        return "ext_sample:icon.png";
    }

    @Override
    public boolean supportsConversion(SampleSource in, DataSourceType t) {
        return t == DataSourceType.TEXT || SimpleFileDataSourceProvider.super.supportsConversion(in, t);
    }

    @Override
    public DataSource<?> convert(SampleSource in, DataSourceType t) throws Exception {
        return t == DataSourceType.TEXT ?
                TextSource.builder().store(in.getStore()).charset(in.getCharset()).newLine(in.getNewLine()).build() :
                SimpleFileDataSourceProvider.super.convert(in, t);
    }

    @Override
    public Region configGui(Property<SampleSource> source, boolean preferQuiet) throws Exception {
        var charset = new SimpleObjectProperty<StreamCharset>(source.getValue().getCharset());
        var newLine = new SimpleObjectProperty<NewLine>(source.getValue().getNewLine());

        var delimiter = new SimpleObjectProperty<Character>(source.getValue().getDelimiter());
        var delimiterNames = new LinkedHashMap<Character, ObservableValue<String>>();
        SampleSource.Delimiter.CHARS.forEach(d -> {
            delimiterNames.put(d.getCharacter(), I18n.observable(d.getTranslationKey()));
        });

        return new DynamicOptionsBuilder()
                .addCharset(charset)
                .addNewLine(newLine)
                .addCharacter(delimiter, I18n.observable("sampleSource.delimiter"), delimiterNames)
                .bind(() -> {
                    return SampleSource.builder()
                            .store(source.getValue().getStore())
                            .charset(charset.getValue())
                            .newLine(newLine.getValue())
                            .delimiter(delimiter.get())
                            .build();
                }, source)
                .build();
    }

    @Override
    public Dialog configDialog(SampleSource source, boolean all) {
        var cs = DialogHelper.charsetQuery(source.getCharset(), all);
        var nl = DialogHelper.newLineQuery(source.getNewLine(), all);
        var delimiterQuery = DialogHelper.query(
                "Delimiter", source.getDelimiter(), false, NamedCharacter.converter(SampleSource.Delimiter.CHARS, false), all);
        return Dialog.chain(cs, nl, delimiterQuery)
                .evaluateTo(() -> SampleSource.builder()
                        .store(source.getStore())
                        .charset(cs.getResult())
                        .newLine(nl.getResult())
                        .delimiter(delimiterQuery.getResult())
                        .build()
                );

    }

    @Override
    public DataSourceType getPrimaryType() {
        return DataSourceType.TABLE;
    }

    @Override
    public Map<String, List<String>> getSupportedExtensions() {
        return Map.of(i18nKey("fileName"), List.of("smpl"));
    }

    @Override
    public SampleSource createDefaultSource(DataStore input) throws Exception {
        // We can be sure that the input is always a stream data store
        // as this provider is an instance of SimpleFileDataSourceProvider
        // which takes care of only accepting streams

        // Always check whether we can even read the input
        // For example, if the file does not exist we always have to return a default value
        // This can be the case if we want to write to a file that does not exist yet
        if (!input.canOpen()) {
            return SampleSource.defaultSource(input);
        }

        // Use the char setter utility to detect charset and new lines
        var result = Charsetter.get().detect(input.asNeeded());

        // Read the first line to determine delimiter character
        // Note that this detection mechanism is prone to errors and not perfect
        // but it works for the sake of this example
        String firstLine = null;
        try (BufferedReader bufferedReader = Charsetter.get().reader((StreamDataStore) input, result.getCharset())) {
            firstLine = bufferedReader.readLine();
        }

        // Check if the file is essentially empty
        if (firstLine == null || firstLine.trim().length() == 0) {
            return SampleSource.defaultSource(input);
        }

        // Check which possible delimiter character is contained in the first line
        String finalFirstLine = firstLine;
        var detectedDelimiter = SampleSource.Delimiter.CHARS.stream()
                .filter(namedCharacter -> finalFirstLine.contains("" + namedCharacter.getCharacter()))
                .findFirst()
                .orElse(SampleSource.Delimiter.CHARS.get(0));

        return SampleSource.builder()
                .store(input.asNeeded())
                .charset(result.getCharset())
                .newLine(result.getNewLine())
                .delimiter(detectedDelimiter.getCharacter())
                .build();
    }

    @Override
    public Class<SampleSource> getSourceClass() {
        return SampleSource.class;
    }

    @Override
    public List<String> getPossibleNames() {
        return List.of("sample", "sample_source");
    }
}
