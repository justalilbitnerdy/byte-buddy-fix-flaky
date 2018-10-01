package net.bytebuddy.build;

import net.bytebuddy.utility.StreamDrainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.jar.*;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PluginEngineForJarFileTest {

    private File file;

    @Before
    public void setUp() throws Exception {
        file = File.createTempFile("foo", "bar");
    }

    @After
    public void tearDown() throws Exception {
        assertThat(file.delete(), is(true));
    }

    @Test
    public void testEmpty() throws Exception {
        JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(file));
        outputStream.close();
        Plugin.Engine.Source source = new Plugin.Engine.Source.ForJarFile(file);
        assertThat(source.getManifest(), nullValue(Manifest.class));
        assertThat(source.getClassFileLocator().locate(Object.class.getName()).isResolved(), is(false));
        assertThat(source.iterator().hasNext(), is(false));
    }

    @Test
    public void testFile() throws Exception {
        JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(file));
        try {
            outputStream.putNextEntry(new JarEntry("Foo.class"));
            outputStream.write(new byte[]{1, 2, 3});
            outputStream.closeEntry();
        } finally {
            outputStream.close();
        }
        Plugin.Engine.Source source = new Plugin.Engine.Source.ForJarFile(this.file);
        assertThat(source.getManifest(), nullValue(Manifest.class));
        assertThat(source.getClassFileLocator().locate("Foo").isResolved(), is(true));
        assertThat(source.getClassFileLocator().locate("Foo").resolve(), is(new byte[]{1, 2, 3}));
        assertThat(source.getClassFileLocator().locate("Bar").isResolved(), is(false));
        Iterator<Plugin.Engine.Source.Element> iterator = source.iterator();
        assertThat(iterator.hasNext(), is(true));
        Plugin.Engine.Source.Element element = iterator.next();
        assertThat(element.getName(), is("Foo.class"));
        assertThat(element.resolveAs(Object.class), nullValue(Object.class));
        assertThat(element.resolveAs(JarEntry.class), notNullValue(JarEntry.class));
        InputStream inputStream = element.getInputStream();
        try {
            assertThat(StreamDrainer.DEFAULT.drain(inputStream), is(new byte[]{1, 2, 3}));
        } finally {
            inputStream.close();
        }
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void testFileInSubFolder() throws Exception {
        JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(file));
        try {
            outputStream.putNextEntry(new JarEntry("bar/Foo.class"));
            outputStream.write(new byte[]{1, 2, 3});
            outputStream.closeEntry();
        } finally {
            outputStream.close();
        }
        Plugin.Engine.Source source = new Plugin.Engine.Source.ForJarFile(this.file);
        assertThat(source.getManifest(), nullValue(Manifest.class));
        assertThat(source.getClassFileLocator().locate("bar.Foo").isResolved(), is(true));
        assertThat(source.getClassFileLocator().locate("bar.Foo").resolve(), is(new byte[]{1, 2, 3}));
        assertThat(source.getClassFileLocator().locate("Bar").isResolved(), is(false));
        Iterator<Plugin.Engine.Source.Element> iterator = source.iterator();
        assertThat(iterator.hasNext(), is(true));
        Plugin.Engine.Source.Element element = iterator.next();
        assertThat(element.getName(), is("bar/Foo.class"));
        assertThat(element.resolveAs(Object.class), nullValue(Object.class));
        assertThat(element.resolveAs(JarEntry.class), notNullValue(JarEntry.class));
        InputStream inputStream = element.getInputStream();
        try {
            assertThat(StreamDrainer.DEFAULT.drain(inputStream), is(new byte[]{1, 2, 3}));
        } finally {
            inputStream.close();
        }
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void testManifest() throws Exception {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(file));
        try {
            outputStream.putNextEntry(new JarEntry(Plugin.Engine.MANIFEST_LOCATION));
            manifest.write(outputStream);
            outputStream.closeEntry();
        } finally {
            outputStream.close();
        }
        Plugin.Engine.Source source = new Plugin.Engine.Source.ForJarFile(this.file);
        Manifest readManifest = source.getManifest();
        assertThat(readManifest, notNullValue(Manifest.class));
        assertThat(readManifest.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION), is("1.0"));
    }
}
