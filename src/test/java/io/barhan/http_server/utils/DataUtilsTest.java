package io.barhan.http_server.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Map;

import org.junit.Test;

public class DataUtilsTest {
	@Test
	public void testBuildMap() {
		Map<String, Object> map = DataUtils.buildMap(new Object[][] { { "2", 2 }, { "1", 1 } });
		assertEquals(2, map.size());
		assertEquals(2, map.get("2"));
		assertEquals(1, map.get("1"));
	}

	@Test
	public void testEmptyBuildMap() {
		Map<String, Object> map = DataUtils.buildMap(new Object[][] {});
		assertEquals(0, map.size());
	}

	@Test
	public void testUnmodificableMap() {
		Map<String, Object> map = DataUtils.buildMap(new Object[][] {});
		Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
			map.clear();
		});
		assertEquals(UnsupportedOperationException.class, exception.getClass());
	}
}
