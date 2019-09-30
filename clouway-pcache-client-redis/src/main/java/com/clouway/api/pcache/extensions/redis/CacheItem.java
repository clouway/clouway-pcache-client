package com.clouway.api.pcache.extensions.redis;

import com.clouway.api.pcache.extensions.redis.RedisFormat.Flag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * CacheItem is representing a single Item persisted in the redis cache using the standard JVM serialization. The entry
 * is holding the binary representation of the value and {@link Flag} entry that represents the associated type with the
 * value.
 * <p/>
 * Flags are used for de-serialization and are providing good benefits.
 *
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
class CacheItem implements Serializable {
  private byte[] value;
  private Flag flags;

  static CacheItem parseFrom(byte[] value) {
    try {
      ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(value));
      CacheItem item = (CacheItem) oin.readObject();
      oin.close();
      return item;
    } catch (IOException e) {
      return null;
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  @SuppressWarnings("unused")
  public CacheItem() {
    // noop
  }

  CacheItem(byte[] value, Flag flags) {
    this.value = value;
    this.flags = flags;
  }

  byte[] getValue() {
    return value;
  }

  Flag getFlags() {
    return flags;
  }

  byte[] toByteArray() {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    try {
      ObjectOutputStream o = new ObjectOutputStream(bout);
      o.writeObject(this);
      o.close();

      return bout.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("unable to generate message");
    }
  }
}
