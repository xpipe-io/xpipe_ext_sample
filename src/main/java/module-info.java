import io.xpipe.extension.DataSourceProvider;

open module io.xpipe.ext.sample {
    exports io.xpipe.ext.sample;

    requires io.xpipe.extension;
    requires io.xpipe.core;
    requires javafx.base;
    requires javafx.graphics;

    requires static lombok;
    requires static com.fasterxml.jackson.annotation;
    requires static com.fasterxml.jackson.databind;


    provides DataSourceProvider with io.xpipe.ext.sample.SampleSourceProvider;
}