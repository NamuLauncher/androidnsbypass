package dev.alexytomi.androidnsbypass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.os.Build;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 26)
public class NamespaceCompatibilityTest {
    private static final long NAMESPACE_SHARED = 2;
    private static final int RTLD_NOW = 2;
    private static final long DEFAULT_NAMESPACE = NativeLib.Namespace.DEFAULT.getValue();

    @Test
    public void nativeInitializationKeepsNamedLinksAndDynamicLoadingAvailable() {
        long namespace = createNamespace("test_named_namespace");
        assertTrue(NativeLib.linkNamespaces(namespace, 0, "libc.so"));

        long library = NativeLib.dlopen("libc.so", RTLD_NOW, DEFAULT_NAMESPACE);
        assertNotEquals(0L, library);
        try {
            long symbol = NativeLib.dlsym(library, "malloc", DEFAULT_NAMESPACE);
            assertNotEquals(0L, symbol);
        } finally {
            assertEquals(0, NativeLib.dlclose(library));
        }
    }

    @Test
    public void allLibraryLinksFollowThePlatformContractWithoutDisablingNamedLinks() {
        long from = createNamespace("test_all_libs_from");
        long to = createNamespace("test_all_libs_to");

        boolean linked = NativeLib.linkNamespacesAllLibs(from, to);
        if (Build.VERSION.SDK_INT >= 28) {
            assertTrue(linked);
        } else {
            assertFalse(linked);
        }
        assertTrue(NativeLib.linkNamespaces(from, to, "libc.so"));
    }

    private long createNamespace(String name) {
        // 기본 namespace에서 상속하며 실제 포인터를 사용해 JNI 계약을 검증한다.
        long namespace = NativeLib.createNamespace(
                name,
                null,
                null,
                NAMESPACE_SHARED,
                null,
                0,
                DEFAULT_NAMESPACE);
        assertNotEquals(0L, namespace);
        return namespace;
    }
}
