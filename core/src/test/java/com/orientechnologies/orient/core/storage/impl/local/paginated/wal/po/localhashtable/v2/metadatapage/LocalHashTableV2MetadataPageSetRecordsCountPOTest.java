package com.orientechnologies.orient.core.storage.impl.local.paginated.wal.po.localhashtable.v2.metadatapage;

import com.orientechnologies.common.directmemory.OByteBufferPool;
import com.orientechnologies.common.directmemory.OPointer;
import com.orientechnologies.orient.core.storage.cache.OCacheEntry;
import com.orientechnologies.orient.core.storage.cache.OCacheEntryImpl;
import com.orientechnologies.orient.core.storage.cache.OCachePointer;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.po.PageOperationRecord;
import com.orientechnologies.orient.core.storage.index.hashindex.local.v2.HashIndexMetadataPageV2;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class LocalHashTableV2MetadataPageSetRecordsCountPOTest {
  @Test
  public void testRedo() {
    final int pageSize = 64 * 1024;
    final OByteBufferPool byteBufferPool = new OByteBufferPool(pageSize);
    try {
      final OPointer pointer = byteBufferPool.acquireDirect(false);
      final OCachePointer cachePointer = new OCachePointer(pointer, byteBufferPool, 0, 0);
      final OCacheEntry entry = new OCacheEntryImpl(0, 0, cachePointer);

      HashIndexMetadataPageV2 page = new HashIndexMetadataPageV2(entry);
      page.setRecordsCount(23);

      entry.clearPageOperations();

      final OPointer restoredPointer = byteBufferPool.acquireDirect(false);
      final OCachePointer restoredCachePointer =
          new OCachePointer(restoredPointer, byteBufferPool, 0, 0);
      final OCacheEntry restoredCacheEntry = new OCacheEntryImpl(0, 0, restoredCachePointer);

      final ByteBuffer originalBuffer = cachePointer.getBufferDuplicate();
      final ByteBuffer restoredBuffer = restoredCachePointer.getBufferDuplicate();

      Assert.assertNotNull(originalBuffer);
      Assert.assertNotNull(restoredBuffer);

      restoredBuffer.put(originalBuffer);

      page.setRecordsCount(42);

      final List<PageOperationRecord> operations = entry.getPageOperations();
      Assert.assertEquals(1, operations.size());

      Assert.assertTrue(operations.get(0) instanceof LocalHashTableV2MetadataPageSetRecordsCountPO);

      final LocalHashTableV2MetadataPageSetRecordsCountPO pageOperation =
          (LocalHashTableV2MetadataPageSetRecordsCountPO) operations.get(0);

      HashIndexMetadataPageV2 restoredPage = new HashIndexMetadataPageV2(restoredCacheEntry);
      Assert.assertEquals(23, restoredPage.getRecordsCount());

      pageOperation.redo(restoredCacheEntry);

      Assert.assertEquals(42, restoredPage.getRecordsCount());

      byteBufferPool.release(pointer);
      byteBufferPool.release(restoredPointer);
    } finally {
      byteBufferPool.clear();
    }
  }

  @Test
  public void testUndo() {
    final int pageSize = 64 * 1024;

    final OByteBufferPool byteBufferPool = new OByteBufferPool(pageSize);
    try {
      final OPointer pointer = byteBufferPool.acquireDirect(false);
      final OCachePointer cachePointer = new OCachePointer(pointer, byteBufferPool, 0, 0);
      final OCacheEntry entry = new OCacheEntryImpl(0, 0, cachePointer);

      HashIndexMetadataPageV2 page = new HashIndexMetadataPageV2(entry);
      page.setRecordsCount(23);

      entry.clearPageOperations();

      page.setRecordsCount(42);

      final List<PageOperationRecord> operations = entry.getPageOperations();
      Assert.assertEquals(1, operations.size());

      Assert.assertTrue(operations.get(0) instanceof LocalHashTableV2MetadataPageSetRecordsCountPO);

      final LocalHashTableV2MetadataPageSetRecordsCountPO pageOperation =
          (LocalHashTableV2MetadataPageSetRecordsCountPO) operations.get(0);

      final HashIndexMetadataPageV2 restoredPage = new HashIndexMetadataPageV2(entry);

      Assert.assertEquals(42, restoredPage.getRecordsCount());

      pageOperation.undo(entry);

      Assert.assertEquals(23, restoredPage.getRecordsCount());

      byteBufferPool.release(pointer);
    } finally {
      byteBufferPool.clear();
    }
  }

  @Test
  public void testSerialization() {
    LocalHashTableV2MetadataPageSetRecordsCountPO operation =
        new LocalHashTableV2MetadataPageSetRecordsCountPO(15, 42);

    operation.setFileId(42);
    operation.setPageIndex(24);
    operation.setOperationUnitId(1);

    final int serializedSize = operation.serializedSize();
    final byte[] stream = new byte[serializedSize + 1];
    int pos = operation.toStream(stream, 1);

    Assert.assertEquals(serializedSize + 1, pos);

    LocalHashTableV2MetadataPageSetRecordsCountPO restoredOperation =
        new LocalHashTableV2MetadataPageSetRecordsCountPO();
    restoredOperation.fromStream(stream, 1);

    Assert.assertEquals(42, restoredOperation.getFileId());
    Assert.assertEquals(24, restoredOperation.getPageIndex());
    Assert.assertEquals(1, restoredOperation.getOperationUnitId());

    Assert.assertEquals(15, restoredOperation.getRecordsCount());
    Assert.assertEquals(42, restoredOperation.getPastRecordsCount());
  }
}
