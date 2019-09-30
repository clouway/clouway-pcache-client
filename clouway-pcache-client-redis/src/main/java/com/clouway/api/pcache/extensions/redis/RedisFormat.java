package com.clouway.api.pcache.extensions.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
class RedisFormat {

  public static class ValueAndFlags implements Serializable {
    public final byte[] value;
    public final Flag flags;

    private ValueAndFlags(byte[] value, Flag flags) {
      this.value = value;
      this.flags = flags;
    }
  }

  public enum Flag {
    BYTES,
    UTF8,
    OBJECT,
    INTEGER,
    LONG,
    BOOLEAN,
    BYTE
  }

  public static <T> Object parse(byte[] value, Flag flagValue) {
    switch (flagValue) {
      case BYTE:
      case INTEGER:
      case LONG:
        long val = (new BigInteger(new String(value, StandardCharsets.US_ASCII))).longValue();
        switch (flagValue) {
          case BYTE:
            return (byte) ((int) val);
          case INTEGER:
            return (int) val;
          case LONG:
            return val;
          default:
            throw new IllegalArgumentException("Cannot deserialize number: bad contents", null);
        }
      case BYTES:
        return value;
      case BOOLEAN:
        if (value.length != 1) {
          throw new IllegalArgumentException("Cannot deserialize Boolean: bad length", null);
        } else {
          switch (value[0]) {
            case 48:
              return Boolean.FALSE;
            case 49:
              return Boolean.TRUE;
            default:
              throw new IllegalArgumentException("Cannot deserialize Boolean: bad contents", null);
          }
        }
      case UTF8:
        return new String(value, StandardCharsets.UTF_8);
      case OBJECT:
        if (value.length == 0) {
          return null;
        }
        try {
          ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(value));
          Object o = in.readObject();

          in.close();
          return o;
        } catch (ClassCastException e) {
          throw new IllegalArgumentException("Cannot read serialized object due unknown type");
        } catch (IOException e) {
          throw new IllegalArgumentException("Cannot read serialized object due IO error");
        } catch (ClassNotFoundException e) {
          throw new IllegalArgumentException("Cannot read serialized object due unknown class");
        }

      default:
        assert false;

        return null;
    }


  }

  public static ValueAndFlags format(Object value) {
    byte[] bytes;
    Flag flags;
    if (value == null) {
      bytes = new byte[0];
      flags = Flag.OBJECT;
    } else if (value instanceof byte[]) {
      flags = Flag.BYTES;
      bytes = (byte[]) value;
    } else if (value instanceof Boolean) {
      flags = Flag.BOOLEAN;
      bytes = new byte[]{(byte) ((Boolean) value ? 49 : 48)};
    } else if (value instanceof String) {
      flags = Flag.UTF8;
      bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
    } else if (value instanceof Integer) {
      flags = Flag.INTEGER;
      bytes = value.toString().getBytes(StandardCharsets.UTF_8);
    } else if (value instanceof Long) {
      flags = Flag.LONG;
      bytes = value.toString().getBytes(StandardCharsets.UTF_8);
    } else if (value instanceof Byte) {
      flags = Flag.BYTE;
      bytes = value.toString().getBytes(StandardCharsets.UTF_8);
    } else {
      if (!(value instanceof Serializable)) {
        String type = String.valueOf(value.getClass());
        throw new IllegalArgumentException(String.format("Value of type %s cannot be cached as it's not serializable.", type));
      }
      flags = Flag.OBJECT;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream objOut;
      try {
        objOut = new ObjectOutputStream(baos);
        objOut.writeObject(value);
        objOut.close();
        bytes = baos.toByteArray();
      } catch (IOException e) {
        throw new IllegalStateException("Unable to serialize object.", e);
      }
    }

    return new ValueAndFlags(bytes, flags);

  }

}
