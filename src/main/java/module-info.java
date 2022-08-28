open module io.xpipe.ext.sample {
    exports io.xpipe.ext.sample;

    requires io.xpipe.extension;
    requires javafx.base;
    requires javafx.graphics;

    requires static lombok;
    requires static com.fasterxml.jackson.annotation;
}