package com.expedia.www.haystack.client;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.expedia.www.haystack.client.dispatchers.Dispatcher;
import com.expedia.www.haystack.client.dispatchers.NoopDispatcher;

import io.opentracing.References;

public class SpanBuilderTest {

    private Dispatcher dispatcher;
    private Tracer tracer;

    @Before
    public void setUp() throws Exception {
        dispatcher = new NoopDispatcher();
        tracer = new Tracer.Builder("TestService", dispatcher).build();
    }

    @Test
    public void testBasic() {
        Span span = tracer.buildSpan("test-operation").start();

        Assert.assertEquals("test-operation", span.getOperatioName());
    }


    @Test
    public void testReferences() {
        Span parent = tracer.buildSpan("parent").start();
        Span following = tracer.buildSpan("following").start();

        Span child = tracer.buildSpan("child")
            .asChildOf(parent)
            .addReference(References.FOLLOWS_FROM, following.context())
            .start();


        Assert.assertEquals(2, child.getReferences().size());
        Assert.assertEquals(child.getReferences().get(0), new Reference(References.CHILD_OF, parent.context()));
        Assert.assertEquals(child.getReferences().get(1), new Reference(References.FOLLOWS_FROM, following.context()));
    }

    @Test
    public void testWithTags() {
        Span child = tracer.buildSpan("child")
            .withTag("string-key", "string-value")
            .withTag("boolean-key", false)
            .withTag("number-key", 1l)
            .start();

        Map<String, ?> tags = child.getTags();

        Assert.assertEquals(3, tags.size());
        Assert.assertTrue(tags.containsKey("string-key"));
        Assert.assertEquals("string-value", tags.get("string-key"));
        Assert.assertTrue(tags.containsKey("boolean-key"));
        Assert.assertEquals(false, tags.get("boolean-key"));
        Assert.assertTrue(tags.containsKey("number-key"));
        Assert.assertEquals(1l, tags.get("number-key"));
    }

}
