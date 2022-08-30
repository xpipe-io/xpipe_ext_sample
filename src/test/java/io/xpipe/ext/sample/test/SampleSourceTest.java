package io.xpipe.ext.sample.test;

import io.xpipe.api.DataSource;
import io.xpipe.core.charsetter.NewLine;
import io.xpipe.core.charsetter.StreamCharset;
import io.xpipe.core.data.node.TupleNode;
import io.xpipe.core.data.node.ValueNode;
import io.xpipe.core.store.FileStore;
import io.xpipe.ext.sample.SampleSource;
import io.xpipe.extension.test.ExtensionTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SampleSourceTest extends ExtensionTest {

    // Use original resource files
    // as we want to work with real files, not resource streams
    static Path utf8SpacesFile = Path.of("src/test/resources/utf8-bom-lf-spaces.txt").toAbsolutePath();
    static Path utf16TabsFile = Path.of("src/test/resources/utf16-crlf-tabs.txt").toAbsolutePath();
    static Path appendReferenceFile = Path.of("src/test/resources/append-reference.txt").toAbsolutePath();
    static Path appendOutputFile;
    static Path writeReferenceFile = Path.of("src/test/resources/write-reference.txt").toAbsolutePath();
    static Path writeOutputFile;

    static DataSource utf8Spaces;
    static DataSource utf16Tabs;
    static DataSource appendReference;
    static DataSource writeReference;
    static DataSource appendOutput;
    static DataSource writeOutput;

    @BeforeAll
    public static void setupStorage() throws Exception {
        utf8Spaces = DataSource.create(null, "sample", FileStore.local(utf8SpacesFile));
        utf16Tabs = DataSource.create(null, "sample", FileStore.local(utf16TabsFile));
        appendReference = DataSource.create(null, "sample", FileStore.local(appendReferenceFile));
        writeReference = DataSource.create(null, "sample", FileStore.local(writeReferenceFile));

        appendOutputFile = Files.createTempFile(null, null);
        appendOutput = DataSource.create(
                null,
                new SampleSource(FileStore.local(appendOutputFile), StreamCharset.get("windows-1252"), NewLine.LF, ' ')
        );

        writeOutputFile = Files.createTempFile(null, null);
        writeOutput = DataSource.create(
                null,
                new SampleSource(FileStore.local(writeOutputFile), StreamCharset.UTF16_LE_BOM, NewLine.CRLF, '\t')
        );
    }

    @Test
    public void testRead() throws IOException {
        var first = utf8Spaces.asTable();
        var firstContents = first.readAll();

        Assertions.assertEquals(1, firstContents.size());
        Assertions.assertEquals(firstContents.at(0), TupleNode.of(List.of(ValueNode.of("hello"), ValueNode.of("world"))));


        var second = utf16Tabs.asTable();
        var secondContents = second.readAll();

        Assertions.assertEquals(3, secondContents.size());
        Assertions.assertEquals(TupleNode.of(List.of(ValueNode.of("hello"), ValueNode.of("world"))), secondContents.at(0));
        Assertions.assertEquals(TupleNode.of(List.of(ValueNode.of("value"), ValueNode.of("value"))), secondContents.at(1));
        Assertions.assertEquals(TupleNode.of(List.of(ValueNode.of("#+ß0üü"), ValueNode.of("testing_symbols"))), secondContents.at(2));
    }

    @Test
    public void testWrite() throws IOException {
        var empty = Files.createTempFile(null, null);
        var emptySource = DataSource.create(
                null,
                new SampleSource(FileStore.local(empty), StreamCharset.UTF32_BOM, NewLine.CRLF, ' ')
        );
        emptySource.forwardTo(writeOutput);

        var first = utf8Spaces.asTable();
        first.forwardTo(writeOutput);
        var second = utf16Tabs.asTable();
        second.forwardTo(writeOutput);

        var text = writeOutput.asTable().readAll();
        var referenceText = writeReference.asTable().readAll();
        Assertions.assertEquals(referenceText, text);

        var bytes = Files.readAllBytes(writeOutputFile);
        var referenceBytes = Files.readAllBytes(writeReferenceFile);
        Assertions.assertArrayEquals(bytes, referenceBytes);
    }

    @Test
    public void testAppend() throws IOException {
        var first = utf8Spaces.asTable();
        first.appendTo(appendOutput);
        var second = utf16Tabs.asTable();
        second.appendTo(appendOutput);

        var node = appendOutput.asTable().readAll();
        var referenceNode = appendReference.asTable().readAll();
        Assertions.assertEquals(referenceNode, node);

        var bytes = Files.readAllBytes(appendOutputFile);
        var referenceBytes = Files.readAllBytes(appendReferenceFile);
        Assertions.assertArrayEquals(bytes, referenceBytes);
    }
}
